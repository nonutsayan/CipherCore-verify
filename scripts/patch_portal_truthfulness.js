const fs = require('fs');

function patchFile(filePath) {
  console.log(`Starting patch on ${filePath}...`);
  let content = fs.readFileSync(filePath, 'utf8');
  const isCrlf = content.includes('\r\n');
  let normalized = content.replace(/\r\n/g, '\n');

  // 1. Runtime Mode Badge in portalHeader
  const headerActionsOld = `      <!-- ZONE 3: ACTIONS (RIGHT) - EXACTLY USER BADGE + THEME TOGGLE + SIGN OUT -->
      <div class="header-actions">
        <a href="index.html"`;

  const headerActionsNew = `      <!-- ZONE 3: ACTIONS (RIGHT) - EXACTLY USER BADGE + THEME TOGGLE + SIGN OUT -->
      <div class="header-actions">
        <!-- Runtime Mode Badge -->
        <div id="runtimeModeBadge" class="hidden sm:inline-flex items-center gap-1.5 px-2.5 py-1 rounded text-[11px] font-mono border border-line subtle-surface" title="Probing Backend Runtime...">
          <span id="runtimeModeDot" class="w-2 h-2 rounded-full bg-slate-400"></span>
          <span id="runtimeModeText" class="text-sub font-medium">Detecting Mode...</span>
        </div>
        <a href="index.html"`;

  if (normalized.includes(headerActionsOld)) {
    normalized = normalized.replace(headerActionsOld, headerActionsNew);
    console.log('1. Added runtimeModeBadge to portalHeader');
  } else {
    console.log('1. Target NOT found');
  }

  // 2. CV Evidence Modal - Top Metadata Bar
  const cvTopOld = `        <div>
          <span class="text-sub block text-[10px] uppercase font-semibold">Carpet Area Audited:</span>
          <span class="text-main font-bold">194.2 m² (Norm: 210.5 m²)</span>
        </div>
        <div>
          <span class="text-sub block text-[10px] uppercase font-semibold">YOLOv8 Norm Status:</span>
          <span class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-800 border border-amber-300">DEFICIENT (-8 Units)</span>
        </div>`;

  const cvTopNew = `        <div>
          <span class="text-sub block text-[10px] uppercase font-semibold">Carpet Area Audited:</span>
          <span class="text-main font-bold">194.2 m² (Claimed: 210.5 m²)</span>
        </div>
        <div>
          <span class="text-sub block text-[10px] uppercase font-semibold">Lab Batch Norm Status:</span>
          <span class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-800 border border-amber-300">BATCH DEFICIENT (-8 Units)</span>
        </div>`;

  if (normalized.includes(cvTopOld)) {
    normalized = normalized.replace(cvTopOld, cvTopNew);
    console.log('2. Updated CV Top Metadata Bar');
  } else {
    console.log('2. Target NOT found');
  }

  // 3. CV Evidence Modal - Right Column Norm Disambiguation
  const cvRightOld = `        <!-- Right Column: Regulatory Breakdown & Statutory Notice (4 Cols) -->
        <div class="lg:col-span-4 flex flex-col space-y-3">
          <div class="p-3.5 rounded border border-line subtle-surface space-y-2">
            <span class="text-[10px] uppercase font-bold text-sub tracking-wider block">AICTE APH 2024-27 Clause 4.1 Audit</span>
            <div class="space-y-1.5 text-xs font-mono">
              <div class="flex justify-between items-center py-1 border-b border-line">
                <span class="text-sub">Mandatory Terminal Ratio:</span>
                <span class="text-main font-bold">1:1 per Approved Intake</span>
              </div>
              <div class="flex justify-between items-center py-1 border-b border-line">
                <span class="text-sub">Required Terminal Count:</span>
                <span class="text-main font-bold">150 Units</span>
              </div>
              <div class="flex justify-between items-center py-1 border-b border-line">
                <span class="text-sub">AI CV Detected Active:</span>
                <span class="text-emerald-600 font-bold">142 Units</span>
              </div>
              <div class="flex justify-between items-center py-1 border-b border-line">
                <span class="text-sub">Net Statutory Deficit:</span>
                <span class="text-red-600 font-bold">-8 Terminals (-5.3%)</span>
              </div>
              <div class="flex justify-between items-center py-1 border-b border-line">
                <span class="text-sub">Fire Safety Extinguishers:</span>
                <span class="text-emerald-600 font-bold">2/2 Verified (Type ABC)</span>
              </div>
              <div class="flex justify-between items-center py-1">
                <span class="text-sub">Carpet Area Shortfall:</span>
                <span class="text-amber-600 font-bold">-16.3 m² (194.2 vs 210.5 m²)</span>
              </div>
            </div>
          </div>

          <!-- Statutory Scrutiny Action Callout -->
          <div class="p-3.5 rounded border-2 border-amber-300 bg-amber-50/60 dark:bg-amber-950/20 text-xs space-y-2 text-amber-950 dark:text-amber-200">
            <div class="flex items-center gap-1.5 font-bold text-[11px] uppercase tracking-wider text-amber-800 dark:text-amber-300">
              <svg class="w-4 h-4 text-amber-600 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
              <span>Automated Scrutiny Finding</span>
            </div>
            <p class="text-[11px] leading-relaxed">
              Institution falls 8 computer terminals short of AICTE Approval Process Handbook (APH 2024-27) norms for 180 CSE intake. Recommend attaching finding to Official Scrutiny Dossier for human officer sign-off.
            </p>
          </div>`;

  const cvRightNew = `        <!-- Right Column: Regulatory Breakdown & Statutory Notice (4 Cols) -->
        <div class="lg:col-span-4 flex flex-col space-y-3">
          <div class="p-3 rounded border border-line subtle-surface space-y-2.5">
            <div class="flex items-center justify-between border-b border-line pb-1.5">
              <span class="text-[10px] uppercase font-bold text-sub tracking-wider">AICTE APH 2024-27 Norm Disambiguation</span>
              <span class="px-1.5 py-0.5 text-[9px] font-mono font-bold bg-sky-100 text-sky-800 rounded">3-Tier Audit</span>
            </div>

            <div class="space-y-2 text-xs font-mono">
              <!-- Tier 1: Lab Batch Declaration -->
              <div class="p-2 rounded card-surface border border-line space-y-0.5">
                <div class="flex justify-between items-center text-[11px]">
                  <span class="font-bold text-main">1. Lab Batch Declaration</span>
                  <span class="px-1.5 py-0.2 rounded text-[10px] font-bold bg-red-100 text-red-700">SHORTFALL (-8)</span>
                </div>
                <div class="flex justify-between text-[11px] text-sub">
                  <span>Claimed: 150 Workstations</span>
                  <span>Detected: <strong class="text-main">142 Units</strong></span>
                </div>
                <div class="text-[10px] text-red-600 font-sans">Deficit: 8 vacant bays (5.3% below institution self-declaration)</div>
              </div>

              <!-- Tier 2: Sanctioned Intake (1:1 Basis) -->
              <div class="p-2 rounded card-surface border border-line space-y-0.5">
                <div class="flex justify-between items-center text-[11px]">
                  <span class="font-bold text-main">2. Total Intake (1:1 Basis)</span>
                  <span class="px-1.5 py-0.2 rounded text-[10px] font-bold bg-amber-100 text-amber-800">SHORTFALL (-38)</span>
                </div>
                <div class="flex justify-between text-[11px] text-sub">
                  <span>Intake: 180 Students</span>
                  <span>Detected: <strong class="text-main">142 Units</strong></span>
                </div>
                <div class="text-[10px] text-amber-700 font-sans">Deficit: 38 terminals if measured against 1:1 concurrent intake</div>
              </div>

              <!-- Tier 3: AICTE Statutory Standard (1:4 Ratio) -->
              <div class="p-2 rounded card-surface border border-line space-y-0.5">
                <div class="flex justify-between items-center text-[11px]">
                  <span class="font-bold text-main">3. APH Appendix 4 (1:4 Norm)</span>
                  <span class="px-1.5 py-0.2 rounded text-[10px] font-bold bg-emerald-100 text-emerald-800">COMPLIANT (+97)</span>
                </div>
                <div class="flex justify-between text-[11px] text-sub">
                  <span>Prescribed: 45 (180/4)</span>
                  <span>Detected: <strong class="text-emerald-600">142 Units</strong></span>
                </div>
                <div class="text-[10px] text-emerald-700 font-sans">Compliant: Ratio is 1:1.27 (Surplus buffer of 97 workstations)</div>
              </div>

              <!-- Carpet Area & Density Breakdown -->
              <div class="p-2 rounded subtle-surface border border-line text-[11px] space-y-1">
                <div class="flex justify-between items-center font-semibold">
                  <span class="text-sub">Carpet Area Shortfall:</span>
                  <span class="text-amber-700 font-bold">-16.3 m² (-7.7%)</span>
                </div>
                <div class="flex justify-between text-[10px] text-sub">
                  <span>Plan: 210.5 m²</span>
                  <span>Physical: 194.2 m²</span>
                  <span>Density: 1.37 m²/unit</span>
                </div>
                <div class="text-[10px] text-slate-500 font-sans">Terminal density 1.37 m² complies with APH 1.2-1.5 m² norm.</div>
              </div>
            </div>
          </div>

          <!-- Statutory Scrutiny Action Callout -->
          <div class="p-3 rounded border-2 border-amber-300 bg-amber-50/60 dark:bg-amber-950/20 text-xs space-y-1.5 text-amber-950 dark:text-amber-200">
            <div class="flex items-center gap-1.5 font-bold text-[11px] uppercase tracking-wider text-amber-800 dark:text-amber-300">
              <svg class="w-4 h-4 text-amber-600 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
              <span>Automated Scrutiny Advisory</span>
            </div>
            <p class="text-[11px] leading-relaxed">
              <strong>Scrutiny Finding:</strong> Institution meets statutory minimum 1:4 norm (45 required vs 142 detected), but falls 8 terminals short of its self-declared 150 batch capacity and has a 16.3 m² carpet area deficit. Recommend human officer review before final decision.
            </p>
          </div>`;

  if (normalized.includes(cvRightOld)) {
    normalized = normalized.replace(cvRightOld, cvRightNew);
    console.log('3. Updated CV Evidence Modal Right Column');
  } else {
    console.log('3. Target NOT found');
  }

  // 4. Forensic Diff Modal Truthfulness
  normalized = normalized.replace(
    '<span class="px-2 py-0.5 rounded text-[10px] font-bold font-mono bg-red-100 text-red-700 border border-red-300">Forensic Analysis</span>',
    '<span class="px-2 py-0.5 rounded text-[10px] font-bold font-mono bg-amber-100 text-amber-800 border border-amber-300">Heuristic Screening (FIND-2026-DOC-02-ELA)</span>'
  );

  normalized = normalized.replace(
    '<span class="px-2 py-0.5 rounded text-[10px] font-bold bg-red-100 text-red-800 border border-red-300">Altered Layer Detected</span>',
    '<span class="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-800 border border-amber-300">Potential Discontinuity Flagged</span>'
  );

  normalized = normalized.replace(
    '<strong>Altered Layer Detected | ELA: 88.4%</strong> (Post-scan digital raster modification detected in acreage and survey number streams).',
    '<strong>Finding ID: FIND-2026-DOC-02-ELA</strong> (ELA 88.4% Variance Index). Method: Error Level Analysis & Pixel Residual Variance. Classification: Potential Inconsistency / Discontinuity Detected. Status: DEMO_SIMULATED / MANUAL_REVIEW_REQUIRED.'
  );

  normalized = normalized.replace(
    '<span class="text-[10px] px-2 py-0.5 rounded bg-red-100 text-red-700 font-bold">DIGITAL TAMPER CONFIRMED</span>',
    '<span class="text-[10px] px-2 py-0.5 rounded bg-amber-100 text-amber-800 font-bold border border-amber-300">POTENTIAL DISCONTINUITY FLAGGED (MANUAL REVIEW REQUIRED)</span>'
  );

  // Add Statutory Limitations Banner in Forensic Diff Modal
  const forensicTableTarget = '      <!-- Forensic Findings Table -->\n      <div class="space-y-1.5 pt-1">';
  const forensicTableWithNotice = `      <!-- Statutory Limitation Notice -->
      <div class="p-2.5 rounded bg-amber-50/70 dark:bg-amber-950/20 border border-amber-300 text-amber-900 dark:text-amber-200 text-xs flex items-start gap-2">
        <svg class="w-4 h-4 text-amber-600 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <div class="text-[11px] leading-relaxed">
          <strong>Statutory Limitation Notice:</strong> Automated forensic indicators (ELA variance, font stream heuristics, metadata divergence) represent screening flags and do not constitute legal proof of document tampering or fraud without certified examination by a government-approved document examiner under Section 45/47A of the Indian Evidence Act / BSA 2023.
        </div>
      </div>

      <!-- Forensic Findings Table -->
      <div class="space-y-1.5 pt-1">`;

  if (normalized.includes(forensicTableTarget)) {
    normalized = normalized.replace(forensicTableTarget, forensicTableWithNotice);
    console.log('4. Added Statutory Limitation Notice to Forensic Diff Modal');
  } else {
    console.log('4. Target NOT found');
  }

  // 5. Blockchain / Audit Ledger Header & Architecture
  const poaNodesOld = `              <!-- Center: Consortium PoA Node Indicators (7 Active Validators) -->
              <div class="flex items-center gap-2 px-3 py-1.5 rounded border border-line subtle-surface text-xs shadow-xs">
                <span class="font-mono text-[11px] text-sub uppercase font-semibold">PoA Nodes:</span>
                <div class="flex items-center gap-1.5" id="poaNodesList">
                  <span class="relative group cursor-pointer inline-flex items-center" onclick="showNodeConsensusToast('AICTE-HQ (New Delhi)', '0.12 ms')">
                    <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                    <span class="absolute bottom-full mb-1.5 left-1/2 -translate-x-1/2 hidden group-hover:block px-2 py-0.5 bg-slate-900 text-white text-[10px] font-mono rounded whitespace-nowrap shadow-lg z-50">AICTE-HQ (0.12 ms)</span>
                  </span>
                  <span class="relative group cursor-pointer inline-flex items-center" onclick="showNodeConsensusToast('IIT-Bombay (Powai)', '0.45 ms')">
                    <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                    <span class="absolute bottom-full mb-1.5 left-1/2 -translate-x-1/2 hidden group-hover:block px-2 py-0.5 bg-slate-900 text-white text-[10px] font-mono rounded whitespace-nowrap shadow-lg z-50">IIT-Bombay (0.45 ms)</span>
                  </span>
                  <span class="relative group cursor-pointer inline-flex items-center" onclick="showNodeConsensusToast('IIT-Madras (Chennai)', '0.62 ms')">
                    <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                    <span class="absolute bottom-full mb-1.5 left-1/2 -translate-x-1/2 hidden group-hover:block px-2 py-0.5 bg-slate-900 text-white text-[10px] font-mono rounded whitespace-nowrap shadow-lg z-50">IIT-Madras (0.62 ms)</span>
                  </span>
                  <span class="relative group cursor-pointer inline-flex items-center" onclick="showNodeConsensusToast('IISc-Bangalore (Bangalore)', '0.38 ms')">
                    <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                    <span class="absolute bottom-full mb-1.5 left-1/2 -translate-x-1/2 hidden group-hover:block px-2 py-0.5 bg-slate-900 text-white text-[10px] font-mono rounded whitespace-nowrap shadow-lg z-50">IISc-Bangalore (0.38 ms)</span>
                  </span>
                  <span class="relative group cursor-pointer inline-flex items-center" onclick="showNodeConsensusToast('AICTE-WRO (Mumbai)', '0.22 ms')">
                    <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                    <span class="absolute bottom-full mb-1.5 left-1/2 -translate-x-1/2 hidden group-hover:block px-2 py-0.5 bg-slate-900 text-white text-[10px] font-mono rounded whitespace-nowrap shadow-lg z-50">AICTE-WRO (0.22 ms)</span>
                  </span>
                  <span class="relative group cursor-pointer inline-flex items-center" onclick="showNodeConsensusToast('MoE-Gov (Shastri Bhawan)', '0.51 ms')">
                    <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                    <span class="absolute bottom-full mb-1.5 left-1/2 -translate-x-1/2 hidden group-hover:block px-2 py-0.5 bg-slate-900 text-white text-[10px] font-mono rounded whitespace-nowrap shadow-lg z-50">MoE-Gov (0.51 ms)</span>
                  </span>
                  <span class="relative group cursor-pointer inline-flex items-center" onclick="showNodeConsensusToast('NIC-DataHub (NIC-Gov)', '0.30 ms')">
                    <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                    <span class="absolute bottom-full mb-1.5 left-1/2 -translate-x-1/2 hidden group-hover:block px-2 py-0.5 bg-slate-900 text-white text-[10px] font-mono rounded whitespace-nowrap shadow-lg z-50">NIC-DataHub (0.30 ms)</span>
                  </span>
                </div>
                <span class="text-[10px] font-mono text-emerald-700 font-bold hidden sm:inline">7/7 PoA Active</span>
              </div>`;

  const poaNodesNew = `              <!-- Center: Cryptographic Ledger Architecture -->
              <div class="flex items-center gap-2 px-3 py-1.5 rounded border border-line subtle-surface text-xs shadow-xs">
                <span class="font-mono text-[11px] text-sub uppercase font-semibold">Ledger Architecture:</span>
                <span class="px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-sky-100 text-sky-800 border border-sky-300">Sequential SHA-256 Hash-Chain</span>
                <span class="text-[10px] font-mono text-slate-500 hidden sm:inline">(Prototype · Single-Node Notary)</span>
              </div>`;

  if (normalized.includes(poaNodesOld)) {
    normalized = normalized.replace(poaNodesOld, poaNodesNew);
    console.log('5. Replaced PoA validator nodes with honest hash-chain architecture label');
  } else {
    console.log('5. Target NOT found');
  }

  // Consortium reference bar
  const ledgerRefOld = `            <!-- Consortium Node Reference Bar -->
            <div class="flex flex-wrap items-center justify-between text-[11px] text-sub pt-2 border-t border-line">
              <span class="font-mono">Consortium Network: BharatVidya PoA Trust Network (AICTE Sovereign Notary Protocol)</span>
              <span class="font-mono text-[#0284c7]">Formula: SHA-256(index|timestamp|prevHash|payload|actor|target|event)</span>
            </div>`;

  const ledgerRefNew = `            <!-- Prototype Ledger Architecture & Transparency Notice -->
            <div class="flex flex-wrap items-center justify-between text-[11px] text-sub pt-2 border-t border-line">
              <span class="font-mono">Architecture: CipherCore Cryptographic Audit Ledger &amp; Hash-Chain Prototype</span>
              <span class="font-mono text-[#0284c7]">Formula: SHA-256(index|timestamp|prevHash|payload|actor|target|event)</span>
            </div>
            <div class="p-2 rounded subtle-surface border border-line text-[11px] text-sub">
              <strong>Educational Demonstration Prototype:</strong> Sequential SHA-256 hash chaining demonstrates tamper-evidence on a local ledger. Any mutation in historical payload breaks the mathematical link to subsequent blocks. Production targets Byzantine fault-tolerant multi-institution consortium.
            </div>`;

  if (normalized.includes(ledgerRefOld)) {
    normalized = normalized.replace(ledgerRefOld, ledgerRefNew);
    console.log('6. Replaced Consortium reference bar with honest prototype disclosure');
  } else {
    console.log('6. Target NOT found');
  }

  // 6. Sub-Tab 3 Issuing Authority Sandbox Transparency
  const authTabOld = `              <div>
                <h2 class="text-sm font-bold text-main">3. Official Issuing Authority Verification</h2>
                <p class="text-xs text-sub mt-0.5">Direct verification against State Fire Portal, Land Registry, University Affiliation Portal, and Digital Signature records.</p>
              </div>`;

  const authTabNew = `              <div>
                <div class="flex items-center gap-2">
                  <h2 class="text-sm font-bold text-main">3. Official Issuing Authority Verification</h2>
                  <span class="px-1.5 py-0.5 rounded text-[10px] font-mono font-bold bg-sky-100 text-sky-800 border border-sky-300">DEMO_SIMULATED / SANDBOX</span>
                </div>
                <p class="text-xs text-sub mt-0.5">Automated queries against State Fire Directorate, DigiLocker Land Records, and University Affiliation Gateways.</p>
              </div>`;

  if (normalized.includes(authTabOld)) {
    normalized = normalized.replace(authTabOld, authTabNew);
    console.log('7. Added DEMO_SIMULATED badge to Issuing Authority tab');
  } else {
    console.log('7. Target NOT found');
  }

  // 7. Inject detectRuntimeMode() in script
  const initPortalTarget = 'async function initPortal() {';
  const detectRuntimeFunc = `    async function detectRuntimeMode() {
      const badge = document.getElementById('runtimeModeBadge');
      const dot = document.getElementById('runtimeModeDot');
      const text = document.getElementById('runtimeModeText');
      if (!badge || !dot || !text) return;

      const isGitHubPages = window.location.hostname.includes('github.io');
      let backendBase = document.getElementById('backendUrlInput')?.value?.trim() || 'http://localhost:8080';
      if (backendBase.endsWith('/api')) backendBase = backendBase.slice(0, -4);

      try {
        const ctrl = new AbortController();
        const timeout = setTimeout(() => ctrl.abort(), 1200);
        const res = await fetch(\`\${backendBase}/api/health\`, { signal: ctrl.signal });
        clearTimeout(timeout);
        if (res.ok) {
          dot.className = 'w-2 h-2 rounded-full bg-emerald-500 animate-pulse';
          text.className = 'text-emerald-700 font-bold';
          text.textContent = 'Backend: Port 8080 Active';
          badge.title = 'Connected to Spring Boot Java Backend (REST API Active)';
          return;
        }
      } catch (e) {}

      if (isGitHubPages) {
        dot.className = 'w-2 h-2 rounded-full bg-sky-500';
        text.className = 'text-sky-700 font-semibold';
        text.textContent = 'GitHub Pages Prototype';
        badge.title = 'Running on GitHub Pages static host with client-side demo state';
      } else {
        dot.className = 'w-2 h-2 rounded-full bg-slate-400';
        text.className = 'text-sub font-medium';
        text.textContent = 'Prototype Mode (Fixtures)';
        badge.title = 'Client-side verification engine active (Spring Boot backend offline)';
      }
    }

    async function initPortal() {
      detectRuntimeMode();`;

  if (normalized.includes(initPortalTarget) && !normalized.includes('detectRuntimeMode()')) {
    normalized = normalized.replace(initPortalTarget, detectRuntimeFunc);
    console.log('8. Injected detectRuntimeMode() and hooked into initPortal()');
  } else {
    console.log('8. Target NOT found or already injected');
  }

  // Restore line endings
  const finalContent = isCrlf ? normalized.replace(/\n/g, '\r\n') : normalized;
  fs.writeFileSync(filePath, finalContent, 'utf8');
  console.log(`Saved ${filePath} successfully.`);
}

patchFile('portal.html');
patchFile('frontend/portal.html');
