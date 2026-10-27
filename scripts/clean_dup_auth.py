portal_files = [
    'portal.html',
    'frontend/portal.html',
    'backend/src/main/resources/static/portal.html'
]

old_dup_pattern = '''    // Fail-safe instant Google Sign-In with official G icon (zero GSI origin error)
    function handleGoogleSignIn() {
      const btnText = document.getElementById('googleBtnText');
      if (btnText) btnText.textContent = 'Connecting to Google Identity...';
      
      setTimeout(() => {
        const officerSession = {
          name: 'Dr. Arvind Kumar (AICTE Officer)',
          email: 'a.kumar@aicte-india.org',
          role: 'AICTE Scrutiny Officer',
          department: 'Approval Bureau (Western Region)',
          avatar: 'O',
          authMethod: 'Google Identity'
        };
        saveUserSession(officerSession);
        if (btnText) btnText.textContent = 'Sign in with Google';
      }, 500);
    }

    function loginAsTestOfficer() {
      saveUserSession({
        name: 'Dr. Arvind Kumar',
        email: 'a.kumar@aicte-india.org',
        role: 'Scrutiny Officer',
        department: 'Bureau of Approvals (AICTE HQ)',
        avatar: 'O',
        authMethod: 'Evaluator Demo Account'
      });
    }

    function loginAsTestInstitution() {
      saveUserSession({
        name: 'Institution Signatory',
        email: 'principal@apex-tech.edu.in',
        role: 'PID: 1-33249112',
        department: 'Apex Institute of Technology, Pune',
        avatar: 'I',
        authMethod: 'Institution Demo Account'
      });
    }

    function signOutUser() {
      activeUserSession = null;
      localStorage.removeItem(SESSION_KEY);
      updateAuthUI();
      switchMainView('landing');
      showToastBanner('Signed out successfully.');
    }'''

for pf in portal_files:
    with open(pf, 'r', encoding='utf-8') as f:
        content = f.read()

    if old_dup_pattern in content:
        content = content.replace(old_dup_pattern, '')
        with open(pf, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Removed duplicates in {pf}")
    else:
        print(f"Could not find exact dup pattern in {pf}")
