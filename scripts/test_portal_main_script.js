const fs = require('fs');
const { JSDOM, VirtualConsole } = require('jsdom');

let html = fs.readFileSync('portal.html', 'utf8');

// Replace tailwind.config with window.tailwind = ... to avoid ReferenceError
html = html.replace('tailwind.config =', 'window.tailwind = window.tailwind || {}; window.tailwind.config =');

const virtualConsole = new VirtualConsole();
virtualConsole.on('error', (err) => {
  console.error('CONSOLE ERROR:', err);
});
virtualConsole.on('jsdomError', (err) => {
  console.error('JSDOM SCRIPT ERROR:', err);
});

const dom = new JSDOM(html, {
  runScripts: 'dangerously',
  virtualConsole: virtualConsole,
  url: 'http://localhost:3000/portal.html'
});

const { window } = dom;
console.log('window.openCvEvidenceModal:', typeof window.openCvEvidenceModal);
console.log('window.openForensicDiffModal:', typeof window.openForensicDiffModal);
console.log('window.activeUserSession:', window.activeUserSession);
