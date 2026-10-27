const fs = require('fs');
const crypto = require('crypto');

// Create mock browser DOM
const documentMock = {
  elements: {},
  body: { style: {} },
  getElementById(id) {
    if (!this.elements[id]) {
      this.elements[id] = {
        id,
        style: { display: 'none' },
        classList: {
          classes: new Set(['hidden', 'modal-backdrop']),
          add(c) { this.classes.add(c); },
          remove(c) { this.classes.delete(c); },
          contains(c) { return this.classes.has(c); },
          toggle(c, force) {
            if (force === undefined) {
              if (this.classes.has(c)) this.classes.delete(c);
              else this.classes.add(c);
            } else if (force) {
              this.classes.add(c);
            } else {
              this.classes.delete(c);
            }
          }
        },
        getContext: () => ({
          fillRect() {}, strokeRect() {}, fillText() {}, beginPath() {},
          arc() {}, fill() {}, stroke() {}, createRadialGradient: () => ({ addColorStop() {} }),
          setLineDash() {}, moveTo() {}, lineTo() {}
        }),
        width: 760,
        height: 440
      };
    }
    return this.elements[id];
  },
  addEventListener() {}
};

global.window = {
  crypto: { subtle: crypto.webcrypto.subtle },
  addEventListener() {}
};
global.tailwind = { config: {} };
global.document = documentMock;
global.localStorage = {
  _data: {},
  getItem(k) { return this._data[k] || null; },
  setItem(k, v) { this._data[k] = String(v); },
  removeItem(k) { delete this._data[k]; }
};
global.sessionStorage = global.localStorage;

const targetFile = fs.existsSync('frontend/portal.html') ? 'frontend/portal.html' : 'frontend/index.html';
const html = fs.readFileSync(targetFile, 'utf8');

// Extract script content
const scriptMatches = [...html.matchAll(/<script\b[^>]*>([\s\S]*?)<\/script>/gi)];
const mainScript = scriptMatches.find(m => m[1].length > 1000)[1];

(0, eval)(mainScript);

