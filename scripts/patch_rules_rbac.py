portal_files = [
    'portal.html',
    'frontend/portal.html',
    'backend/src/main/resources/static/portal.html'
]

old_rules_block = '''    function renderValidationRulesTable() {
      const tbody = document.getElementById('validationRulesTableBody');
      tbody.innerHTML = rulesList.map((r, idx) => `
        <tr>
          <td class="py-3 px-3 font-mono font-bold text-[#0284c7]">${r.code}</td>
          <td class="py-3 px-3"><span class="tag-neutral">${r.category}</span></td>
          <td class="py-3 px-3 font-medium text-main">${r.description}</td>
          <td class="py-3 px-3">${renderStatusTag(r.enabled ? 'Enabled' : 'Disabled')}</td>
          <td class="py-3 px-3 text-right">
            <button onclick="toggleRuleItem(${idx})" class="btn-secondary py-1 px-3 text-xs">
              ${r.enabled ? 'Disable' : 'Enable'}
            </button>
          </td>
        </tr>
      `).join('');
    }

    async function toggleRuleItem(idx) {
      const rule = rulesList[idx];
      if (!rule) return;
      rule.enabled = !rule.enabled;
      renderValidationRulesTable();
      const actor = activeUserSession?.name || 'Compliance Admin';
      logAuditEvent(actor, rule.code, `Rule ${rule.enabled ? 'Enabled' : 'Disabled'}`, rule.description);
      
      await cipherBlockchain.addBlock(rule.code, 'RULE_CONFIG_CHANGED', actor, {
        ruleCode: rule.code,
        status: rule.enabled ? 'ENABLED' : 'DISABLED',
        category: rule.category,
        description: rule.description
      });
      renderBlockchainLedgerUI();
      renderBlockchainLedgerUI();

      showToastBanner(`${rule.code} ${rule.enabled ? 'enabled' : 'disabled'} & notarized on blockchain.`);
    }'''

new_rules_block = '''    function renderValidationRulesTable() {
      const tbody = document.getElementById('validationRulesTableBody');
      if (!tbody) return;
      const isInstitution = activeUserSession && activeUserSession.role && activeUserSession.role.includes('Institution');

      tbody.innerHTML = rulesList.map((r, idx) => `
        <tr class="hover:bg-subtle transition">
          <td class="py-3 px-3 font-mono font-bold text-[#0284c7]">${r.code}</td>
          <td class="py-3 px-3"><span class="tag-neutral">${r.category}</span></td>
          <td class="py-3 px-3 font-medium text-main">${r.description}</td>
          <td class="py-3 px-3">
            ${isInstitution 
              ? `<span class="px-2 py-0.5 rounded text-[10px] font-mono font-semibold text-amber-700 bg-amber-500/10 border border-amber-500/30">Read-Only (Statutory AICTE Authority Only)</span>`
              : renderStatusTag(r.enabled ? 'Enabled' : 'Disabled')}
          </td>
          <td class="py-3 px-3 text-right">
            ${isInstitution 
              ? `<button disabled class="btn-secondary py-1 px-3 text-xs opacity-50 cursor-not-allowed" style="border-radius:4px;" title="Rule modification restricted to AICTE Regulatory Authority">Disabled</button>`
              : `<button onclick="toggleRuleItem(${idx})" class="btn-secondary py-1 px-3 text-xs" style="border-radius:4px;">${r.enabled ? 'Disable' : 'Enable'}</button>`}
          </td>
        </tr>
      `).join('');
    }

    async function toggleRuleItem(idx) {
      if (activeUserSession && activeUserSession.role && activeUserSession.role.includes('Institution')) {
        showToastBanner('Access Denied: Statutory validation rules are Read-Only for technical institutions.');
        return;
      }
      const rule = rulesList[idx];
      if (!rule) return;
      rule.enabled = !rule.enabled;
      renderValidationRulesTable();
      const actor = activeUserSession?.name || 'Compliance Admin';
      logAuditEvent(actor, rule.code, `Rule ${rule.enabled ? 'Enabled' : 'Disabled'}`, rule.description);
      
      if (typeof cipherBlockchain !== 'undefined' && cipherBlockchain.addBlock) {
        await cipherBlockchain.addBlock(rule.code, 'RULE_CONFIG_CHANGED', actor, {
          ruleCode: rule.code,
          status: rule.enabled ? 'ENABLED' : 'DISABLED',
          category: rule.category,
          description: rule.description
        });
        if (typeof renderBlockchainLedgerUI === 'function') {
          renderBlockchainLedgerUI();
        }
      }

      showToastBanner(`${rule.code} ${rule.enabled ? 'enabled' : 'disabled'} & notarized on blockchain.`);
    }'''

for pf in portal_files:
    with open(pf, 'r', encoding='utf-8') as f:
        content = f.read()

    if old_rules_block in content:
        content = content.replace(old_rules_block, new_rules_block)
        with open(pf, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated rules table and toggle in {pf}")
    else:
        print(f"Could not find exact old_rules_block in {pf}")
