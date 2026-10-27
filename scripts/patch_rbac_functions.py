portal_files = [
    'portal.html',
    'frontend/portal.html',
    'backend/src/main/resources/static/portal.html'
]

get_role_fn = '''    function getRoleFilteredApplications() {
      if (!applicationsData) return [];
      const isInstitution = activeUserSession && activeUserSession.role && activeUserSession.role.includes('Institution');
      if (isInstitution) {
        // Institution Signatory: ONLY show Apex Institute of Technology (AP-2026-101 / APP-2026-101)
        return applicationsData.filter(app => app.id === 'APP-2026-101' || app.institution.toLowerCase().includes('apex'));
      }
      // AICTE Scrutiny Officer: display ALL 5 institutions
      return applicationsData;
    }
'''

for pf in portal_files:
    with open(pf, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Add getRoleFilteredApplications right before renderApplicationsTable
    if 'function getRoleFilteredApplications()' not in content:
        content = content.replace(
            '    function renderApplicationsTable() {',
            get_role_fn + '\n    function renderApplicationsTable() {'
        )

    # 2. Update renderApplicationsTable to use roleData
    target_apps_check = '''      // If zero applications in queue, display the zero state
      if (applicationsData.length === 0) {
        tableWrapper.classList.add('hidden');
        zeroState.classList.remove('hidden');
        return;
      }

      tableWrapper.classList.remove('hidden');
      zeroState.classList.add('hidden');

      const query = (document.getElementById('appSearchBox')?.value || '').trim().toLowerCase();

      const filtered = applicationsData.filter(app => {'''

    replacement_apps_check = '''      const roleData = getRoleFilteredApplications();

      // If zero applications in queue, display the zero state
      if (!roleData || roleData.length === 0) {
        tableWrapper.classList.add('hidden');
        zeroState.classList.remove('hidden');
        return;
      }

      tableWrapper.classList.remove('hidden');
      zeroState.classList.add('hidden');

      const query = (document.getElementById('appSearchBox')?.value || '').trim().toLowerCase();

      const filtered = roleData.filter(app => {'''

    if target_apps_check in content:
        content = content.replace(target_apps_check, replacement_apps_check)

    # 3. Update switcher in renderInspectView and decision container
    target_switcher = '''      // Populate Switcher Dropdown
      const switcher = document.getElementById('appSwitcherSelect');
      switcher.innerHTML = applicationsData.map(a =>
        `<option value="${a.id}" ${a.id === app.id ? 'selected' : ''}>${a.id} : ${a.institution}</option>`
      ).join('');'''

    replacement_switcher = '''      // Populate Switcher Dropdown (Role Filtered)
      const roleData = getRoleFilteredApplications();
      const switcher = document.getElementById('appSwitcherSelect');
      if (switcher) {
        switcher.innerHTML = roleData.map(a =>
          `<option value="${a.id}" ${a.id === app.id ? 'selected' : ''}>${a.id} : ${a.institution}</option>`
        ).join('');
      }

      // RBAC Action Button: Officer vs Institution Signatory
      const decisionContainer = document.getElementById('decisionOrUploadContainer');
      const isInstitution = activeUserSession && activeUserSession.role && activeUserSession.role.includes('Institution');
      if (decisionContainer) {
        if (isInstitution) {
          decisionContainer.innerHTML = `<button class="btn btn-primary btn-sm py-1.5 px-3 text-xs bg-[#0284c7] text-white flex items-center gap-1.5" style="border-radius:4px;" onclick="alert('Rectification dossier upload portal opened')">
            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" /></svg>
            <span>Upload Rectification Document</span>
          </button>`;
        } else {
          decisionContainer.innerHTML = `<button onclick="openOfficialDecisionModal()" class="btn-primary flex items-center gap-1.5 text-xs">
            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span>Record Official Decision</span>
          </button>`;
        }
      }'''

    if target_switcher in content:
        content = content.replace(target_switcher, replacement_switcher)

    with open(pf, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Patched RBAC in {pf}")
