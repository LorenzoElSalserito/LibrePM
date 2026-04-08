const path = require("path");
const { spawnSync } = require("child_process");

const repoRoot = path.resolve(__dirname, "..", "..");
const wrapper = process.platform === "win32" ? "gradlew.bat" : "./gradlew";
const wrapperPath = path.join(repoRoot, wrapper);
const gradleArgs = process.argv.slice(2);

const result = spawnSync(wrapperPath, gradleArgs, {
  cwd: repoRoot,
  stdio: "inherit",
  shell: process.platform === "win32"
});

if (result.status !== 0) {
  process.exit(result.status ?? 1);
}
