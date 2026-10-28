/**
 * LibrePM - Data export/import (main process)
 *
 * Narrow IPC handlers behind window.librepm.dataTransfer (see preload.cjs). Every handler opens
 * the native file dialog itself, talks to the bundled backend over HTTP and writes/reads the file
 * in the main process, so the renderer never gets generic file-system or IPC access.
 *
 *   transfer:capabilities          database engine + whether raw .db transfer is available
 *   transfer:exportJson(userId)    portable workspace JSON  (works on SQLite and MariaDB)
 *   transfer:importJson(userId)    additive import of a workspace JSON
 *   transfer:exportCsv(u, p)       tasks of one project as CSV
 *   transfer:importCsv(u, p)       tasks from CSV into one project
 *   transfer:exportDb              raw SQLite snapshot (.db)            (SQLite only)
 *   transfer:importDb              validate + stage .db, restart backend (SQLite only)
 *   transfer:exportEncrypted(pw)   AES-256-GCM encrypted .db backup      (SQLite only)
 *   transfer:importEncrypted(pw)   decrypt + stage, restart backend      (SQLite only)
 *
 * Restoring a database needs a backend restart (the backend keeps the SQLite file open): the
 * backend stages the validated file, and on the next start PendingDbImportRunListener backs up the
 * current database and swaps the file in. `restartBackend` is supplied by the caller.
 */

const fs = require("fs");
const path = require("path");
const http = require("http");

const ID_PATTERN = /^[A-Za-z0-9_-]{1,64}$/;
const MAX_JSON_BYTES = 512 * 1024 * 1024;
const SQLITE_HEADER = Buffer.from("SQLite format 3\0", "latin1");
const ENCRYPTED_FORMAT = "librepm-encrypted-db";

function pad2(n) {
    return String(n).padStart(2, "0");
}

