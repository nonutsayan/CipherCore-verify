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
        type: 'text',
        disabled: false,
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
  console.log('=== TEST SUITE: EMAIL & PASSWORD LOGIN & INTERACTIVE AUTH MODAL ===\n');
  initUserSession();

  // Test 1: Tab Switching
  console.log('--- TEST 1: TAB SWITCHING ---');
  switchAuthTab('google');
  if (document.getElementById('authSectionGoogle').classList.contains('hidden')) {
    throw new Error('Google tab should be visible');
  }
  if (!document.getElementById('authSectionEmail').classList.contains('hidden')) {
    throw new Error('Email tab should be hidden');
  }
  console.log('Switched to Google tab successfully.');

  switchAuthTab('email');
  if (document.getElementById('authSectionEmail').classList.contains('hidden')) {
    throw new Error('Email tab should be visible');
  }
  if (!document.getElementById('authSectionGoogle').classList.contains('hidden')) {
    throw new Error('Google tab should be hidden');
  }
  console.log('Switched back to Email tab successfully.\n');

  // Test 2: Quick-Fill Credentials
  console.log('--- TEST 2: QUICK-FILL CREDENTIALS ---');
  fillAuthCredentials('r.sharma@aicte-india.org', 'officer123');
  if (document.getElementById('authEmailInput').value !== 'r.sharma@aicte-india.org') {
    throw new Error('fillAuthCredentials failed to set email');
  }
  if (document.getElementById('authPasswordInput').value !== 'officer123') {
    throw new Error('fillAuthCredentials failed to set password');
  }
  console.log('Quick-fill populated input fields successfully.\n');

  // Test 3: Password Visibility Toggle
  console.log('--- TEST 3: PASSWORD VISIBILITY TOGGLE ---');
  const pwdInput = document.getElementById('authPasswordInput');
  pwdInput.type = 'password';
  togglePasswordVisibility();
  if (pwdInput.type !== 'text') {
    throw new Error('Password visibility should be text after toggle');
  }
  togglePasswordVisibility();
  if (pwdInput.type !== 'password') {
    throw new Error('Password visibility should be password after second toggle');
  }
  console.log('Password visibility toggling functions correctly.\n');

  // Test 4: Invalid Credentials Error Handling
  console.log('--- TEST 4: INVALID CREDENTIALS HANDLING ---');
  document.getElementById('authEmailInput').value = 'wrong.user@unknown.com';
  document.getElementById('authPasswordInput').value = 'invalidpassword';
  await handleEmailPasswordLogin({ preventDefault() {} });
  const errorBox = document.getElementById('authLoginError');
  if (errorBox.classList.contains('hidden')) {
    throw new Error('Error box should be visible on invalid login');
  }
  console.log('Error correctly displayed:', errorBox.textContent);
  console.log('Invalid credentials rejected successfully.\n');

  // Test 5: Valid Scrutiny Officer Login
  console.log('--- TEST 5: VALID SCRUTINY OFFICER LOGIN ---');
  document.getElementById('authEmailInput').value = 'r.sharma@aicte-india.org';
  document.getElementById('authPasswordInput').value = 'officer123';
  await handleEmailPasswordLogin({ preventDefault() {} });
  const user1 = getStoredSession();
  console.log('Logged in user:', user1.name, '| Role:', user1.role, '| Email:', user1.email);
  if (!user1.role.includes('Officer')) {
    throw new Error('Expected Scrutiny Officer role');
  }
  const officerApps = getRoleFilteredApplications();
  console.log('Officer application queue count:', officerApps.length);
  if (officerApps.length !== 6) {
    throw new Error(`Expected 6 apps for Officer, got ${officerApps.length}`);
  }
  console.log('Officer login successful with full 6-institution queue.\n');

  // Test 6: Valid Institution Signatory Login
  console.log('--- TEST 6: VALID INSTITUTION SIGNATORY LOGIN ---');
  logoutUser();
  document.getElementById('authEmailInput').value = 'principal@apextech.edu.in';
  document.getElementById('authPasswordInput').value = 'apex123';
  await handleEmailPasswordLogin({ preventDefault() {} });
  const user2 = getStoredSession();
  console.log('Logged in user:', user2.name, '| Role:', user2.role, '| Email:', user2.email);
  if (user2.role.includes('Officer')) {
    throw new Error('Expected Applicant/Institution role, not Officer');
  }
  const instApps = getRoleFilteredApplications();
  console.log('Institution application queue count:', instApps.length);
  if (instApps.length !== 1) {
    throw new Error(`Expected 1 app for Institution, got ${instApps.length}`);
  }
  console.log('Institution applicant login successful with restricted 1-institution queue.\n');

  console.log('ALL EMAIL & PASSWORD LOGIN TESTS PASSED WITH 100% SUCCESS!');
})();
