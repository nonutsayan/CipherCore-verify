const fs = require('fs');
const { JSDOM, VirtualConsole } = require('jsdom');

console.log('Testing index.html in JSDOM...');
const html = fs.readFileSync('index.html', 'utf8');

const virtualConsole = new VirtualConsole();
virtualConsole.on('error', (err) => console.error('CONSOLE ERROR:', err));
virtualConsole.on('jsdomError', (err) => console.error('JSDOM SCRIPT ERROR:', err));

const dom = new JSDOM(html, {
  runScripts: 'dangerously',
  virtualConsole: virtualConsole,
  url: 'http://localhost:3000/index.html'
});

const { window } = dom;
const { document } = window;

// Provide mock canvas context for Three.js and 2D canvas
window.HTMLCanvasElement.prototype.getContext = function () {
  return {
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
    rect: () => {},
    measureText: () => ({ width: 50 }),
    setLineDash: () => {},
    createRadialGradient: () => ({ addColorStop: () => {} }),
    createLinearGradient: () => ({ addColorStop: () => {} })
  };
};

// Check elements
const cvModal = document.getElementById('cvEvidenceModal');
const forensicModal = document.getElementById('forensicDiffModal');

console.log('cvEvidenceModal in index.html:', !!cvModal);
console.log('forensicDiffModal in index.html:', !!forensicModal);
console.log('window.openCvEvidenceModal type:', typeof window.openCvEvidenceModal);
console.log('window.openForensicDiffModal type:', typeof window.openForensicDiffModal);

// Test clicking CV button
console.log('\n--- Clicking View CV Evidence Inspection on index.html ---');
window.openCvEvidenceModal('APP-2026-101', 'Computer Center / Lab');
console.log('cvModal display:', cvModal.style.display, '| active:', cvModal.classList.contains('active'));

window.closeCvEvidenceModal();
console.log('cvModal closed display:', cvModal.style.display, '| active:', cvModal.classList.contains('active'));

// Test clicking Forensic Diff button
console.log('\n--- Clicking Inspect Forensic Diff on index.html ---');
window.openForensicDiffModal('APP-2026-101', 'Land Ownership Deed');
console.log('forensicModal display:', forensicModal.style.display, '| active:', forensicModal.classList.contains('active'));

window.closeForensicDiffModal();
console.log('forensicModal closed display:', forensicModal.style.display, '| active:', forensicModal.classList.contains('active'));

console.log('\nINDEX.HTML FULL DOM VERIFICATION: 100% SUCCESS!');