function stamp() {
    const d = new Date();
    return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}_${pad2(d.getHours())}${pad2(d.getMinutes())}`;
}

class TransferError extends Error {
    constructor(code, message) {
        super(message || code);
        this.code = code;
    }
}

function requireId(value, what) {
    if (typeof value !== "string" || !ID_PATTERN.test(value)) {
        throw new TransferError("INVALID_ARGUMENT", `Invalid ${what}`);
    }
    return value;
}

function errorFromBody(status, text) {
    let message = text;
    try {
        const parsed = JSON.parse(text);
        message = parsed.message || parsed.error || text;
    } catch {
        // plain text body
    }
    const code = status === 409 ? "FILE_TRANSFER_UNSUPPORTED" : "BACKEND_ERROR";
    return new TransferError(code, message || `HTTP ${status}`);
}

/**
 * Creates the handlers. Options:
 *  - dialog, app: Electron modules (dialog is looked up at call time, so tests can stub it)
 *  - getBackendPort(): current backend port
 *  - getWindow(): parent window for dialogs (optional)
 *  - getDataDir(): LibrePM data directory (for the backup location shown to the user)
 *  - restartBackend(): async, restarts the backend so a staged import is applied;
 *    resolves to { restarted: boolean }
 */
function createDataTransfer(options) {
    const { dialog, app, getBackendPort, getWindow, getDataDir, restartBackend } = options;
    let busy = false;

    function request(method, apiPath, { json, body, bodyStream, contentType, userId, toFile, timeoutMs = 120000 } = {}) {
        return new Promise((resolve, reject) => {
            const headers = { Accept: "application/json" };
            if (userId) headers["X-User-Id"] = userId;
            let payload = null;
            if (json !== undefined) {
                payload = Buffer.from(JSON.stringify(json), "utf-8");
                headers["Content-Type"] = "application/json";
            } else if (body !== undefined) {
                payload = Buffer.isBuffer(body) ? body : Buffer.from(String(body), "utf-8");
                headers["Content-Type"] = contentType || "text/plain; charset=utf-8";
            } else if (bodyStream) {
                headers["Content-Type"] = contentType || "application/octet-stream";
            }
            if (payload) headers["Content-Length"] = payload.length;

            const req = http.request({
                hostname: "127.0.0.1",
                port: getBackendPort(),
                path: `/api${apiPath}`,
                method,
                headers,
            }, (res) => {
                const ok = res.statusCode >= 200 && res.statusCode < 300;
                if (ok && toFile) {
                    const out = fs.createWriteStream(toFile);
                    res.pipe(out);
                    out.on("finish", () => resolve({ status: res.statusCode }));
                    out.on("error", reject);
                    res.on("error", reject);
                    return;
                }
                const chunks = [];
                res.on("data", (c) => chunks.push(c));
                res.on("end", () => {
                    const text = Buffer.concat(chunks).toString("utf-8");
                    if (!ok) {
                        reject(errorFromBody(res.statusCode, text));
                        return;
                    }
                    try {
                        resolve(text ? JSON.parse(text) : null);
                    } catch {
                        resolve(text);
                    }
                });
                res.on("error", reject);
            });
            req.on("error", (e) => reject(new TransferError("BACKEND_UNREACHABLE", e.message)));
            req.setTimeout(timeoutMs, () => req.destroy(new TransferError("BACKEND_TIMEOUT", "Backend request timed out")));
            if (bodyStream) {
                bodyStream.on("error", (e) => req.destroy(e));
                bodyStream.pipe(req);
            } else {
                if (payload) req.write(payload);
                req.end();
            }
        });
    }

    function parent() {
        const win = getWindow ? getWindow() : null;
        return win && !win.isDestroyed() ? win : null;
    }

    async function askSavePath(title, defaultName, filters) {
        const opts = {
            title,
            defaultPath: path.join(app.getPath("documents"), defaultName),
            filters,
            properties: ["createDirectory", "showOverwriteConfirmation"],
        };
        const win = parent();
        const res = win ? await dialog.showSaveDialog(win, opts) : await dialog.showSaveDialog(opts);
        if (res.canceled || !res.filePath) return null;
        return res.filePath;
    }

    async function askOpenPath(title, filters) {
        const opts = { title, filters, properties: ["openFile"] };
        const win = parent();
        const res = win ? await dialog.showOpenDialog(win, opts) : await dialog.showOpenDialog(opts);
        if (res.canceled || !res.filePaths || res.filePaths.length === 0) return null;
        return res.filePaths[0];
    }

    /** Writes via a temp file + rename so a failed export never leaves a truncated file. */
    async function writeAtomically(target, writer) {
        const tmp = `${target}.part-${process.pid}`;
        try {
            await writer(tmp);
            await fs.promises.rename(tmp, target);
        } finally {
            await fs.promises.rm(tmp, { force: true });
        }
    }

    async function capabilities() {
        const status = await request("GET", "/db/status");
        return {
            engine: status?.engine || "unknown",
            fileTransferSupported: status?.fileTransferSupported === true,
        };
    }

    async function requireFileTransfer() {
        const caps = await capabilities();
        if (!caps.fileTransferSupported) {
            throw new TransferError("FILE_TRANSFER_UNSUPPORTED",
                `Database file transfer is not available with the ${caps.engine} database`);
        }
    }

    async function applyStagedImport(result) {
        if (!result?.accepted) {
            throw new TransferError("IMPORT_REJECTED", result?.message || "Import rejected");
        }
        const restart = await restartBackend();
        return {
            restarted: restart?.restarted === true,
            backupDir: getDataDir ? path.join(getDataDir(), "config", "backup") : null,
        };
    }

    const handlers = {
        capabilities,

        async exportJson(userId) {
            requireId(userId, "user");
            const filePath = await askSavePath("LibrePM - JSON export", `LibrePM-export-${stamp()}.json`,
                [{ name: "JSON", extensions: ["json"] }]);
            if (!filePath) return { canceled: true };
            const doc = await request("GET", `/users/${userId}/workspace/export`, { userId });
            await writeAtomically(filePath, (tmp) => fs.promises.writeFile(tmp, JSON.stringify(doc, null, 2), "utf-8"));
            return { filePath, summary: doc?.summary || null };
        },

        async importJson(userId) {
            requireId(userId, "user");
            const filePath = await askOpenPath("LibrePM - JSON import", [{ name: "JSON", extensions: ["json"] }]);
            if (!filePath) return { canceled: true };
            const stat = await fs.promises.stat(filePath);
            if (stat.size > MAX_JSON_BYTES) throw new TransferError("FILE_TOO_LARGE", "File too large");
            let doc;
            try {
                doc = JSON.parse(await fs.promises.readFile(filePath, "utf-8"));
            } catch {
                throw new TransferError("INVALID_FILE", "The file is not valid JSON");
            }
            if (doc?.format === ENCRYPTED_FORMAT || (doc && doc.ciphertext && doc.salt && doc.iv)) {
                throw new TransferError("ENCRYPTED_FILE", "This is an encrypted database backup: use Import encrypted backup in Settings");
            }
            const summary = await request("POST", `/users/${userId}/workspace/import`, { json: doc, userId, timeoutMs: 600000 });
            return { filePath, summary };
        },

        async exportCsv(userId, projectId) {
            requireId(userId, "user");
            requireId(projectId, "project");
            const filePath = await askSavePath("LibrePM - CSV export", `LibrePM-tasks-${stamp()}.csv`,
                [{ name: "CSV", extensions: ["csv"] }]);
            if (!filePath) return { canceled: true };
            const job = await request("POST", `/users/${userId}/projects/${projectId}/export/csv`, { userId });
            if (job?.status !== "COMPLETED") {
                throw new TransferError("EXPORT_FAILED", job?.errorMessage || "CSV export failed");
            }
            await writeAtomically(filePath, (tmp) => fs.promises.writeFile(tmp, "﻿" + (job.resultPayload || ""), "utf-8"));
            return { filePath, summary: { tasks: job.recordCount ?? 0 } };
        },

        async importCsv(userId, projectId) {
            requireId(userId, "user");
            requireId(projectId, "project");
            const filePath = await askOpenPath("LibrePM - CSV import", [{ name: "CSV", extensions: ["csv"] }]);
            if (!filePath) return { canceled: true };
            const csv = await fs.promises.readFile(filePath, "utf-8");
            const job = await request("POST", `/users/${userId}/projects/${projectId}/import/csv`,
                { body: csv, contentType: "text/plain; charset=utf-8", userId, timeoutMs: 600000 });
            if (job?.status !== "COMPLETED") {
                throw new TransferError("IMPORT_REJECTED", job?.errorMessage || "CSV import failed");
            }
            return { filePath, summary: { tasks: job.recordCount ?? 0, warnings: job.errorMessage || null } };
        },

        async exportDb() {
            await requireFileTransfer();
            const filePath = await askSavePath("LibrePM - Database export", `LibrePM-db-${stamp()}.db`,
                [{ name: "SQLite database", extensions: ["db"] }]);
            if (!filePath) return { canceled: true };
            await writeAtomically(filePath, async (tmp) => {
                await request("GET", "/db/export?format=db", { toFile: tmp, timeoutMs: 600000 });
                const fd = await fs.promises.open(tmp, "r");
                try {
                    const head = Buffer.alloc(SQLITE_HEADER.length);
                    await fd.read(head, 0, head.length, 0);
                    if (!head.equals(SQLITE_HEADER)) throw new TransferError("EXPORT_FAILED", "The exported file is not a SQLite database");
                } finally {
                    await fd.close();
                }
            });
            return { filePath };
        },

        async importDb() {
            await requireFileTransfer();
            const filePath = await askOpenPath("LibrePM - Database import",
                [{ name: "SQLite database", extensions: ["db", "sqlite", "sqlite3"] }]);
            if (!filePath) return { canceled: true };
            const result = await request("POST", "/db/import", {
                bodyStream: fs.createReadStream(filePath),
                contentType: "application/octet-stream",
                timeoutMs: 600000,
            });
            return { filePath, ...(await applyStagedImport(result)) };
        },

        async exportEncrypted(passphrase) {
            if (typeof passphrase !== "string" || passphrase.length < 4) {
                throw new TransferError("WEAK_PASSPHRASE", "Passphrase must be at least 4 characters");
            }
            await requireFileTransfer();
            const filePath = await askSavePath("LibrePM - Encrypted backup", `LibrePM-encrypted-${stamp()}.enc.json`,
                [{ name: "LibrePM encrypted backup", extensions: ["json"] }]);
            if (!filePath) return { canceled: true };
            const payload = await request("POST", "/db/export-encrypted", { json: { passphrase }, timeoutMs: 600000 });
            if (!payload?.ciphertext) throw new TransferError("EXPORT_FAILED", payload?.error || "Encryption failed");
            await writeAtomically(filePath, (tmp) => fs.promises.writeFile(tmp, JSON.stringify({
                format: ENCRYPTED_FORMAT,
                formatVersion: 1,
                exportedAt: new Date().toISOString(),
                algorithm: payload.algorithm || "AES-256-GCM/PBKDF2",
                salt: payload.salt,
                iv: payload.iv,
                ciphertext: payload.ciphertext,
            }), "utf-8"));
            return { filePath };
        },

        async importEncrypted(passphrase) {
            if (typeof passphrase !== "string" || passphrase.length === 0) {
                throw new TransferError("WEAK_PASSPHRASE", "Passphrase required");
            }
            await requireFileTransfer();
            const filePath = await askOpenPath("LibrePM - Encrypted backup import",
                [{ name: "LibrePM encrypted backup", extensions: ["json"] }]);
            if (!filePath) return { canceled: true };
            let doc;
            try {
                doc = JSON.parse(await fs.promises.readFile(filePath, "utf-8"));
            } catch {
                throw new TransferError("INVALID_FILE", "The file is not a LibrePM encrypted backup");
            }
            if (!doc || typeof doc.ciphertext !== "string" || typeof doc.salt !== "string" || typeof doc.iv !== "string") {
                throw new TransferError("INVALID_FILE", "The file is not a LibrePM encrypted backup");
            }
            const result = await request("POST", "/db/import-encrypted", {
                json: { ciphertext: doc.ciphertext, salt: doc.salt, iv: doc.iv, passphrase },
                timeoutMs: 600000,
            }).catch((e) => {
                // The backend answers 400 with a DatabaseImportResponse for a wrong passphrase.
                throw new TransferError("IMPORT_REJECTED", e.message);
            });
            return { filePath, ...(await applyStagedImport(result)) };
        },
    };

    /** Wraps a handler: serialises transfers and converts errors to { ok:false, code, error }. */
    function wrap(name, fn) {
        return async (_event, ...args) => {
            if (name !== "capabilities" && busy) {
                return { ok: false, code: "BUSY", error: "Another export/import is in progress" };
            }
            if (name !== "capabilities") busy = true;
            try {
                const result = await fn(...args);
                return { ok: !result?.canceled, ...result };
            } catch (e) {
                return { ok: false, code: e.code || "ERROR", error: e.message || String(e) };
            } finally {
                if (name !== "capabilities") busy = false;
            }
        };
    }

    function register(ipcMain) {
        for (const [name, fn] of Object.entries(handlers)) {
            ipcMain.handle(`transfer:${name}`, wrap(name, fn));
        }
    }

    return { register, handlers };
}

module.exports = { createDataTransfer, ENCRYPTED_FORMAT };
