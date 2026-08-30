const fs = require("fs");
const path = require("path");
const { spawnSync } = require("child_process");

const desktopDir = path.resolve(__dirname, "..");
const repoRoot = path.resolve(desktopDir, "..");
const isWindows = process.platform === "win32";

function run(label, command, args, cwd) {
  console.log(`[package-prepare] ${label}`);
  const result = spawnSync(command, args, { cwd, stdio: "inherit", shell: isWindows });
  if (result.error || result.status !== 0) throw result.error || new Error(`${label}: exit ${result.status}`);
}

function pruneAndStageBackend() {
  const version = JSON.parse(fs.readFileSync(path.join(desktopDir, "package.json"), "utf8")).version;
  const libs = path.join(repoRoot, "build", "libs");
  const jars = fs.readdirSync(libs).filter((name) => name.endsWith(".jar") && !name.endsWith("-plain.jar"));
  const expected = jars.filter((name) => name.endsWith(`-${version}.jar`));
  if (expected.length !== 1) throw new Error(`Atteso un JAR backend versione ${version}; trovati: ${jars.join(", ") || "nessuno"}`);
  for (const jar of jars) if (jar !== expected[0]) fs.rmSync(path.join(libs, jar));
}

try {
  run("backend bootJar", isWindows ? "gradlew.bat" : "./gradlew", ["bootJar"], repoRoot);
  pruneAndStageBackend();
  run("JRE", isWindows ? "npm.cmd" : "npm", ["run", "build:jre"], desktopDir);
  run("renderer", isWindows ? "npm.cmd" : "npm", ["run", "build"], desktopDir);
  run("staging self-contained", process.execPath, ["scripts/prepare-self-contained.cjs"], desktopDir);
  run("verifica packaging", isWindows ? "npm.cmd" : "npm", ["run", "verify:packaging"], desktopDir);
} catch (error) {
  console.error(`[package-prepare] ${error.message}`);
  process.exit(1);
}
