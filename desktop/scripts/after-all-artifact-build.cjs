const fs = require("fs");
const path = require("path");
const { spawnSync } = require("child_process");
const meta = require("./lib/release-meta.cjs");

exports.default = async function afterAllArtifactBuild(context) {
  const debs = (context.artifactPaths || []).filter((file) => file.endsWith(".deb"));
  for (const deb of debs) {
    const available = spawnSync("sh", ["-c", "command -v fakeroot"], { stdio: "ignore" }).status === 0;
    if (!available) throw new Error("fakeroot richiesto per finalizzare pacchetto .deb");
    const result = spawnSync("fakeroot", [process.execPath, path.join(__dirname, "deb-finalize.cjs"), deb], { stdio: "inherit" });
    if (result.error || result.status !== 0) throw result.error || new Error(`deb-finalize exit ${result.status}`);
  }
  if (fs.existsSync(meta.paths.pending)) fs.rmSync(meta.paths.pending);
};
