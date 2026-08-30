#!/usr/bin/env node
const fs = require("fs");
const meta = require("./lib/release-meta.cjs");
const ARTIFACT_NAME = "librepm_v${version}.${ext}";

function flush(writes) {
  const backups = [];
  try {
    for (const write of writes) {
      backups.push({ file: write.file, exists: fs.existsSync(write.file), content: fs.existsSync(write.file) ? fs.readFileSync(write.file) : null });
      fs.writeFileSync(write.file, write.content);
    }
  } catch (error) {
    for (const backup of backups.reverse()) backup.exists ? fs.writeFileSync(backup.file, backup.content) : fs.rmSync(backup.file, { force: true });
    throw error;
  }
}

function run(argv) {
  const args = new Set(argv);
  const setArg = argv.find((arg) => arg.startsWith("--set="));
  const setIndex = argv.indexOf("--set");
  const pkg = meta.readJson(meta.paths.packageJson);
  const lock = meta.readJson(meta.paths.packageLock);
  const gradle = fs.readFileSync(meta.paths.buildGradle, "utf8");
  const changelog = fs.existsSync(meta.paths.changelog) ? fs.readFileSync(meta.paths.changelog, "utf8") : "# Changelog\n\n## [Unreleased]\n";
  const current = pkg.version;
  const parts = meta.semver(current);
  const pending = fs.existsSync(meta.paths.pending) ? meta.readJson(meta.paths.pending) : null;
  const noBump = args.has("--no-bump") || ((process.env.CI || process.env.LIBREPM_NO_BUMP === "1") && !args.has("--force")) || (pending?.version === current && !args.has("--force"));
  let version = current;
  if (!noBump) {
    if (setArg || setIndex >= 0) version = setArg ? setArg.slice(6) : argv[setIndex + 1];
    else if (args.has("--major")) version = `${parts[0] + 1}.0.0`;
    else if (args.has("--minor")) version = `${parts[0]}.${parts[1] + 1}.0`;
    else version = `${parts[0]}.${parts[1]}.${parts[2] + 1}`;
    meta.semver(version);
  }

  pkg.version = version;
  pkg.build.artifactName = ARTIFACT_NAME;
  lock.version = version;
  if (lock.packages?.[""]) lock.packages[""].version = version;
  const nextGradle = gradle.replace(/^version\s*=\s*['"][^'"]+['"]\s*$/m, `version = '${version}'`);
  if (nextGradle === gradle && !gradle.includes(`version = '${version}'`)) throw new Error("build.gradle: assegnazione version top-level non trovata");

  const data = meta.history();
  let nextChangelog = changelog;
  if (!data.releases.some((release) => release.version === version)) {
    const releaseEntries = meta.entries(meta.unreleased(changelog));
    if (!releaseEntries.length) releaseEntries.push("Maintenance release.");
    const now = new Date();
    const section = `## [${version}] - ${meta.isoDate(now)}\n\n${releaseEntries.map((entry) => `- ${entry}`).join("\n")}\n\n`;
    nextChangelog = changelog.replace(/^## \[Unreleased\][\s\S]*?(?=^## |$(?![\s\S]))/m, `## [Unreleased]\n\n${section}`);
    data.releases.unshift({ version, date: meta.rfc2822(now), distribution: "unstable", urgency: "medium", maintainer: `${pkg.author.name} <${pkg.author.email}>`, entries: releaseEntries });
  }

  const writes = [
    { file: meta.paths.packageJson, content: `${JSON.stringify(pkg, null, 2)}\n` },
    { file: meta.paths.packageLock, content: `${JSON.stringify(lock, null, 2)}\n` },
    { file: meta.paths.buildGradle, content: nextGradle },
    { file: meta.paths.changelog, content: nextChangelog },
    { file: meta.paths.history, content: `${JSON.stringify(data, null, 2)}\n` }
  ];
  if (!noBump) writes.push({ file: meta.paths.pending, content: `${JSON.stringify({ version, startedAt: new Date().toISOString() }, null, 2)}\n` });
  console.log(`[version-bump] ${current} -> ${version}${noBump ? " (invariata)" : ""}`);
  if (args.has("--dry-run")) return console.log("[version-bump] dry-run: nessun file scritto");
  flush(writes);
}

if (require.main === module) {
  try { run(process.argv.slice(2)); } catch (error) { console.error(`[version-bump] ${error.message}`); process.exit(1); }
}
module.exports = { run, ARTIFACT_NAME };
