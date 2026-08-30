const fs = require("fs");
const path = require("path");
const meta = require("./lib/release-meta.cjs");

const pkg = meta.readJson(meta.paths.packageJson);
const lock = meta.readJson(meta.paths.packageLock);
const gradle = fs.readFileSync(meta.paths.buildGradle, "utf8");
const problems = [];
const required = [
  "dist/index.html",
  "electron/main.cjs",
  "electron/preload.cjs",
  "build-resources/jre/bin/java",
  "build-resources/backend/backend.jar",
  "src/assets/icon.png",
  "src/assets/icon.ico",
  "src/assets/icon.icns"
];
for (const file of required) if (!fs.existsSync(path.join(meta.paths.desktopDir, file))) problems.push(`file mancante: ${file}`);
if (lock.version !== pkg.version || lock.packages?.[""]?.version !== pkg.version) problems.push("package-lock.json non allineato");
if (!new RegExp(`^version\\s*=\\s*['\"]${pkg.version.replace(/\./g, "\\.")}['\"]\\s*$`, "m").test(gradle)) problems.push("build.gradle non allineato");
if (pkg.name !== "librepm") problems.push(`name deve essere librepm, trovato ${pkg.name}`);
if (pkg.build?.executableName !== "librepm") problems.push("build.executableName deve essere librepm");
if (pkg.build?.artifactName !== "librepm_v${version}.${ext}") problems.push("build.artifactName non usa macro versione prevista");
if (!meta.history().releases.some((release) => release.version === pkg.version)) problems.push(`release-history.json non contiene ${pkg.version}`);
if (!fs.readFileSync(meta.paths.changelog, "utf8").includes(`## [${pkg.version}]`)) problems.push(`CHANGELOG.md non contiene ${pkg.version}`);
if (problems.length) {
  console.error("[verify-packaging] Infrastruttura incoerente:");
  problems.forEach((problem) => console.error(`  - ${problem}`));
  process.exit(1);
}
console.log(`[verify-packaging] Asset e versione coerenti (${pkg.version}).`);
