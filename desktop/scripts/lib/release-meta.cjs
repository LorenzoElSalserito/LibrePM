const fs = require("fs");
const path = require("path");

const desktopDir = path.resolve(__dirname, "..", "..");
const repoRoot = path.resolve(desktopDir, "..");
const paths = {
  desktopDir,
  repoRoot,
  packageJson: path.join(desktopDir, "package.json"),
  packageLock: path.join(desktopDir, "package-lock.json"),
  buildGradle: path.join(repoRoot, "build.gradle"),
  changelog: path.join(repoRoot, "CHANGELOG.md"),
  license: path.join(repoRoot, "LICENSE"),
  history: path.join(desktopDir, "scripts", "release-history.json"),
  pending: path.join(desktopDir, "scripts", ".release-pending.json")
};

const readJson = (file) => JSON.parse(fs.readFileSync(file, "utf8"));
const semver = (value) => {
  const match = /^(\d+)\.(\d+)\.(\d+)$/.exec(String(value));
  if (!match) throw new Error(`Versione "${value}" non valida: atteso X.Y.Z`);
  return match.slice(1).map(Number);
};
const currentVersion = () => readJson(paths.packageJson).version;
const history = () => fs.existsSync(paths.history) ? readJson(paths.history) : { releases: [] };
const pad = (n) => String(n).padStart(2, "0");
const isoDate = (d = new Date()) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
const rfc2822 = (d = new Date()) => {
  const days = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
  const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
  const offset = -d.getTimezoneOffset();
  return `${days[d.getDay()]}, ${pad(d.getDate())} ${months[d.getMonth()]} ${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())} ${offset >= 0 ? "+" : "-"}${pad(Math.floor(Math.abs(offset) / 60))}${pad(Math.abs(offset) % 60)}`;
};

function unreleased(markdown) {
  const match = /^## \[Unreleased\]\s*$([\s\S]*?)(?=^## |$(?![\s\S]))/m.exec(markdown);
  return match ? match[1] : "";
}

function entries(body) {
  const result = [];
  let category = "";
  let current = "";
  const flush = () => {
    if (current.trim()) result.push(`${category ? `${category}: ` : ""}${current.replace(/\s+/g, " ").trim()}`);
    current = "";
  };
  for (const raw of body.split("\n")) {
    const heading = /^###\s+(.+)$/.exec(raw.trim());
    const bullet = /^\s*[-*+]\s+(.+)$/.exec(raw);
    if (heading) { flush(); category = heading[1].replace(/:$/, ""); }
    else if (bullet) { flush(); current = bullet[1]; }
    else if (raw.trim() && current) current += ` ${raw.trim()}`;
    else if (!raw.trim()) flush();
  }
  flush();
  return result;
}

function renderDebianChangelog(data, packageName, maintainer) {
  return data.releases.map((release) => {
    const bullets = (release.entries.length ? release.entries : ["Maintenance release."])
      .map((entry) => `  * ${entry.replace(/\s+/g, " ").trim()}`).join("\n");
    return `${packageName} (${release.version}) ${release.distribution || "unstable"}; urgency=${release.urgency || "medium"}\n\n${bullets}\n\n -- ${release.maintainer || maintainer}  ${release.date}\n`;
  }).join("\n");
}

module.exports = { paths, readJson, semver, currentVersion, history, isoDate, rfc2822, unreleased, entries, renderDebianChangelog };
