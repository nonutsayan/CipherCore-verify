/* ==========================================================================
   MAILSCOPE SLIDE-OVER DRAWER CONTROLLER FOR CIPHERCORE
   Universal Slide-Over Panel with deep domain telemetry & interactive tabs
   ========================================================================== */

(function(window) {
  'use strict';

  let currentDrawerType = null;
  let currentDrawerData = null;
  let currentActiveTab = 'overview';

  // Ensure DOM container exists
  function ensureDrawerMarkup() {
    let backdrop = document.getElementById('chimeraDrawerBackdrop');
    let drawer = document.getElementById('chimeraSlideDrawer');

    if (!backdrop) {
      backdrop = document.createElement('div');
      backdrop.id = 'chimeraDrawerBackdrop';
      backdrop.className = 'drawer-backdrop';
      backdrop.addEventListener('click', closeDrawer);
      document.body.appendChild(backdrop);
    }

    if (!drawer) {
      drawer = document.createElement('aside');
      drawer.id = 'chimeraSlideDrawer';
      drawer.className = 'drawer';
      drawer.innerHTML = `
        <div class="drawer-header" id="drawerHeaderArea">
          <!-- Dynamic Header Loaded on openDrawer -->
        </div>
        <div class="drawer-tabs" id="drawerTabsArea">
          <button class="drawer-tab-btn active" data-dtab="overview" onclick="switchDrawerTab('overview')">Overview</button>
          <button class="drawer-tab-btn" data-dtab="visual" onclick="switchDrawerTab('visual')">Visual Evidence</button>
          <button class="drawer-tab-btn" data-dtab="rules" onclick="switchDrawerTab('rules')">Statutory Rules</button>
          <button class="drawer-tab-btn" data-dtab="crypto" onclick="switchDrawerTab('crypto')">Cryptographic Proof</button>
          <button class="drawer-tab-btn" data-dtab="telemetry" onclick="switchDrawerTab('telemetry')">Raw Telemetry</button>
        </div>
        <div class="drawer-body" id="drawerBodyArea">
          <!-- Dynamic Content Loaded on openDrawer -->
        </div>
        <div class="drawer-footer" id="drawerFooterArea">
          <!-- Dynamic Action Buttons -->
        </div>
      `;
      document.body.appendChild(drawer);
    }
  }

  // Open Master Slide-Over Drawer
  function openDrawer(type, data) {
    ensureDrawerMarkup();
    currentDrawerType = type;
    currentDrawerData = data || {};
    currentActiveTab = 'overview';

    const backdrop = document.getElementById('chimeraDrawerBackdrop');
    const drawer = document.getElementById('chimeraSlideDrawer');

    renderDrawerHeader(type, currentDrawerData);
    renderDrawerTabs(type);
    renderDrawerBody(type, currentDrawerData, currentActiveTab);
    renderDrawerFooter(type, currentDrawerData);

    // Trigger sliding animation
    requestAnimationFrame(() => {
      backdrop.classList.add('open');
      drawer.classList.add('open');
    });

    document.body.style.overflow = 'hidden';
  }

  // Close Master Slide-Over Drawer
  function closeDrawer() {
    const backdrop = document.getElementById('chimeraDrawerBackdrop');
    const drawer = document.getElementById('chimeraSlideDrawer');

    if (backdrop) backdrop.classList.remove('open');
    if (drawer) drawer.classList.remove('open');

    document.body.style.overflow = '';
  }

  // Switch Tab inside Drawer
  function switchDrawerTab(tabName) {
    currentActiveTab = tabName;
    document.querySelectorAll('.drawer-tab-btn').forEach(btn => {
      btn.classList.toggle('active', btn.getAttribute('data-dtab') === tabName);
    });
    renderDrawerBody(currentDrawerType, currentDrawerData, tabName);
  }

  // Render Drawer Header based on Domain Entity
  function renderDrawerHeader(type, data) {
    const headerEl = document.getElementById('drawerHeaderArea');
    if (!headerEl) return;

    let category = 'REGULATORY EVIDENCE';
    let title = data.title || 'Inspection Details';
    let statusClass = 'satisfied';
    let statusLabel = 'VERIFIED';
    let metaInfo = `App #2026-NRO-88412 · Timestamp: ${new Date().toLocaleTimeString()}`;

    if (type === 'document') {
      category = 'DOCUMENT OCR &amp; SPATIAL LAYOUT';
      title = data.name || data.fileName || 'Institutional Land Deed';
      statusClass = data.ocrStatus === 'PROCESSED' ? 'satisfied' : 'warning';
      statusLabel = data.ocrStatus || 'OCR EXTRACTED';
      metaInfo = `Doc ID: #${data.id || '2'} · Confidence: ${data.confidenceScore || '99.4%'} · SHA-256 Sealed`;
    } else if (type === 'forensics') {
      category = 'MULTI-SPECTRAL FORENSIC ANOMALY';
      title = data.anomalyType || 'Error Level Analysis (ELA) Variance';
      statusClass = 'violated';
      statusLabel = data.severity || 'CRITICAL FLAG';
      metaInfo = `Clause: APH Ch. 6 · Coordinate: [x:120, y:450] · Model: ELA ResNet50`;
    } else if (type === 'authority') {
      category = 'ISSUING AUTHORITY GATEWAY';
      title = data.name || 'DigiLocker National Academic Depository';
      statusClass = 'satisfied';
      statusLabel = '200 OK (ONLINE)';
      metaInfo = `Protocol: OAuth2 + XML DSig · Latency: ${data.latency || '180ms'} · TLS 1.3 Verified`;
    } else if (type === 'compliance') {
      category = 'AICTE APH 2024-27 RULE ENGINE';
      title = data.clause || 'Faculty Cadre Ratio (1:2:6 Cadence)';
      statusClass = data.compliant ? 'satisfied' : 'warning';
      statusLabel = data.compliant ? 'COMPLIANT' : 'ACTION REQUIRED';
      metaInfo = `APH 2024-27 Chapter II, Section 2.4.1 · Mathematical Deficiency: 2 Associate Profs Short`;
    } else if (type === 'block') {
      category = 'BHARATVIDYA POA BLOCKCHAIN NOTARY';
      title = `Block #${data.blockNumber || '4094'} [MINED]`;
      statusClass = 'satisfied';
      statusLabel = 'CONSENSUS REACHED';
      metaInfo = `Validator: ${data.validator || 'AICTE HQ Node'} · Gas: 0.0 Gwei · Finality: 2.0s`;
    } else if (type === 'audit') {
      category = 'RFC 3161 IMMUTABLE AUDIT TRAIL';
      title = data.action || 'Officer Scrutiny Determination';
      statusClass = 'satisfied';
      statusLabel = 'CRYPTOGRAPHIC SEALED';
      metaInfo = `Signer: Dr. Rajesh Sharma · Previous Hash Linked · SHA-256 Continuity Valid`;
    } else if (type === 'notifications') {
      category = 'LIVE SCRUTINY TELEMETRY';
      title = 'Real-Time Scrutiny Alerts';
      statusClass = 'neutral';
      statusLabel = 'ACTIVE QUEUE';
      metaInfo = '3 Unresolved Priority Notifications';
    }

    headerEl.innerHTML = `
      <div class="drawer-top-row">
        <span class="drawer-category-badge">${category}</span>
        <button class="drawer-close-btn" onclick="closeDrawer()" title="Close Drawer (Esc)">✕</button>
      </div>
      <div class="drawer-title-row">
        <h2 class="drawer-title">${title}</h2>
        <span class="status-pill ${statusClass}">${statusLabel}</span>
      </div>
      <div class="drawer-meta-row">
        <span>${metaInfo}</span>
      </div>
    `;
  }

  // Adjust available tabs depending on entity
  function renderDrawerTabs(type) {
    const tabsEl = document.getElementById('drawerTabsArea');
    if (!tabsEl) return;

    if (type === 'notifications') {
      tabsEl.style.display = 'none';
      return;
    }
    tabsEl.style.display = 'flex';
  }

  // Render Drawer Body by Tab
  function renderDrawerBody(type, data, tab) {
    const bodyEl = document.getElementById('drawerBodyArea');
    if (!bodyEl) return;

    if (tab === 'visual') {
      bodyEl.innerHTML = `
        <div class="drawer-section">
          <div class="drawer-section-title">📷 High-Resolution Spatial Inspection</div>
          <div class="drawer-visual-wrap">
            <div style="text-align:center; padding:20px; color:#57E8FF;">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" style="margin-bottom:8px;"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
              <div style="font-size:0.85rem; font-weight:700;">Spatial Coordinate Overlay Loaded</div>
              <div style="font-size:0.75rem; color:var(--text-muted); margin-top:4px;">Bounding Box: [x: 120, y: 450, w: 88, h: 24] · Bounding Resolution: 300 DPI</div>
            </div>
            <div class="drawer-bbox-highlight" style="top:40%; left:25%; width:180px; height:45px;"></div>
          </div>
          <div style="display:flex; justify-content:space-between; margin-top:10px; font-size:0.75rem; color:var(--text-muted);">
            <span>Scale: 100% (Pixel-Aligned)</span>
            <span style="color:#00F0FF; cursor:pointer;" onclick="alert('Zooming into OCR bounding box region...')">🔍 Zoom 200%</span>
          </div>
        </div>

        <div class="drawer-section">
          <div class="drawer-section-title">🔬 Error Level Analysis (ELA) Compression Artifacts</div>
          <p style="font-size:0.82rem; color:var(--text-muted); line-height:1.55; margin-bottom:12px;">
            Highlights high-frequency JPEG compression variance. Areas that were modified after initial document scanning exhibit distinctive bright pixel clustering.
          </p>
          <div style="background:#020307; border:1px solid rgba(255,59,105,0.3); border-radius:8px; padding:12px; font-family:var(--font-mono); font-size:0.75rem; color:#FF3B69;">
            [ELA DETECTOR] Compression Variance Ratio: +34.2% on Bounding Box Region<br>
            [STATUS] Cosine Perceptual Mismatch: 0.884 (Threshold 0.950)
          </div>
        </div>
      `;
      return;
    }

    if (tab === 'rules') {
      bodyEl.innerHTML = `
        <div class="drawer-section">
          <div class="drawer-section-title">⚖️ AICTE Approval Process Handbook (APH 2024-27) Statutory Clauses</div>
          <table class="drawer-kv-table">
            <tr><td class="key">Regulatory Clause</td><td class="val">Chapter II, Section 2.4.1 (Faculty Cadre Ratio)</td></tr>
            <tr><td class="key">Mandated Cadence</td><td class="val">1 Professor : 2 Associate Professors : 6 Assistant Professors</td></tr>
            <tr><td class="key">Claimed by Institute</td><td class="val">1 Professor, 0 Associate Professors, 8 Assistant Professors</td></tr>
            <tr><td class="key">Deficiency Flag</td><td class="val" style="color:#FF3B69; font-weight:700;">2 Associate Professors Short</td></tr>
            <tr><td class="key">Action Prescribed</td><td class="val">Issue Statutory Show-Cause or require SAC conditional compliance</td></tr>
          </table>
        </div>

        <div class="drawer-section">
          <div class="drawer-section-title">🏛️ Institutional Land &amp; Built-up Norms (Chapter I)</div>
          <table class="drawer-kv-table">
            <tr><td class="key">Land Area Required</td><td class="val">4.00 Acres (Mega / Urban Category)</td></tr>
            <tr><td class="key">Verified Land Area</td><td class="val" style="color:#00F5A0; font-weight:700;">5.42 Acres (+35.5% Surplus · COMPLIANT)</td></tr>
            <tr><td class="key">Built-up Area Req</td><td class="val">16,500 sq. metres</td></tr>
            <tr><td class="key">Verified Built-up</td><td class="val" style="color:#00F5A0; font-weight:700;">18,400 sq. metres (COMPLIANT)</td></tr>
          </table>
        </div>
      `;
      return;
    }

    if (tab === 'crypto') {
      bodyEl.innerHTML = `
        <div class="drawer-section">
          <div class="drawer-section-title">🛡️ Cryptographic Integrity &amp; PoA Proof Chain</div>
          <table class="drawer-kv-table">
            <tr><td class="key">Document SHA-256</td><td class="val">9f83a214d5e82b7c614392f4405a8d9a9f83a214d5e82b7c614392f4405a8d9a</td></tr>
            <tr><td class="key">Keccak-256 Hash</td><td class="val">0x3e12c488a1b24f5d8821039472ef88a10x3e12c488a1b24f5d8821039472ef88a1</td></tr>
            <tr><td class="key">PoA Block Number</td><td class="val" style="color:#00F0FF; font-weight:700;">Block #4094 [MINED]</td></tr>
            <tr><td class="key">Merkle Root</td><td class="val">0x77ab429c01824f923e10283b4c61928301928374657192837465019283746501</td></tr>
            <tr><td class="key">Consensus Signers</td><td class="val" style="color:#00F5A0;">7 / 7 Validator Nodes Synced (0.0 Gwei)</td></tr>
            <tr><td class="key">RFC 3161 Timestamp</td><td class="val">${new Date().toUTCString()}</td></tr>
          </table>
        </div>

        <div class="drawer-section">
          <div class="drawer-section-title">⛓️ Merkle Inclusion Proof Path</div>
          <div style="background:#020307; border:1px solid rgba(0,240,255,0.25); border-radius:8px; padding:14px; font-family:var(--font-mono); font-size:0.75rem; color:#57E8FF;">
            Root: 0x77ab429c...<br>
            &nbsp;└── Branch [Left]: 0x9f83a... (AICTE Land Notary)<br>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└── Leaf [Hash]: 0x3e12c... [Verified Inclusion Proof ✅]
          </div>
        </div>
      `;
      return;
    }

    if (tab === 'telemetry') {
      bodyEl.innerHTML = `
        <div class="drawer-section">
          <div class="drawer-section-title">💻 Raw JSON Telemetry Payload</div>
          <pre class="drawer-json-box">${JSON.stringify(data, null, 2)}</pre>
          <div style="margin-top:10px; display:flex; justify-content:flex-end;">
            <button class="btn-drawer-secondary" onclick="navigator.clipboard.writeText(JSON.stringify(currentDrawerData, null, 2)); alert('Raw JSON copied to clipboard!');">
              📋 Copy Raw JSON
            </button>
          </div>
        </div>
      `;
      return;
    }

    // Default 'overview' tab:
    if (type === 'notifications') {
      bodyEl.innerHTML = `
        <div class="drawer-section">
          <div class="drawer-section-title">🔔 Recent Scrutiny Notifications</div>
          <div style="display:flex; flex-direction:column; gap:12px;">
            <div style="background:rgba(255,59,105,0.08); border:1px solid rgba(255,59,105,0.25); border-radius:10px; padding:14px;">
              <div style="display:flex; justify-content:space-between; margin-bottom:4px;">
                <strong style="color:#FF3B69; font-size:0.85rem;">Critical ELA Anomaly Flagged</strong>
                <span style="font-size:0.72rem; color:var(--text-muted);">2 mins ago</span>
              </div>
              <p style="font-size:0.8rem; color:#E2E8F0; line-height:1.5;">Land Area Title Deed exhibits +34% compression variance on numeric block. Corroboration via Land Revenue Gateway recommended.</p>
            </div>

            <div style="background:rgba(245,158,11,0.08); border:1px solid rgba(245,158,11,0.25); border-radius:10px; padding:14px;">
              <div style="display:flex; justify-content:space-between; margin-bottom:4px;">
                <strong style="color:#F59E0B; font-size:0.85rem;">Faculty Cadre Deficit Identified</strong>
                <span style="font-size:0.72rem; color:var(--text-muted);">15 mins ago</span>
              </div>
              <p style="font-size:0.8rem; color:#E2E8F0; line-height:1.5;">Apex Institute CADRE ratio 1:2:6 lacks 2 Associate Professors. Statutory notice prepared.</p>
            </div>

            <div style="background:rgba(0,245,160,0.08); border:1px solid rgba(0,245,160,0.25); border-radius:10px; padding:14px;">
              <div style="display:flex; justify-content:space-between; margin-bottom:4px;">
                <strong style="color:#00F5A0; font-size:0.85rem;">DigiLocker Gateway Attestation Complete</strong>
                <span style="font-size:0.72rem; color:var(--text-muted);">32 mins ago</span>
              </div>
              <p style="font-size:0.8rem; color:#E2E8F0; line-height:1.5;">State Fire NOC verified via direct digital signature XML check (latency 180ms).</p>
            </div>
          </div>
        </div>
      `;
      return;
    }

    // Standard Overview
    bodyEl.innerHTML = `
      <div class="drawer-section">
        <div class="drawer-section-title">📊 Key Verification Metrics</div>
        <table class="drawer-kv-table">
          <tr><td class="key">Entity Reference</td><td class="val">${data.name || data.title || 'Institutional Land Deed'}</td></tr>
          <tr><td class="key">Document Category</td><td class="val">${data.docType || 'Statutory Title Certificate (APH Ch. 1)'}</td></tr>
          <tr><td class="key">Extraction Accuracy</td><td class="val" style="color:#00F5A0; font-weight:700;">${data.confidenceScore || '99.4%'}</td></tr>
          <tr><td class="key">Issuing Authority</td><td class="val">Office of the Sub-Registrar / Municipal Board</td></tr>
          <tr><td class="key">Verification Status</td><td class="val" style="color:#00F0FF; font-weight:700;">SEALED ON BHARATVIDYA POA</td></tr>
          <tr><td class="key">Timestamped At</td><td class="val">${new Date().toLocaleString()}</td></tr>
        </table>
      </div>

      <div class="drawer-section">
        <div class="drawer-section-title">📝 Extracted Field Key-Values</div>
        <div style="display:grid; grid-template-columns:1fr 1fr; gap:10px;">
          <div style="background:#050711; border:1px solid rgba(255,255,255,0.08); padding:10px; border-radius:8px;">
            <div style="font-size:0.72rem; color:var(--text-muted);">Land Area</div>
            <div style="font-size:0.95rem; font-weight:700; color:#00F5A0;">5.42 Acres</div>
          </div>
          <div style="background:#050711; border:1px solid rgba(255,255,255,0.08); padding:10px; border-radius:8px;">
            <div style="font-size:0.72rem; color:var(--text-muted);">Built-up Area</div>
            <div style="font-size:0.95rem; font-weight:700; color:#00F5A0;">18,400 sq.m</div>
          </div>
          <div style="background:#050711; border:1px solid rgba(255,255,255,0.08); padding:10px; border-radius:8px;">
            <div style="font-size:0.72rem; color:var(--text-muted);">FDR Endowment</div>
            <div style="font-size:0.95rem; font-weight:700; color:#00F0FF;">₹1,00,00,000</div>
          </div>
          <div style="background:#050711; border:1px solid rgba(255,255,255,0.08); padding:10px; border-radius:8px;">
            <div style="font-size:0.72rem; color:var(--text-muted);">Principal Cadre</div>
            <div style="font-size:0.95rem; font-weight:700; color:#8B5CF6;">Ph.D Verified</div>
          </div>
        </div>
      </div>
    `;
  }

  // Render Drawer Footer
  function renderDrawerFooter(type, data) {
    const footerEl = document.getElementById('drawerFooterArea');
    if (!footerEl) return;

    if (type === 'notifications') {
      footerEl.innerHTML = `
        <button class="btn-drawer-secondary" onclick="alert('All notifications marked as read.'); closeDrawer();">
          Mark All Read
        </button>
        <button class="btn-drawer-primary" onclick="closeDrawer()">
          Close
        </button>
      `;
      return;
    }

    footerEl.innerHTML = `
      <button class="btn-drawer-secondary" onclick="navigator.clipboard.writeText(JSON.stringify(currentDrawerData)); alert('Cryptographic record copied to clipboard!');">
        📋 Copy Record
      </button>
      <button class="btn-drawer-primary" onclick="if (typeof verifyAuditChainIntegrity === 'function') verifyAuditChainIntegrity();">
        🛡️ Verify On-Chain
      </button>
      <button class="btn-drawer-primary" style="background:linear-gradient(135deg, #00F5A0 0%, #00B377 100%); color:#000;" onclick="alert('Adjudication decision recorded in cryptographic audit trail.'); closeDrawer();">
        ✓ Confirm Finding
      </button>
    `;
  }

  // Global Keyboard Listener (Escape closes drawer)
  window.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
      closeDrawer();
    }
  });

  // Attach to window
  window.openDrawer = openDrawer;
  window.closeDrawer = closeDrawer;
  window.switchDrawerTab = switchDrawerTab;

})(window);
