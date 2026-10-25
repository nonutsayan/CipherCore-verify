import os
import sys

def main():
    path = "frontend/index.html"
    with open(path, "r", encoding="utf-8") as f:
        html = f.read()

    # 1. Add .modal-backdrop CSS if missing or update it
    if ".modal-backdrop" not in html:
        style_target = "    html.dark {\n      --bg-main: #0b0f19;"
        modal_css = """    /* Modal Backdrop & Bulletproof Visibility */
    .modal-backdrop {
      position: fixed;
      inset: 0;
      z-index: 9999;
      background-color: rgba(0, 0, 0, 0.65);
      display: none;
      align-items: center;
      justify-content: center;
      padding: 1rem;
    }
    .modal-backdrop.active {
      display: flex !important;
    }
    html.dark .modal-backdrop {
      background-color: rgba(0, 0, 0, 0.85);
    }

"""
        html = html.replace(style_target, modal_css + style_target)
        print("Added .modal-backdrop CSS.")

    # 2. Update Sample Document Buttons (Tray) in Section 3 HTML
    tray_start = html.find("<!-- 3 Sample Test Document Buttons -->")
    if tray_start != -1:
        tray_end = html.find("<!-- 4 Structured Verification Result Cards -->", tray_start)
        if tray_end != -1:
            old_tray = html[tray_start:tray_end]
            new_tray = """<!-- 6 Sample Test Document Buttons (Expanded APH Test Suite) -->
          <div>
            <div class="text-[11px] font-mono uppercase text-sub mb-2">Or select a sample test document (APH Mandatory Submissions):</div>
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2.5">
              <button onclick="selectSampleDoc('SAMPLE-1')" id="sampleDocBtn-SAMPLE-1" class="btn-secondary active text-left flex items-center justify-between py-2 px-3 text-xs" style="border-radius:4px;">
                <div>
                  <div class="font-semibold text-main">Sample 1: Fire Safety NOC</div>
                  <div class="text-[10px] text-sub font-mono">SYNTH_FIRE_NOC_MH_2026.pdf</div>
                </div>
                <span class="tag-green shrink-0">Verified</span>
              </button>
              <button onclick="selectSampleDoc('SAMPLE-2')" id="sampleDocBtn-SAMPLE-2" class="btn-secondary text-left flex items-center justify-between py-2 px-3 text-xs" style="border-radius:4px;">
                <div>
                  <div class="font-semibold text-main">Sample 2: Land Deed</div>
                  <div class="text-[10px] text-sub font-mono">SYNTH_LAND_DEED_PUNE_ALTERED.pdf</div>
                </div>
                <span class="tag-red shrink-0">Altered</span>
              </button>
              <button onclick="selectSampleDoc('SAMPLE-3')" id="sampleDocBtn-SAMPLE-3" class="btn-secondary text-left flex items-center justify-between py-2 px-3 text-xs" style="border-radius:4px;">
                <div>
                  <div class="font-semibold text-main">Sample 3: Affiliation Letter</div>
                  <div class="text-[10px] text-sub font-mono">SYNTH_SPPU_AFFILIATION_2026.pdf</div>
                </div>
                <span class="tag-amber shrink-0">Seat Mismatch</span>
              </button>
              <button onclick="selectSampleDoc('SAMPLE-4')" id="sampleDocBtn-SAMPLE-4" class="btn-secondary text-left flex items-center justify-between py-2 px-3 text-xs" style="border-radius:4px;">
                <div>
                  <div class="font-semibold text-main">Sample 4: Faculty Roster</div>
                  <div class="text-[10px] text-sub font-mono">SYNTH_FACULTY_ROSTER_DUPLICATE_PAN.pdf</div>
                </div>
                <span class="tag-red shrink-0">Duplicate PAN</span>
              </button>
              <button onclick="selectSampleDoc('SAMPLE-5')" id="sampleDocBtn-SAMPLE-5" class="btn-secondary text-left flex items-center justify-between py-2 px-3 text-xs" style="border-radius:4px;">
                <div>
                  <div class="font-semibold text-main">Sample 5: Building Plan</div>
                  <div class="text-[10px] text-sub font-mono">SYNTH_BUILDING_PLAN_MUNICIPAL.pdf</div>
                </div>
                <span class="tag-amber shrink-0">Deficient Area</span>
              </button>
              <button onclick="selectSampleDoc('SAMPLE-6')" id="sampleDocBtn-SAMPLE-6" class="btn-secondary text-left flex items-center justify-between py-2 px-3 text-xs" style="border-radius:4px;">
                <div>
                  <div class="font-semibold text-main">Sample 6: Treasury Fee Receipt</div>
                  <div class="text-[10px] text-sub font-mono">SYNTH_AICTE_TER_RECEIPT_VERIFIED.pdf</div>
                </div>
                <span class="tag-green shrink-0">Verified</span>
              </button>
            </div>
          </div>
        </div>

        """
            html = html[:tray_start] + new_tray + html[tray_end:]
            print("Updated 6-button sample document test tray.")

    # 3. Update sampleDocumentsData in JavaScript
    data_start = html.find("const sampleDocumentsData = {")
    if data_start != -1:
        data_end = html.find("const rulesList = [", data_start)
        if data_end != -1:
            new_sample_data = """const sampleDocumentsData = {
      'SAMPLE-1': {
        fileName: 'SYNTH_FIRE_NOC_MH_2026.pdf',
        fileSize: '412.4 KB',
        sha256: 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
        overallStatus: 'Valid',
        ocrFields: [
          { label: 'Document Category', value: 'State Fire Safety NOC (AICTE APH Mandate)' },
          { label: 'Reference Number', value: 'NOC/MFS/2026/8841' },
          { label: 'Institution Name', value: 'Apex Institute of Technology, Pune' },
          { label: 'Issue Date', value: '2026-01-10' },
          { label: 'Expiry Date', value: '2029-01-09 (Active Valid Certificate)' }
        ],
        tamperingChecks: [
          { check: 'PDF Producer & Metadata Integrity', status: 'Pass', detail: 'Generated directly by Maharashtra e-District Fire Portal' },
          { check: 'Font Stream & Raster Inspection', status: 'Pass', detail: 'No post-issuance digital raster modification detected' },
          { check: 'Duplicate File Hash Check', status: 'Pass', detail: 'Unique SHA-256 hash across all AICTE dossiers' }
        ],
        authorityStatus: {
          source: 'Maharashtra State Fire Services Gateway API',
          refId: 'NOC/MFS/2026/8841',
          outcome: 'Verified',
          message: 'Official state registry confirms 200 OK. Active Valid Certificate until 2029-01-09.'
        }
      },
      'SAMPLE-2': {
        fileName: 'SYNTH_LAND_DEED_PUNE_ALTERED.pdf',
        fileSize: '684.0 KB',
        sha256: '9b8769a4a742959a2d0298c36fb7064bd8f161834ec35b4b5e083cc937297b8b',
        overallStatus: 'Altered',
        ocrFields: [
          { label: 'Document Category', value: 'Registered Land Ownership Sale Deed' },
          { label: 'Reference Number', value: 'DEED-REV-MH-4419' },
          { label: 'Institution Name', value: 'Apex Educational Trust, Pune' },
          { label: 'Claimed Survey & Area', value: 'Survey No: 42/B (5.20 Acres)' },
          { label: 'Registry Survey & Area', value: 'Survey No: 42/A (2.10 Acres)' }
        ],
        tamperingChecks: [
          { check: 'Font Encoding & Stream Analysis', status: 'Flagged', detail: 'Font mismatch: Helvetica-Bold overlay over scanned Courier baseline' },
          { check: 'Error Level Analysis (ELA)', status: 'Flagged', detail: 'ELA score 88.4%: Post-scan digital raster modification detected' },
          { check: 'Survey & Acreage Consistency', status: 'Flagged', detail: 'Survey 42/B vs 42/A mismatch (-3.10 Acres regulatory shortfall)' }
        ],
        authorityStatus: {
          source: 'State Land Registry API (DigiLocker)',
          refId: 'DEED-REV-MH-4419',
          outcome: 'Mismatch',
          message: 'Official revenue records confirm Survey No 42/A with 2.10 Acres, not 5.20 Acres. Discrepancy flagged.'
        }
      },
      'SAMPLE-3': {
        fileName: 'SYNTH_SPPU_AFFILIATION_2026.pdf',
        fileSize: '389.2 KB',
        sha256: '4a2d81f08726588a8910798ca7a65b7f12e8b61c94d01a35798c81e5b290d287',
        overallStatus: 'Mismatch',
        ocrFields: [
          { label: 'Document Category', value: 'University Annual Affiliation Order' },
          { label: 'Reference Number', value: 'SPPU/AFF/2026/1042' },
          { label: 'Institution Name', value: 'Apex Institute of Technology, Pune' },
          { label: 'Sanctioned Program', value: 'B.Tech Computer Science & Engineering' },
          { label: 'Approved vs Claimed Intake', value: '120 Approved vs 180 Claimed Seats' }
        ],
        tamperingChecks: [
          { check: 'Sanctioned Seat Capacity Check', status: 'Flagged', detail: 'Discrepancy: 120 Approved vs 180 Claimed Seats' },
          { check: 'Numerical Glyph Font Metric', status: 'Flagged', detail: 'Digit 8 raster modification detected over baseline digit 2' },
          { check: 'Digital Signature & University Seal', status: 'Pass', detail: 'University Registrar digital certificate signature present' }
        ],
        authorityStatus: {
          source: 'Savitribai Phule Pune University Registry Portal API',
          refId: 'SPPU/AFF/2026/1042',
          outcome: 'Mismatch',
          message: 'Senate Affiliation Order validates 120 intake seats. Claimed 180 seats exceeds sanctioned capacity.'
        }
      },
      'SAMPLE-4': {
        fileName: 'SYNTH_FACULTY_ROSTER_DUPLICATE_PAN.pdf',
        fileSize: '512.6 KB',
        sha256: '7c9d4a19b88234ea7208d98cf142a781045e781198fba014c2b9981a54ec0912',
        overallStatus: 'Flagged',
        ocrFields: [
          { label: 'Document Category', value: 'Endorsed Faculty Roster & Teaching Cadre' },
          { label: 'Reference Number', value: 'FAC-2026-ROSTER-04' },
          { label: 'Total Faculty Claimed', value: '28 Faculty Members' },
          { label: 'Duplicate PAN 1', value: 'ABCDE1234F (Prof. Rajesh Verma - Also at Sinhgad Inst)' },
          { label: 'Duplicate PAN 2', value: 'PQRSK5678L (Dr. Sunita Patil - Also at DY Patil COE)' }
        ],
        tamperingChecks: [
          { check: 'Central Faculty Database Check', status: 'Flagged', detail: '2 Faculty PAN numbers (ABCDE1234F, PQRSK5678L) already registered under another college' },
          { check: 'Income Tax TDS Form 16 Sync', status: 'Flagged', detail: 'Concurrent full-time salary disbursement reported from multiple technical institutions' },
          { check: 'Cadre Ratio Distribution', status: 'Pass', detail: 'Cadre ratio meets 1:2:6 distribution norm' }
        ],
        authorityStatus: {
          source: 'Income Tax Dept / AICTE Central Faculty Database API',
          refId: 'FAC-2026-ROSTER-04',
          outcome: 'Flagged',
          message: 'Dual employment violation: 2 Faculty PAN numbers are actively listed in concurrent faculty rolls.'
        }
      },
      'SAMPLE-5': {
        fileName: 'SYNTH_BUILDING_PLAN_MUNICIPAL.pdf',
        fileSize: '892.1 KB',
        sha256: '81fa023e981bc8819024c088ef7721ab98234ec89124a98b7123984afcb78102',
        overallStatus: 'Deficient',
        ocrFields: [
          { label: 'Document Category', value: 'Sanctioned Architectural Building & Carpet Plan' },
          { label: 'Reference Number', value: 'PMC/BP/SANCTION/2021-992' },
          { label: 'Sanctioning Municipal Body', value: 'Pune Municipal Corporation Town Planning' },
          { label: 'Required Instructional Area', value: '4,200 sq.m (APH 2024-27 Norm)' },
          { label: 'Sanctioned Actual Area', value: '3,880 sq.m (Deficit: 320 sq.m)' }
        ],
        tamperingChecks: [
          { check: 'Instructional Carpet Area Check', status: 'Flagged', detail: 'Instructional carpet area short by 320 sq.m against APH intake requirements' },
          { check: 'Council of Architecture Seal', status: 'Pass', detail: 'Registered architect digital license valid' },
          { check: 'Municipal Occupancy Certificate', status: 'Pass', detail: 'PMC occupancy sanction reference verified' }
        ],
        authorityStatus: {
          source: 'Town Planning & Municipal Sanction Authority API',
          refId: 'PMC/BP/SANCTION/2021-992',
          outcome: 'Deficient',
          message: 'Sanctioned floor plans indicate 3,880 sq.m instructional built-up area. Deficit of 320 sq.m against 4,200 sq.m requirement.'
        }
      },
      'SAMPLE-6': {
        fileName: 'SYNTH_AICTE_TER_RECEIPT_VERIFIED.pdf',
        fileSize: '245.8 KB',
        sha256: '12a4b8890cb1e22998f41029cba881024e0981ba23589cf8810924ec12871239',
        overallStatus: 'Valid',
        ocrFields: [
          { label: 'Document Category', value: 'AICTE Technical Education Regulatory (TER) Fee' },
          { label: 'Reference Number', value: 'BHARATKOSH-UTR-99214710' },
          { label: 'Institution Name', value: 'Apex Institute of Technology, Pune' },
          { label: 'TER Fee Amount', value: 'INR 3,00,000 (Extension of Approval Fee)' },
          { label: 'Payment Timestamp', value: '2026-02-04 14:22:18 IST' }
        ],
        tamperingChecks: [
          { check: 'Central Treasury Gateway Reconciliation', status: 'Pass', detail: '0 Detected: Transaction UTR Verified via Bharatkosh API' },
          { check: 'Bank Challan Digital Stamp', status: 'Pass', detail: 'SBI Payment Gateway digital cryptographic seal verified' },
          { check: 'Duplicate Transaction Check', status: 'Pass', detail: 'Unique UTR receipt across all national applications' }
        ],
        authorityStatus: {
          source: 'Bharatkosh AICTE Treasury Gateway API',
          refId: 'BHARATKOSH-UTR-99214710',
          outcome: 'Verified',
          message: 'Treasury transaction confirmed settled in AICTE Consolidated Regulatory Account. Payment valid.'
        }
      }
    };
    window.sampleDocumentsData = sampleDocumentsData;

    """
            html = html[:data_start] + new_sample_data + html[data_end:]
            print("Updated sampleDocumentsData to 6 documents.")

    # 4. Update selectSampleDoc & handleStandaloneFileUpload
    html = html.replace("async async function selectSampleDoc(sampleKey) {", "async function selectSampleDoc(sampleKey) {")
    sel_start = html.find("async function selectSampleDoc(sampleKey) {")
    if sel_start == -1:
        sel_start = html.find("function selectSampleDoc(sampleKey) {")
    if sel_start != -1:
        sel_end = html.find("function renderDocumentCheckerCards(doc) {", sel_start)
        if sel_end != -1:
            new_select_fn = """async function selectSampleDoc(sampleKey) {
      currentSampleDocKey = sampleKey;
      for (let i = 1; i <= 6; i++) {
        const btn = document.getElementById('sampleDocBtn-SAMPLE-' + i);
        if (btn) btn.classList.toggle('active', ('SAMPLE-' + i) === sampleKey);
      }
      const doc = sampleDocumentsData[sampleKey];
      if (!doc) return;
      renderDocumentCheckerCards(doc);
      logAuditEvent(activeUserSession?.name || 'Document Checker', doc.fileName, 'Sample Document Verified', `Verified ${doc.fileName} via ${doc.authorityStatus?.source || 'Issuing Gateway'} (${doc.overallStatus})`);
      
      if (window.cipherBlockchain) {
        await cipherBlockchain.addBlock(doc.fileName, 'DOCUMENT_VERIFIED', activeUserSession?.name || 'AICTE_VERIFY_GATEWAY', {
          fileName: doc.fileName,
          fileSize: doc.fileSize,
          sha256Fingerprint: doc.sha256,
          overallStatus: doc.overallStatus,
          issuingGateway: doc.authorityStatus?.source,
          verificationOutcome: doc.authorityStatus?.outcome,
          tamperFlagsCount: doc.tamperingChecks.filter(c => c.status !== 'Pass').length
        });
        renderBlockchainLedgerUI();
      }
      showToastBanner(`Inspected & notarized on blockchain: ${doc.fileName}`);
    }
    window.selectSampleDoc = selectSampleDoc;

    async function handleStandaloneFileUpload(event) {
      const file = event.target.files?.[0];
      if (!file) return;

      const sizeKb = (file.size / 1024).toFixed(1) + ' KB';
      let computedHash = 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855';
      try {
        const arrayBuffer = await file.arrayBuffer();
        if (window.crypto && window.crypto.subtle) {
          const hashBuf = await window.crypto.subtle.digest('SHA-256', arrayBuffer);
          computedHash = Array.from(new Uint8Array(hashBuf)).map(b => b.toString(16).padStart(2, '0')).join('');
        }
      } catch (e) {}

      const customDoc = {
        fileName: file.name,
        fileSize: sizeKb,
        sha256: computedHash,
        overallStatus: 'Valid',
        ocrFields: [
          { label: 'Document Category', value: 'Uploaded Regulatory Certificate' },
          { label: 'Reference Number', value: 'DOC-2026-' + Math.floor(1000 + Math.random() * 8999) },
          { label: 'File Name', value: file.name },
          { label: 'Issue Date', value: '2026-01-15' },
          { label: 'Expiry Date', value: '2028-01-14' }
        ],
        tamperingChecks: [
          { check: 'PDF Producer & Metadata Integrity', status: 'Pass', detail: 'Standard PDF header and creation metadata verified' },
          { check: 'Signature & Layer Inspection', status: 'Pass', detail: 'No post-issuance raster overlay detected' },
          { check: 'Duplicate File Hash Check', status: 'Pass', detail: 'Unique SHA-256 file fingerprint' }
        ],
        authorityStatus: {
          source: 'Official Issuing Authority Gateway',
          refId: 'VERIFIED-UPLOAD',
          outcome: 'Verified',
          message: `Uploaded file ${file.name} (${sizeKb}) passed metadata and registry verification.`
        }
      };

      for (let i = 1; i <= 6; i++) {
        const btn = document.getElementById('sampleDocBtn-SAMPLE-' + i);
        if (btn) btn.classList.remove('active');
      }

      renderDocumentCheckerCards(customDoc);
      logAuditEvent(activeUserSession?.name || 'Document Checker', file.name, 'Single File Checked', `Checked ${file.name} (${sizeKb}) with SHA-256: ${computedHash.substring(0, 16)}...`);
      
      if (window.cipherBlockchain) {
        await cipherBlockchain.addBlock(file.name, 'DOCUMENT_VERIFIED', activeUserSession?.name || 'AICTE_VERIFY_GATEWAY', {
          fileName: file.name,
          fileSize: sizeKb,
          sha256Fingerprint: computedHash,
          overallStatus: 'Valid',
          issuingGateway: 'VERIFIED-GATEWAY',
          verificationTimestamp: new Date().toISOString()
        });
        renderBlockchainLedgerUI();
      }

      showToastBanner(`Inspected & notarized on blockchain: ${file.name}`);
    }

    """
            html = html[:sel_start] + new_select_fn + html[sel_end:]
            print("Updated selectSampleDoc and handleStandaloneFileUpload.")

    # 5. Update Inspect Application Sub-tab 2 and Sub-tab 4 button onclicks to use window.openForensicDiffModal & window.openCvEvidenceModal
    html = html.replace('onclick="openForensicDiffModal()"', 'onclick="window.openForensicDiffModal(\'${app.id}\', \'Land Ownership Deed\')"')
    html = html.replace('onclick="openCvInspectionModal()"', 'onclick="window.openCvEvidenceModal(\'${app.id}\', \'Computer Center / Lab\')"')

    # 6. Update CV Evidence Modal & Forensic Diff Modal JavaScript engine
    cv_engine_marker = "// MODULE 1: INTERACTIVE COMPUTER VISION (CV) CANVAS & MODAL ENGINE"
    cv_start = html.find(cv_engine_marker)
    if cv_start != -1:
        # Find where Module 4 ends / exportScrutinyReportJson ends
        report_end = html.find("function showNodeConsensusToast", cv_start)
        if report_end == -1:
            report_end = html.find("window.addEventListener('DOMContentLoaded'", cv_start)
        
        # Let's see the slice to replace: from cv_start to right before printAicteScrutinyReport or exportScrutinyReportJson
        js_replace_target = html.find("// MODULE 1: INTERACTIVE COMPUTER VISION", cv_start)
        js_replace_end = html.find("function printAicteScrutinyReport", cv_start)
        
        new_cv_forensic_js = """// MODULE 1: INTERACTIVE COMPUTER VISION (CV) CANVAS & MODAL ENGINE
    // =====================================================================
    let cvBoundingBoxesVisible = true;
    let cvHeatmapVisible = false;
    let cvGeofenceVisible = true;

    // Bulletproof Window Modal Controllers
    window.openCvEvidenceModal = function(appId, facilityType) {
      const modal = document.getElementById('cvEvidenceModal') || document.getElementById('cvInspectionModal');
      if (!modal) return;
      modal.style.display = 'flex';
      modal.classList.add('active');
      modal.classList.remove('hidden');
      document.body.style.overflow = 'hidden';
      setTimeout(drawCvCanvas, 60);
    };

    window.closeCvEvidenceModal = function() {
      const modal = document.getElementById('cvEvidenceModal') || document.getElementById('cvInspectionModal');
      if (!modal) return;
      modal.style.display = 'none';
      modal.classList.remove('active');
      modal.classList.add('hidden');
      document.body.style.overflow = '';
    };

    window.openForensicDiffModal = function(appId, docType) {
      const modal = document.getElementById('forensicDiffModal');
      if (!modal) return;
      modal.style.display = 'flex';
      modal.classList.add('active');
      modal.classList.remove('hidden');
      document.body.style.overflow = 'hidden';
    };

    window.closeForensicDiffModal = function() {
      const modal = document.getElementById('forensicDiffModal');
      if (!modal) return;
      modal.style.display = 'none';
      modal.classList.remove('active');
      modal.classList.add('hidden');
      document.body.style.overflow = '';
    };

    window.openReportModal = function(appId) {
      const modal = document.getElementById('aicteScrutinyReportModal') || document.getElementById('reportModal');
      if (!modal) return;
      modal.style.display = 'flex';
      modal.classList.add('active');
      modal.classList.remove('hidden');
      document.body.style.overflow = 'hidden';
    };

    window.closeReportModal = function() {
      const modal = document.getElementById('aicteScrutinyReportModal') || document.getElementById('reportModal');
      if (!modal) return;
      modal.style.display = 'none';
      modal.classList.remove('active');
      modal.classList.add('hidden');
      document.body.style.overflow = '';
    };

    // Backward-compatibility aliases
    window.openCvInspectionModal = window.openCvEvidenceModal;
    window.closeCvInspectionModal = window.closeCvEvidenceModal;
    window.openAicteScrutinyReportModal = window.openReportModal;
    window.closeAicteScrutinyReportModal = window.closeReportModal;

    window.toggleCvBoundingBoxes = function(val) {
      cvBoundingBoxesVisible = (typeof val === 'boolean') ? val : !cvBoundingBoxesVisible;
      const chk = document.getElementById('cvBoxesCheckbox');
      if (chk) chk.checked = cvBoundingBoxesVisible;
      drawCvCanvas();
      showToastBanner(`YOLOv8 Bounding Boxes: ${cvBoundingBoxesVisible ? 'ON' : 'OFF'}`);
    };

    window.toggleCvHeatmap = function(val) {
      cvHeatmapVisible = (typeof val === 'boolean') ? val : !cvHeatmapVisible;
      const chk = document.getElementById('cvHeatmapCheckbox');
      if (chk) chk.checked = cvHeatmapVisible;
      drawCvCanvas();
      showToastBanner(`Terminal Heatmap Density: ${cvHeatmapVisible ? 'ON' : 'OFF'}`);
    };

    window.toggleCvGeofence = function(val) {
      cvGeofenceVisible = (typeof val === 'boolean') ? val : !cvGeofenceVisible;
      const chk = document.getElementById('cvGeofenceCheckbox');
      if (chk) chk.checked = cvGeofenceVisible;
      drawCvCanvas();
      showToastBanner(`Geofence Overlay: ${cvGeofenceVisible ? 'ON' : 'OFF'}`);
    };

    function drawCvCanvas() {
      const canvas = document.getElementById('cvInspectionCanvas');
      if (!canvas) return;
      const ctx = canvas.getContext('2d');
      const w = canvas.width;
      const h = canvas.height;

      // 1. Draw Lab Background (Flooring & Perspective Room)
      ctx.fillStyle = '#0f172a';
      ctx.fillRect(0, 0, w, h);

      // Floor tiles
      ctx.strokeStyle = '#1e293b';
      ctx.lineWidth = 1;
      for (let y = 140; y < h; y += 32) {
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(w, y);
        ctx.stroke();
      }
      for (let x = 0; x < w; x += 48) {
        ctx.beginPath();
        ctx.moveTo(x, 140);
        ctx.lineTo(x * 1.15 - 50, h);
        ctx.stroke();
      }

      // Walls
      ctx.fillStyle = '#1e293b';
      ctx.fillRect(0, 0, w, 140);
      ctx.strokeStyle = '#334155';
      ctx.strokeRect(0, 0, w, 140);

      // Whiteboard / Front Projector Screen
      ctx.fillStyle = '#f8fafc';
      ctx.fillRect(180, 20, 400, 90);
      ctx.strokeStyle = '#64748b';
      ctx.lineWidth = 2;
      ctx.strokeRect(180, 20, 400, 90);
      ctx.fillStyle = '#0284c7';
      ctx.font = 'bold 12px monospace';
      ctx.fillText('AICTE CCF LAB-03 | 180 INTAKE B.TECH CSE', 205, 55);
      ctx.fillStyle = '#64748b';
      ctx.font = '10px monospace';
      ctx.fillText('REQUIRED TERMINALS: 150 | DETECTED UNITS: 142', 205, 75);

      // Rear Doorway (Left Wall)
      ctx.fillStyle = '#090d16';
      ctx.fillRect(24, 25, 60, 115);
      ctx.strokeStyle = '#475569';
      ctx.lineWidth = 2;
      ctx.strokeRect(24, 25, 60, 115);

      // Fire Extinguisher on Right Wall
      ctx.fillStyle = '#dc2626';
      ctx.fillRect(w - 55, 45, 20, 50);
      ctx.fillStyle = '#94a3b8';
      ctx.fillRect(w - 48, 38, 6, 8);
      ctx.fillRect(w - 58, 48, 5, 20);

      // Second Fire Extinguisher near front entrance
      ctx.fillStyle = '#dc2626';
      ctx.fillRect(95, 70, 16, 40);
      ctx.fillStyle = '#94a3b8';
      ctx.fillRect(100, 64, 5, 6);

      // Draw 3 Rows of Computer Benches
      const rows = [
        { y: 170, count: 6, spacing: 105, startX: 70, w: 75, h: 50 },
        { y: 250, count: 6, spacing: 110, startX: 60, w: 82, h: 55 },
        { y: 340, count: 6, spacing: 115, startX: 50, w: 90, h: 60 }
      ];

      rows.forEach(r => {
        for (let i = 0; i < r.count; i++) {
          const bx = r.startX + i * r.spacing;
          const by = r.y;

          // Desk
          ctx.fillStyle = '#334155';
          ctx.fillRect(bx - 5, by + 20, r.w + 10, r.h - 15);
          ctx.strokeStyle = '#475569';
          ctx.strokeRect(bx - 5, by + 20, r.w + 10, r.h - 15);

          // Monitor
          const isEmptyBay = (r.y === 250 && (i === 2 || i === 4)) || (r.y === 340 && i === 3);
          if (!isEmptyBay) {
            ctx.fillStyle = '#0f172a';
            ctx.fillRect(bx + 8, by - 6, r.w - 16, r.h - 14);
            ctx.strokeStyle = '#0284c7';
            ctx.lineWidth = 1.5;
            ctx.strokeRect(bx + 8, by - 6, r.w - 16, r.h - 14);

            // Screen glow
            ctx.fillStyle = '#0369a1';
            ctx.fillRect(bx + 11, by - 3, r.w - 22, r.h - 20);

            // Stand
            ctx.fillStyle = '#64748b';
            ctx.fillRect(bx + r.w / 2 - 3, by + r.h - 20, 6, 12);
            ctx.fillRect(bx + r.w / 2 - 12, by + r.h - 8, 24, 3);
          } else {
            // Vacant desk mark
            ctx.fillStyle = '#475569';
            ctx.font = '9px monospace';
            ctx.fillText('VACANT', bx + 16, by + 14);
          }

          // Chair
          ctx.fillStyle = '#1e293b';
          ctx.beginPath();
          ctx.arc(bx + r.w / 2, by + r.h + 8, 11, 0, Math.PI * 2);
          ctx.fill();
          ctx.strokeStyle = '#475569';
          ctx.stroke();
        }
      });

      // 2. Heatmap Density Layer
      if (cvHeatmapVisible) {
        rows.forEach(r => {
          for (let i = 0; i < r.count; i++) {
            const bx = r.startX + i * r.spacing + r.w / 2;
            const by = r.y + 15;
            const grad = ctx.createRadialGradient(bx, by, 5, bx, by, 48);
            grad.addColorStop(0, 'rgba(16, 185, 129, 0.45)');
            grad.addColorStop(0.5, 'rgba(6, 182, 212, 0.3)');
            grad.addColorStop(1, 'rgba(15, 23, 42, 0)');
            ctx.fillStyle = grad;
            ctx.beginPath();
            ctx.arc(bx, by, 48, 0, Math.PI * 2);
            ctx.fill();
          }
        });
      }

      // 3. YOLOv8 Object Detection Bounding Boxes
      if (cvBoundingBoxesVisible) {
        rows.forEach(r => {
          for (let i = 0; i < r.count; i++) {
            const bx = r.startX + i * r.spacing;
            const by = r.y;
            const isEmptyBay = (r.y === 250 && (i === 2 || i === 4)) || (r.y === 340 && i === 3);

            if (!isEmptyBay) {
              // 142 Green Boxes: Terminal Detected
              ctx.strokeStyle = '#10b981';
              ctx.lineWidth = 1.8;
              ctx.setLineDash([]);
              ctx.strokeRect(bx + 6, by - 10, r.w - 12, r.h + 16);

              ctx.fillStyle = '#10b981';
              ctx.fillRect(bx + 6, by - 22, 74, 12);
              ctx.fillStyle = '#ffffff';
              ctx.font = 'bold 9px monospace';
              ctx.fillText('terminal: 0.94', bx + 8, by - 13);
            } else {
              // 8 Dashed Red Boxes: Unoccupied Bay Shortfall
              ctx.strokeStyle = '#ef4444';
              ctx.lineWidth = 2;
              ctx.setLineDash([5, 4]);
              ctx.strokeRect(bx + 4, by - 12, r.w - 8, r.h + 20);
              ctx.setLineDash([]);

              ctx.fillStyle = '#ef4444';
              ctx.fillRect(bx + 4, by - 24, 102, 12);
              ctx.fillStyle = '#ffffff';
              ctx.font = 'bold 9px monospace';
              ctx.fillText('unoccupied_bay: 0.89', bx + 6, by - 15);
            }
          }
        });

        // 2 Blue Boxes: Fire Extinguishers
        // Right wall
        ctx.strokeStyle = '#0284c7';
        ctx.lineWidth = 2;
        ctx.setLineDash([]);
        ctx.strokeRect(w - 62, 34, 32, 68);
        ctx.fillStyle = '#0284c7';
        ctx.fillRect(w - 62, 22, 150, 12);
        ctx.fillStyle = '#ffffff';
        ctx.font = 'bold 9px monospace';
        ctx.fillText('fire_extinguisher_type_abc: 0.97', w - 60, 31);

        // Entrance
        ctx.strokeStyle = '#0284c7';
        ctx.lineWidth = 2;
        ctx.strokeRect(90, 58, 26, 56);
        ctx.fillStyle = '#0284c7';
        ctx.fillRect(90, 46, 150, 12);
        ctx.fillStyle = '#ffffff';
        ctx.font = 'bold 9px monospace';
        ctx.fillText('fire_extinguisher_type_abc: 0.96', 92, 55);

        // 1 Dashed Amber Box: Missing Exit Luminaire (above doorway)
        ctx.strokeStyle = '#f59e0b';
        ctx.lineWidth = 2;
        ctx.setLineDash([5, 4]);
        ctx.strokeRect(20, 8, 68, 24);
        ctx.setLineDash([]);
        ctx.fillStyle = '#f59e0b';
        ctx.fillRect(20, -2, 136, 12);
        ctx.fillStyle = '#000000';
        ctx.font = 'bold 9px monospace';
        ctx.fillText('missing_exit_luminaire: 0.91', 22, 7);
      }

      // 4. Geofence Overlay HUD
      if (cvGeofenceVisible) {
        ctx.fillStyle = 'rgba(15, 23, 42, 0.85)';
        ctx.fillRect(10, h - 34, w - 20, 24);
        ctx.strokeStyle = '#0284c7';
        ctx.lineWidth = 1;
        ctx.strokeRect(10, h - 34, w - 20, 24);

        ctx.fillStyle = '#10b981';
        ctx.beginPath();
        ctx.arc(22, h - 22, 4, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = '#f8fafc';
        ctx.font = 'bold 10px monospace';
        ctx.fillText('GEOFENCE LOCKED: 18.5204 N, 73.8567 E | Apex Institute Registered Polygon Match | Accuracy: ±1.2m', 32, h - 18);
      }
    }

    window.appendCvFindingToDossier = async function() {
      logAuditEvent(activeUserSession?.name || 'CV Scrutiny Officer', 'APP-2026-101', 'CV Finding Notarized', 'Shortfall (-8 workstations) and missing exit luminaire attached to official dossier.');
      if (window.cipherBlockchain) {
        await cipherBlockchain.addBlock('APP-2026-101', 'ANOMALY_FLAGGED', activeUserSession?.name || 'CV_SCRUTINY_ENGINE', {
          target: 'Lab 03 (Central Computing Facility)',
          finding: 'Workstation Shortfall (-8 units) & Missing Exit Luminaire',
          detectedWorkstations: 142,
          prescribedNorm: 150,
          geotag: '18.5204 N, 73.8567 E',
          inferenceConfidence: 'YOLOv8x (94.2% mean confidence)'
        });
        renderBlockchainLedgerUI();
      }
      showToastBanner('Attached CV finding to official scrutiny dossier & notarized on blockchain.');
      window.closeCvEvidenceModal();
    };
    window.attachCvFindingToDossier = window.appendCvFindingToDossier;

    // =====================================================================
    // MODULE 2: SIDE-BY-SIDE FORENSIC DOCUMENT DIFF VIEWER
    // =====================================================================
    window.attachForensicProofToDossier = async function() {
      logAuditEvent(activeUserSession?.name || 'Forensic Officer', 'APP-2026-101', 'Forensic Proof Attached', 'Land Deed font overlay and acreage gap (5.20 vs 2.10 Acres) notarized as regulatory deficiency.');
      if (window.cipherBlockchain) {
        await cipherBlockchain.addBlock('APP-2026-101', 'ANOMALY_FLAGGED', activeUserSession?.name || 'FORENSIC_SCRUTINY_OFFICER', {
          documentId: 'DEED-REV-MH-4419',
          finding: 'Land Deed Alteration: Survey 42/B (5.20 Acres) vs Official 42/A (2.10 Acres)',
          elaScore: '88.4% digital raster modification',
          producerAnomaly: 'Adobe Acrobat Pro v24.2',
          regulatoryDeficiency: '-3.10 Contiguous Acres'
        });
        renderBlockchainLedgerUI();
      }
      showToastBanner('Attached Forensic Land Deed proof to Official Dossier & notarized on blockchain.');
      window.closeForensicDiffModal();
    };

    """
        html = html[:js_replace_target] + new_cv_forensic_js + html[js_replace_end:]
        print("Updated CV and Forensic Diff JS engine.")

    # 7. Update Modal HTML containers
    # Find cvInspectionModal and replace with new cvEvidenceModal and forensicDiffModal
    cv_html_start = html.find('<!-- MODULE 1: INTERACTIVE COMPUTER VISION (CV) LAB & INFRASTRUCTURE MODAL')
    if cv_html_start == -1:
        cv_html_start = html.find('<div id="cvInspectionModal"')
    if cv_html_start != -1:
        rep_html_start = html.find('<!-- MODULE 4: 1-CLICK OFFICIAL AICTE SCRUTINY DEFICIENCY REPORT MODAL', cv_html_start)
        if rep_html_start == -1:
            rep_html_start = html.find('<div id="aicteScrutinyReportModal"', cv_html_start)
        
        if rep_html_start != -1:
            new_modals_html = """<!-- =====================================================================
       MODULE 1: INTERACTIVE COMPUTER VISION (CV) LAB & INFRASTRUCTURE MODAL
       ===================================================================== -->
  <div id="cvEvidenceModal" class="modal-backdrop fixed inset-0 z-50 bg-black/60 flex items-center justify-center p-3 sm:p-4" style="display:none;" onclick="if(event.target === this) window.closeCvEvidenceModal()">
    <div class="card-surface max-w-5xl w-full p-5 space-y-4 shadow-2xl border border-line rounded flex flex-col max-h-[92vh]" onclick="event.stopPropagation()">
      <!-- Header -->
      <div class="flex items-center justify-between border-b border-line pb-3">
        <div>
          <div class="flex items-center gap-2">
            <span class="px-2 py-0.5 rounded text-[10px] font-bold font-mono bg-[#0284c7]/10 text-[#0284c7] border border-[#0284c7]/30">YOLOv8 Edge CV</span>
            <h3 class="text-sm font-bold text-main">AI Computer Vision Infrastructure Audit | APH 2024-27 Norm Validation</h3>
          </div>
          <div class="flex items-center gap-2 mt-1">
            <span class="tag-neutral text-[10px] font-semibold">Apex Institute of Technology (AP-2026-101) - Lab 03 (Computing Facility)</span>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <button onclick="window.closeCvEvidenceModal()" class="btn-secondary px-2.5 py-1 text-xs" style="border-radius:4px;">Close</button>
          <button onclick="window.closeCvEvidenceModal()" class="text-sub hover:text-main p-1 rounded" title="Close Modal">
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>

      <!-- Top Metadata Bar -->
      <div class="p-2.5 rounded subtle-surface border border-line text-xs grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-2 font-mono">
        <div>
          <span class="text-sub block text-[10px] uppercase font-semibold">Target Facility:</span>
          <span class="text-main font-bold">Apex IT - Lab 3 (Computing Facility)</span>
        </div>
        <div>
          <span class="text-sub block text-[10px] uppercase font-semibold">Sensor / Resolution:</span>
          <span class="text-main">1920x1080 | IMX686</span>
        </div>
        <div>
          <span class="text-sub block text-[10px] uppercase font-semibold">EXIF Timestamp:</span>
          <span class="text-main">2026-08-14 11:24 IST</span>
        </div>
        <div>
          <span class="text-sub block text-[10px] uppercase font-semibold">Geotag Verification:</span>
          <span class="text-emerald-600 font-bold">18.5204 N, 73.8567 E (Pass)</span>
        </div>
      </div>

      <!-- Main Body: Viewport & Inference Drawer -->
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-4 flex-1 overflow-y-auto">
        <!-- Left: Interactive Canvas Viewport (65% / 8 Cols) -->
        <div class="lg:col-span-8 flex flex-col space-y-2">
          <div class="relative w-full rounded border border-slate-700 overflow-hidden bg-slate-900 shadow-inner flex items-center justify-center">
            <canvas id="cvInspectionCanvas" width="760" height="440" class="w-full h-auto block"></canvas>
            
            <!-- Live Model Pill -->
            <div class="absolute top-2.5 left-2.5 px-2 py-0.5 rounded bg-black/75 text-emerald-400 font-mono text-[10px] border border-emerald-500/40 flex items-center gap-1.5">
              <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
              <span>LIVE INFERENCE: YOLOv8x-SIH26261 (18.4ms)</span>
            </div>
          </div>

          <!-- Canvas Viewport Controls: 3 Checkbox Toggles -->
          <div class="flex flex-wrap items-center justify-between gap-3 p-2.5 rounded subtle-surface border border-line text-xs">
            <div class="flex flex-wrap items-center gap-3">
              <label class="inline-flex items-center gap-1.5 cursor-pointer font-medium text-main text-xs">
                <input type="checkbox" id="cvBoxesCheckbox" checked onchange="toggleCvBoundingBoxes(this.checked)" class="rounded text-[#0284c7]">
                <span>Show Bounding Boxes (YOLOv8)</span>
              </label>
              <label class="inline-flex items-center gap-1.5 cursor-pointer font-medium text-main text-xs">
                <input type="checkbox" id="cvHeatmapCheckbox" onchange="toggleCvHeatmap(this.checked)" class="rounded text-[#0284c7]">
                <span>Show Terminal Heatmap</span>
              </label>
              <label class="inline-flex items-center gap-1.5 cursor-pointer font-medium text-main text-xs">
                <input type="checkbox" id="cvGeofenceCheckbox" checked onchange="toggleCvGeofence(this.checked)" class="rounded text-[#0284c7]">
                <span>Overlay Geofence Coordinates (18.5204 N, 73.8567 E)</span>
              </label>
            </div>
            <div class="text-[11px] font-mono text-sub">
              Confidence: <span class="font-bold text-main">&gt;= 0.85</span>
            </div>
          </div>
        </div>

        <!-- Right: CV Inference Analytics Drawer (35% / 4 Cols) -->
        <div class="lg:col-span-4 space-y-3 text-xs">
          <!-- Workstation Tally -->
          <div class="p-3 rounded subtle-surface border border-line space-y-1.5">
            <div class="flex items-center justify-between">
              <span class="text-sub font-semibold">Workstation Tally:</span>
              <span class="tag-red font-bold">DEFICIENT (-8 Units Required for 180 Intake)</span>
            </div>
            <div class="text-lg font-mono font-bold text-main">
              142 Terminals <span class="text-xs text-sub font-normal">/ 150 AICTE APH Prescribed</span>
            </div>
            <div class="w-full bg-slate-200 h-1.5 rounded overflow-hidden">
              <div class="bg-amber-500 h-full" style="width: 94.6%;"></div>
            </div>
            <div class="text-[11px] text-sub">
              AICTE APH Norm prescribes 150 active terminals for 180 B.Tech CSE intake. Detected shortfall of 8 units.
            </div>
          </div>

          <!-- Carpet Area Density -->
          <div class="p-3 rounded subtle-surface border border-line space-y-1">
            <div class="flex items-center justify-between">
              <span class="text-sub font-semibold">Carpet Area Density:</span>
              <span class="tag-green font-bold">COMPLIANT</span>
            </div>
            <div class="text-sm font-mono font-bold text-main">
              Measured: 1.25 sq.m per terminal
            </div>
            <div class="text-[11px] text-sub">
              Meets AICTE Norm (&gt;= 1.20 sq.m per workstation). Adequate circulation aisle spacing verified.
            </div>
          </div>

          <!-- Life Safety & Accessibility -->
          <div class="p-3 rounded subtle-surface border border-line space-y-1.5">
            <div class="flex items-center justify-between">
              <span class="text-sub font-semibold">Life Safety &amp; Accessibility:</span>
              <span class="tag-amber font-bold">Action Required</span>
            </div>
            <ul class="text-[11px] space-y-1.5 text-main">
              <li class="flex items-center gap-1.5 text-emerald-700">
                <svg class="w-3.5 h-3.5 text-emerald-600 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/></svg>
                <span>Fire Extinguishers: 2 Detected (Compliant)</span>
              </li>
              <li class="flex items-center gap-1.5 text-red-700">
                <svg class="w-3.5 h-3.5 text-red-600 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/></svg>
                <span>Emergency Illumination: Missing (Non-Compliant)</span>
              </li>
            </ul>
          </div>

          <!-- Action Button -->
          <button onclick="appendCvFindingToDossier()" class="btn-primary w-full py-2.5 text-xs flex items-center justify-center gap-1.5" style="border-radius:4px;">
            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
            </svg>
            <span>Append CV Finding to Official Scrutiny Dossier</span>
          </button>
        </div>
      </div>

      <!-- Footer -->
      <div class="flex items-center justify-between pt-2 border-t border-line text-xs">
        <span class="text-[11px] text-sub font-mono">SIH Problem ID: 26261 | AICTE Regulatory Decision-Support</span>
        <button onclick="window.closeCvEvidenceModal()" class="btn-secondary text-xs" style="border-radius:4px;">Close Inspection</button>
      </div>
    </div>
  </div>

  <!-- =====================================================================
       MODULE 2: SIDE-BY-SIDE FORENSIC DOCUMENT DIFF MODAL
       ===================================================================== -->
  <div id="forensicDiffModal" class="modal-backdrop fixed inset-0 z-50 bg-black/60 flex items-center justify-center p-3 sm:p-4" style="display:none;" onclick="if(event.target === this) window.closeForensicDiffModal()">
    <div class="card-surface max-w-5xl w-full p-5 space-y-4 shadow-2xl border border-line rounded flex flex-col max-h-[92vh]" onclick="event.stopPropagation()">
      <!-- Header -->
      <div class="flex items-center justify-between border-b border-line pb-3">
        <div>
          <div class="flex items-center gap-2">
            <span class="px-2 py-0.5 rounded text-[10px] font-bold font-mono bg-red-100 text-red-800 border border-red-300">Forensic Tamper Detection</span>
            <h3 class="text-sm font-bold text-main">Cryptographic &amp; Forensic Document Diff Viewer</h3>
          </div>
          <p class="text-[11px] text-sub mt-0.5">Document Comparison: Institution Submitted Upload vs. DigiLocker State Land Registry Record</p>
        </div>
        <div class="flex items-center gap-2">
          <button onclick="window.closeForensicDiffModal()" class="btn-secondary px-2.5 py-1 text-xs" style="border-radius:4px;">Close</button>
          <button onclick="window.closeForensicDiffModal()" class="text-sub hover:text-main p-1 rounded" title="Close Modal">
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>

      <!-- Split-Screen Comparison Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 flex-1 overflow-y-auto">
        <!-- Column A (Left: Institution Submitted PDF - Land Deed) -->
        <div class="p-4 rounded border-2 border-red-400 bg-red-50/20 space-y-3">
          <div class="flex items-center justify-between border-b border-red-200 pb-2">
            <div>
              <span class="tag-red font-bold text-[10px]">Institution Submitted PDF - Land Deed</span>
              <div class="font-mono text-[11px] text-main font-semibold mt-0.5">Land Sale Deed No. 1849/2018</div>
            </div>
            <span class="px-2 py-0.5 rounded text-[10px] font-mono bg-red-100 text-red-800 border border-red-300 font-bold">Altered Layer</span>
          </div>

          <!-- Document Snippet Mock -->
          <div class="p-3 bg-white rounded border border-line text-xs font-serif space-y-2 text-slate-800 shadow-xs">
            <div class="text-center border-b border-line pb-1.5">
              <div class="font-bold text-[11px] uppercase tracking-wider text-slate-900">Government of Maharashtra - Revenue Dept</div>
              <div class="text-[10px] text-slate-500 font-sans">Index-II Registered Property Card Sale Deed No. 1849/2018</div>
            </div>

            <div class="space-y-1.5 text-[11px]">
              <div><span class="text-slate-500 font-sans">Village / Taluka:</span> Pashan / Haveli, Pune</div>
              <div><span class="text-slate-500 font-sans">Registered Holder:</span> Apex Educational Trust, Pune</div>
              
              <!-- Tampered Area Highlight -->
              <div class="p-2.5 rounded border-2 border-red-500 bg-red-50 space-y-1">
                <div class="text-[10px] font-mono text-red-700 font-bold uppercase">Altered Text Stream (Post-Issuance):</div>
                <div class="font-mono font-bold text-red-900 text-xs">
                  Survey No: 42/B (Contiguous Land Area: 5.20 Acres)
                </div>
                <div class="text-[10px] text-red-700 font-sans">
                  Callout Box: Altered Layer Detected | Error Level Analysis (ELA) score: 88.4% (Post-scan digital raster modification detected)
                </div>
              </div>

              <div><span class="text-slate-500 font-sans">Stamp Duty Paid:</span> Rs. 4,50,000 (Challan #8812)</div>
            </div>
          </div>

          <!-- Hash Pill -->
          <div class="p-2 rounded subtle-surface border border-line text-[11px] font-mono space-y-0.5">
            <span class="text-sub block text-[10px] uppercase font-semibold">Submitted File SHA-256 Fingerprint:</span>
            <span class="text-red-700 font-bold break-all">7f8c12e492b415a77002dc1490214c718290ba419082ca71829012a4198231b8</span>
          </div>
        </div>

        <!-- Column B (Right: DigiLocker / State Revenue Dept Record) -->
        <div class="p-4 rounded border-2 border-emerald-400 bg-emerald-50/20 space-y-3">
          <div class="flex items-center justify-between border-b border-emerald-200 pb-2">
            <div>
              <span class="tag-green font-bold text-[10px]">DigiLocker / State Revenue Dept Record</span>
              <div class="font-mono text-[11px] text-main font-semibold mt-0.5">DL-REV-MH-4419 (Live Government API)</div>
            </div>
            <span class="px-2 py-0.5 rounded text-[10px] font-mono bg-emerald-100 text-emerald-800 border border-emerald-300 font-bold">Authentic Source</span>
          </div>

          <!-- Document Snippet Mock -->
          <div class="p-3 bg-white rounded border border-line text-xs font-serif space-y-2 text-slate-800 shadow-xs">
            <div class="text-center border-b border-line pb-1.5">
              <div class="font-bold text-[11px] uppercase tracking-wider text-slate-900">Government of Maharashtra - Revenue Dept</div>
              <div class="text-[10px] text-slate-500 font-sans">Authentic Government Property Card (Mahabhulekh 7/12 Extract)</div>
            </div>

            <div class="space-y-1.5 text-[11px]">
              <div><span class="text-slate-500 font-sans">Village / Taluka:</span> Pashan / Haveli, Pune</div>
              <div><span class="text-slate-500 font-sans">Registered Holder:</span> Apex Educational Trust, Pune</div>

              <!-- Authentic Area Highlight -->
              <div class="p-2.5 rounded border-2 border-emerald-500 bg-emerald-50 space-y-1">
                <div class="text-[10px] font-mono text-emerald-700 font-bold uppercase">Official Verified Registry Extract:</div>
                <div class="font-mono font-bold text-emerald-900 text-xs">
                  Survey No: 42/A (Contiguous Land Area: 2.10 Acres)
                </div>
                <div class="text-[10px] text-emerald-700 font-sans">
                  Callout Box: Authentic Record Verified via Maharashtra Land Registry API | Hash: 3e8b...910a
                </div>
              </div>

              <div><span class="text-slate-500 font-sans">Stamp Duty Paid:</span> Rs. 4,50,000 (Challan #8812)</div>
            </div>
          </div>

          <!-- Hash Pill -->
          <div class="p-2 rounded subtle-surface border border-line text-[11px] font-mono space-y-0.5">
            <span class="text-sub block text-[10px] uppercase font-semibold">DigiLocker Root SHA-256 Fingerprint:</span>
            <span class="text-emerald-700 font-bold break-all">3a9e21049281a8c7b6102dc1490214c718290ba419082ca71829012a41988b42</span>
          </div>
        </div>
      </div>

      <!-- Bottom Forensic Metadata Comparison Table -->
      <div class="overflow-x-auto rounded border border-line">
        <table class="w-full text-left border-collapse text-xs">
          <thead>
            <tr class="subtle-surface border-b border-line text-sub font-mono text-[11px] uppercase">
              <th class="py-2.5 px-3">Parameter</th>
              <th class="py-2.5 px-3">Submitted Document</th>
              <th class="py-2.5 px-3">Official Registry Source</th>
              <th class="py-2.5 px-3">Status</th>
            </tr>
          </thead>
          <tbody class="divide-y border-line text-main">
            <tr>
              <td class="py-2.5 px-3 font-semibold">Font Stream</td>
              <td class="py-2.5 px-3 font-mono text-red-600">Helvetica-Bold (Digital Overlay)</td>
              <td class="py-2.5 px-3 font-mono text-emerald-600">Monospaced Typewriter Courier</td>
              <td class="py-2.5 px-3"><span class="tag-red font-bold">MISMATCH</span></td>
            </tr>
            <tr>
              <td class="py-2.5 px-3 font-semibold">Software Producer</td>
              <td class="py-2.5 px-3 font-mono text-red-600">Adobe Acrobat Pro v24.2</td>
              <td class="py-2.5 px-3 font-mono text-emerald-600">Government C-DAC Digital Stamp</td>
              <td class="py-2.5 px-3"><span class="tag-red font-bold">IRREGULAR</span></td>
            </tr>
            <tr>
              <td class="py-2.5 px-3 font-semibold">Contiguous Acreage</td>
              <td class="py-2.5 px-3 font-mono text-red-600 font-bold">5.20 Acres</td>
              <td class="py-2.5 px-3 font-mono text-emerald-600 font-bold">2.10 Acres</td>
              <td class="py-2.5 px-3"><span class="tag-red font-bold">REGULATORY DEFICIENCY (-3.10 Acres)</span></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Footer -->
      <div class="flex items-center justify-between pt-2 border-t border-line text-xs">
        <button onclick="window.attachForensicProofToDossier()" class="btn-primary flex items-center gap-1.5" style="border-radius:4px;">
          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span>Attach Forensic Proof to Dossier</span>
        </button>
        <button onclick="window.closeForensicDiffModal()" class="btn-secondary text-xs" style="border-radius:4px;">Close Inspector</button>
      </div>
    </div>
  </div>

  """
            html = html[:cv_html_start] + new_modals_html + html[rep_html_start:]
            print("Updated CV Evidence Modal and Forensic Diff Modal HTML.")

    # 8. Update Scrutiny Report Modal to have .modal-backdrop and dual safety
    html = html.replace('<div id="aicteScrutinyReportModal" class="fixed inset-0 z-50 hidden bg-black/60 flex items-center justify-center p-3 sm:p-4">', '<div id="aicteScrutinyReportModal" class="modal-backdrop fixed inset-0 z-50 bg-black/60 flex items-center justify-center p-3 sm:p-4" style="display:none;" onclick="if(event.target === this) window.closeReportModal()">')

    # Update global escape and backdrop click listeners
    click_listener = """    document.addEventListener('click', function(e) {
      if (e.target && e.target.classList && e.target.classList.contains('modal-backdrop')) {
        if (e.target.id === 'cvEvidenceModal' || e.target.id === 'cvInspectionModal') window.closeCvEvidenceModal();
        else if (e.target.id === 'forensicDiffModal') window.closeForensicDiffModal();
        else if (e.target.id === 'aicteScrutinyReportModal' || e.target.id === 'reportModal') window.closeReportModal();
        else if (e.target.id === 'officialDecisionModal') closeOfficialDecisionModal();
        else if (e.target.id === 'uploadDossierModal') closeUploadDossierModal();
        else if (e.target.id === 'authModal') closeAuthModal();
      }
    });

    document.addEventListener('keydown', function(e) {
      if (e.key === 'Escape') {
        window.closeCvEvidenceModal();
        window.closeForensicDiffModal();
        window.closeReportModal();
        closeOfficialDecisionModal();
        closeUploadDossierModal();
        closeAuthModal();
      }
    });"""

    old_keydown_marker = "window.addEventListener('keydown', (e) => {"
    old_kd_start = html.find(old_keydown_marker)
    if old_kd_start != -1:
        old_kd_end = html.find("});", old_kd_start) + 3
        html = html[:old_kd_start] + click_listener + html[old_kd_end:]
        print("Updated global click and keydown modal dismiss listeners.")

    # 9. Write updated file to frontend/index.html, index.html, and backend
    targets = [
        "frontend/index.html",
        "index.html",
        "backend/src/main/resources/static/index.html"
    ]
    for tgt in targets:
        os.makedirs(os.path.dirname(os.path.abspath(tgt)), exist_ok=True)
        with open(tgt, "w", encoding="utf-8") as f:
            f.write(html)
        print(f"Successfully wrote {tgt}")

if __name__ == "__main__":
    main()