(async () => {
  console.log('--- 1. VERIFY MODAL LIFECYCLE CONTROLLERS ON WINDOW ---');
  console.log('window.openCvEvidenceModal exists:', typeof window.openCvEvidenceModal);
  console.log('window.closeCvEvidenceModal exists:', typeof window.closeCvEvidenceModal);
  console.log('window.openForensicDiffModal exists:', typeof window.openForensicDiffModal);
  console.log('window.closeForensicDiffModal exists:', typeof window.closeForensicDiffModal);
  console.log('window.openReportModal exists:', typeof window.openReportModal);
  console.log('window.closeReportModal exists:', typeof window.closeReportModal);

  if (typeof window.openCvEvidenceModal !== 'function' ||
      typeof window.openForensicDiffModal !== 'function' ||
      typeof window.openReportModal !== 'function') {
    throw new Error('Required window modal controllers are missing!');
  }

  // Test opening CV Evidence Modal
  window.openCvEvidenceModal('APP-2026-101', 'Computer Center / Lab');
  const cvModal = document.getElementById('cvEvidenceModal');
  console.log('CV Evidence Modal opened style.display:', cvModal.style.display);
  console.log('CV Evidence Modal classList active:', cvModal.classList.contains('active'));
  console.log('CV Evidence Modal classList hidden:', cvModal.classList.contains('hidden'));
  console.log('document.body overflow:', document.body.style.overflow);

  if (cvModal.style.display !== 'flex' || !cvModal.classList.contains('active') || cvModal.classList.contains('hidden')) {
    throw new Error('CV Evidence Modal open check failed');
  }

  // Test closing CV Evidence Modal
  window.closeCvEvidenceModal();
  console.log('CV Evidence Modal closed style.display:', cvModal.style.display);
  console.log('CV Evidence Modal classList active:', cvModal.classList.contains('active'));
  console.log('CV Evidence Modal classList hidden:', cvModal.classList.contains('hidden'));
  console.log('document.body overflow after close:', document.body.style.overflow);

  if (cvModal.style.display !== 'none' || cvModal.classList.contains('active') || !cvModal.classList.contains('hidden')) {
    throw new Error('CV Evidence Modal close check failed');
  }

  // Test Forensic Diff Modal
  window.openForensicDiffModal('APP-2026-101', 'Land Ownership Deed');
  const diffModal = document.getElementById('forensicDiffModal');
  if (diffModal.style.display !== 'flex' || !diffModal.classList.contains('active')) {
    throw new Error('Forensic Diff Modal open check failed');
  }
  window.closeForensicDiffModal();
  if (diffModal.style.display !== 'none' || diffModal.classList.contains('active')) {
    throw new Error('Forensic Diff Modal close check failed');
  }

  console.log('--- 2. VERIFY 6-DOCUMENT SYNTHETIC DATASET ---');
  const expectedDocs = [
    { key: 'SAMPLE-1', file: 'SYNTH_FIRE_NOC_MH_2026.pdf', hash: 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855' },
    { key: 'SAMPLE-2', file: 'SYNTH_LAND_DEED_PUNE_ALTERED.pdf', hash: '9b8769a4a742959a2d0298c36fb7064bd8f161834ec35b4b5e083cc937297b8b' },
    { key: 'SAMPLE-3', file: 'SYNTH_SPPU_AFFILIATION_2026.pdf', hash: '4a2d81f08726588a8910798ca7a65b7f12e8b61c94d01a35798c81e5b290d287' },
    { key: 'SAMPLE-4', file: 'SYNTH_FACULTY_ROSTER_DUPLICATE_PAN.pdf', hash: '7c9d4a19b88234ea7208d98cf142a781045e781198fba014c2b9981a54ec0912' },
    { key: 'SAMPLE-5', file: 'SYNTH_BUILDING_PLAN_MUNICIPAL.pdf', hash: '81fa023e981bc8819024c088ef7721ab98234ec89124a98b7123984afcb78102' },
    { key: 'SAMPLE-6', file: 'SYNTH_AICTE_TER_RECEIPT_VERIFIED.pdf', hash: '12a4b8890cb1e22998f41029cba881024e0981ba23589cf8810924ec12871239' }
  ];

  for (const exp of expectedDocs) {
    const doc = window.sampleDocumentsData[exp.key];
    if (!doc) throw new Error(`Missing ${exp.key} in sampleDocumentsData!`);
    if (doc.fileName !== exp.file) throw new Error(`Mismatch file in ${exp.key}: ${doc.fileName} vs ${exp.file}`);
    if (doc.sha256 !== exp.hash) throw new Error(`Mismatch hash in ${exp.key}: ${doc.sha256} vs ${exp.hash}`);
    console.log(`Verified ${exp.key}: ${doc.fileName} (${doc.overallStatus}) - Hash: ${doc.sha256.substring(0, 16)}...`);
  }

  console.log('--- 3. TEST SELECT SAMPLE DOC & BLOCKCHAIN MINING ---');
  await window.cipherBlockchain.init();
  const initialChainLen = window.cipherBlockchain.chain.length;
  console.log('Blockchain chain length before selecting sample doc:', initialChainLen);

  await window.selectSampleDoc('SAMPLE-2');
  console.log('Blockchain chain length after selecting SAMPLE-2:', window.cipherBlockchain.chain.length);
  const latestBlock = window.cipherBlockchain.chain[window.cipherBlockchain.chain.length - 1];
  console.log('Latest mined block event:', latestBlock.eventType, '| target:', latestBlock.targetId);

  if (window.cipherBlockchain.chain.length !== initialChainLen + 1 ||
      latestBlock.eventType !== 'DOCUMENT_VERIFIED' ||
      latestBlock.targetId !== 'SYNTH_LAND_DEED_PUNE_ALTERED.pdf') {
    throw new Error('Blockchain notarization on selectSampleDoc failed!');
  }

  console.log('ALL VERIFICATION CHECKS PASSED WITH 100% SUCCESS!');
})();
