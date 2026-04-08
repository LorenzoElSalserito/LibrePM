const fs = require("fs");
const path = require("path");
const { spawnSync } = require("child_process");

const desktopDir = path.resolve(__dirname, "..");
const outputDir = path.join(desktopDir, "jre");

const modules = [
  "java.base",
  "java.desktop",
  "java.instrument",
  "java.logging",
  "java.management",
  "java.naming",
  "java.net.http",
  "java.prefs",
  "java.rmi",
  "java.scripting",
  "java.security.jgss",
  "java.sql",
  "java.xml",
  "jdk.unsupported",
  "jdk.crypto.ec",
  "jdk.localedata"
];

fs.rmSync(outputDir, { recursive: true, force: true });

const result = spawnSync(
  "jlink",
  [
    "--add-modules",
    modules.join(","),
    "--compress=2",
    "--no-header-files",
    "--no-man-pages",
    "--strip-debug",
    "--output",
    outputDir
  ],
  {
    cwd: desktopDir,
    stdio: "inherit",
    shell: process.platform === "win32"
  }
);

if (result.status !== 0) {
  process.exit(result.status ?? 1);
}
