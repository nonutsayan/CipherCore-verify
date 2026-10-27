const fs = require('fs');

function patchFile(filePath) {
  console.log(`Patching ledger & runtime in ${filePath}...`);
  let content = fs.readFileSync(filePath, 'utf8');
  const isCrlf = content.includes('\r\n');
  let normalized = content.replace(/\r\n/g, '\n');

  // 1. Update Genesis Block payload
  const oldGenesisPayload = `        const payload = {
          regulatoryBody: 'All India Council for Technical Education',
          statutoryHandbook: 'Approval Process Handbook (APH 2024-27)',
          problemStatement: 'SIH26261',
          consensus: 'Consortium Proof-of-Authority (PoA)',
          nodes: ['AICTE-HQ', 'IIT-Bombay', 'IIT-Madras', 'IISc-Bangalore', 'AICTE-WRO', 'MoE-Gov', 'NIC-DataHub']
        };`;

  const newGenesisPayload = `        const payload = {
          regulatoryBody: 'All India Council for Technical Education',
          statutoryHandbook: 'Approval Process Handbook (APH 2024-27)',
          problemStatement: 'SIH26261',
          architecture: 'Cryptographic Audit Ledger & Hash-Chain Prototype',
          hashAlgorithm: 'Sequential SHA-256 Hash Chaining',
          notaryAgents: ['AICTE-HQ Central Root Notary', 'AICTE-WRO Directorate', 'Institutional Scrutiny Cell'],
          trustLimitations: 'Educational demonstration prototype running sequential SHA-256 hash chaining on single host. Production deployment requires distributed Byzantine fault-tolerant multi-node consortium.'
        };`;

  if (normalized.includes(oldGenesisPayload)) {
    normalized = normalized.replace(oldGenesisPayload, newGenesisPayload);
    console.log('1. Updated Genesis Block Payload');
  }

  // 2. Update exportLedgerJson
  const oldExport = `      const exportObject = {
        title: 'AICTE CipherCore Sovereign Consortium Blockchain Ledger',
        standard: 'AICTE-APH-2024-27',
        problemStatementId: 'SIH26261',
        algorithm: 'SHA-256 Merkle Linkage Protocol',
        consortiumPoANetwork: [
          { node: 'AICTE-HQ (New Delhi)', status: 'ACTIVE', latency: '0.12 ms' },
          { node: 'IIT-Bombay (Powai)', status: 'ACTIVE', latency: '0.45 ms' },
          { node: 'IIT-Madras (Chennai)', status: 'ACTIVE', latency: '0.62 ms' },
          { node: 'IISc-Bangalore (Bangalore)', status: 'ACTIVE', latency: '0.38 ms' },
          { node: 'AICTE-WRO (Mumbai)', status: 'ACTIVE', latency: '0.22 ms' },
          { node: 'MoE-Gov (Shastri Bhawan)', status: 'ACTIVE', latency: '0.51 ms' },
          { node: 'NIC-DataHub (NIC-Gov)', status: 'ACTIVE', latency: '0.30 ms' }
        ],
        exportedAt: new Date().toISOString(),
        totalBlocksMined: cipherBlockchain.chain.length,
        chainIntegrityStatus: cipherBlockchain.isTampered ? 'TAMPERED_ALERT' : 'VALID_SECURE',
        blocks: cipherBlockchain.chain
      };`;

  const newExport = `      const exportObject = {
        title: 'CipherCore Cryptographic Audit Ledger & Tamper-Evident Hash-Chain Prototype',
        standard: 'AICTE-APH-2024-27',
        problemStatementId: 'SIH26261',
        architecture: 'Sequential SHA-256 Cryptographic Hash Chaining',
        trustLimitations: 'Single-server prototype demonstration. Demonstrates sequential block hash immutability and live tamper detection.',
        notaryAgents: [
          { agent: 'AICTE-HQ Central Root Notary', status: 'ACTIVE', role: 'Root Anchor' },
          { agent: 'AICTE-WRO Regional Directorate', status: 'ACTIVE', role: 'Regional Notary' },
          { agent: 'Institutional Scrutiny Cell', status: 'ACTIVE', role: 'Inspection Signatory' }
        ],
        exportedAt: new Date().toISOString(),
        totalBlocksInLedger: cipherBlockchain.chain.length,
        chainIntegrityStatus: cipherBlockchain.isTampered ? 'TAMPERED_ALERT' : 'VALID_SECURE',
        blocks: cipherBlockchain.chain
      };`;

  if (normalized.includes(oldExport)) {
    normalized = normalized.replace(oldExport, newExport);
    console.log('2. Updated exportLedgerJson');
  }

  // 3. Update DOMContentLoaded with detectRuntimeMode
  const oldDcl = `    window.addEventListener('DOMContentLoaded', async () => {
      await cipherBlockchain.init();
      const isDemoInit = localStorage.getItem(INITIALIZED_KEY) === 'true';
      if (isDemoInit && cipherBlockchain.chain.length <= 1) {
        await seedDemoBlockchainBlocks();
      }
      initUserSession();
      renderBlockchainLedgerUI();
    });`;

  const newDcl = `    async function detectRuntimeMode() {
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
        text.textContent = 'GitHub Pages Demo';
        badge.title = 'Running on GitHub Pages static host with client-side fixture state';
      } else {
        dot.className = 'w-2 h-2 rounded-full bg-slate-400';
        text.className = 'text-sub font-medium';
        text.textContent = 'Prototype (Fixtures)';
        badge.title = 'Client-side verification engine active (Spring Boot backend offline)';
      }
    }

    window.addEventListener('DOMContentLoaded', async () => {
      detectRuntimeMode();
      await cipherBlockchain.init();
      const isDemoInit = localStorage.getItem(INITIALIZED_KEY) === 'true';
      if (isDemoInit && cipherBlockchain.chain.length <= 1) {
        await seedDemoBlockchainBlocks();
      }
      initUserSession();
      renderBlockchainLedgerUI();
    });`;

  if (normalized.includes(oldDcl)) {
    normalized = normalized.replace(oldDcl, newDcl);
    console.log('3. Injected detectRuntimeMode into DOMContentLoaded');
  }

  // 4. Update triggerChainIntegrityCheck toast
  normalized = normalized.replace(
    'showToastBanner(`Chain Integrity Verified: All ${cipherBlockchain.chain.length} blocks cryptographically valid across 7 Consortium PoA Nodes.`);',
    'showToastBanner(`Audit Ledger Integrity Verified: All ${cipherBlockchain.chain.length} blocks sequentially linked with valid SHA-256 cryptographic hashes.`);'
  );

  normalized = normalized.replace(
    "showToastBanner('Consortium PoA Ledger Restored: All blocks cryptographically verified and valid.');",
    "showToastBanner('Cryptographic Audit Ledger Restored: All blocks cryptographically verified and valid.');"
  );

  const finalContent = isCrlf ? normalized.replace(/\n/g, '\r\n') : normalized;
  fs.writeFileSync(filePath, finalContent, 'utf8');
  console.log(`Saved ${filePath}`);
}

patchFile('portal.html');
patchFile('frontend/portal.html');
