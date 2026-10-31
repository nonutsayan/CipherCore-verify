portal_files = [
    'portal.html',
    'frontend/portal.html',
    'backend/src/main/resources/static/portal.html'
]

broken_block = '''    function handleInstitutionLogin(e) {
      e.preventDefault();
      const pid = document.getElementById('instPidInput').value.trim();
      const email = document.getElementById('instEmailInput').value.trim();
      saveUserSession({
        name: 'Institution Signatory',
        email: email || 'principal@institution.edu.in',
        role: 'PID: ' + (pid || '1-33249112'),
        department: 'Technical Institution',
        avatar: 'I',
        authMethod: 'Institution Credentials'
      }

    function logoutUser() {
      activeUserSession = null;
      try {
        if (typeof sessionStorage !== 'undefined') {
          sessionStorage.removeItem('ciphercore_user');
        }
        localStorage.removeItem('ciphercore_user');
      } catch (e) {}
      updateAuthUI();
      openAuthModal(true);
      showToastBanner('Signed out. Please select credentials to enter portal.');
    }

    function signOutUser() {
      logoutUser();
    }
);
    }'''

clean_block = '''    function handleInstitutionLogin(e) {
      e.preventDefault();
      const pid = document.getElementById('instPidInput').value.trim();
      const email = document.getElementById('instEmailInput').value.trim();
      saveUserSession({
        name: 'Institution Signatory',
        email: email || 'principal@institution.edu.in',
        role: 'PID: ' + (pid || '1-33249112'),
        department: 'Technical Institution',
        avatar: 'I',
        authMethod: 'Institution Credentials'
      });
    }

    function logoutUser() {
      activeUserSession = null;
      try {
        if (typeof sessionStorage !== 'undefined') {
          sessionStorage.removeItem('ciphercore_user');
        }
        localStorage.removeItem('ciphercore_user');
      } catch (e) {}
      updateAuthUI();
      openAuthModal(true);
      showToastBanner('Signed out. Please select credentials to enter portal.');
    }

    function signOutUser() {
      logoutUser();
    }'''

for pf in portal_files:
    with open(pf, 'r', encoding='utf-8') as f:
        content = f.read()

    if broken_block in content:
        content = content.replace(broken_block, clean_block)
        with open(pf, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed syntax in {pf}")
    else:
        print(f"Broken block not matched exactly in {pf}, trying regex...")
        import re
        content = re.sub(
            r'function handleInstitutionLogin\(e\) \{.*?authMethod: \'Institution Credentials\'\s*\}\s*function logoutUser\(\) \{.*?function signOutUser\(\) \{\s*logoutUser\(\);\s*\}\s*\);\s*\}',
            clean_block,
            content,
            flags=re.DOTALL
        )
        with open(pf, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Regex fixed syntax in {pf}")
