const fs = require('fs');
const path = require('path');

const themeMin = path.join(__dirname, '../src/main/resources/static/assets/js/theme.min.js');
let content = fs.readFileSync(themeMin, 'utf8');

const brokenPrefix = '.concat(document.location.href.split("/").slice(0,5).join("/"),';
const replacements = [
  [brokenPrefix + "'/app/events/create-an-event.html\"", "'/app/events/create-an-event'"],
  [brokenPrefix + "'/app/events/event-detail.html'", "'/app/events/event-detail'"],
  [brokenPrefix + '"/app/events/create-an-event.html"', '"/app/events/create-an-event"'],
  [brokenPrefix + '"/app/events/event-detail.html"', '"/app/events/event-detail"'],
];

for (const [from, to] of replacements) {
  content = content.split(from).join(to);
}

fs.writeFileSync(themeMin, content);
console.log(content.includes('slice(0,5)') ? 'WARN: slice(0,5) still present' : 'theme.min.js calendar URLs fixed');
