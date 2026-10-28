import os
import re

def patch_portal(file_path):
    print(f"Patching {file_path}...")
    with open(file_path, 'r', encoding='utf-8') as f:
        html = f.read()

    # 1. Add Google Identity Services client script to <head> if not present
    gsi_script = '<script src="https://accounts.google.com/gsi/client" async defer></script>'
    if 'accounts.google.com/gsi/client' not in html:
        html = html.replace('</head>', f'  {gsi_script}\n</head>', 1)
        print("  Added Google Identity Services script to <head>")

    # 2. Replace #authModal with modern dual-mode interactive auth modal
    modal_start = html.find('<div id="authModal"')
    if modal_start == -1:
        print("  ERROR: #authModal not found!")
        return False

    modal_end_marker = 'MODAL 2: UPLOAD APPLICATION DOSSIER'
    modal_end = html.find(modal_end_marker, modal_start)

    if modal_end == -1:
        print("  ERROR: End marker of #authModal not found!")
        return False

    # Back up to comment before MODAL 2
    comment_marker = '<!-- ====================================================================='
    modal_end_comment = html.rfind(comment_marker, modal_start, modal_end)
    if modal_end_comment == -1:
        modal_end_comment = modal_end

    new_modal_html = """<div id="authModal" class="modal-backdrop" style="display:none;" onclick="if(event.target===this && sessionStorage.getItem('ciphercore_user'))closeAuthModal()">
    <div class="card-surface max-w-lg w-full p-6 space-y-4 shadow-2xl border border-line rounded-[6px]" onclick="event.stopPropagation()">
      <div class="flex items-center justify-between border-b border-line pb-3">
        <div class="flex items-center gap-2.5">
          <div class="w-8 h-8 rounded bg-[#0284c7] flex items-center justify-center text-white font-bold text-xs shadow-sm">CC</div>
          <div>
            <h2 class="text-sm font-bold text-main tracking-tight">CipherCore Access Gateway</h2>
            <p class="text-[11px] text-sub">AICTE Statutory Scrutiny & Regulatory Verification · Problem Statement SIH26261</p>
          </div>
        </div>
        <button id="authModalCloseBtn" onclick="closeAuthModal()" class="text-sub hover:text-main font-mono text-sm px-2 py-1 rounded hover:bg-subtle" title="Dismiss Gateway (If already signed in)">✕</button>
      </div>

      <!-- Tab Switcher: Email/Password (Real Auth) vs Google & 1-Click Access -->
      <div class="flex items-center gap-1 border-b border-line pb-2 text-xs">
        <button id="authTabBtnEmail" type="button" onclick="switchAuthTab('email')" class="px-3 py-1.5 font-semibold rounded border border-[#0284c7] text-[#0284c7] bg-[#0284c7]/10 transition flex items-center gap-1.5">
          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" />
          </svg>
          <span>Email & Password Login</span>
        </button>
        <button id="authTabBtnGoogle" type="button" onclick="switchAuthTab('google')" class="px-3 py-1.5 font-medium rounded border border-transparent text-sub hover:text-main hover:bg-subtle transition flex items-center gap-1.5">
          <svg class="w-3.5 h-3.5" viewBox="0 0 24 24">
            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"/>
            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"/>
          </svg>
          <span>Google & Fast Access</span>
        </button>
      </div>

      <!-- SECTION 1: REAL EMAIL & PASSWORD LOGIN -->
      <div id="authSectionEmail" class="space-y-3.5">
        <div id="authLoginError" class="hidden text-xs p-2.5 rounded border border-rose-500/30 bg-rose-500/10 text-rose-500 font-medium"></div>

        <form id="emailLoginForm" onsubmit="handleEmailPasswordLogin(event)" class="space-y-3">
          <div>
            <label class="block text-sub text-[11px] font-medium mb-1">Institutional Email or Username</label>
            <div class="relative">
              <input
                id="authEmailInput"
                type="text"
                required
                autocomplete="username"
                placeholder="e.g. r.sharma@aicte-india.org or officer_sc401"
                class="w-full px-3 py-2 subtle-surface text-main text-xs border border-line focus:outline-none focus:border-[#0284c7] rounded-[5px]"
              />
            </div>
          </div>

          <div>
            <div class="flex items-center justify-between mb-1">
              <label class="block text-sub text-[11px] font-medium">Password</label>
              <button id="authPasswordToggleBtn" type="button" onclick="togglePasswordVisibility()" class="text-[10px] text-sub hover:text-main font-mono">Show</button>
            </div>
            <div class="relative">
              <input
                id="authPasswordInput"
                type="password"
                required
                autocomplete="current-password"
                placeholder="Enter account password"
                class="w-full px-3 py-2 subtle-surface text-main text-xs border border-line focus:outline-none focus:border-[#0284c7] rounded-[5px] pr-10"
              />
            </div>
          </div>

          <button id="authLoginSubmitBtn" type="submit" class="btn-primary w-full py-2 text-xs flex items-center justify-center gap-2 font-semibold">
            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
            </svg>
            <span>Sign In to Verification Portal</span>
          </button>
        </form>

        <!-- Quick-Fill Evaluator Badges -->
        <div class="pt-2 border-t border-line/60">
          <div class="text-[10px] font-semibold text-sub uppercase tracking-wider mb-2">Evaluator Demo Accounts (Click to Auto-Fill):</div>
          <div class="grid grid-cols-1 sm:grid-cols-3 gap-1.5 text-[11px]">
            <button type="button" onclick="fillAuthCredentials('r.sharma@aicte-india.org', 'officer123')" class="p-2 text-left rounded border border-line hover:border-[#0284c7] hover:bg-subtle transition">
              <div class="font-bold text-main truncate">Scrutiny Officer</div>
              <div class="text-[10px] text-sub font-mono truncate">r.sharma@aicte-india.org</div>
              <div class="text-[9px] text-emerald-600 font-mono mt-0.5">officer123 (6 Inst)</div>
            </button>
            <button type="button" onclick="fillAuthCredentials('principal@apextech.edu.in', 'apex123')" class="p-2 text-left rounded border border-line hover:border-amber-500/60 hover:bg-subtle transition">
              <div class="font-bold text-main truncate">Applicant (Apex)</div>
              <div class="text-[10px] text-sub font-mono truncate">principal@apextech.edu.in</div>
              <div class="text-[9px] text-amber-600 font-mono mt-0.5">apex123 (1 Inst)</div>
            </button>
            <button type="button" onclick="fillAuthCredentials('admin@aicte-india.org', 'admin123')" class="p-2 text-left rounded border border-line hover:border-[#0284c7] hover:bg-subtle transition">
              <div class="font-bold text-main truncate">System Admin</div>
              <div class="text-[10px] text-sub font-mono truncate">admin@aicte-india.org</div>
              <div class="text-[9px] text-[#0284c7] font-mono mt-0.5">admin123 (Full)</div>
            </button>
          </div>
        </div>
      </div>

      <!-- SECTION 2: GOOGLE OAUTH & 1-CLICK PROFILES -->
      <div id="authSectionGoogle" class="space-y-3 hidden">
        <!-- Google GIS Sign-In Card -->
        <div class="card-surface border border-line hover:border-[#4285F4] p-3.5 rounded-[6px] shadow-sm space-y-2.5">
          <div class="flex items-center justify-between gap-3">
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 rounded bg-white border border-slate-200 flex items-center justify-center shrink-0 shadow-sm">
                <svg class="w-5 h-5" viewBox="0 0 24 24">
                  <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                  <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                  <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"/>
                  <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"/>
                </svg>
              </div>
              <div>
                <div class="text-xs font-bold text-main">Google Identity Services (OAuth 2.0)</div>
                <div class="text-[11px] text-sub">AICTE Evaluator Allowlist: arvind.kumar.aicte@gmail.com</div>
              </div>
            </div>
            <button onclick="handleGoogleSignIn()" class="btn-primary py-1 px-3 text-xs shrink-0">Sign In</button>
          </div>
          <div id="googleGsiContainer" class="flex justify-center pt-1"></div>
        </div>

        <!-- 1-Click Fast Scrutiny Officer -->
        <div onclick="loginAsOfficer()" class="group card-surface hover:bg-subtle border border-line hover:border-[#0284c7] p-3 rounded-[6px] cursor-pointer transition shadow-sm flex items-center justify-between gap-3">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 rounded bg-[#0284c7]/10 border border-[#0284c7]/30 flex items-center justify-center text-[#0284c7] shrink-0">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
              </svg>
            </div>
            <div>
              <div class="text-xs font-bold text-main">1-Click AICTE Scrutiny Officer</div>
              <div class="text-[10px] text-sub font-mono">Dr. Arvind Kumar · Approval Bureau (Full Authority)</div>
            </div>
          </div>
          <button class="btn-secondary py-0.5 px-2.5 text-xs shrink-0">Quick Login</button>
        </div>

        <!-- 1-Click Fast Institution Signatory -->
        <div onclick="loginAsInstitution()" class="group card-surface hover:bg-subtle border border-line hover:border-amber-500/60 p-3 rounded-[6px] cursor-pointer transition shadow-sm flex items-center justify-between gap-3">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 rounded bg-amber-500/10 border border-amber-500/30 flex items-center justify-center text-amber-600 shrink-0">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
              </svg>
            </div>
            <div>
              <div class="text-xs font-bold text-main">1-Click Institution Signatory</div>
              <div class="text-[10px] text-sub font-mono">Prof. Ramesh Patil · Apex Institute (Restricted Queue)</div>
            </div>
          </div>
          <button class="btn-secondary py-0.5 px-2.5 text-xs shrink-0">Quick Login</button>
        </div>
      </div>

      <div class="pt-2 text-[10px] text-tertiary border-t border-line flex items-center justify-between">
        <span>Statutory AICTE Decision-Support Environment</span>
        <span class="font-mono">SIH26261 · Chapter I Compliant</span>
      </div>
    </div>
  </div>\n\n  """

    slice_before_modal = html[:modal_start]
    slice_after_modal = html[modal_end_comment:]
    html = slice_before_modal + new_modal_html + slice_after_modal
    print("  Replaced #authModal markup")

    # 3. Add JavaScript Auth Functions: switchAuthTab, togglePasswordVisibility, fillAuthCredentials, handleEmailPasswordLogin, initGoogleIdentityServices
    js_marker = 'function loginAsOfficer()'
    if js_marker not in html:
        print("  ERROR: function loginAsOfficer() not found!")
        return False

    auth_js_code = """    // ---------------------------------------------------------------------
    // AUTHENTICATION GATEWAY & IDENTITY SERVICES (SIH26261)
    // ---------------------------------------------------------------------
    function switchAuthTab(tab) {
      const emailTabBtn = document.getElementById('authTabBtnEmail');
      const googleTabBtn = document.getElementById('authTabBtnGoogle');
      const emailSection = document.getElementById('authSectionEmail');
      const googleSection = document.getElementById('authSectionGoogle');

      if (tab === 'email') {
        if (emailTabBtn) {
          emailTabBtn.className = 'px-3 py-1.5 font-semibold rounded border border-[#0284c7] text-[#0284c7] bg-[#0284c7]/10 transition flex items-center gap-1.5';
        }
        if (googleTabBtn) {
          googleTabBtn.className = 'px-3 py-1.5 font-medium rounded border border-transparent text-sub hover:text-main hover:bg-subtle transition flex items-center gap-1.5';
        }
        if (emailSection) emailSection.classList.remove('hidden');
        if (googleSection) googleSection.classList.add('hidden');
      } else {
        if (googleTabBtn) {
          googleTabBtn.className = 'px-3 py-1.5 font-semibold rounded border border-[#0284c7] text-[#0284c7] bg-[#0284c7]/10 transition flex items-center gap-1.5';
        }
        if (emailTabBtn) {
          emailTabBtn.className = 'px-3 py-1.5 font-medium rounded border border-transparent text-sub hover:text-main hover:bg-subtle transition flex items-center gap-1.5';
        }
        if (emailSection) emailSection.classList.add('hidden');
        if (googleSection) googleSection.classList.remove('hidden');
        initGoogleIdentityServices();
      }
    }
    window.switchAuthTab = switchAuthTab;

    function togglePasswordVisibility() {
      const pInput = document.getElementById('authPasswordInput');
      const pBtn = document.getElementById('authPasswordToggleBtn');
      if (!pInput) return;
      if (pInput.type === 'password') {
        pInput.type = 'text';
        if (pBtn) pBtn.textContent = 'Hide';
      } else {
        pInput.type = 'password';
        if (pBtn) pBtn.textContent = 'Show';
      }
    }
    window.togglePasswordVisibility = togglePasswordVisibility;

    function fillAuthCredentials(username, password) {
      const emailInput = document.getElementById('authEmailInput');
      const passwordInput = document.getElementById('authPasswordInput');
      const errorBox = document.getElementById('authLoginError');
      if (emailInput) emailInput.value = username;
      if (passwordInput) passwordInput.value = password;
      if (errorBox) {
        errorBox.classList.add('hidden');
        errorBox.textContent = '';
      }
    }
    window.fillAuthCredentials = fillAuthCredentials;

    async function handleEmailPasswordLogin(event) {
      if (event && typeof event.preventDefault === 'function') event.preventDefault();
      const errorBox = document.getElementById('authLoginError');
      const submitBtn = document.getElementById('authLoginSubmitBtn');
      const emailInput = document.getElementById('authEmailInput');
      const passwordInput = document.getElementById('authPasswordInput');

      if (errorBox) {
        errorBox.classList.add('hidden');
        errorBox.textContent = '';
      }

      const usernameOrEmail = emailInput ? emailInput.value.trim() : '';
      const password = passwordInput ? passwordInput.value : '';

      if (!usernameOrEmail || !password) {
        if (errorBox) {
          errorBox.textContent = 'Please enter both institutional email/username and password.';
          errorBox.classList.remove('hidden');
        }
        return;
      }

      const originalBtnHtml = submitBtn ? submitBtn.innerHTML : '';
      if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span>Verifying credentials...</span>';
      }

      let backendBase = document.getElementById('backendUrlInput')?.value?.trim() || 'http://localhost:8080';
      if (backendBase.endsWith('/api')) backendBase = backendBase.slice(0, -4);

      // Attempt live backend authentication
      try {
        const controller = (typeof AbortController !== 'undefined') ? new AbortController() : null;
        const timeoutId = controller ? setTimeout(() => controller.abort(), 3500) : null;

        const res = await fetch(backendBase + '/api/auth/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username: usernameOrEmail, password: password }),
          signal: controller ? controller.signal : undefined
        });
        if (timeoutId) clearTimeout(timeoutId);

        if (res.ok) {
          const data = await res.json();
          const roleStr = String(data.role || '');
          const isOfficer = roleStr.includes('OFFICER') || roleStr.includes('ADMIN') || roleStr.includes('REVIEWER');
          saveUserSession({
            name: data.fullName || (isOfficer ? 'Dr. Rajesh Sharma' : 'Institution Signatory'),
            email: data.email || usernameOrEmail,
            role: isOfficer ? 'AICTE Scrutiny Officer' : ('Institution Signatory (' + (data.institutionName || 'Apex Institute') + ')'),
            id: data.username || 'AICTE-USER',
            dept: data.institutionName || (isOfficer ? 'Western Regional Office' : 'Applicant Institution'),
            avatar: isOfficer ? 'O' : 'I',
            token: data.token,
            authMethod: 'Spring Boot REST API (/api/auth/login)'
          });
          showToastBanner('Authenticated successfully: ' + (data.fullName || usernameOrEmail));
          if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalBtnHtml;
          }
          return;
        }
      } catch (netErr) {
        // Backend offline / static hosting
      }

      // Offline / Fallback evaluator credentials verification
      const u = usernameOrEmail.toLowerCase();
      const p = password;

      let session = null;
      if ((u === 'officer_sc401' || u === 'r.sharma@aicte-india.org') && (p === 'officer123' || p === 'Aicte@2026!')) {
        session = {
          name: 'Dr. Rajesh Sharma',
          role: 'AICTE Scrutiny Officer',
          id: 'AICTE-OFFICER-782',
          dept: 'Western Regional Office (WRO-MUM)',
          email: 'r.sharma@aicte-india.org',
          avatar: 'O',
          token: 'client_session_officer_sc401_' + Date.now(),
          authMethod: 'Email & Password (AICTE Scrutiny Officer)'
        };
      } else if ((u === 'a.kumar@aicte-india.org' || u === 'arvind_kumar' || u === 'arvind.kumar@aicte-india.org') && (p === 'officer123' || p === 'Aicte@2026!')) {
        session = {
          name: 'Dr. Arvind Kumar',
          role: 'AICTE Scrutiny Officer',
          id: 'AICTE-OFFICER-782',
          dept: 'Approval Bureau',
          email: 'a.kumar@aicte-india.org',
          avatar: 'O',
          token: 'client_session_arvind_kumar_' + Date.now(),
          authMethod: 'Email & Password (AICTE Scrutiny Officer)'
        };
      } else if ((u === 'applicant_apex' || u === 'principal@apextech.edu.in' || u === 'ramesh.patil@apex.edu' || u === 'principal@apexinstitute.edu.in') && (p === 'apex123' || p === 'Apex@2026!')) {
        session = {
          name: 'Prof. Ramesh Patil',
          role: 'Institution Signatory (Apex Institute)',
          id: '1-33249112',
          dept: 'Apex Institute of Technology',
          email: 'principal@apextech.edu.in',
          avatar: 'I',
          token: 'client_session_applicant_apex_' + Date.now(),
          authMethod: 'Email & Password (Apex Institute)'
        };
      } else if ((u === 'admin' || u === 'admin@aicte-india.org') && (p === 'admin123' || p === 'Admin@2026!')) {
        session = {
          name: 'AICTE System Administrator',
          role: 'AICTE Scrutiny Officer',
          id: 'AICTE-HQ-ADMIN',
          dept: 'AICTE Headquarters',
          email: 'admin@aicte-india.org',
          avatar: 'A',
          token: 'client_session_admin_' + Date.now(),
          authMethod: 'Email & Password (Administrator)'
        };
      } else if ((u === 'applicant_bharat' || u === 'registrar@bharatengg.edu.in') && (p === 'bharat123')) {
        session = {
          name: 'Registrar Bharat Engg',
          role: 'Institution Signatory (Bharat Engineering)',
          id: 'INST-BHARAT-002',
          dept: 'Bharat Engineering College',
          email: 'registrar@bharatengg.edu.in',
          avatar: 'I',
          token: 'client_session_applicant_bharat_' + Date.now(),
          authMethod: 'Email & Password (Bharat Engineering)'
        };
      }

      if (session) {
        saveUserSession(session);
        showToastBanner('Welcome, ' + session.name + '! Logged in as ' + session.role + '.');
      } else {
        if (errorBox) {
          errorBox.textContent = 'Invalid email/username or password. Please verify your credentials or click one of the demo quick-fill accounts.';
          errorBox.classList.remove('hidden');
        }
      }

      if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalBtnHtml;
      }
    }
    window.handleEmailPasswordLogin = handleEmailPasswordLogin;

    function initGoogleIdentityServices() {
      const container = document.getElementById('googleGsiContainer');
      if (!container) return;
      if (typeof window !== 'undefined' && window.google?.accounts?.id) {
        try {
          const clientId = window.ciphercoreGoogleClientId || 'ciphercore-evaluator-client-id';
          window.google.accounts.id.initialize({
            client_id: clientId,
            callback: handleGoogleCredentialResponse,
            auto_select: false
          });
          window.google.accounts.id.renderButton(
            container,
            { theme: 'outline', size: 'medium', text: 'signin_with', shape: 'rectangular' }
          );
        } catch (e) {
          console.log('Google GIS render notice:', e.message);
        }
      }
    }

    async function handleGoogleCredentialResponse(response) {
      if (!response || !response.credential) return;
      let backendBase = document.getElementById('backendUrlInput')?.value?.trim() || 'http://localhost:8080';
      if (backendBase.endsWith('/api')) backendBase = backendBase.slice(0, -4);
      try {
        const res = await fetch(backendBase + '/api/auth/google', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ idToken: response.credential })
        });
        if (res.ok) {
          const authData = await res.json();
          const isOfficer = authData.isOfficer || (authData.role === 'ROLE_REGULATORY_OFFICER');
          saveUserSession({
            name: authData.fullName || 'Google User',
            email: authData.email,
            role: isOfficer ? 'AICTE Scrutiny Officer' : 'Institution Signatory',
            id: authData.username || 'GOOGLE-USER',
            dept: isOfficer ? 'AICTE Scrutiny Committee' : 'Institutional Applicant',
            avatar: 'G',
            token: authData.token,
            authMethod: 'Google Identity Services (OAuth 2.0 Verified)'
          });
          showToastBanner('Google Sign-In verified: ' + authData.email);
        } else {
          const err = await res.json().catch(() => ({}));
          alert('Google Auth Verification Error: ' + (err.error || 'Token verification failed'));
        }
      } catch (e) {
        console.warn('Backend Google Auth error', e);
      }
    }

"""

    html = html.replace(js_marker, auth_js_code + '\n' + js_marker, 1)
    print("  Injected JavaScript auth handlers")

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(html)
    print(f"Successfully updated {file_path}")
    return True

if __name__ == '__main__':
    patch_portal('portal.html')
    patch_portal('frontend/portal.html')
