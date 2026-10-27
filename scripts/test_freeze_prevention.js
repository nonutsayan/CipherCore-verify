/**
 * Comprehensive Automated Test Suite for Freeze Prevention & Modal Lifecycle
 * Tests DOM events, tab switching auto-dismiss, backdrop click outside, and overflow unlock.
 */

const fs = require('fs');
const path = require('path');
const { JSDOM } = require('jsdom');

const PORTAL_PATH = path.join(__dirname, '..', 'portal.html');
const htmlSource = fs.readFileSync(PORTAL_PATH, 'utf-8');

function setupDom() {
  const dom = new JSDOM(htmlSource, {
    runScripts: 'dangerously',
    url: 'http://localhost:3000/portal.html'
  });

  const { window } = dom;

  // Mock essential browser APIs not in basic JSDOM
  window.alert = () => {};
  window.scrollTo = () => {};
  window.crypto = window.crypto || {
    subtle: {
      digest: async () => new Uint8Array(32)
    }
  };

  // Preset user session so authModal doesn't block
  window.sessionStorage.setItem('ciphercore_user', JSON.stringify({
    name: 'Dr. Arvind Kumar',
    role: 'AICTE Scrutiny Officer',
    officerId: 'AICTE-OFFICER-782',
    institution: 'All India Council for Technical Education'
  }));

  // Initialize session and demo applications data
  if (typeof window.initUserSession === 'function') {
    window.initUserSession();
  }

  return { dom, window, document: window.document };
}

function runTests() {
  console.log('Starting Freeze Prevention and Modal Lifecycle Test Suite...\n');
  let passed = 0;
  let total = 0;

  function assertTest(name, condition, extraInfo = '') {
    total++;
    if (condition) {
      console.log(`[PASS] ${name}`);
      passed++;
    } else {
      console.error(`[FAIL] ${name} ${extraInfo}`);
    }
  }

  const { window, document } = setupDom();

  // Test 1: Initial state has zero body overflow lock
  assertTest('Initial state: document.body.style.overflow is clean',
    document.body.style.overflow === '' || document.body.style.overflow === 'auto',
    `Found: ${document.body.style.overflow}`
  );

  // Test 2: Upload Dossier Modal Lifecycle
  window.openUploadDossierModal();
  const uploadModal = document.getElementById('uploadDossierModal');
  assertTest('Open Upload Modal sets active and overflow hidden',
    uploadModal.classList.contains('active') && document.body.style.overflow === 'hidden'
  );

  window.closeUploadDossierModal();
  assertTest('Close Upload Modal clears active and resets body overflow',
    !uploadModal.classList.contains('active') && document.body.style.overflow === ''
  );

  // Test 3: Backdrop Click Outside Dismissal for Upload Modal
  window.openUploadDossierModal();
  assertTest('Re-open Upload Modal sets overflow hidden', document.body.style.overflow === 'hidden');
  
  // Simulate clicking backdrop directly
  const clickEvent = new window.MouseEvent('click', { bubbles: true, cancelable: true });
  uploadModal.dispatchEvent(clickEvent);
  assertTest('Clicking outside Upload Modal dismisses it and clears overflow',
    !uploadModal.classList.contains('active') && document.body.style.overflow === ''
  );

  // Test 4: Official Decision Modal Lifecycle
  window.openOfficialDecisionModal();
  const decisionModal = document.getElementById('officialDecisionModal');
  assertTest('Open Decision Modal sets active and overflow hidden',
    decisionModal.classList.contains('active') && document.body.style.overflow === 'hidden'
  );

  window.closeOfficialDecisionModal();
  assertTest('Close Decision Modal clears active and resets body overflow',
    !decisionModal.classList.contains('active') && document.body.style.overflow === ''
  );

  // Test 5: Tab Switch Auto-Dismissal (Anti-Freeze Guard)
  // If user opens a modal and clicks a navigation tab, the modal must auto-dismiss
  window.openOfficialDecisionModal();
  assertTest('Decision Modal opened before tab switch', decisionModal.classList.contains('active'));
  
  // Switch to 'checker' view
  window.switchMainView('checker');
  assertTest('Tab switch to checker auto-dismisses Decision Modal',
    !decisionModal.classList.contains('active')
  );
  assertTest('Tab switch resets body overflow to empty string',
    document.body.style.overflow === ''
  );

  // Switch to 'rules' view after opening upload modal
  window.openUploadDossierModal();
  window.switchMainView('rules');
  assertTest('Tab switch to rules auto-dismisses Upload Modal',
    !uploadModal.classList.contains('active') && document.body.style.overflow === ''
  );

  // Test 6: CV Evidence Modal Lifecycle and Backdrop Click
  window.openCvEvidenceModal('APP-2026-101', 'Computing Lab');
  const cvModal = document.getElementById('cvEvidenceModal');
  assertTest('Open CV Modal sets active and overflow hidden',
    cvModal.classList.contains('active') && document.body.style.overflow === 'hidden'
  );

  cvModal.dispatchEvent(clickEvent);
  assertTest('Click outside CV Modal closes it and resets overflow',
    !cvModal.classList.contains('active') && document.body.style.overflow === ''
  );

  // Test 7: Escape Key Auto-Cleanup
  window.openOfficialDecisionModal();
  assertTest('Decision Modal active before Escape key', decisionModal.classList.contains('active'));
  
  const escEvent = new window.KeyboardEvent('keydown', { key: 'Escape', bubbles: true });
  document.dispatchEvent(escEvent);
  assertTest('Escape key closes all active modals and clears overflow',
    !decisionModal.classList.contains('active') && document.body.style.overflow === ''
  );

  // Test 8: Report Modal and Python Download Integration
  assertTest('window.downloadOfficialPdfReport is defined',
    typeof window.downloadOfficialPdfReport === 'function'
  );
  assertTest('window.exportScrutinyReportJson is defined',
    typeof window.exportScrutinyReportJson === 'function'
  );

  console.log(`\nTest Summary: ${passed}/${total} Passed`);
  if (passed === total) {
    process.exit(0);
  } else {
    process.exit(1);
  }
}

try {
  runTests();
} catch (e) {
  console.error('Test execution error:', e);
  process.exit(1);
}
