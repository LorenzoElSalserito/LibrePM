import assert from "node:assert/strict";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { execFileSync, spawnSync } from "node:child_process";
import test from "node:test";
import { createRequire } from "node:module";

const require = createRequire(import.meta.url);
const meta = require("../scripts/lib/release-meta.cjs");
const finalize = require("../scripts/deb-finalize.cjs");
const desktopDir = path.resolve(import.meta.dirname, "..");
const repoRoot = path.resolve(desktopDir, "..");
const has = (command) => spawnSync("sh", ["-c", `command -v ${command}`], { stdio: "ignore" }).status === 0;
const canUseFakeroot = () => {
  if (process.env.CODEX_PERMISSION_PROFILE) return false;
  const result = spawnSync("fakeroot", ["true"], { stdio: "ignore" });
  return !result.error && result.status === 0;
};

test("identità e versioni restano coerenti", () => {
  const pkg = meta.readJson(meta.paths.packageJson);
  const lock = meta.readJson(meta.paths.packageLock);
  assert.equal(pkg.name, "librepm");
  assert.equal(pkg.build.executableName, "librepm");
  assert.equal(pkg.build.artifactName, "librepm_v${version}.${ext}");
  assert.equal(lock.name, "librepm");
  assert.equal(lock.version, pkg.version);
  assert.equal(lock.packages[""].name, "librepm");
  assert.equal(lock.packages[""].version, pkg.version);
  assert.match(fs.readFileSync(meta.paths.buildGradle, "utf8"), new RegExp(`^version = '${pkg.version.replace(/\./g, "\\.")}'$`, "m"));
});

test("version:show non modifica file", () => {
  const watched = [meta.paths.packageJson, meta.paths.packageLock, meta.paths.buildGradle, meta.paths.changelog, meta.paths.history];
  const before = watched.map((file) => fs.readFileSync(file));
  const result = spawnSync(process.execPath, ["scripts/version-bump.cjs", "--no-bump", "--dry-run"], { cwd: desktopDir, encoding: "utf8" });
  assert.equal(result.status, 0, result.stderr);
  watched.forEach((file, index) => assert.deepEqual(fs.readFileSync(file), before[index]));
});

test("MariaDB è configurabile solo lato amministratore e UI resta disabilitata", () => {
  const yaml = fs.readFileSync(path.join(repoRoot, "src/main/resources/application.yml"), "utf8");
  const gradle = fs.readFileSync(meta.paths.buildGradle, "utf8");
  const settings = fs.readFileSync(path.join(desktopDir, "src/pages/SettingsPage.jsx"), "utf8");
  assert.match(gradle, /org\.mariadb\.jdbc:mariadb-java-client/);
  assert.match(yaml, /on-profile: mariadb/);
  assert.match(yaml, /jdbc:mariadb:\/\/\$\{LIBREPM_DB_HOST\}/);
  assert.match(yaml, /username: \$\{LIBREPM_DB_USERNAME\}/);
  assert.match(yaml, /password: \$\{LIBREPM_DB_PASSWORD\}/);
  assert.match(settings, /MariaDB \(External, administrator configuration\)/);
  assert.match(settings, /btn btn-secondary btn-sm" disabled aria-disabled="true"/);
});

test("verifica packaging usa eseguibile Java corretto su Windows", () => {
  const verifier = fs.readFileSync(path.join(desktopDir, "scripts/verify-packaging-assets.cjs"), "utf8");
  assert.match(verifier, /process\.platform === "win32"/);
  assert.match(verifier, /build-resources\/jre\/bin\/java\.exe/);
  assert.match(verifier, /build-resources\/jre\/bin\/java"/);
});

test("finalizzazione .deb produce metadati LibrePM conformi", { skip: !(has("dpkg-deb") && has("md5sum") && canUseFakeroot()) }, () => {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), "librepm-deb-test-"));
  try {
    const stage = path.join(root, "stage");
    fs.mkdirSync(path.join(stage, "DEBIAN"), { recursive: true });
    fs.writeFileSync(path.join(stage, "DEBIAN/control"), [
      "Package: librepm", `Version: ${meta.currentVersion()}`, "Architecture: amd64",
      "Maintainer: Lorenzo DM <commercial.lorenzodm@gmail.com>", "Section: default", "Priority: extra",
      "License: AGPL-3.0-only", "Vendor: LibrePM", "Description: test", ""
    ].join("\n"));
    const app = path.join(stage, "opt/LibrePM");
    fs.mkdirSync(app, { recursive: true });
    fs.writeFileSync(path.join(app, "librepm"), "#!/bin/sh\nexit 0\n", { mode: 0o755 });
    fs.writeFileSync(path.join(app, "libEGL.so"), "x", { mode: 0o775 });
    const deb = path.join(root, `librepm_v${meta.currentVersion()}.deb`);
    execFileSync("fakeroot", ["dpkg-deb", "--build", stage, deb], { stdio: "ignore" });
    execFileSync("fakeroot", [process.execPath, path.join(desktopDir, "scripts/deb-finalize.cjs"), deb], { cwd: desktopDir });
    const info = execFileSync("dpkg-deb", ["-f", deb], { encoding: "utf8" });
    const listing = execFileSync("dpkg-deb", ["-c", deb], { encoding: "utf8" });
    assert.match(info, /^Package: librepm$/m);
    assert.match(info, /^Section: misc$/m);
    assert.doesNotMatch(info, /^(License|Vendor):/m);
    assert.match(listing, /usr\/share\/doc\/librepm\/changelog\.gz/);
    assert.match(listing, /usr\/share\/doc\/librepm\/copyright/);
    assert.match(listing, /usr\/share\/pixmaps\/librepm\.png/);
    assert.match(listing, /etc\/xdg\/autostart\/librepm\.desktop/);
    assert.match(listing, /-rw-r--r-- root\/root .*libEGL\.so/);
  } finally {
    fs.rmSync(root, { recursive: true, force: true });
  }
});
