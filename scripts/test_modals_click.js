const fs = require('fs');
const { JSDOM, VirtualConsole } = require('jsdom');

let html = fs.readFileSync('portal.html', 'utf8');
html = html.replace('tailwind.config =', 'window.tailwind = window.tailwind || {}; window.tailwind.config =');

const virtualConsole = new VirtualConsole();
virtualConsole.on('error', (err) => console.error('CONSOLE ERROR:', err));
virtualConsole.on('jsdomError', (err) => console.error('JSDOM SCRIPT ERROR:', err));

const dom = new JSDOM(html, {
  runScripts: 'dangerously',
  virtualConsole: virtualConsole,
  url: 'http://localhost:3000/portal.html'
});

const { window } = dom;
const { document } = window;

// Provide mock canvas context
const mockCanvas = document.getElementById('cvInspectionCanvas');
console.log('cvInspectionCanvas in DOM:', !!mockCanvas);

if (mockCanvas) {
  mockCanvas.getContext = () => ({
    fillRect: () => {},
    clearRect: () => {},
    getImageData: () => ({ data: [] }),
    putImageData: () => {},
    createImageData: () => ({ data: [] }),
    drawImage: () => {},
    save: () => {},
    fillText: () => {},
    restore: () => {},
    beginPath: () => {},
    moveTo: () => {},
    lineTo: () => {},
    closePath: () => {},
    stroke: () => {},
    fill: () => {},
    arc: () => {},
    strokeRect: () => {},
    rect: () => {}
  });
}

// Call openCvEvidenceModal
console.log('\n--- Calling window.openCvEvidenceModal ---');
window.openCvEvidenceModal('APP-2026-101', 'Computer Center / Lab');

const cvModal = document.getElementById('cvEvidenceModal');
console.log('cvModal.style.display:', cvModal.style.display);
console.log('cvModal.classList:', Array.from(cvModal.classList).join(' '));

// Call openForensicDiffModal
console.log('\n--- Calling window.openForensicDiffModal ---');
window.openForensicDiffModal('APP-2026-101', 'Land Ownership Deed');

const forensicModal = document.getElementById('forensicDiffModal');
console.log('forensicModal.style.display:', forensicModal.style.display);
console.log('forensicModal.classList:', Array.from(forensicModal.classList).join(' '));
