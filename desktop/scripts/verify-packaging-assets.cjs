const fs = require("fs");
const path = require("path");
const { spawnSync } = require("child_process");
const meta = require("./lib/release-meta.cjs");

const pkg = meta.readJson(meta.paths.packageJson);
const lock = meta.readJson(meta.paths.packageLock);
const gradle = fs.readFileSync(meta.paths.buildGradle, "utf8");
const problems = [];
const javaExecutable = process.platform === "win32" ? "build-resources/jre/bin/java.exe" : "build-resources/jre/bin/java";
const required = [
  "dist/index.html",
  "electron/main.cjs",
  "electron/preload.cjs",
  javaExecutable,
  "build-resources/backend/backend.jar",
  "src/assets/icon.png",
  "src/assets/icon.ico",
  "src/assets/icon.icns"
];
for (const file of required) if (!fs.existsSync(path.join(meta.paths.desktopDir, file))) problems.push(`file mancante: ${file}`);
if (lock.version !== pkg.version || lock.packages?.[""]?.version !== pkg.version) problems.push("package-lock.json non allineato");
if (!new RegExp(`^version\\s*=\\s*['"]${pkg.version.replace(/\./g, "\\.")}['"]\\s*$`, "m").test(gradle)) problems.push("build.gradle non allineato");
if (pkg.name !== "librepm") problems.push(`name deve essere librepm, trovato ${pkg.name}`);
if (pkg.build?.executableName !== "librepm") problems.push("build.executableName deve essere librepm");
if (pkg.build?.artifactName !== "librepm_v${version}.${ext}") problems.push("build.artifactName non usa macro versione prevista");
if (!meta.history().releases.some((release) => release.version === pkg.version)) problems.push(`release-history.json non contiene ${pkg.version}`);
if (!fs.readFileSync(meta.paths.changelog, "utf8").includes(`## [${pkg.version}]`)) problems.push(`CHANGELOG.md non contiene ${pkg.version}`);
// La fpm 1.9.3 inclusa in electron-builder genera spec incompatibili con rpm >= 4.20
// (ignora --define buildroot e la build muore su "File not found"): su quelle distribuzioni
// serve una fpm di sistema recente esposta con USE_SYSTEM_FPM=true.
if (process.platform === "linux" && (pkg.build?.linux?.target || []).includes("rpm")) {
  const rpmVersion = spawnSync("rpm", ["--version"], { encoding: "utf8" });
  if (rpmVersion.error || rpmVersion.status !== 0) problems.push("target rpm richiesto ma rpmbuild non è disponibile (Debian/Ubuntu: apt-get install rpm)");
  else {
    const [, major, minor] = /(\d+)\.(\d+)/.exec(rpmVersion.stdout) || [];
    const systemFpm = process.env.USE_SYSTEM_FPM === "true" && spawnSync("sh", ["-c", "command -v fpm"], { stdio: "ignore" }).status === 0;
    if (Number(major) >= 5 || (Number(major) === 4 && Number(minor) >= 20)) {
      if (!systemFpm) problems.push(`rpm ${major}.${minor} non è compatibile con la fpm inclusa in electron-builder: esporta USE_SYSTEM_FPM=true con una fpm >= 1.15 nel PATH`);
    }
  }
}
if (problems.length) {
  console.error("[verify-packaging] Infrastruttura incoerente:");
  problems.forEach((problem) => console.error(`  - ${problem}`));
  process.exit(1);
}
console.log(`[verify-packaging] Asset e versione coerenti (${pkg.version}).`);
