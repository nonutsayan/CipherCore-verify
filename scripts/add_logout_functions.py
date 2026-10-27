portal_files = [
    'portal.html',
    'frontend/portal.html',
    'backend/src/main/resources/static/portal.html'
]

logout_code = '''
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
'''

for pf in portal_files:
    with open(pf, 'r', encoding='utf-8') as f:
        content = f.read()

    target = '    function handleInstitutionLogin(e) {'
    idx = content.find(target)
    if idx != -1:
        end_idx = content.find('    }', idx)
        if end_idx != -1:
            end_idx += 5
            if 'function logoutUser()' not in content:
                content = content[:end_idx] + '\n' + logout_code + content[end_idx:]
                with open(pf, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f"Added logoutUser and signOutUser to {pf}")
            else:
                print(f"logoutUser already present in {pf}")
        else:
            print(f"Could not find end of handleInstitutionLogin in {pf}")
    else:
        print(f"Could not find handleInstitutionLogin in {pf}")
