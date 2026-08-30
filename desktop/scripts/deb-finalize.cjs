#!/usr/bin/env node
const crypto = require("crypto");
const fs = require("fs");
const os = require("os");
const path = require("path");
const zlib = require("zlib");
const { spawnSync } = require("child_process");
const meta = require("./lib/release-meta.cjs");

const PKG_NAME = "librepm";
const SECTION = "misc";
const pkg = meta.readJson(meta.paths.packageJson);

function run(command, args, options = {}) {
  const result = spawnSync(command, args, { encoding: "utf8", ...options });
  if (result.error || result.status !== 0) throw result.error || new Error(`${command}: ${result.stderr || result.stdout}`);
  return result.stdout;
}

function copyright() {
  return [
    "Format: https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/",
    "Upstream-Name: LibrePM",
    `Upstream-Contact: ${pkg.author.name} <${pkg.author.email}>`,
    `Source: ${pkg.repository.url}`,
    "",
    "Files: *",
    `Copyright: 2026 ${pkg.author.name} <${pkg.author.email}>`,
    "License: AGPL-3+",
    "",
    "License: AGPL-3+",
    " This program is free software: you can redistribute it and/or modify it",
    " under the terms of the GNU Affero General Public License version 3 or later.",
    " .",
    " On Debian systems, the complete license text is available in",
    " /usr/share/common-licenses/AGPL-3.",
    ""
  ].join("\n");
}

function control(raw) {
  const fields = new Map();
  let key = null;
  for (const line of raw.split("\n")) {
    const match = /^([A-Za-z0-9-]+):\s*(.*)$/.exec(line);
    if (match) { key = match[1]; fields.set(key, match[2]); }
    else if (key && line.startsWith(" ")) fields.set(key, `${fields.get(key)}\n${line}`);
  }
  fields.delete("License");
  fields.delete("Vendor");
  fields.set("Package", PKG_NAME);
  fields.set("Version", pkg.version);
  fields.set("Section", SECTION);
  fields.set("Priority", "optional");
  fields.set("Description", "LibrePM project governance platform\n LibrePM manages projects, grants, finance, planning and team collaboration.\n .\n Data remains local by default; an external MariaDB profile is available for collaborative deployments.");
  const order = ["Package", "Version", "Section", "Priority", "Architecture", "Depends", "Recommends", "Suggests", "Maintainer", "Homepage", "Installed-Size", "Description"];
  return `${order.filter((name) => fields.has(name)).map((name) => `${name}: ${fields.get(name)}`).join("\n")}\n`;
}

function files(root) {
  const result = [];
  const walk = (dir) => fs.readdirSync(dir, { withFileTypes: true }).forEach((entry) => {
    const file = path.join(dir, entry.name);
    if (entry.isDirectory()) walk(file);
    else if (entry.isFile()) result.push(file);
  });
  walk(root);
  return result.sort();
}

function normalizeModes(root) {
  const output = run("find", [root, "-mindepth", "0", "-printf", "%m %y %p\\n"]);
  for (const line of output.trim().split("\n")) {
    const match = /^(\d+) ([df]) (.+)$/.exec(line);
    if (!match) continue;
    const [, mode, type, item] = match;
    if (type === "d") fs.chmodSync(item, 0o755);
    else {
      const executable = (parseInt(mode, 8) & 0o111) !== 0;
      const shared = /\.so(?:\.\d+)*$/.test(item);
      const maintainerScript = item.startsWith(path.join(root, "DEBIAN") + path.sep) && /\/(preinst|postinst|prerm|postrm|config)$/.test(item);
      fs.chmodSync(item, shared ? 0o644 : (executable || maintainerScript ? 0o755 : 0o644));
    }
  }
}

function writeMd5(root) {
  const lines = files(root).filter((file) => !file.startsWith(path.join(root, "DEBIAN") + path.sep)).map((file) => {
    const hash = crypto.createHash("md5").update(fs.readFileSync(file)).digest("hex");
    return `${hash}  ${path.relative(root, file)}`;
  });
  fs.writeFileSync(path.join(root, "DEBIAN", "md5sums"), `${lines.join("\n")}\n`);
  run("md5sum", ["-c", "--quiet", path.join(root, "DEBIAN", "md5sums")], { cwd: root });
}

function finalize(deb) {
  const history = meta.history();
  if (!history.releases.some((release) => release.version === pkg.version)) throw new Error(`release-history.json non contiene ${pkg.version}`);
  const temp = fs.mkdtempSync(path.join(os.tmpdir(), "librepm-deb-"));
  const root = path.join(temp, "root");
  const rebuilt = path.join(temp, path.basename(deb));
  try {
    run("dpkg-deb", ["-R", deb, root]);
    fs.writeFileSync(path.join(root, "DEBIAN", "control"), control(fs.readFileSync(path.join(root, "DEBIAN", "control"), "utf8")));
    const doc = path.join(root, "usr", "share", "doc", PKG_NAME);
    fs.mkdirSync(doc, { recursive: true });
    for (const old of ["LICENSE", "changelog.Debian.gz", "changelog.gz"]) fs.rmSync(path.join(doc, old), { force: true });
    fs.writeFileSync(path.join(doc, "copyright"), copyright());
    const changelog = meta.renderDebianChangelog(history, PKG_NAME, `${pkg.author.name} <${pkg.author.email}>`);
    const changelogFile = path.join(temp, "changelog");
    fs.writeFileSync(changelogFile, changelog);
    if (spawnSync("dpkg-parsechangelog", ["--version"], { stdio: "ignore" }).status === 0) run("dpkg-parsechangelog", ["-l", changelogFile]);
    fs.writeFileSync(path.join(doc, "changelog.gz"), zlib.gzipSync(Buffer.from(changelog), { level: 9, mtime: 0 }));

    const pixmaps = path.join(root, "usr", "share", "pixmaps");
    fs.mkdirSync(pixmaps, { recursive: true });
    fs.copyFileSync(path.join(meta.paths.desktopDir, "src", "assets", "icon.png"), path.join(pixmaps, `${PKG_NAME}.png`));
    const autostart = path.join(root, "etc", "xdg", "autostart");
    fs.mkdirSync(autostart, { recursive: true });
    fs.writeFileSync(path.join(autostart, `${PKG_NAME}.desktop`), `[Desktop Entry]\nType=Application\nName=LibrePM\nExec=/opt/LibrePM/librepm %U\nIcon=librepm\nTerminal=false\nHidden=true\nNoDisplay=true\nX-GNOME-Autostart-enabled=false\n`);
    fs.writeFileSync(path.join(root, "DEBIAN", "conffiles"), `/etc/xdg/autostart/${PKG_NAME}.desktop\n`);
    normalizeModes(root);
    writeMd5(root);
    run("dpkg-deb", ["--build", root, rebuilt]);
    fs.copyFileSync(rebuilt, `${deb}.tmp`);
    fs.renameSync(`${deb}.tmp`, deb);
  } finally {
    fs.rmSync(temp, { recursive: true, force: true });
  }
}

if (require.main === module) {
  try {
    if (!process.argv[2]) throw new Error("Uso: deb-finalize.cjs <pacchetto.deb>");
    finalize(path.resolve(process.argv[2]));
  } catch (error) { console.error(`[deb-finalize] ${error.message}`); process.exit(1); }
}
module.exports = { finalize, control, PKG_NAME, SECTION };
