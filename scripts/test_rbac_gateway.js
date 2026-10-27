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
          classes: new Set(['hidden']),
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
        innerHTML: '',
        textContent: '',
        value: '',
        setAttribute(k, v) { this[k] = v; },
        getAttribute(k) { return this[k] || null; },
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
  addEventListener() {},
  scrollTo() {}
};
global.tailwind = { config: {} };
global.document = documentMock;
global.localStorage = {
  _data: {},
  getItem(k) { return this._data[k] || null; },
  setItem(k, v) { this._data[k] = String(v); },
  removeItem(k) { delete this._data[k]; }
};
global.sessionStorage = {
  _data: {},
  getItem(k) { return this._data[k] || null; },
  setItem(k, v) { this._data[k] = String(v); },
  removeItem(k) { delete this._data[k]; }
};

const html = fs.readFileSync('portal.html', 'utf8');
const scriptMatches = [...html.matchAll(/<script\b[^>]*>([\s\S]*?)<\/script>/gi)];
const mainScript = scriptMatches.find(m => m[1].length > 1000)[1];

(0, eval)(mainScript);

(async () => {
  console.log('--- TEST 1: UN-AUTHENTICATED ACCESS ---');
  sessionStorage.removeItem('ciphercore_user');
  localStorage.removeItem('ciphercore_user');
  initUserSession();

  const authModal = document.getElementById('authModal');
  console.log('authModal display:', authModal.style.display);
  console.log('authModal hidden class:', authModal.classList.contains('hidden'));
  if (authModal.style.display !== 'flex' || authModal.classList.contains('hidden')) {
    throw new Error('Gateway modal failed to display on unauthenticated load!');
  }

  console.log('--- TEST 2: LOGIN AS OFFICER ---');
  loginAsOfficer();
  const session1 = getStoredSession();
  console.log('Active user:', session1.name, '| Role:', session1.role);
  if (session1.name !== 'Dr. Arvind Kumar' || session1.role !== 'AICTE Scrutiny Officer') {
    throw new Error('Officer login failed');
  }
  const officerApps = getRoleFilteredApplications();
  console.log('Officer visible applications count:', officerApps.length);
  if (officerApps.length !== 5) {
    throw new Error(`Expected 5 institutions for Officer, got ${officerApps.length}`);
  }

  console.log('--- TEST 3: LOGIN AS INSTITUTION SIGNATORY ---');
  loginAsInstitution();
  const session2 = getStoredSession();
  console.log('Active user:', session2.name, '| Role:', session2.role);
  if (session2.name !== 'Prof. Ramesh Patil' || !session2.role.includes('Institution Signatory')) {
    throw new Error('Institution login failed');
  }
  const instApps = getRoleFilteredApplications();
  console.log('Institution visible applications count:', instApps.length);
  if (instApps.length !== 1 || !instApps[0].institution.toLowerCase().includes('apex')) {
    throw new Error(`Expected only Apex Institute for Institution Signatory, got ${instApps.length}`);
  }

  // Check read-only rules behavior
  console.log('Testing Rule toggle as Institution Signatory:');
  const initialRuleState = window.rulesList[0].enabled;
  await toggleRuleItem(0);
  if (window.rulesList[0].enabled !== initialRuleState) {
    throw new Error('Institution Signatory was able to toggle statutory rule!');
  }
  console.log('Rule successfully protected as Read-Only for technical institutions.');

  console.log('--- TEST 4: GOOGLE AUTH SIGN-IN ---');
  handleGoogleSignIn();
  const session3 = getStoredSession();
  console.log('Google Auth User:', session3.name, '| Auth Method:', session3.authMethod);
  if (session3.name !== 'Dr. Arvind Kumar' || session3.role !== 'AICTE Scrutiny Officer') {
    throw new Error('Google Sign-in failed to map to Officer credentials');
  }

  console.log('--- TEST 5: LOGOUT ---');
  logoutUser();
  const session4 = getStoredSession();
  console.log('After logout, activeUserSession:', session4);
  console.log('Session storage ciphercore_user:', sessionStorage.getItem('ciphercore_user'));
  if (session4 !== null || sessionStorage.getItem('ciphercore_user') !== null) {
    throw new Error('Logout failed to clear active session');
  }

  console.log('ALL RBAC & GATEWAY TESTS PASSED WITH 100% SUCCESS!');
})();
