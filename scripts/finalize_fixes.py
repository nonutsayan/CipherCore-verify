import re

# 1. Clean em dash in landing pages
landing_files = [
    'index.html',
    'landing.html',
    'frontend/index.html',
    'frontend/landing.html',
    'backend/src/main/resources/static/index.html',
    'backend/src/main/resources/static/landing.html'
]

for lf in landing_files:
    try:
        with open(lf, 'r', encoding='utf-8') as f:
            content = f.read()
        fixed = content.replace('\u2014', '-')
        with open(lf, 'w', encoding='utf-8') as f:
            f.write(fixed)
        print(f"Purged em-dashes from {lf}")
    except Exception as e:
        print(f"Error updating {lf}: {e}")

# 2. Update portal files
portal_files = [
    'portal.html',
    'frontend/portal.html',
    'backend/src/main/resources/static/portal.html'
]

new_auth_modal_html = '''  <!-- =====================================================================
       MODAL 1: AUTHENTICATION / ACCESS GATEWAY (ENFORCED ON ENTRY)
       ===================================================================== -->
  <div id="authModal" class="fixed inset-0 z-50 hidden flex items-center justify-center p-4" style="background: rgba(15, 23, 42, 0.75); backdrop-filter: blur(8px); -webkit-backdrop-filter: blur(8px);">
    <div class="card-surface max-w-lg w-full p-6 space-y-5 shadow-2xl border border-line rounded-[6px]" onclick="event.stopPropagation()">
      <div class="flex items-center justify-between border-b border-line pb-3">
        <div class="flex items-center gap-2.5">
          <div class="w-8 h-8 rounded bg-[#0284c7] flex items-center justify-center text-white font-bold text-xs shadow-sm">CC</div>
          <div>
            <h2 class="text-sm font-bold text-main tracking-tight">CipherCore Access Gateway</h2>
            <p class="text-[11px] text-sub">AICTE Autonomous Scrutiny · Problem Statement 26261</p>
          </div>
        </div>
        <button id="authModalCloseBtn" onclick="closeAuthModal()" class="text-sub hover:text-main font-mono text-sm px-2 py-1 rounded hover:bg-subtle" title="Dismiss Gateway (If already signed in)">✕</button>
      </div>

      <div class="text-xs text-sub leading-relaxed">
        Select authorized credentials to access the AICTE Regulatory Decision-Support environment. Logged-in role strictly enforces queue visibility and statutory sign-off authority.
      </div>

      <!-- 3 Distinct 1-Click Credential Cards + Google Auth -->
      <div class="space-y-3">
        <!-- CARD 1: AICTE Scrutiny Officer -->
        <div onclick="loginAsOfficer()" class="group card-surface hover:bg-subtle border border-line hover:border-[#0284c7] p-3.5 rounded-[6px] cursor-pointer transition shadow-sm flex items-start justify-between gap-3">
          <div class="flex items-start gap-3">
            <div class="w-9 h-9 rounded bg-[#0284c7]/10 border border-[#0284c7]/30 flex items-center justify-center text-[#0284c7] shrink-0 mt-0.5 group-hover:bg-[#0284c7] group-hover:text-white transition">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
              </svg>
            </div>
            <div>
              <div class="flex items-center gap-2">
                <span class="text-xs font-bold text-main">AICTE Scrutiny Officer</span>
                <span class="px-1.5 py-0.2 rounded text-[10px] font-mono font-semibold bg-emerald-500/10 text-emerald-600 border border-emerald-500/30">Full Statutory Authority</span>
              </div>
              <div class="text-xs text-main font-semibold mt-0.5">Dr. Arvind Kumar</div>
              <div class="text-[11px] font-mono text-sub mt-0.5">ID: AICTE-OFFICER-782 · Approval Bureau</div>
              <div class="text-[10px] text-tertiary mt-1">Full 5-institution queue access, forensic diff, CV lab audit, and statutory decision sign-off.</div>
            </div>
          </div>
          <button class="btn-primary py-1 px-3 text-xs shrink-0 self-center">Login</button>
        </div>

        <!-- CARD 2: Institution Signatory -->
        <div onclick="loginAsInstitution()" class="group card-surface hover:bg-subtle border border-line hover:border-amber-500/60 p-3.5 rounded-[6px] cursor-pointer transition shadow-sm flex items-start justify-between gap-3">
          <div class="flex items-start gap-3">
            <div class="w-9 h-9 rounded bg-amber-500/10 border border-amber-500/30 flex items-center justify-center text-amber-600 shrink-0 mt-0.5 group-hover:bg-amber-600 group-hover:text-white transition">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
              </svg>
            </div>
            <div>
              <div class="flex items-center gap-2">
                <span class="text-xs font-bold text-main">Institution Signatory</span>
                <span class="px-1.5 py-0.2 rounded text-[10px] font-mono font-semibold bg-amber-500/10 text-amber-600 border border-amber-500/30">Restricted Access</span>
              </div>
              <div class="text-xs text-main font-semibold mt-0.5">Prof. Ramesh Patil</div>
              <div class="text-[11px] font-mono text-sub mt-0.5">Apex Institute of Technology · PID: 1-33249112</div>
              <div class="text-[10px] text-tertiary mt-1">Single-institution queue (AP-2026-101), rectification dossier upload, read-only validation rules.</div>
            </div>
          </div>
          <button class="btn-secondary py-1 px-3 text-xs shrink-0 self-center">Login</button>
        </div>

        <!-- CARD 3: Sign in with Google -->
        <div onclick="handleGoogleSignIn()" class="group card-surface hover:bg-subtle border border-line hover:border-[#4285F4] p-3.5 rounded-[6px] cursor-pointer transition shadow-sm flex items-center justify-between gap-3">
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
              <div class="text-xs font-bold text-main">Sign in with Google</div>
              <div class="text-[11px] text-sub">Instant Evaluator Access · Dr. Arvind Kumar (AICTE Scrutiny Officer)</div>
            </div>
          </div>
          <button class="btn-primary py-1 px-3 text-xs shrink-0 self-center">OAuth</button>
        </div>
      </div>
    </div>
  </div>'''

for pf in portal_files:
    try:
        with open(pf, 'r', encoding='utf-8') as f:
            p_content = f.read()

        # Replace authModal
        auth_start = p_content.find('id="authModal"')
        if auth_start != -1:
            # find previous div start
            div_start = p_content.rfind('<div', 0, auth_start)
            # find next modal start
            next_modal = p_content.find('id="uploadDossierModal"', auth_start)
            if next_modal != -1:
                div_end = p_content.rfind('<!-- ===', 0, next_modal)
                p_content = p_content[:div_start] + new_auth_modal_html + '\n\n  ' + p_content[div_end:]

        # Also purge any em dash
        p_content = p_content.replace('\u2014', '-')

        # Ensure signOutUser is wired to logoutUser
        if 'function signOutUser()' not in p_content:
            p_content = p_content.replace('function logoutUser() {', 'function signOutUser() { logoutUser(); }\n    function logoutUser() {')

        with open(pf, 'w', encoding='utf-8') as f:
            f.write(p_content)
        print(f"Updated {pf} successfully!")
    except Exception as e:
        print(f"Error updating {pf}: {e}")
