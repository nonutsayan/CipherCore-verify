const fs = require('fs');
const html = fs.readFileSync('portal.html', 'utf8');
const lines = html.split('\n');
lines.forEach((l, idx) => {
  if (l.includes('DOMContentLoaded') || l.includes('function init') || l.includes('window.onload')) {
    console.log((idx + 1) + ': ' + l.trim().substring(0, 80));
  }
});
