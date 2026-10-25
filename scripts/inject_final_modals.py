import os

target_files = [
    "frontend/index.html",
    "index.html",
    "backend/src/main/resources/static/index.html"
]

def update_file(fpath):
    with open(fpath, "r", encoding="utf-8") as f:
        html = f.read()

    # 1. Update CSS to include .modal-container and .modal-backdrop
    css_target = "    /* Modal Backdrop & Bulletproof Visibility */"
    css_end = "html.dark .modal-backdrop {\n      background-color: rgba(0, 0, 0, 0.88) !important;\n    }"
    
    new_css = """    /* Modal Backdrop & Container Architecture */
    .modal-backdrop,
    #cvEvidenceModal,
    #cvInspectionModal,
    #forensicDiffModal,
    #reportModal,
    #aicteScrutinyReportModal {
      position: fixed !important;
      top: 0 !important;
      left: 0 !important;
      right: 0 !important;
      bottom: 0 !important;
      width: 100vw !important;
      height: 100vh !important;
      z-index: 999999 !important;
      background-color: rgba(0, 0, 0, 0.75) !important;
      display: none;
      align-items: center !important;
      justify-content: center !important;
      padding: 1rem !important;
    }
    .modal-backdrop.active,
    #cvEvidenceModal.active,
    #cvInspectionModal.active,
    #forensicDiffModal.active,
    #reportModal.active,
    #aicteScrutinyReportModal.active {
      display: flex !important;
      visibility: visible !important;
      opacity: 1 !important;
    }
    .modal-backdrop:not(.active),
    #cvEvidenceModal:not(.active),
    #cvInspectionModal:not(.active),
    #forensicDiffModal:not(.active),
    #reportModal:not(.active),
    #aicteScrutinyReportModal:not(.active) {
      display: none !important;
    }
    .modal-container {
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: 6px;
      box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
      width: 100%;
      max-height: 90vh;
      display: flex;
      flex-direction: column;
      overflow-y: auto;
    }
    html.dark .modal-backdrop {
      background-color: rgba(0, 0, 0, 0.88) !important;
    }"""

    idx_css = html.find(css_target)
    if idx_css != -1:
        idx_css_e = html.find(css_end, idx_css) + len(css_end)
        html = html[:idx_css] + new_css + html[idx_css_e:]
        print("Updated CSS with .modal-container and bulletproof visibility.")

    # 2. Update JavaScript controllers to match section 2 exactly
    js_marker_start = "// Bulletproof Window Modal Controllers"
    js_marker_end = "window.toggleCvBoundingBoxes = function(val) {"
    
    exact_js = """// Bulletproof Window Modal Controllers
    window.openCvEvidenceModal = function(appId, facilityType) {
        const m = document.getElementById('cvEvidenceModal');
        if (m) {
            m.style.display = 'flex';
            m.classList.add('active');
            m.classList.remove('hidden');
            document.body.style.overflow = 'hidden';
            if (typeof drawCvCanvas === 'function') {
                setTimeout(drawCvCanvas, 30);
                setTimeout(drawCvCanvas, 150);
            }
        } else {
            console.error('cvEvidenceModal element not found in DOM');
        }
    };

    window.closeCvEvidenceModal = function() {
        const m = document.getElementById('cvEvidenceModal');
        if (m) {
            m.style.display = 'none';
            m.classList.remove('active');
            m.classList.add('hidden');
            document.body.style.overflow = '';
        }
    };

    window.openForensicDiffModal = function(appId, docType) {
        const m = document.getElementById('forensicDiffModal');
        if (m) {
            m.style.display = 'flex';
            m.classList.add('active');
            m.classList.remove('hidden');
            document.body.style.overflow = 'hidden';
        } else {
            console.error('forensicDiffModal element not found in DOM');
        }
    };

    window.closeForensicDiffModal = function() {
        const m = document.getElementById('forensicDiffModal');
        if (m) {
            m.style.display = 'none';
            m.classList.remove('active');
            m.classList.add('hidden');
            document.body.style.overflow = '';
        }
    };

    window.openReportModal = function(appId) {
        const m = document.getElementById('reportModal') || document.getElementById('aicteScrutinyReportModal');
        if (m) {
            m.style.display = 'flex';
            m.classList.add('active');
            m.classList.remove('hidden');
            document.body.style.overflow = 'hidden';
        } else {
            console.error('reportModal element not found in DOM');
        }
    };

    window.closeReportModal = function() {
        const m = document.getElementById('reportModal') || document.getElementById('aicteScrutinyReportModal');
        if (m) {
            m.style.display = 'none';
            m.classList.remove('active');
            m.classList.add('hidden');
            document.body.style.overflow = '';
        }
    };

    // Backward-compatibility aliases
    window.openCvInspectionModal = window.openCvEvidenceModal;
    window.closeCvInspectionModal = window.closeCvEvidenceModal;
    window.openAicteScrutinyReportModal = window.openReportModal;
    window.closeAicteScrutinyReportModal = window.closeReportModal;

    // Global escape key listener to close active modals
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            window.closeCvEvidenceModal();
            window.closeForensicDiffModal();
            window.closeReportModal();
        }
    });

    """

    idx_js = html.find(js_marker_start)
    if idx_js != -1:
        idx_js_e = html.find(js_marker_end, idx_js)
        html = html[:idx_js] + exact_js + html[idx_js_e:]
        print("Updated global window JS controllers to exact prompt specification.")

    # 3. Replace all modal containers right before </body>
    idx_modals = -1
    for m_cand in [
        "<!-- =====================================================================\n       MODAL 1: INTERACTIVE COMPUTER VISION (CV) LAB & INFRASTRUCTURE MODAL",
        "<!-- =====================================================================\n       MODULE 1: INTERACTIVE COMPUTER VISION (CV) LAB & INFRASTRUCTURE MODAL",
        '<div id="cvEvidenceModal"'
    ]:
        idx = html.find(m_cand)
        if idx != -1:
            idx_modals = idx
            break
    
    idx_body_close = html.rfind("</body>")
    if idx_modals != -1 and idx_body_close != -1:
        all_modals_html = """<!-- =====================================================================
       MODAL 1: INTERACTIVE COMPUTER VISION (CV) LAB & INFRASTRUCTURE MODAL
       ===================================================================== -->
  <div id="cvEvidenceModal" class="modal-backdrop" style="display:none;" onclick="if(event.target===this)closeCvEvidenceModal()">
    <div class="modal-container card-surface max-w-[1000px] w-full p-5 space-y-4 shadow-2xl border border-line rounded-[6px] flex flex-col max-h-[90vh] overflow-y-auto" onclick="event.stopPropagation()">
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
          <button onclick="closeCvEvidenceModal()" class="btn-secondary px-2.5 py-1 text-xs" style="border-radius:4px;">Close</button>
          <button onclick="closeCvEvidenceModal()" class="text-sub hover:text-main p-1 rounded" title="Close Modal">
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

      <!-- Body: Two-Column Grid -->
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-4 flex-1 overflow-y-auto">
        <!-- Left Column: Canvas & Evidence View (65% width / 8 Cols) -->
        <div class="lg:col-span-8 flex flex-col space-y-2">
          <div class="relative w-full rounded border border-slate-700 overflow-hidden bg-slate-900 shadow-inner flex items-center justify-center">
            <canvas id="cvInspectionCanvas" width="760" height="380" class="w-full h-[380px] block" style="background:#0f172a;"></canvas>
            
            <!-- Live Model Pill -->
            <div class="absolute top-2.5 left-2.5 px-2 py-0.5 rounded bg-black/75 text-emerald-400 font-mono text-[10px] border border-emerald-500/40 flex items-center gap-1.5">
              <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
              <span>LIVE INFERENCE: YOLOv8x-SIH26261 (18.4ms)</span>
            </div>
          </div>

          <!-- Viewport Controls (3 Checkboxes) -->
          <div class="flex flex-wrap items-center justify-between gap-3 p-2.5 rounded subtle-surface border border-line text-xs">
            <div class="flex flex-wrap items-center gap-3">
              <label class="inline-flex items-center gap-1.5 cursor-pointer font-medium text-main text-xs">
                <input type="checkbox" id="cvBoxesCheckbox" checked onchange="toggleCvBoundingBoxes(this.checked)" class="rounded text-[#0284c7]">
                <span>Show YOLOv8 Bounding Boxes</span>
              </label>
              <label class="inline-flex items-center gap-1.5 cursor-pointer font-medium text-main text-xs">
                <input type="checkbox" id="cvHeatmapCheckbox" onchange="toggleCvHeatmap(this.checked)" class="rounded text-[#0284c7]">
                <span>Show Thermal Density Heatmap</span>
              </label>
              <label class="inline-flex items-center gap-1.5 cursor-pointer font-medium text-main text-xs">
                <input type="checkbox" id="cvGeofenceCheckbox" checked onchange="toggleCvGeofence(this.checked)" class="rounded text-[#0284c7]">
                <span>Geofence Verification (18.5204 N, 73.8567 E - Match)</span>
              </label>
            </div>
            <div class="text-[11px] font-mono text-sub">
              Confidence: <span class="font-bold text-main">&gt;= 0.85</span>
            </div>
          </div>
        </div>

        <!-- Right Column: CV Inference Stats Drawer (35% width / 4 Cols) -->
        <div class="lg:col-span-4 space-y-3 text-xs">
          <!-- Terminal Count Card -->
          <div class="p-3 rounded subtle-surface border border-line space-y-1.5">
            <div class="flex items-center justify-between">
              <span class="text-sub font-semibold">Terminal Count:</span>
              <span class="tag-red font-bold" style="background:#fee2e2;color:#dc2626;border:1px solid #fca5a5;">DEFICIENT (-8 Units for 180 Intake)</span>
            </div>
            <div class="text-lg font-mono font-bold text-main">
              142 Terminals <span class="text-xs text-sub font-normal">/ 150 APH Requirement</span>
            </div>
            <div class="w-full bg-slate-200 h-1.5 rounded overflow-hidden">
              <div class="bg-amber-500 h-full" style="width: 94.6%;"></div>
            </div>
            <div class="text-[11px] text-sub">
              AICTE APH Norm prescribes 150 active terminals for 180 B.Tech CSE intake. Detected shortfall of 8 units.
            </div>
          </div>

          <!-- Carpet Area Density Card -->
          <div class="p-3 rounded subtle-surface border border-line space-y-1">
            <div class="flex items-center justify-between">
              <span class="text-sub font-semibold">Carpet Area Density:</span>
              <span class="tag-green font-bold" style="background:#dcfce7;color:#16a34a;border:1px solid #86efac;">COMPLIANT</span>
            </div>
            <div class="text-sm font-mono font-bold text-main">
              Measured: 1.25 sq.m / Terminal
            </div>
            <div class="text-[11px] text-sub">
              Norm: &gt;= 1.20 sq.m per workstation. Adequate circulation aisle spacing verified.
            </div>
          </div>

          <!-- Safety Compliance Card -->
          <div class="p-3 rounded subtle-surface border border-line space-y-1.5">
            <div class="flex items-center justify-between">
              <span class="text-sub font-semibold">Safety Compliance:</span>
              <span class="tag-amber font-bold">Action Required</span>
            </div>
            <ul class="text-[11px] space-y-1.5 text-main">
              <li class="flex items-center gap-1.5 text-emerald-700">
                <svg class="w-3.5 h-3.5 text-emerald-600 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/></svg>
                <span>Fire Extinguishers: 2 Units Detected (Valid)</span>
              </li>
              <li class="flex items-center gap-1.5 text-red-700">
                <svg class="w-3.5 h-3.5 text-red-600 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/></svg>
                <span>Emergency Exit Signage: Not Detected (Flagged)</span>
              </li>
            </ul>
          </div>

          <!-- Action Button -->
          <button onclick="appendCvFindingToDossier()" class="btn-primary w-full py-2.5 text-xs flex items-center justify-center gap-1.5" style="border-radius:4px;">
            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
            </svg>
            <span>Append Finding to Scrutiny Dossier</span>
          </button>
        </div>
      </div>

      <!-- Footer -->
      <div class="flex items-center justify-between pt-2 border-t border-line text-xs">
        <span class="text-[11px] text-sub font-mono">SIH Problem ID: 26261 | AICTE Regulatory Decision-Support</span>
        <button onclick="closeCvEvidenceModal()" class="btn-secondary text-xs" style="border-radius:4px;">Close Inspection</button>
      </div>
    </div>
  </div>

  <!-- =====================================================================
       MODAL 2: SIDE-BY-SIDE FORENSIC DOCUMENT DIFF MODAL
       ===================================================================== -->
  <div id="forensicDiffModal" class="modal-backdrop" style="display:none;" onclick="if(event.target===this)closeForensicDiffModal()">
    <div class="modal-container card-surface max-w-[1000px] w-full p-5 space-y-4 shadow-2xl border border-line rounded-[6px] flex flex-col max-h-[90vh] overflow-y-auto" onclick="event.stopPropagation()">
      <!-- Header -->
      <div class="flex items-center justify-between border-b border-line pb-3">
        <div>
          <div class="flex items-center gap-2">
            <span class="px-2 py-0.5 rounded text-[10px] font-bold font-mono bg-red-100 text-red-800 border border-red-300">Forensic Tamper Detection</span>
            <h3 class="text-sm font-bold text-main">Cryptographic &amp; Forensic Document Diff Viewer</h3>
          </div>
          <p class="text-[11px] text-sub mt-0.5">Side-by-Side Comparison: Submitted PDF vs. DigiLocker State Land Registry</p>
        </div>
        <div class="flex items-center gap-2">
          <button onclick="closeForensicDiffModal()" class="btn-secondary px-2.5 py-1 text-xs" style="border-radius:4px;">Close</button>
          <button onclick="closeForensicDiffModal()" class="text-sub hover:text-main p-1 rounded" title="Close Modal">
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>

      <!-- Body: Split View (Two Columns) -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 flex-1 overflow-y-auto">
        <!-- Column 1: Institution Submitted Land Deed -->
        <div class="p-4 rounded border-2 border-red-400 bg-red-50/20 space-y-3">
          <div class="flex items-center justify-between border-b border-red-200 pb-2">
            <div>
              <span class="tag-red font-bold text-[10px]">Institution Submitted Land Deed</span>
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
                <div class="text-[10px] text-red-700 font-sans font-medium">
                  Altered Layer Detected | Error Level Analysis (ELA): 88.4% (Post-scan digital raster modification)
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

        <!-- Column 2: DigiLocker / State Revenue Dept Record -->
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
                <div class="text-[10px] text-emerald-700 font-sans font-medium">
                  Authentic Record Verified via Maharashtra Land Registry API (HTTP 200 OK)
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

      <!-- Forensic Findings Table -->
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
              <td class="py-2.5 px-3 font-mono text-emerald-600">Monospaced Courier (Scanned)</td>
              <td class="py-2.5 px-3"><span class="tag-red font-bold">MISMATCH</span></td>
            </tr>
            <tr>
              <td class="py-2.5 px-3 font-semibold">Software Producer</td>
              <td class="py-2.5 px-3 font-mono text-red-600">Adobe Acrobat Pro v24.2 (Modified 14 days post-issue)</td>
              <td class="py-2.5 px-3 font-mono text-emerald-600">Government C-DAC Digital Stamp</td>
              <td class="py-2.5 px-3"><span class="tag-red font-bold">ANOMALY</span></td>
            </tr>
            <tr>
              <td class="py-2.5 px-3 font-semibold">Land Acreage</td>
              <td class="py-2.5 px-3 font-mono text-red-600 font-bold">5.20 Acres Claimed</td>
              <td class="py-2.5 px-3 font-mono text-emerald-600 font-bold">2.10 Acres Actual</td>
              <td class="py-2.5 px-3"><span class="tag-red font-bold">REGULATORY DEFICIENCY (-3.10 Acres)</span></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Footer -->
      <div class="flex items-center justify-between pt-2 border-t border-line text-xs">
        <button onclick="attachForensicProofToDossier()" class="btn-primary flex items-center gap-1.5" style="border-radius:4px;">
          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span>Attach Forensic Proof to Dossier</span>
        </button>
        <button onclick="closeForensicDiffModal()" class="btn-secondary text-xs" style="border-radius:4px;">Close Inspector</button>
      </div>
    </div>
  </div>

  <!-- =====================================================================
       MODAL 3: 1-CLICK OFFICIAL AICTE SCRUTINY DEFICIENCY REPORT MODAL
       ===================================================================== -->
  <div id="reportModal" class="modal-backdrop" style="display:none;" onclick="if(event.target===this)closeReportModal()">
    <div class="modal-container card-surface max-w-[850px] w-full p-6 space-y-5 shadow-2xl border border-line rounded-[6px] flex flex-col max-h-[90vh] overflow-y-auto" onclick="event.stopPropagation()">
      <!-- Header Controls -->
      <div class="flex items-center justify-between border-b border-line pb-3 no-print">
        <div>
          <span class="px-2 py-0.5 rounded text-[10px] font-bold font-mono bg-[#0284c7]/10 text-[#0284c7] border border-[#0284c7]/30">Statutory Assessment</span>
          <h3 class="text-sm font-bold text-main">AICTE Official Scrutiny Assessment Report (AY 2026-27)</h3>
        </div>
        <div class="flex items-center gap-2">
          <button onclick="printAicteScrutinyReport()" class="btn-primary flex items-center gap-1.5 text-xs" style="border-radius:4px;">
            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 002 2h2m2 4h6a2 2 0 002-2v-4a2 2 0 00-2-2H9a2 2 0 00-2 2v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z" />
            </svg>
            <span>Print / Save PDF</span>
          </button>
          <button onclick="exportScrutinyReportJson()" class="btn-secondary flex items-center gap-1.5 text-xs" style="border-radius:4px;">
            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
            </svg>
            <span>Export JSON</span>
          </button>
          <button onclick="closeReportModal()" class="btn-secondary px-2.5 py-1 text-xs" style="border-radius:4px;">Close</button>
        </div>
      </div>

      <!-- Printable Content -->
      <div id="aictePrintableDocument" class="bg-white p-6 rounded border border-line text-slate-900 space-y-5 overflow-y-auto font-sans leading-normal">
        <!-- AICTE Header Emblem & Body Title -->
        <div class="text-center border-b-2 border-slate-900 pb-4 space-y-1">
          <div class="flex items-center justify-center gap-2 mb-1">
            <svg class="w-8 h-8 text-[#0284c7]" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
            <span class="font-bold text-base tracking-wider uppercase text-slate-900">All India Council for Technical Education</span>
          </div>
          <div class="text-xs uppercase tracking-wide font-semibold text-slate-700">Statutory Scrutiny Committee Assessment Report (AY 2026-27)</div>
          <div class="text-[11px] font-mono text-slate-500">Ministry of Education, Government of India | Problem Statement SIH26261</div>
        </div>

        <!-- Institutional Details Grid -->
        <div class="grid grid-cols-2 gap-3 text-xs p-3 rounded bg-slate-50 border border-slate-200 font-mono">
          <div><span class="text-slate-500">Institution:</span> <strong class="text-slate-900">Apex Institute of Technology, Pune</strong></div>
          <div><span class="text-slate-500">Permanent AICTE ID:</span> <strong class="text-slate-900">1-33249112</strong></div>
          <div><span class="text-slate-500">Application Number:</span> <strong class="text-[#0284c7]">APP-2026-101</strong></div>
          <div><span class="text-slate-500">Category:</span> <strong class="text-slate-900">Intake Increase (B.Tech CSE 120 to 180)</strong></div>
        </div>

        <!-- 6 Mandatory Documents Summary Table -->
        <div class="space-y-2">
          <div class="text-xs font-bold uppercase tracking-wider text-slate-800 border-b border-slate-300 pb-1">1. Mandatory Regulatory Documents Verification (6/6 Ingested)</div>
          <table class="w-full text-left border-collapse text-xs">
            <thead>
              <tr class="bg-slate-100 text-slate-700 font-mono text-[11px] uppercase border-b border-slate-300">
                <th class="py-1.5 px-2">Document Name</th>
                <th class="py-1.5 px-2">Uploaded File</th>
                <th class="py-1.5 px-2">Status</th>
                <th class="py-1.5 px-2">SHA-256 Fingerprint</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-200 font-mono text-[11px]">
              <tr>
                <td class="py-1.5 px-2 font-sans font-semibold">Fire Safety Certificate</td>
                <td class="py-1.5 px-2">SYNTH_FIRE_NOC_MH_2026.pdf</td>
                <td class="py-1.5 px-2 text-emerald-700 font-bold">Valid (2029-01-09)</td>
                <td class="py-1.5 px-2 text-slate-500">e3b0c442...b855</td>
              </tr>
              <tr>
                <td class="py-1.5 px-2 font-sans font-semibold">Land Ownership Deed</td>
                <td class="py-1.5 px-2">SYNTH_LAND_DEED_PUNE_ALTERED.pdf</td>
                <td class="py-1.5 px-2 text-red-700 font-bold">Altered (Survey 42/B)</td>
                <td class="py-1.5 px-2 text-slate-500">9b8769a4...7b8b</td>
              </tr>
              <tr>
                <td class="py-1.5 px-2 font-sans font-semibold">University Affiliation Order</td>
                <td class="py-1.5 px-2">SYNTH_SPPU_AFFILIATION_2026.pdf</td>
                <td class="py-1.5 px-2 text-amber-700 font-bold">Mismatch (120 vs 180)</td>
                <td class="py-1.5 px-2 text-slate-500">4a2d81f0...d287</td>
              </tr>
              <tr>
                <td class="py-1.5 px-2 font-sans font-semibold">Faculty Roster &amp; PANs</td>
                <td class="py-1.5 px-2">SYNTH_FACULTY_ROSTER_DUPLICATE_PAN.pdf</td>
                <td class="py-1.5 px-2 text-red-700 font-bold">Duplicate PAN Flagged</td>
                <td class="py-1.5 px-2 text-slate-500">7c9d4a19...0912</td>
              </tr>
              <tr>
                <td class="py-1.5 px-2 font-sans font-semibold">Approved Building Plan</td>
                <td class="py-1.5 px-2">SYNTH_BUILDING_PLAN_MUNICIPAL.pdf</td>
                <td class="py-1.5 px-2 text-amber-700 font-bold">Deficient (-320 sq.m)</td>
                <td class="py-1.5 px-2 text-slate-500">81fa023e...8102</td>
              </tr>
              <tr>
                <td class="py-1.5 px-2 font-sans font-semibold">TER Fee Receipt</td>
                <td class="py-1.5 px-2">SYNTH_AICTE_TER_RECEIPT_VERIFIED.pdf</td>
                <td class="py-1.5 px-2 text-emerald-700 font-bold">Valid (UTR Settled)</td>
                <td class="py-1.5 px-2 text-slate-500">12a4b889...1239</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Flagged Deficiencies -->
        <div class="space-y-2">
          <div class="text-xs font-bold uppercase tracking-wider text-slate-800 border-b border-slate-300 pb-1">2. Scrutiny Findings &amp; Regulatory Deficiencies Flagged</div>
          <div class="space-y-1.5 text-xs">
            <div class="p-2.5 rounded bg-red-50 border border-red-200 text-red-900 space-y-0.5">
              <div class="font-bold flex items-center gap-1.5">
                <span class="w-2 h-2 rounded-full bg-red-600"></span>
                <span>Critical Deficiency: Land Area &amp; Survey Discrepancy</span>
              </div>
              <div class="text-[11px] text-red-800">
                Application claims 5.20 Acres on Survey 42/B. Official DigiLocker Maharashtra Land Registry extract confirms only 2.10 Acres on Survey 42/A. Net deficiency: -3.10 Acres against APH mandatory norm. Font raster overlay detected (ELA: 88.4%).
              </div>
            </div>

            <div class="p-2.5 rounded bg-amber-50 border border-amber-200 text-amber-900 space-y-0.5">
              <div class="font-bold flex items-center gap-1.5">
                <span class="w-2 h-2 rounded-full bg-amber-600"></span>
                <span>Infrastructure Shortfall: Computing Workstation Deficit</span>
              </div>
              <div class="text-[11px] text-amber-800">
                AI Computer Vision (YOLOv8x) audit of CCF Lab-3 detected 142 functional workstations against APH prescribed norm of 150 units for 180 intake. Shortfall: 8 units. Emergency exit luminaire missing.
              </div>
            </div>
          </div>
        </div>

        <!-- Blockchain Notarization Stamp -->
        <div class="p-3 rounded border border-slate-300 bg-slate-50 text-xs font-mono space-y-1">
          <div class="flex items-center justify-between border-b border-slate-200 pb-1 text-[11px]">
            <span class="font-bold text-slate-900">IMMUTABLE BLOCKCHAIN NOTARIZATION STAMP</span>
            <span class="text-emerald-700 font-bold">PoA Consensus: 7/7 Nodes Validated</span>
          </div>
          <div class="grid grid-cols-2 gap-2 text-[11px] pt-1">
            <div><span class="text-slate-500">Block Index:</span> <strong>Block #4</strong></div>
            <div><span class="text-slate-500">Transaction Hash:</span> <strong>0x7a8f192b...910123</strong></div>
            <div><span class="text-slate-500">Timestamp:</span> <strong>2026-10-25 02:30:12 UTC</strong></div>
            <div><span class="text-slate-500">Signatory Officer:</span> <strong>Dr. Arvind Kumar (AICTE-OFFICER-782)</strong></div>
          </div>
        </div>

        <!-- Statutory Footer -->
        <div class="border-t border-slate-300 pt-3 text-[10px] text-slate-500 flex flex-col sm:flex-row items-center justify-between">
          <div>This is an official AICTE Scrutiny Committee Assessment record generated under SIH 2026.</div>
          <div>Page 1 of 1 | Authorized Electronic Signature</div>
        </div>
      </div>
    </div>
  </div>

  <!-- Fallback ID alias for any legacy scripts -->
  <div id="aicteScrutinyReportModal" style="display:none;" data-alias="reportModal"></div>

  """
        html = html[:idx_modals] + all_modals_html + html[idx_body_close:]
        print("Injected all 3 modals right before </body>.")

    with open(fpath, "w", encoding="utf-8") as f:
        f.write(html)
    print(f"Successfully processed {fpath}")

for fpath in target_files:
    update_file(fpath)

print("All files updated successfully.")
