const fs = require("fs");
const path = require("path");

const desktopDir = path.resolve(__dirname, "..");
const repoRoot = path.resolve(desktopDir, "..");
const jreDir = path.join(desktopDir, "jre");
const buildLibsDir = path.join(repoRoot, "build", "libs");
const stagingDir = path.join(desktopDir, "build-resources");
const stagedJreDir = path.join(stagingDir, "jre");
const stagedBackendDir = path.join(stagingDir, "backend");
const stagedBackendJar = path.join(stagedBackendDir, "backend.jar");

function fail(message) {
  console.error(`[prepare-self-contained] ${message}`);
  process.exit(1);
}

if (!fs.existsSync(jreDir)) {
  fail(`Bundled JRE not found at ${jreDir}. Run the JRE build first.`);
}

if (!fs.existsSync(buildLibsDir)) {
  fail(`Backend libs directory not found at ${buildLibsDir}. Build or download the backend JAR first.`);
}

const backendJars = fs
  .readdirSync(buildLibsDir, { withFileTypes: true })
  .filter((entry) => entry.isFile())
  .map((entry) => entry.name)
  .filter((name) => name.endsWith(".jar") && !name.endsWith("-plain.jar"))
  .map((name) => {
    const absolutePath = path.join(buildLibsDir, name);
    return {
      name,
      absolutePath,
      mtimeMs: fs.statSync(absolutePath).mtimeMs
    };
  })
  .sort((a, b) => b.mtimeMs - a.mtimeMs);

if (backendJars.length === 0) {
  fail(`No runnable backend JAR found in ${buildLibsDir}.`);
}

if (backendJars.length > 1) {
  console.warn(
    `[prepare-self-contained] Multiple backend JARs found. Packaging the most recent one: ${backendJars[0].name}`
  );
}

fs.rmSync(stagingDir, { recursive: true, force: true });
fs.mkdirSync(stagedBackendDir, { recursive: true });

fs.cpSync(jreDir, stagedJreDir, { recursive: true });
fs.copyFileSync(backendJars[0].absolutePath, stagedBackendJar);

console.log(`[prepare-self-contained] Staged JRE from ${jreDir}`);
console.log(`[prepare-self-contained] Staged backend JAR ${backendJars[0].name} as backend.jar`);
