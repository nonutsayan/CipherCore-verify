import re
import os
import sys
import shutil

def main():
    with open('frontend/index.html', 'r', encoding='utf-8') as f:
        html = f.read()

    # 1. Update rulesPane-audit-log HTML
    old_audit_pane_pattern = re.compile(
        r'<!-- Sub-Tab 2: Audit Log.*?-->\s*<div id="rulesPane-audit-log" class="hidden card-surface p-5 space-y-4">.*?</div>\s*</div>\s*</section>',
        re.DOTALL
    )

    new_audit_pane = '''<!-- Sub-Tab 2: Audit Log & Cryptographic Blockchain Ledger -->
      <div id="rulesPane-audit-log" class="hidden card-surface p-5 space-y-4">
        <!-- Dual View Header -->
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-line pb-4">
          <div>
            <h2 class="text-base font-bold text-main">Session Audit Trail &amp; Cryptographic Ledger</h2>
            <p class="text-xs text-sub mt-0.5">Chronological audit stream cross-linked with client-side SHA-256 tamper-evident blockchain ledger.</p>
          </div>
          <!-- Dual-View Switcher -->
          <div class="flex items-center gap-1.5 p-1 rounded border border-line subtle-surface">
            <button id="auditModeBtn-tabular" onclick="switchAuditViewMode('tabular')" class="btn-secondary py-1 px-3 text-xs" style="border-radius:4px;">
              Tabular Audit Log
            </button>
            <button id="auditModeBtn-blockchain" onclick="switchAuditViewMode('blockchain')" class="btn-primary py-1 px-3 text-xs" style="border-radius:4px;">
              Interactive Blockchain Ledger (SHA-256)
            </button>
          </div>
        </div>

        <!-- VIEW PANE 1: TABULAR AUDIT LOG -->
        <div id="auditViewPane-tabular" class="hidden space-y-4">
          <div class="flex items-center justify-end">
            <button onclick="downloadAuditCsv()" class="btn-primary flex items-center gap-1.5 text-xs">
              <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
              </svg>
              <span>Download CSV</span>
            </button>
          </div>

          <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse text-xs">
              <thead>
                <tr class="subtle-surface border-b border-line text-sub font-mono text-[11px] uppercase">
                  <th class="py-2.5 px-3">Timestamp</th>
                  <th class="py-2.5 px-3">Actor</th>
                  <th class="py-2.5 px-3">Target ID</th>
                  <th class="py-2.5 px-3">Event Type</th>
                  <th class="py-2.5 px-3">Details</th>
                </tr>
              </thead>
              <tbody id="auditLogTableBody" class="divide-y border-line">
                <!-- Populated by JavaScript -->
              </tbody>
            </table>
          </div>
        </div>

        <!-- VIEW PANE 2: INTERACTIVE BLOCKCHAIN LEDGER (SHA-256) -->
        <div id="auditViewPane-blockchain" class="space-y-4">
          <!-- Top Ledger Summary & Controls Bar -->
          <div class="p-4 rounded border border-line subtle-surface space-y-3">
            <div class="flex flex-col lg:flex-row lg:items-center justify-between gap-3">
              <!-- Left: Chain Metrics & Status Badge -->
              <div class="flex flex-wrap items-center gap-2.5">
                <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded bg-[#0284c7]/10 text-[#0284c7] font-mono text-xs font-bold border border-[#0284c7]/30">
                  <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                  </svg>
                  <span id="ledgerBlockCount">0</span> Blocks in Chain
                </span>
                
                <div id="chainIntegrityBadge">
                  <!-- Injected dynamically: green valid or red alert -->
                </div>
              </div>

              <!-- Right: Interactive Action Buttons -->
              <div class="flex flex-wrap items-center gap-2">
                <button onclick="triggerChainIntegrityCheck()" class="btn-primary flex items-center gap-1.5 text-xs">
                  <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <span>Verify Chain Integrity</span>
                </button>
                <button onclick="triggerSimulateTamper()" class="btn-secondary flex items-center gap-1.5 text-xs text-amber-700 border-amber-300 hover:bg-amber-50" title="Modify historical Block #1 payload to test anti-tamper detection">
                  <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                  </svg>
                  <span>Test Anti-Tamper Protection</span>
                </button>
                <button onclick="triggerRestoreLedger()" class="btn-secondary flex items-center gap-1.5 text-xs" title="Restore ledger to pristine cryptographic state">
                  <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                  </svg>
                  <span>Restore Ledger</span>
                </button>
                <button onclick="exportLedgerJson()" class="btn-secondary flex items-center gap-1.5 text-xs" title="Download signed JSON ledger">
                  <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                  </svg>
                  <span>Export JSON</span>
                </button>
              </div>
            </div>

            <!-- Consortium Node Reference Bar -->
            <div class="flex flex-wrap items-center justify-between text-[11px] text-sub pt-2 border-t border-line">
              <span class="font-mono">Consortium PoA Network: 7 Validator Nodes Active (AICTE HQ, IIT Bombay, IIT Madras, IISc Bangalore, AICTE WRO, MoE, NIC)</span>
              <span class="font-mono text-[#0284c7]">Algorithm: SHA-256 Merkle Chaining (window.crypto.subtle)</span>
            </div>

            <!-- Live Anti-Tamper Alert Banner (Hidden by default, shown when tampered) -->
            <div id="tamperAlertBanner" class="hidden p-3.5 rounded border border-red-300 bg-red-50 text-red-900 text-xs">
              <!-- Injected dynamically -->
            </div>
          </div>

          <!-- Visual Block Stream Visualizer -->
          <div id="blockchainLedgerVisualizer" class="space-y-4 pt-1">
            <!-- Rendered dynamically by renderBlockchainLedgerUI() -->
          </div>
        </div>
      </div>

    </section>'''

    if old_audit_pane_pattern.search(html):
        html = old_audit_pane_pattern.sub(new_audit_pane, html)

    # 2. Add Blockchain implementation & helpers in JavaScript if not already present
    if 'class CipherBlockchain' not in html:
        blockchain_js = '''
    // =====================================================================
    // CRYPTOGRAPHIC SHA-256 ENGINE & BLOCKCHAIN NOTARY SERVICE
    // =====================================================================
    function sha256Pure(ascii) {
      function rightRotate(value, amount) { return (value >>> amount) | (value << (32 - amount)); }
      var mathPow = Math.pow, maxWord = mathPow(2, 32), lengthProperty = 'length', i, j, result = '', words = [];
      var asciiBitLength = ascii[lengthProperty] * 8, hash = [], k = [], primeCounter = 0, isComposite = {};
      for (var candidate = 2; primeCounter < 64; candidate++) {
        if (!isComposite[candidate]) {
          for (i = 0; i < 313; i += candidate) isComposite[i] = candidate;
          hash[primeCounter] = (mathPow(candidate, .5) * maxWord) | 0;
          k[primeCounter++] = (mathPow(candidate, 1/3) * maxWord) | 0;
        }
      }
      ascii += '\\x80';
      while (ascii[lengthProperty] % 64 - 56) ascii += '\\x00';
      for (i = 0; i < ascii[lengthProperty]; i++) {
        j = ascii.charCodeAt(i);
        if (j >> 8) return;
        words[i >> 2] |= j << ((3 - i) % 4) * 8;
      }
      words[words[lengthProperty]] = ((asciiBitLength / maxWord) | 0);
      words[words[lengthProperty]] = (asciiBitLength);
      for (j = 0; j < words[lengthProperty];) {
        var w = words.slice(j, j += 16), oldHash = hash;
        hash = hash.slice(0, 8);
        for (i = 0; i < 64; i++) {
          var i2 = i + j, w15 = w[i - 15], w2 = w[i - 2], a = hash[0], e = hash[4];
          var temp1 = hash[7] + (rightRotate(e, 6) ^ rightRotate(e, 11) ^ rightRotate(e, 25)) + ((e & hash[5]) ^ ((~e) & hash[6])) + k[i]
            + (w[i] = (i < 16) ? w[i] : (w[i - 16] + (rightRotate(w15, 7) ^ rightRotate(w15, 18) ^ (w15 >>> 3)) + w[i - 7] + (rightRotate(w2, 17) ^ rightRotate(w2, 19) ^ (w2 >>> 10))) | 0);
          var temp2 = (rightRotate(a, 2) ^ rightRotate(a, 13) ^ rightRotate(a, 22)) + ((a & hash[1]) ^ (a & hash[2]) ^ (hash[1] & hash[2]));
          hash = [(temp1 + temp2) | 0].concat(hash);
          hash[4] = (hash[4] + temp1) | 0;
        }
        for (i = 0; i < 8; i++) hash[i] = (hash[i] + oldHash[i]) | 0;
      }
      for (i = 0; i < 8; i++) {
        for (j = 3; j + 1; j--) {
          var b = (hash[i] >> (j * 8)) & 255;
          result += ((b < 16) ? '0' : '') + b.toString(16);
        }
      }
      return result;
    }

    class CipherBlockchain {
      constructor() {
        this.chain = [];
        this.backupChain = [];
        this.isTampered = false;
        this.tamperDetails = null;
        this.tamperedIndex = null;
      }

      async sha256(text) {
        if (window.crypto && window.crypto.subtle) {
          try {
            const encoder = new TextEncoder();
            const data = encoder.encode(text);
            const hashBuf = await window.crypto.subtle.digest('SHA-256', data);
            return Array.from(new Uint8Array(hashBuf))
              .map(b => b.toString(16).padStart(2, '0'))
              .join('');
          } catch (e) {}
        }
        return sha256Pure(text);
      }

      async calculateBlockHash(index, timestamp, previousHash, payloadData, actorId) {
        const raw = `${index}${timestamp}${previousHash}${JSON.stringify(payloadData)}${actorId}`;
        return await this.sha256(raw);
      }

      async init() {
        const saved = localStorage.getItem('ciphercore_blockchain_ledger_v1');
        if (saved) {
          try {
            const parsed = JSON.parse(saved);
            if (Array.isArray(parsed) && parsed.length > 0) {
              this.chain = parsed;
              this.backupChain = JSON.parse(JSON.stringify(this.chain));
              return;
            }
          } catch (e) {}
        }
        await this.createGenesisBlock();
      }

      async createGenesisBlock() {
        const genesisTime = '2026-10-25T00:00:00.000Z';
        const prevHash = '0000000000000000000000000000000000000000000000000000000000000000';
        const actorId = 'AICTE_ROOT_NOTARY';
        const payload = {
          standard: 'AICTE-APH-2026-27',
          protocol: 'SHA-256 Sovereign PoA Notary',
          consortium: 'BharatVidya Consortium PoA Network',
          rootAuthority: 'All India Council for Technical Education (HQ New Delhi)',
          notaryMessage: 'Genesis Block Initialized for AICTE Verification & Forensic Ledger'
        };
        const hash = await this.calculateBlockHash(0, genesisTime, prevHash, payload, actorId);

        const genesisBlock = {
          index: 0,
          timestamp: genesisTime,
          targetId: 'AICTE-SIH-GENESIS',
          eventType: 'GENESIS',
          actorId: actorId,
          previousHash: prevHash,
          blockHash: hash,
          payloadData: payload
        };

        this.chain = [genesisBlock];
        this.backupChain = JSON.parse(JSON.stringify(this.chain));
        this.persist();
      }

      async addBlock(targetId, eventType, actorId, payloadData) {
        if (this.chain.length === 0) {
          await this.createGenesisBlock();
        }
        const index = this.chain.length;
        const timestamp = new Date().toISOString();
        const previousHash = this.chain[this.chain.length - 1].blockHash;
        const actor = actorId || (activeUserSession?.name ? `${activeUserSession.name} (${activeUserSession.role})` : 'AICTE-OFFICER-782');
        const blockHash = await this.calculateBlockHash(index, timestamp, previousHash, payloadData, actor);

        const newBlock = {
          index,
          timestamp,
          targetId,
          eventType,
          actorId: actor,
          previousHash,
          blockHash,
          payloadData
        };

        this.chain.push(newBlock);
        if (!this.isTampered) {
          this.backupChain = JSON.parse(JSON.stringify(this.chain));
        }
        this.persist();

        // Optional non-blocking sync with Spring Boot Consortium backend if running
        try {
          const backendBase = document.getElementById('backendUrlInput')?.value?.trim() || 'http://localhost:8080';
          fetch(`${backendBase}/api/blockchain/notarize`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              documentTitle: targetId,
              documentHash: blockHash,
              institutionCode: targetId,
              institutionName: payloadData?.institution || targetId,
              docCategory: eventType,
              notaryOfficer: actor
            })
          }).catch(() => {});
        } catch (e) {}

        return newBlock;
      }

      async verifyChainIntegrity() {
        if (!this.chain || this.chain.length === 0) {
          return { valid: true, totalBlocks: 0 };
        }

        const b0 = this.chain[0];
        if (b0.index !== 0 || b0.previousHash !== '0000000000000000000000000000000000000000000000000000000000000000') {
          return {
            valid: false,
            brokenIndex: 0,
            reason: 'INVALID_GENESIS',
            expectedHash: 'Genesis block linkage corrupted',
            actualHash: b0.blockHash
          };
        }
        const b0Hash = await this.calculateBlockHash(b0.index, b0.timestamp, b0.previousHash, b0.payloadData, b0.actorId);
        if (b0Hash !== b0.blockHash) {
          return {
            valid: false,
            brokenIndex: 0,
            reason: 'HASH_MISMATCH',
            expectedHash: b0Hash,
            actualHash: b0.blockHash
          };
        }

        for (let i = 1; i < this.chain.length; i++) {
          const prevBlock = this.chain[i - 1];
          const curBlock = this.chain[i];

          if (curBlock.previousHash !== prevBlock.blockHash) {
            return {
              valid: false,
              brokenIndex: i,
              reason: 'BROKEN_LINKAGE',
              expectedHash: prevBlock.blockHash,
              actualHash: curBlock.previousHash
            };
          }

          const calculatedHash = await this.calculateBlockHash(
            curBlock.index,
            curBlock.timestamp,
            curBlock.previousHash,
            curBlock.payloadData,
            curBlock.actorId
          );

          if (calculatedHash !== curBlock.blockHash) {
            return {
              valid: false,
              brokenIndex: i,
              reason: 'HASH_MISMATCH',
              expectedHash: calculatedHash,
              actualHash: curBlock.blockHash
            };
          }
        }

        return { valid: true, totalBlocks: this.chain.length };
      }

      simulateTamper(blockIndex = 1) {
        if (this.chain.length <= 1) return false;
        const idx = Math.min(blockIndex, this.chain.length - 1);
        const targetBlock = this.chain[idx];

        // Mutate payload without re-hashing
        targetBlock.payloadData = {
          ...targetBlock.payloadData,
          status: 'FRAUDULENT_OVERRIDE_APPROVED',
          tamperExploit: 'MALICIOUS_OFFLINE_PAYLOAD_MODIFICATION',
          unauthorizedBypass: 'Adverse Regulatory Sanction Illegally Suppressed',
          auditViolation: 'CRITICAL_INTEGRITY_BREACH'
        };

        this.isTampered = true;
        this.tamperedIndex = idx;
        return true;
      }

      restoreLedger() {
        this.chain = JSON.parse(JSON.stringify(this.backupChain));
        this.isTampered = false;
        this.tamperDetails = null;
        this.tamperedIndex = null;
        this.persist();
      }

      persist() {
        try {
          localStorage.setItem('ciphercore_blockchain_ledger_v1', JSON.stringify(this.chain));
        } catch (e) {}
      }
    }

    const cipherBlockchain = new CipherBlockchain();
    window.cipherBlockchain = cipherBlockchain;
    let currentAuditViewMode = 'blockchain';

    async function seedDemoBlockchainBlocks() {
      if (cipherBlockchain.chain.length <= 1) {
        await cipherBlockchain.addBlock('APP-2026-101', 'ANOMALY_FLAGGED', 'AICTE_AUTOMATED_ENGINE', {
          institution: 'Apex Institute of Technology',
          status: 'Needs Review',
          issues: ['Fire NOC expired 2024-12-31', 'Land Area mismatch (2.50 vs 1.80 Acres)'],
          documentsUploaded: 6,
          forensicConfidence: '98.4%'
        });
        await cipherBlockchain.addBlock('APP-2026-102', 'DOCUMENT_VERIFIED', 'Dr. Arvind Kumar', {
          institution: 'Bharti Vidyapeeth College of Engineering',
          status: 'Verified',
          checksPassed: '6/6 Statutory Documents Valid',
          fireNocRef: 'NOC-DEL-2025-0891',
          issuingGateway: 'FIRE_DELHI_GOV_VERIFIED'
        });
        await cipherBlockchain.addBlock('APP-2026-103', 'ANOMALY_FLAGGED', 'AICTE_AUTOMATED_ENGINE', {
          institution: 'Global Institute of Management & Science',
          status: 'Needs Review',
          issues: ['Built-up area shortfall: 3,120 sq.m vs 4,200 sq.m APH requirement'],
          builtUpNorm: 'FAIL'
        });
        await cipherBlockchain.addBlock('APP-2026-104', 'ANOMALY_FLAGGED', 'AICTE_AUTOMATED_ENGINE', {
          institution: 'Delhi Technical Campus',
          status: 'Needs Review',
          issues: ['Missing University Affiliation Letter (5/6 docs uploaded)'],
          completeness: 'INCOMPLETE'
        });
        await cipherBlockchain.addBlock('APP-2026-105', 'DOCUMENT_VERIFIED', 'AICTE_AUTOMATED_ENGINE', {
          institution: 'Kalyani Government Engineering College',
          status: 'Verified',
          checksPassed: 'All 6 documents valid and verified with issuing authorities',
          autonomousStatus: 'UGC_AUTONOMOUS'
        });
      }
    }

    function switchAuditViewMode(mode) {
      currentAuditViewMode = mode;
      const tabularPane = document.getElementById('auditViewPane-tabular');
      const blockchainPane = document.getElementById('auditViewPane-blockchain');
      const tabularBtn = document.getElementById('auditModeBtn-tabular');
      const blockchainBtn = document.getElementById('auditModeBtn-blockchain');

      if (mode === 'tabular') {
        if (tabularPane) tabularPane.classList.remove('hidden');
        if (blockchainPane) blockchainPane.classList.add('hidden');
        if (tabularBtn) tabularBtn.className = 'btn-primary py-1 px-3 text-xs';
        if (blockchainBtn) blockchainBtn.className = 'btn-secondary py-1 px-3 text-xs';
        renderAuditLogTable();
      } else {
        if (tabularPane) tabularPane.classList.add('hidden');
        if (blockchainPane) blockchainPane.classList.remove('hidden');
        if (tabularBtn) tabularBtn.className = 'btn-secondary py-1 px-3 text-xs';
        if (blockchainBtn) blockchainBtn.className = 'btn-primary py-1 px-3 text-xs';
        renderBlockchainLedgerUI();
      }
    }

    function copyLedgerHash(text) {
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).catch(() => {});
      } else {
        const input = document.createElement('textarea');
        input.value = text;
        document.body.appendChild(input);
        input.select();
        document.execCommand('copy');
        document.body.removeChild(input);
      }
      showToastBanner(`Copied hash to clipboard: ${text.substring(0, 16)}...`);
    }

    function renderPayloadSummary(payload) {
      if (!payload || typeof payload !== 'object') {
        return `<div class="text-sub font-mono">${escapeHtml(String(payload))}</div>`;
      }
      const entries = Object.entries(payload);
      return `
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-x-4 gap-y-1 text-xs">
          ${entries.map(([k, v]) => `
            <div class="flex items-start gap-2">
              <span class="text-sub font-mono text-[11px] shrink-0">${escapeHtml(k)}:</span>
              <span class="font-medium text-main text-[11px] break-words">${escapeHtml(typeof v === 'object' ? JSON.stringify(v) : String(v))}</span>
            </div>
          `).join('')}
        </div>
      `;
    }

    function renderBlockchainLedgerUI() {
      const countEl = document.getElementById('ledgerBlockCount');
      if (countEl) {
        countEl.textContent = cipherBlockchain.chain.length;
      }

      const badgeEl = document.getElementById('chainIntegrityBadge');
      const alertEl = document.getElementById('tamperAlertBanner');

      if (cipherBlockchain.isTampered && cipherBlockchain.tamperDetails) {
        const td = cipherBlockchain.tamperDetails;
        if (badgeEl) {
          badgeEl.innerHTML = `
            <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded bg-red-100 text-red-900 text-xs font-bold border-2 border-red-500 animate-pulse">
              <svg class="w-3.5 h-3.5 text-red-600 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
              <span>CRITICAL ALERT: Chain Tamper Detected!</span>
            </span>
          `;
        }

        if (alertEl) {
          alertEl.classList.remove('hidden');
          alertEl.innerHTML = `
            <div class="flex items-start gap-3">
              <svg class="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
              <div class="space-y-1.5 flex-1">
                <div class="font-bold text-red-900 text-xs uppercase tracking-wide">
                  CRITICAL SECURITY ALERT: Block #${td.brokenIndex} Hash Mismatch!
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-2 font-mono text-[11px]">
                  <div class="p-2 bg-red-100 rounded border border-red-200">
                    <span class="text-red-700 font-semibold block text-[10px] uppercase">Recorded On-Chain Hash:</span>
                    <span class="text-red-900 break-all">${td.actualHash || 'N/A'}</span>
                  </div>
                  <div class="p-2 bg-red-100 rounded border border-red-200">
                    <span class="text-red-700 font-semibold block text-[10px] uppercase">Calculated Hash (With Tampered Payload):</span>
                    <span class="text-red-900 break-all font-bold">${td.expectedHash}</span>
                  </div>
                </div>
                <p class="text-[11px] text-red-800 font-medium pt-0.5">
                  Cryptographic Merkle link broken. Malicious payload alteration detected on Block #${td.brokenIndex}. Click "Restore Ledger" to recover the verifiable consortium state.
                </p>
              </div>
            </div>
          `;
        }
      } else {
        if (badgeEl) {
          badgeEl.innerHTML = `
            <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded bg-emerald-50 text-emerald-800 text-xs font-semibold border border-emerald-300">
              <svg class="w-3.5 h-3.5 text-emerald-600 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>Chain Integrity: VALID (Tamper-Free)</span>
            </span>
          `;
        }
        if (alertEl) {
          alertEl.classList.add('hidden');
        }
      }

      const container = document.getElementById('blockchainLedgerVisualizer');
      if (!container) return;

      const eventBadgeStyles = {
        GENESIS: 'bg-slate-100 text-slate-800 border-slate-300',
        DOCUMENT_VERIFIED: 'bg-sky-50 text-sky-800 border-sky-300',
        DECISION_RECORDED: 'bg-emerald-50 text-emerald-800 border-emerald-300',
        ANOMALY_FLAGGED: 'bg-amber-50 text-amber-800 border-amber-300',
        RULE_CONFIG_CHANGED: 'bg-indigo-50 text-indigo-800 border-indigo-300'
      };

      container.innerHTML = cipherBlockchain.chain.map((b, idx) => {
        const isTamperedBlock = cipherBlockchain.isTampered && b.index === cipherBlockchain.tamperedIndex;
        const badgeStyle = eventBadgeStyles[b.eventType] || 'bg-slate-50 text-slate-700 border-slate-200';

        const connectorHtml = idx > 0 ? `
          <div class="flex items-center justify-center my-3">
            <div class="flex items-center gap-2 px-3 py-1 bg-white border ${isTamperedBlock ? 'border-red-400 text-red-700' : 'border-line text-sub'} rounded text-[11px] font-mono shadow-xs">
              <svg class="w-3.5 h-3.5 ${isTamperedBlock ? 'text-red-600' : 'text-[#0284c7]'}" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
              </svg>
              <span>SHA-256 LINK: Block #${b.index - 1} &rarr; Block #${b.index} ${isTamperedBlock ? '(BROKEN / TAMPERED)' : '(VALID)'}</span>
            </div>
          </div>
        ` : '';

        return `
          ${connectorHtml}
          <div class="rounded border p-4 space-y-3 transition-colors ${isTamperedBlock ? 'border-2 border-red-500 bg-red-50/50' : 'card-surface border-line'}">
            <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-line pb-3">
              <div class="flex flex-wrap items-center gap-2">
                <span class="px-2 py-0.5 rounded font-mono text-xs font-bold ${b.index === 0 ? 'bg-slate-800 text-white' : 'bg-subtle border border-line text-main'}">
                  BLOCK #${b.index}${b.index === 0 ? ' - GENESIS' : ''}
                </span>
                <span class="px-2 py-0.5 rounded text-[11px] font-semibold border ${badgeStyle}">
                  ${b.eventType}
                </span>
                <span class="font-mono text-xs font-bold text-[#0284c7]">
                  ${escapeHtml(b.targetId)}
                </span>
              </div>

              <div class="flex flex-wrap items-center gap-2 text-xs">
                <span class="font-mono text-sub text-[11px]">
                  ${b.timestamp}
                </span>
                <span class="px-2 py-0.5 rounded subtle-surface border border-line text-[11px] font-semibold text-main">
                  ${escapeHtml(b.actorId)}
                </span>
              </div>
            </div>

            ${isTamperedBlock ? `
              <div class="p-2.5 bg-red-100 border border-red-300 rounded text-xs text-red-900 font-bold flex items-center gap-2">
                <svg class="w-4 h-4 text-red-600 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                <span>TAMPERED BLOCK DETECTED: Payload altered without cryptographic notary signoff!</span>
              </div>
            ` : ''}

            <div class="grid grid-cols-1 md:grid-cols-2 gap-2 text-xs">
              <div class="p-2.5 rounded subtle-surface border border-line space-y-1">
                <div class="text-[10px] font-mono uppercase text-sub font-semibold">Previous Hash (prevHash):</div>
                <div class="flex items-center justify-between gap-2">
                  <span class="font-mono text-[11px] text-sub break-all">${b.previousHash}</span>
                  <button onclick="copyLedgerHash('${b.previousHash}')" class="btn-secondary py-0.5 px-2 text-[10px] shrink-0" title="Copy hash">
                    Copy
                  </button>
                </div>
              </div>

              <div class="p-2.5 rounded subtle-surface border border-line space-y-1 ${isTamperedBlock ? 'border-red-400 bg-red-50' : ''}">
                <div class="text-[10px] font-mono uppercase font-semibold ${isTamperedBlock ? 'text-red-700' : 'text-sub'}">
                  Block Hash (SHA-256):
                </div>
                <div class="flex items-center justify-between gap-2">
                  <span class="font-mono text-[11px] font-bold ${isTamperedBlock ? 'text-red-700 line-through' : 'text-[#0284c7]'} break-all">
                    ${b.blockHash}
                  </span>
                  <button onclick="copyLedgerHash('${b.blockHash}')" class="btn-secondary py-0.5 px-2 text-[10px] shrink-0" title="Copy hash">
                    Copy
                  </button>
                </div>
              </div>
            </div>

            <div class="p-2.5 rounded subtle-surface border border-line space-y-1.5 text-xs">
              <div class="text-[10px] font-mono uppercase text-sub font-semibold">Notarized Payload Data:</div>
              ${renderPayloadSummary(b.payloadData)}
            </div>

            <details class="text-xs pt-1">
              <summary class="cursor-pointer text-[#0284c7] font-semibold text-[11px] hover:underline flex items-center gap-1 select-none">
                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
                </svg>
                <span>View Full Cryptographic Block JSON</span>
              </summary>
              <pre class="mt-2 p-3 bg-slate-900 text-slate-100 font-mono text-[11px] rounded overflow-x-auto leading-relaxed border border-slate-800">${escapeHtml(JSON.stringify(b, null, 2))}</pre>
            </details>
          </div>
        `;
      }).join('');
    }

    async function triggerChainIntegrityCheck() {
      const result = await cipherBlockchain.verifyChainIntegrity();
      if (result.valid) {
        cipherBlockchain.isTampered = false;
        cipherBlockchain.tamperDetails = null;
        showToastBanner(`Cryptographic validation passed: All ${cipherBlockchain.chain.length} blocks verified with SHA-256 integrity.`);
      } else {
        cipherBlockchain.isTampered = true;
        cipherBlockchain.tamperDetails = result;
        showToastBanner(`ALERT: Block #${result.brokenIndex} cryptographic signature mismatch! Hash chain broken.`);
      }
      renderBlockchainLedgerUI();
    }

    async function triggerSimulateTamper() {
      const success = cipherBlockchain.simulateTamper(1);
      if (!success) {
        showToastBanner('Please load demo data first to populate blocks.');
        return;
      }
      const result = await cipherBlockchain.verifyChainIntegrity();
      cipherBlockchain.isTampered = true;
      cipherBlockchain.tamperDetails = result;
      renderBlockchainLedgerUI();
      showToastBanner(`Tamper Simulation Active: Block #1 mutated without valid hash signature.`);
    }

    async function triggerRestoreLedger() {
      cipherBlockchain.restoreLedger();
      const result = await cipherBlockchain.verifyChainIntegrity();
      cipherBlockchain.isTampered = false;
      cipherBlockchain.tamperDetails = null;
      renderBlockchainLedgerUI();
      showToastBanner('Blockchain ledger restored to original cryptographic state.');
    }

    function exportLedgerJson() {
      const data = JSON.stringify(cipherBlockchain.chain, null, 2);
      const blob = new Blob([data], { type: 'application/json;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'ciphercore_blockchain_ledger.json';
      a.click();
      URL.revokeObjectURL(url);
      showToastBanner('Downloaded ciphercore_blockchain_ledger.json');
    }
'''
        html = html.replace('function switchRulesSubTab(subTabName)', blockchain_js + '\n    function switchRulesSubTab(subTabName)')

    with open('frontend/index.html', 'w', encoding='utf-8') as f:
        f.write(html)

    # 3. Mirror frontend/index.html to root index.html and backend static index.html
    shutil.copyfile('frontend/index.html', 'index.html')
    backend_static = 'backend/src/main/resources/static/index.html'
    if os.path.exists(os.path.dirname(backend_static)):
        shutil.copyfile('frontend/index.html', backend_static)

    print("Successfully built & synced blockchain portal across all targets.")
    return True

if __name__ == '__main__':
    main()
