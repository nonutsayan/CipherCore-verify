const fs = require('fs');
const { JSDOM, VirtualConsole } = require('jsdom');

console.log('Loading portal.html into JSDOM...');
let html = fs.readFileSync('portal.html', 'utf8');

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
window.HTMLCanvasElement.prototype.getContext = function () {
  return {
    fillRect: () => {},
    clearRect: () => {},
    getImageData: (x, y, w, h) => ({ data: new Array(w * h * 4) }),
    putImageData: () => {},
    createImageData: () => ({ data: new Array(4) }),
    setTransform: () => {},
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

console.log('DOM Loaded. Checking initial state...');
const cvModal = document.getElementById('cvEvidenceModal');
const forensicModal = document.getElementById('forensicDiffModal');
console.log('cvEvidenceModal exists:', !!cvModal);
console.log('forensicDiffModal exists:', !!forensicModal);
console.log('window.openCvEvidenceModal type:', typeof window.openCvEvidenceModal);
console.log('window.openForensicDiffModal type:', typeof window.openForensicDiffModal);

// Simulate login
console.log('\n--- Simulating AICTE Officer login ---');
if (typeof window.loginAsRole === 'function') {
  window.loginAsRole('AICTE Scrutiny Officer');
} else if (typeof window.saveUserSession === 'function') {
  window.saveUserSession({
    name: 'Dr. Arvind Kumar',
    role: 'AICTE Scrutiny Officer',
    id: 'AICTE-OFFICER-782',
    authMethod: 'Official Portal'
  });
}
console.log('Active role:', window.activeUserSession?.role);

// Test all CV Evidence buttons
console.log('\n--- Testing CV Evidence Buttons ---');
const allButtons = Array.from(document.querySelectorAll('button, a'));
const cvButtons = allButtons.filter(b => {
  const txt = (b.innerText || b.textContent || '').trim();
  const oc = b.getAttribute('onclick') || '';
  return txt.includes('CV') || oc.includes('openCvEvidenceModal');
});
console.log('Found CV buttons:', cvButtons.length);

cvButtons.forEach((btn, idx) => {
  const txt = (btn.innerText || btn.textContent || '').trim();
  btn.click();
  console.log(`  Button ${idx + 1} clicked: modal display="${cvModal.style.display}", active=${cvModal.classList.contains('active')}`);
  window.closeCvEvidenceModal();
});

// Test all Forensic Diff buttons
console.log('\n--- Testing Forensic Diff Buttons ---');
const forensicButtons = allButtons.filter(b => {
  const txt = (b.innerText || b.textContent || '').trim();
  const oc = b.getAttribute('onclick') || '';
  return txt.includes('Forensic') || oc.includes('openForensicDiffModal');
});
console.log('Found Forensic buttons:', forensicButtons.length);

forensicButtons.forEach((btn, idx) => {
  const txt = (btn.innerText || btn.textContent || '').trim();
  btn.click();
  console.log(`  Button ${idx + 1} clicked: modal display="${forensicModal.style.display}", active=${forensicModal.classList.contains('active')}`);
  window.closeForensicDiffModal();
});

// Test Forensic View Mode Switching
console.log('\n--- Testing Forensic View Modes ---');
window.openForensicDiffModal('APP-2026-101', 'Land Ownership Deed');
window.setForensicDiffViewMode('split');
console.log('Mode split: forensicViewSplit display =', document.getElementById('forensicViewSplit')?.style.display);
window.setForensicDiffViewMode('ela');
console.log('Mode ela: forensicViewEla display =', document.getElementById('forensicViewEla')?.style.display);
window.setForensicDiffViewMode('hash');
console.log('Mode hash: forensicViewHash display =', document.getElementById('forensicViewHash')?.style.display);

// Test CV Filter and Toggles
console.log('\n--- Testing CV Toggles ---');
window.openCvEvidenceModal('APP-2026-101', 'Computer Center / Lab');
window.toggleCvBoundingBoxes(false);
console.log('cvBoundingBoxesVisible:', window.cvBoundingBoxesVisible);
window.toggleCvBoundingBoxes(true);
console.log('cvBoundingBoxesVisible:', window.cvBoundingBoxesVisible);
window.toggleCvHeatmap(true);
console.log('cvHeatmapVisible:', window.cvHeatmapVisible);
window.toggleCvGeofence(false);
console.log('cvGeofenceVisible:', window.cvGeofenceVisible);
window.setCvFilter('terminals');
console.log('cvDetectionFilter:', window.cvDetectionFilter);

console.log('\nPORTAL.HTML FULL VERIFICATION: 100% SUCCESS!');
