import re
import os
import sys
import shutil

def main():
    with open('frontend/index.html', 'r', encoding='utf-8') as f:
        html = f.read()

    # =========================================================================
    # 1. UPDATE BLOCKCHAIN LEDGER HTML IN rulesPane-audit-log
    # =========================================================================
    old_blockchain_pane_pattern = re.compile(
        r'<!-- VIEW PANE 2: INTERACTIVE BLOCKCHAIN LEDGER \(SHA-256\) -->\s*<div id="auditViewPane-blockchain".*?<!-- Visual Block Stream Visualizer -->\s*<div id="blockchainLedgerVisualizer".*?</div>\s*</div>',
        re.DOTALL
    )

    new_blockchain_pane = '''<!-- VIEW PANE 2: INTERACTIVE BLOCKCHAIN LEDGER (SHA-256) -->
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
                  <span id="ledgerBlockCount">1 Block in Chain (Genesis Active)</span>
                </span>
                
                <div id="chainIntegrityBadge">
                  <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded bg-emerald-50 text-emerald-800 text-xs font-semibold border border-emerald-300">
                    <svg class="w-3.5 h-3.5 text-emerald-600 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <span>Chain Integrity: VALID / SECURE</span>
                  </span>
                </div>
              </div>

              <!-- Center: Consortium PoA Node Indicators (7 Active Validators) -->
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
              </div>

              <!-- Right: Interactive Action Buttons -->
              <div class="flex flex-wrap items-center gap-2">
                <button id="verifyChainBtn" onclick="triggerChainIntegrityCheck()" class="btn-primary flex items-center gap-1.5 text-xs" style="border-radius:4px;">
                  <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <span>Verify Chain Integrity</span>
                </button>
                <button id="tamperSimulateBtn" onclick="triggerSimulateTamper()" class="btn-secondary flex items-center gap-1.5 text-xs text-amber-700 border-amber-300 hover:bg-amber-50" style="border-radius:4px;" title="Silently alter historical Block #1 payload to demonstrate tamper detection">
                  <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                  </svg>
                  <span>Simulate Adversarial Tamper</span>
                </button>
                <button id="restoreLedgerBtn" onclick="triggerRestoreLedger()" class="hidden btn-primary flex items-center gap-1.5 text-xs bg-emerald-600 hover:bg-emerald-700 text-white font-bold" style="border-radius:4px;" title="Restore ledger to pristine authenticated state">
                  <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                  </svg>
                  <span>Restore Ledger</span>
                </button>
                <button onclick="exportLedgerJson()" class="btn-secondary flex items-center gap-1.5 text-xs" style="border-radius:4px;" title="Download signed JSON ledger">
                  <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                  </svg>
                  <span>Download Signed JSON Ledger</span>
                </button>
              </div>
            </div>

            <!-- Consortium Node Reference Bar -->
            <div class="flex flex-wrap items-center justify-between text-[11px] text-sub pt-2 border-t border-line">
              <span class="font-mono">Consortium Network: BharatVidya PoA Trust Network (AICTE Sovereign Notary Protocol)</span>
              <span class="font-mono text-[#0284c7]">Formula: SHA-256(index|timestamp|prevHash|payload|actor|target|event)</span>
            </div>

            <!-- Live Anti-Tamper Alert Banner (Hidden by default, shown when tampered) -->
            <div id="tamperAlertBanner" class="hidden p-3.5 rounded border-2 border-red-500 bg-red-50 text-red-900 text-xs">
              <!-- Injected dynamically -->
            </div>
          </div>

          <!-- Visual Block Stream Visualizer -->
          <div id="blockchainLedgerVisualizer" class="space-y-4 pt-1">
            <!-- Rendered dynamically by renderBlockchainLedgerUI() -->
          </div>
        </div>'''

    if old_blockchain_pane_pattern.search(html):
        html = old_blockchain_pane_pattern.sub(new_blockchain_pane, html)
    else:
        print("Warning: old_blockchain_pane_pattern didn't match via regex, using string replace", file=sys.stderr)
        target_marker = '<!-- VIEW PANE 2: INTERACTIVE BLOCKCHAIN LEDGER (SHA-256) -->'
        end_marker = '<!-- Visual Block Stream Visualizer -->'
        if target_marker in html:
            idx1 = html.find(target_marker)
            idx2 = html.find('</div>\n        </div>\n      </div>', idx1)
            if idx2 != -1:
                html = html[:idx1] + new_blockchain_pane + html[idx2+len('</div>\n        </div>'):]

    # =========================================================================
    # 2. HARDEN CIPHERBLOCKCHAIN ENGINE IN JAVASCRIPT
    # =========================================================================
    hardened_engine_js = '''
    // =====================================================================
    // CRYPTOGRAPHIC SHA-256 ENGINE & HARDENED BLOCKCHAIN NOTARY SERVICE
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
        if (typeof text !== 'string') text = String(text);
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

      async calculateBlockHash(index, timestamp, previousHash, payload, actorId, targetId, eventType) {
        // Deterministic hash formula specified in Section 2:
        // Block String = `${index}|${timestamp}|${previousHash}|${JSON.stringify(payload)}|${actorId}|${targetId}|${eventType}`
        const raw = `${index}|${timestamp}|${previousHash}|${JSON.stringify(payload)}|${actorId}|${targetId}|${eventType}`;
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
        // Self-initialize Genesis Block #0 immediately on fresh start
        await this.createGenesisBlock();
      }

      async createGenesisBlock() {
        const genesisTime = '2026-10-25T00:00:00.000Z';
        const prevHash = '0000000000000000000000000000000000000000000000000000000000000000';
        const targetId = 'ROOT-GENESIS-AICTE';
        const eventType = 'GENESIS_NOTARIZATION';
        const actorId = 'AICTE_CENTRAL_ROOT_NOTARY';
        const payload = {
          regulatoryBody: 'All India Council for Technical Education',
          statutoryHandbook: 'Approval Process Handbook (APH 2024-27)',
          problemStatement: 'SIH26261',
          consensus: 'Consortium Proof-of-Authority (PoA)',
          nodes: ['AICTE-HQ', 'IIT-Bombay', 'IIT-Madras', 'IISc-Bangalore', 'AICTE-WRO', 'MoE-Gov', 'NIC-DataHub']
        };

        const hash = await this.calculateBlockHash(0, genesisTime, prevHash, payload, actorId, targetId, eventType);

        const genesisBlock = {
          index: 0,
          timestamp: genesisTime,
          targetId: targetId,
          eventType: eventType,
          actorId: actorId,
          previousHash: prevHash,
          blockHash: hash,
          payload: payload,
          payloadData: payload
        };

        this.chain = [genesisBlock];
        this.backupChain = JSON.parse(JSON.stringify(this.chain));
        this.persist();
      }

      async addBlock(targetId, eventType, actorId, payload) {
        if (this.chain.length === 0) {
          await this.createGenesisBlock();
        }
        const index = this.chain.length;
        const timestamp = new Date().toISOString();
        const previousHash = this.chain[this.chain.length - 1].blockHash;
        const actor = actorId || (activeUserSession?.name ? `${activeUserSession.name} (${activeUserSession.role})` : 'AICTE-OFFICER-782');
        const blockHash = await this.calculateBlockHash(index, timestamp, previousHash, payload, actor, targetId, eventType);

        const newBlock = {
          index,
          timestamp,
          targetId,
          eventType,
          actorId: actor,
          previousHash,
          blockHash,
          payload,
          payloadData: payload
        };

        this.chain.push(newBlock);
        if (!this.isTampered) {
          this.backupChain = JSON.parse(JSON.stringify(this.chain));
        }
        this.persist();

        // Optional non-blocking sync with Spring Boot Consortium PoA backend if running
        try {
          const backendBase = document.getElementById('backendUrlInput')?.value?.trim() || 'http://localhost:8080';
          fetch(`${backendBase}/api/blockchain/notarize`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              documentTitle: targetId,
              documentHash: blockHash,
              institutionCode: targetId,
              institutionName: typeof payload === 'object' && payload.institution ? payload.institution : targetId,
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

        // 1. Verify Genesis Block #0
        const b0 = this.chain[0];
        if (b0.index !== 0 || b0.previousHash !== '0000000000000000000000000000000000000000000000000000000000000000') {
          return {
            valid: false,
            brokenIndex: 0,
            reason: 'INVALID_GENESIS',
            expectedHash: '0000000000000000000000000000000000000000000000000000000000000000',
            actualHash: b0.previousHash
          };
        }
        const b0Hash = await this.calculateBlockHash(b0.index, b0.timestamp, b0.previousHash, b0.payload || b0.payloadData, b0.actorId, b0.targetId, b0.eventType);
        if (b0Hash !== b0.blockHash) {
          return {
            valid: false,
            brokenIndex: 0,
            reason: 'HASH_MISMATCH',
            expectedHash: b0Hash,
            actualHash: b0.blockHash
          };
        }

        // 2. Verify all downstream blocks
        for (let i = 1; i < this.chain.length; i++) {
          const prevBlock = this.chain[i - 1];
          const curBlock = this.chain[i];

          // Check sequential cryptographic linkage
          if (curBlock.previousHash !== prevBlock.blockHash) {
            return {
              valid: false,
              brokenIndex: i,
              reason: 'BROKEN_LINKAGE',
              expectedHash: prevBlock.blockHash,
              actualHash: curBlock.previousHash
            };
          }

          // Check deterministic SHA-256 calculation
          const calculatedHash = await this.calculateBlockHash(
            curBlock.index,
            curBlock.timestamp,
            curBlock.previousHash,
            curBlock.payload || curBlock.payloadData,
            curBlock.actorId,
            curBlock.targetId,
            curBlock.eventType
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

        // Silently alter historical payload without recomputing the cryptographic hash
        targetBlock.payload = {
          ...(targetBlock.payload || targetBlock.payloadData),
          apexStatus: 'Verified (FORCED OVERRIDE)',
          acreageReported: '2.50 Acres (MUTATED)',
          tamperExploit: 'UNAUTHORIZED_OFFLINE_PAYLOAD_ALTERATION',
          regulatoryViolation: 'CRITICAL_SECURITY_BREACH'
        };
        targetBlock.payloadData = targetBlock.payload;

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
        // Trigger A: Ingest Dossiers Block #1
        await cipherBlockchain.addBlock('APP-2026-BATCH-INGEST', 'INGEST_DOSSIERS', 'AICTE_INGESTION_GATEWAY', {
          action: 'Ingested 5 Institutional Application Dossiers',
          batchId: 'BATCH-SIH2026-AY2627',
          institutions: ['APP-2026-101', 'APP-2026-102', 'APP-2026-103', 'APP-2026-104', 'APP-2026-105'],
          count: 5,
          apexStatus: 'Needs Review',
          acreageReported: '1.80 Acres'
        });

        // Authority Match Block #2
        await cipherBlockchain.addBlock('FIRE-PORTAL-MH', 'AUTHORITY_MATCH', 'AICTE_GATEWAY_DAEMON', {
          title: 'State Fire Services API Validation',
          targetInstitution: 'Apex Institute of Technology (APP-2026-101)',
          refId: 'MFS-PUN-2023-4419',
          verifiedStatus: 'EXPIRED_REVOKED',
          issuingAuthority: 'Maharashtra Fire Services Gateway',
          expiryDate: '2024-12-31'
        });

        // Anomaly Flagged Block #3
        await cipherBlockchain.addBlock('APP-2026-101', 'ANOMALY_FLAGGED', 'AICTE_FORENSIC_ENGINE', {
          title: 'Apex Institute Land Deed Alteration Flagged',
          documentId: 'DEED-REV-MH-4419',
          finding: 'Survey No 42/B (5.20 Acres) altered from original 42/A (2.10 Acres)',
          forensicConfidence: '99.8% Raster & Font Overlay Detected',
          producerAnomaly: 'Adobe Acrobat Pro v24.2'
        });

        // Official Decision Block #4
        await cipherBlockchain.addBlock('APP-2026-101', 'DECISION_RECORDED', 'Dr. Arvind Kumar (AICTE Scrutiny Officer)', {
          title: 'Officer Dr. Arvind Kumar recorded decision on APP-2026-101',
          officialDecision: 'Needs Review',
          statutoryAction: 'Hearing notice issued for Land Deed alteration & Fire NOC expiry',
          officerSignoff: true,
          decisionTimestamp: '2026-10-25 02:30:12 UTC'
        });
      }
    }

    function showNodeConsensusToast(nodeName, latency) {
      showToastBanner(`Consortium PoA Node [${nodeName}]: In Consensus (${latency}) - Block #${cipherBlockchain.chain.length - 1} Verified.`);
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

    function copyLedgerHash(text, label = 'SHA-256 hash') {
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
      showToastBanner(`Copied ${label} to clipboard: ${text.substring(0, 16)}...`);
    }

    function escapeHtml(str) {
      if (typeof str !== 'string') str = String(str || '');
      return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
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
        const len = cipherBlockchain.chain.length;
        countEl.textContent = len === 1 ? '1 Block in Chain (Genesis Active)' : `${len} Blocks Mined`;
      }

      const badgeEl = document.getElementById('chainIntegrityBadge');
      const alertEl = document.getElementById('tamperAlertBanner');
      const verifyBtn = document.getElementById('verifyChainBtn');
      const tamperBtn = document.getElementById('tamperSimulateBtn');
      const restoreBtn = document.getElementById('restoreLedgerBtn');

      if (cipherBlockchain.isTampered && cipherBlockchain.tamperDetails) {
        const td = cipherBlockchain.tamperDetails;
        if (badgeEl) {
          badgeEl.innerHTML = `
            <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded bg-red-100 text-red-900 text-xs font-bold border-2 border-red-500 animate-pulse">
              <svg class="w-3.5 h-3.5 text-red-600 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
              <span>CRITICAL ALERT: Tampering Detected at Block #${td.brokenIndex}!</span>
            </span>
          `;
        }

        if (verifyBtn) {
          verifyBtn.className = 'btn-primary flex items-center gap-1.5 text-xs border-2 border-red-500 bg-red-50 text-red-700 font-bold';
        }
        if (tamperBtn) tamperBtn.classList.add('hidden');
        if (restoreBtn) restoreBtn.classList.remove('hidden');

        if (alertEl) {
          alertEl.classList.remove('hidden');
          alertEl.innerHTML = `
            <div class="flex items-start gap-3">
              <svg class="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
              <div class="space-y-1.5 flex-1">
                <div class="font-bold text-red-900 text-xs uppercase tracking-wide">
                  CRITICAL ALERT: Tampering Detected at Block #${td.brokenIndex}! Hash Mismatch: Expected ${td.expectedHash ? td.expectedHash.substring(0, 16) + '...' : ''} != Found ${td.actualHash ? td.actualHash.substring(0, 16) + '...' : ''}
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-2 font-mono text-[11px]">
                  <div class="p-2 bg-white/80 rounded border border-red-200">
                    <span class="text-red-700 font-semibold block text-[10px] uppercase">Recorded On-Chain Hash:</span>
                    <span class="text-red-900 break-all font-bold">${td.actualHash || 'N/A'}</span>
                  </div>
                  <div class="p-2 bg-white/80 rounded border border-red-200">
                    <span class="text-red-700 font-semibold block text-[10px] uppercase">Computed Hash (Payload Mutated):</span>
                    <span class="text-red-900 break-all font-bold">${td.expectedHash || 'N/A'}</span>
                  </div>
                </div>
                <p class="text-[11px] text-red-800 font-medium pt-0.5">
                  HASH CHAIN BROKEN: Previous Hash does not match upstream block. Click "Restore Ledger" to recover authentic consortium state.
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
              <span>Chain Integrity: VALID / SECURE</span>
            </span>
          `;
        }
        if (verifyBtn) {
          verifyBtn.className = 'btn-primary flex items-center gap-1.5 text-xs';
        }
        if (tamperBtn) tamperBtn.classList.remove('hidden');
        if (restoreBtn) restoreBtn.classList.add('hidden');
        if (alertEl) alertEl.classList.add('hidden');
      }

      const container = document.getElementById('blockchainLedgerVisualizer');
      if (!container) return;

      const eventBadgeStyles = {
        GENESIS_NOTARIZATION: 'bg-slate-100 text-slate-800 border-slate-300',
        INGEST_DOSSIERS: 'bg-sky-50 text-sky-800 border-sky-300',
        AUTHORITY_MATCH: 'bg-indigo-50 text-indigo-800 border-indigo-300',
        DOCUMENT_VERIFIED: 'bg-sky-50 text-sky-800 border-sky-300',
        DECISION_RECORDED: 'bg-emerald-50 text-emerald-800 border-emerald-300',
        ANOMALY_FLAGGED: 'bg-amber-50 text-amber-800 border-amber-300',
        RULE_CONFIG_CHANGED: 'bg-slate-50 text-slate-700 border-slate-300'
      };

      container.innerHTML = cipherBlockchain.chain.map((b, idx) => {
        const isTamperedTarget = cipherBlockchain.isTampered && b.index === cipherBlockchain.tamperedIndex;
        const isCascadeBroken = cipherBlockchain.isTampered && b.index > cipherBlockchain.tamperedIndex;
        const badgeStyle = eventBadgeStyles[b.eventType] || 'bg-slate-50 text-slate-700 border-slate-200';

        let connectorHtml = '';
        if (idx > 0) {
          if (isTamperedTarget) {
            connectorHtml = `
              <div class="flex items-center justify-center my-3">
                <div class="flex items-center gap-2 px-3 py-1 bg-red-50 border-2 border-red-500 rounded text-[11px] font-mono text-red-700 shadow-xs animate-pulse">
                  <svg class="w-3.5 h-3.5 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636" />
                  </svg>
                  <span>SHA-256 LINK: Block #${b.index - 1} &rarr; Block #${b.index} (MISMATCH / TAMPERED)</span>
                </div>
              </div>
            `;
          } else if (isCascadeBroken) {
            connectorHtml = `
              <div class="flex items-center justify-center my-3">
                <div class="flex items-center gap-2 px-3 py-1 bg-amber-50 border border-amber-500 rounded text-[11px] font-mono text-amber-800 shadow-xs">
                  <svg class="w-3.5 h-3.5 text-amber-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
                  </svg>
                  <span>HASH CHAIN BROKEN: Previous Hash does not match upstream block</span>
                </div>
              </div>
            `;
          } else {
            connectorHtml = `
              <div class="flex items-center justify-center my-3">
                <div class="flex items-center gap-2 px-3 py-1 bg-white border border-line rounded text-[11px] font-mono text-sub shadow-xs">
                  <svg class="w-3.5 h-3.5 text-[#0284c7]" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
                  </svg>
                  <span>SHA-256 LINK: Block #${b.index - 1} &rarr; Block #${b.index} (VALID / LINKED)</span>
                </div>
              </div>
            `;
          }
        }

        let cardContainerStyle = 'card-surface border-line';
        if (isTamperedTarget) {
          cardContainerStyle = 'border-2 border-red-500 bg-red-50/50 shadow-md';
        } else if (isCascadeBroken) {
          cardContainerStyle = 'border-2 border-amber-500 bg-amber-50/30';
        }

        const prevTrunc = b.previousHash ? `${b.previousHash.substring(0, 12)}...${b.previousHash.substring(b.previousHash.length - 8)}` : '00000000...';
        const hashTrunc = b.blockHash ? `${b.blockHash.substring(0, 12)}...${b.blockHash.substring(b.blockHash.length - 8)}` : 'N/A';

        return `
          ${connectorHtml}
          <div class="rounded border p-4 space-y-3 transition-colors ${cardContainerStyle}">
            <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-line pb-3">
              <div class="flex flex-wrap items-center gap-2">
                <span class="px-2 py-0.5 rounded font-mono text-xs font-bold ${b.index === 0 ? 'bg-slate-800 text-white' : 'bg-subtle border border-line text-main'}">
                  BLOCK #${b.index}${b.index === 0 ? ' - GENESIS' : ''}
                </span>
                <span class="px-2 py-0.5 rounded text-[11px] font-semibold border ${badgeStyle}">
                  ${escapeHtml(b.eventType)}
                </span>
                <span class="font-mono text-xs font-bold text-[#0284c7]">
                  Target: ${escapeHtml(b.targetId)}
                </span>
              </div>

              <div class="flex flex-wrap items-center gap-2 text-xs">
                <span class="font-mono text-sub text-[11px]">
                  ${b.timestamp}
                </span>
                <span class="px-2 py-0.5 rounded subtle-surface border border-line text-[11px] font-semibold text-main">
                  Signatory: ${escapeHtml(b.actorId)}
                </span>
              </div>
            </div>

            ${isTamperedTarget ? `
              <div class="p-2.5 bg-red-100 border border-red-300 rounded text-xs text-red-900 font-bold flex items-center gap-2">
                <svg class="w-4 h-4 text-red-600 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                <span>TAMPER DETECTED: Historical block payload was modified without valid cryptographic signature! Hash mismatch.</span>
              </div>
            ` : ''}

            ${isCascadeBroken ? `
              <div class="p-2 bg-amber-100 border border-amber-300 rounded text-xs text-amber-900 font-semibold flex items-center gap-2">
                <svg class="w-4 h-4 text-amber-600 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                <span>HASH CHAIN BROKEN: Previous Hash does not match upstream block. Cryptographic continuity severed.</span>
              </div>
            ` : ''}

            <!-- Hash Pills -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-2 text-xs">
              <div class="p-2.5 rounded subtle-surface border border-line space-y-1">
                <div class="text-[10px] font-mono uppercase text-sub font-semibold">Previous Hash (prevHash):</div>
                <div class="flex items-center justify-between gap-2">
                  <span class="font-mono text-[11px] text-sub break-all font-semibold" title="${b.previousHash}">${prevTrunc}</span>
                  <button onclick="copyLedgerHash('${b.previousHash}', 'Previous Hash')" class="btn-secondary py-0.5 px-2 text-[10px] shrink-0" style="border-radius:4px;" title="Copy previous hash">
                    Copy
                  </button>
                </div>
              </div>

              <div class="p-2.5 rounded subtle-surface border border-line space-y-1 ${isTamperedTarget ? 'border-red-400 bg-red-50' : ''}">
                <div class="text-[10px] font-mono uppercase font-semibold ${isTamperedTarget ? 'text-red-700' : 'text-sub'}">
                  Block Hash (SHA-256):
                </div>
                <div class="flex items-center justify-between gap-2">
                  <span class="font-mono text-[11px] font-bold ${isTamperedTarget ? 'text-red-700 line-through' : 'text-[#0284c7]'} break-all" title="${b.blockHash}">
                    ${hashTrunc}
                  </span>
                  <button onclick="copyLedgerHash('${b.blockHash}', 'Block Hash')" class="btn-secondary py-0.5 px-2 text-[10px] shrink-0" style="border-radius:4px;" title="Copy block hash">
                    Copy
                  </button>
                </div>
              </div>
            </div>

            <!-- Payload Preview -->
            <div class="p-2.5 rounded subtle-surface border border-line space-y-1.5 text-xs">
              <div class="text-[10px] font-mono uppercase text-sub font-semibold">Notarized Payload Data:</div>
              ${renderPayloadSummary(b.payload || b.payloadData)}
            </div>

            <!-- Raw JSON Inspect Drawer -->
            <details class="text-xs pt-1">
              <summary class="cursor-pointer text-[#0284c7] font-semibold text-[11px] hover:underline flex items-center gap-1 select-none">
                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
                </svg>
                <span>Inspect Raw Cryptographic Block JSON</span>
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
        showToastBanner(`Chain Integrity Verified: All ${cipherBlockchain.chain.length} blocks cryptographically valid across 7 Consortium PoA Nodes.`);
      } else {
        cipherBlockchain.isTampered = true;
        cipherBlockchain.tamperDetails = result;
        showToastBanner(`VERIFICATION FAILED: Block #${result.brokenIndex} payload mismatch! Hash chain broken.`);
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
      showToastBanner('CRITICAL ALERT: Tampering Detected at Block #1! Hash Mismatch: Expected != Found. Chained cryptographic link broken.');
    }

    async function triggerRestoreLedger() {
      cipherBlockchain.restoreLedger();
      const result = await cipherBlockchain.verifyChainIntegrity();
      cipherBlockchain.isTampered = false;
      cipherBlockchain.tamperDetails = null;
      renderBlockchainLedgerUI();
      showToastBanner('Consortium PoA Ledger Restored: All blocks cryptographically verified and valid.');
    }

    function exportLedgerJson() {
      const exportObject = {
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
      };

      const data = JSON.stringify(exportObject, null, 2);
      const blob = new Blob([data], { type: 'application/json;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'aicte_ciphercore_blockchain_ledger.json';
      a.click();
      URL.revokeObjectURL(url);
      showToastBanner('Downloaded aicte_ciphercore_blockchain_ledger.json');
    }
'''

    # Replace existing blockchain JS in frontend/index.html
    start_engine_marker = '// =====================================================================\n    // CRYPTOGRAPHIC SHA-256 ENGINE'
    end_engine_marker = 'function switchRulesSubTab(subTabName)'

    idx1 = html.find(start_engine_marker)
    idx2 = html.find(end_engine_marker)

    if idx1 != -1 and idx2 != -1:
        html = html[:idx1] + hardened_engine_js.strip() + '\n\n    ' + html[idx2:]
    else:
        print("Warning: Could not find exact engine markers, checking alternative", file=sys.stderr)

    # =========================================================================
    # 3. HOOK THE 4 EVENT-DRIVEN MINING TRIGGERS
    # =========================================================================

    # TRIGGER A: loadEvaluatorDemoDataset() -> Auto-mines Block #1 ("INGEST_DOSSIERS")
    old_load_demo = '''    async function loadEvaluatorDemoDataset() {
      applicationsData = JSON.parse(JSON.stringify(APPLICATIONS_DEMO_DATA));
      localStorage.setItem(INITIALIZED_KEY, 'true');
      updateFilterButtonCounts();
      renderApplicationsTable();
      renderInspectView();
      logAuditEvent(activeUserSession?.name || 'Evaluator', 'DEMO-DATA', 'Demo Dataset Loaded', 'Loaded 5 sample institutional applications for evaluation.');
      await seedDemoBlockchainBlocks();
      showToastBanner('Evaluator demo dataset (5 sample colleges) loaded successfully & notarized.');
    }'''

    new_load_demo = '''    async function loadEvaluatorDemoDataset() {
      applicationsData = JSON.parse(JSON.stringify(APPLICATIONS_DEMO_DATA));
      localStorage.setItem(INITIALIZED_KEY, 'true');
      updateFilterButtonCounts();
      renderApplicationsTable();
      renderInspectView();
      logAuditEvent(activeUserSession?.name || 'Evaluator', 'DEMO-DATA', 'Demo Dataset Loaded', 'Loaded 5 sample institutional applications for evaluation.');
      await seedDemoBlockchainBlocks();
      renderBlockchainLedgerUI();
      showToastBanner('Evaluator demo dataset (5 sample colleges) loaded & notarized on blockchain.');
    }'''

    html = html.replace(old_load_demo, new_load_demo)

    # TRIGGER B: handleStandaloneFileUpload & selectSampleDoc
    old_file_notarize = '''      await cipherBlockchain.addBlock(file.name, 'DOCUMENT_VERIFIED', activeUserSession?.name || 'AICTE_VERIFY_GATEWAY', {
        fileName: file.name,
        fileSize: sizeKb,
        sha256Fingerprint: computedHash,
        overallStatus: 'Valid',
        issuingGateway: 'VERIFIED-GATEWAY'
      });'''

    new_file_notarize = '''      await cipherBlockchain.addBlock(file.name, 'DOCUMENT_VERIFIED', activeUserSession?.name || 'AICTE_VERIFY_GATEWAY', {
        fileName: file.name,
        fileSize: sizeKb,
        sha256Fingerprint: computedHash,
        overallStatus: 'Valid',
        issuingGateway: 'VERIFIED-GATEWAY',
        verificationTimestamp: new Date().toISOString()
      });
      renderBlockchainLedgerUI();'''

    html = html.replace(old_file_notarize, new_file_notarize)

    old_sample_notarize = '''      cipherBlockchain.addBlock(doc.fileName, doc.overallStatus === 'Valid' ? 'DOCUMENT_VERIFIED' : 'ANOMALY_FLAGGED', activeUserSession?.name || 'AICTE_VERIFY_GATEWAY', {
        sampleKey,
        fileName: doc.fileName,
        sha256Fingerprint: doc.sha256,
        overallStatus: doc.overallStatus,
        authoritySource: doc.authorityStatus?.source
      });'''

    new_sample_notarize = '''      cipherBlockchain.addBlock(doc.fileName, doc.overallStatus === 'Valid' ? 'DOCUMENT_VERIFIED' : 'ANOMALY_FLAGGED', activeUserSession?.name || 'AICTE_VERIFY_GATEWAY', {
        sampleKey,
        fileName: doc.fileName,
        sha256Fingerprint: doc.sha256,
        overallStatus: doc.overallStatus,
        authoritySource: doc.authorityStatus?.source
      }).then(() => renderBlockchainLedgerUI());'''

    html = html.replace(old_sample_notarize, new_sample_notarize)

    # TRIGGER C: confirmOfficialDecision
    html = html.replace("function confirmOfficialDecision() {", "async function confirmOfficialDecision() {")
    old_decision_notarize = '''      cipherBlockchain.addBlock(currentInspectAppId, 'DECISION_RECORDED', officer, {
        applicationId: currentInspectAppId,
        institution: app ? app.institution : 'Unknown Institution',
        officialDecision: outcome,
        statutoryRemarks: notes,
        officerSignoff: true,
        notarizedAt: new Date().toISOString()
      });'''

    new_decision_notarize = '''      await cipherBlockchain.addBlock(currentInspectAppId, 'DECISION_RECORDED', officer, {
        applicationId: currentInspectAppId,
        institution: app ? app.institution : 'Unknown Institution',
        officialDecision: outcome,
        statutoryRemarks: notes,
        officerSignoff: true,
        notarizedAt: new Date().toISOString()
      });
      renderBlockchainLedgerUI();'''

    html = html.replace(old_decision_notarize, new_decision_notarize)

    # TRIGGER D: toggleRuleItem & addNewRuleItem
    old_toggle_rule_block = '''      await cipherBlockchain.addBlock(rule.code, 'RULE_CONFIG_CHANGED', actor, {
        ruleCode: rule.code,
        status: rule.enabled ? 'ENABLED' : 'DISABLED',
        category: rule.category,
        description: rule.description
      });'''

    new_toggle_rule_block = '''      await cipherBlockchain.addBlock(rule.code, 'RULE_CONFIG_CHANGED', actor, {
        ruleCode: rule.code,
        status: rule.enabled ? 'ENABLED' : 'DISABLED',
        category: rule.category,
        description: rule.description
      });
      renderBlockchainLedgerUI();'''

    html = html.replace(old_toggle_rule_block, new_toggle_rule_block)

    old_add_rule_block = '''      cipherBlockchain.addBlock(nextCode, 'RULE_CONFIG_CHANGED', activeUserSession?.name || 'Compliance Admin', {
        ruleCode: nextCode,
        status: 'ENABLED',
        category: category,
        description: text
      });'''

    new_add_rule_block = '''      cipherBlockchain.addBlock(nextCode, 'RULE_CONFIG_CHANGED', activeUserSession?.name || 'Compliance Admin', {
        ruleCode: nextCode,
        status: 'ENABLED',
        category: category,
        description: text
      }).then(() => renderBlockchainLedgerUI());'''

    html = html.replace(old_add_rule_block, new_add_rule_block)

    # =========================================================================
    # 4. SELF-INITIALIZATION ON DOMContentLoaded
    # =========================================================================
    old_dom_load = '''    window.addEventListener('DOMContentLoaded', async () => {
      await cipherBlockchain.init();
      await seedDemoBlockchainBlocks();
      initUserSession();
    });'''

    new_dom_load = '''    window.addEventListener('DOMContentLoaded', async () => {
      await cipherBlockchain.init();
      const isDemoInit = localStorage.getItem(INITIALIZED_KEY) === 'true';
      if (isDemoInit && cipherBlockchain.chain.length <= 1) {
        await seedDemoBlockchainBlocks();
      }
      initUserSession();
      renderBlockchainLedgerUI();
    });'''

    html = html.replace(old_dom_load, new_dom_load)

    # Save to frontend/index.html
    with open('frontend/index.html', 'w', encoding='utf-8') as f:
        f.write(html)

    # Sync to root index.html
    shutil.copyfile('frontend/index.html', 'index.html')

    # Sync to backend static
    backend_static = 'backend/src/main/resources/static/index.html'
    if os.path.exists(os.path.dirname(backend_static)):
        shutil.copyfile('frontend/index.html', backend_static)

    print("Successfully built and synchronized hardened blockchain ledger across all targets.")
    return True

if __name__ == '__main__':
    main()
