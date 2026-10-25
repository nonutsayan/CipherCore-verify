import os
import re

target_files = [
    "frontend/index.html",
    "index.html",
    "backend/src/main/resources/static/index.html"
]

def apply_fixes_to_file(fpath):
    with open(fpath, "r", encoding="utf-8") as f:
        html = f.read()

    # =========================================================================
    # 1. CSS FIX: Remove :not(.active) blocking display:flex, add .btn-close & widths
    # =========================================================================
    css_start_marker = "/* Modal Backdrop & Container Architecture */"
    if css_start_marker not in html:
        css_start_marker = "/* Modal Backdrop & Bulletproof Visibility */"

    new_css = """/* Modal Backdrop & Container Architecture */
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
      align-items: center !important;
      justify-content: center !important;
      padding: 1rem !important;
    }
    .modal-backdrop[style*="display: none"],
    .modal-backdrop[style*="display:none"],
    #cvEvidenceModal[style*="display: none"],
    #cvEvidenceModal[style*="display:none"],
    #forensicDiffModal[style*="display: none"],
    #forensicDiffModal[style*="display:none"],
    #reportModal[style*="display: none"],
    #reportModal[style*="display:none"] {
      display: none !important;
    }
    .modal-backdrop[style*="display: flex"],
    .modal-backdrop[style*="display:flex"],
    .modal-backdrop.active,
    #cvEvidenceModal[style*="display: flex"],
    #cvEvidenceModal[style*="display:flex"],
    #cvEvidenceModal.active,
    #forensicDiffModal[style*="display: flex"],
    #forensicDiffModal[style*="display:flex"],
    #forensicDiffModal.active,
    #reportModal[style*="display: flex"],
    #reportModal[style*="display:flex"],
    #reportModal.active {
      display: flex !important;
      visibility: visible !important;
      opacity: 1 !important;
    }
    .modal-container {
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: 6px;
      box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
      max-height: 90vh;
      display: flex;
      flex-direction: column;
      overflow-y: auto;
    }
    .btn-close {
      background: transparent;
      border: none;
      font-size: 24px;
      line-height: 1;
      color: var(--text-sub);
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 4px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      transition: color 0.15s ease, background-color 0.15s ease;
    }
    .btn-close:hover {
      color: var(--text-main);
      background-color: rgba(0, 0, 0, 0.05);
    }
    html.dark .btn-close:hover {
      background-color: rgba(255, 255, 255, 0.1);
    }
    html.dark .modal-backdrop {
      background-color: rgba(0, 0, 0, 0.88) !important;
    }"""

    idx_css = html.find(css_start_marker)
    if idx_css != -1:
        css_end = "html.dark .modal-backdrop {\n      background-color: rgba(0, 0, 0, 0.88) !important;\n    }"
        idx_css_e = html.find(css_end, idx_css)
        if idx_css_e != -1:
            idx_css_e += len(css_end)
            html = html[:idx_css] + new_css + html[idx_css_e:]
            print(f"Updated CSS architecture in {fpath}")

    # =========================================================================
    # 2. TOOLBAR BUTTONS: Ensure exact button labels and onclick triggers
    # =========================================================================
    # Fix top toolbar action buttons in "2. Inspect Application"
    old_toolbar_target = """<div class="flex flex-wrap items-center gap-2">
                    <button onclick="window.openForensicDiffModal('APP-2026-101', 'Land Ownership Deed')" class="btn-secondary flex items-center gap-1.5 text-xs" style="border-radius:4px;">
            <svg class="w-3.5 h-3.5 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
            </svg>
            <span>Inspect Forensic Diff</span>
          </button>
          <button onclick="window.openCvEvidenceModal('APP-2026-101', 'Computer Center / Lab')" class="btn-secondary flex items-center gap-1.5 text-xs" style="border-radius:4px;">
            <svg class="w-3.5 h-3.5 text-[#0284c7]" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
            </svg>
            <span>View CV Evidence</span>
          </button>
          <button onclick="openAicteScrutinyReportModal()" class="btn-primary flex items-center gap-1.5 text-xs bg-[#0284c7] text-white" >
            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <span>Generate AICTE Scrutiny Report</span>
          </button>"""

    new_toolbar = """<div class="flex flex-wrap items-center gap-2">
          <button onclick="window.openForensicDiffModal('APP-2026-101', 'Land Ownership Deed')" class="btn-secondary flex items-center gap-1.5 text-xs" style="border-radius:4px;">
            <svg class="w-3.5 h-3.5 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
            </svg>
            <span>Inspect Forensic Diff</span>
          </button>
          <button onclick="window.openCvEvidenceModal('APP-2026-101', 'Computer Center / Lab')" class="btn-secondary flex items-center gap-1.5 text-xs" style="border-radius:4px;">
            <svg class="w-3.5 h-3.5 text-[#0284c7]" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
            </svg>
            <span>View CV Evidence Inspection</span>
          </button>
          <button onclick="window.openReportModal('APP-2026-101')" class="btn-primary flex items-center gap-1.5 text-xs bg-[#0284c7] text-white" style="border-radius:4px;">
            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <span>Generate AICTE Scrutiny Report</span>
          </button>"""

    if old_toolbar_target in html:
        html = html.replace(old_toolbar_target, new_toolbar)
        print("Updated top action toolbar buttons.")
    else:
        # Fallback regex replace for toolbar
        html = re.sub(
            r'<span>View CV Evidence</span>',
            '<span>View CV Evidence Inspection</span>',
            html
        )
        html = re.sub(
            r'onclick=["\']openAicteScrutinyReportModal\(\)["\']',
            'onclick="window.openReportModal(\'APP-2026-101\')"',
            html
        )

    # =========================================================================
    # 3. GLOBAL JAVASCRIPT CONTROLLERS (STEP 2 OF PROMPT + ROBUST INTERCEPTOR)
    # =========================================================================
    exact_js_controllers = """// Bulletproof Window Modal Controllers (Step 2)
    window.openCvEvidenceModal = function(appId, facilityType) {
        const m = document.getElementById('cvEvidenceModal');
        if (m) {
            if (m.style.setProperty) m.style.setProperty('display', 'flex', 'important');
            m.style.display = 'flex';
            m.classList.add('active');
            m.classList.remove('hidden');
            document.body.style.overflow = 'hidden';
            if (typeof drawCvCanvas === 'function') {
                setTimeout(drawCvCanvas, 20);
                setTimeout(drawCvCanvas, 100);
            }
        } else {
            console.error('cvEvidenceModal element not found in DOM');
        }
    };

    window.closeCvEvidenceModal = function() {
        const m = document.getElementById('cvEvidenceModal');
        if (m) {
            if (m.style.setProperty) m.style.setProperty('display', 'none', 'important');
            m.style.display = 'none';
            m.classList.remove('active');
            m.classList.add('hidden');
            document.body.style.overflow = '';
        }
    };

    window.openForensicDiffModal = function(appId, docType) {
        const m = document.getElementById('forensicDiffModal');
        if (m) {
            if (m.style.setProperty) m.style.setProperty('display', 'flex', 'important');
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
            if (m.style.setProperty) m.style.setProperty('display', 'none', 'important');
            m.style.display = 'none';
            m.classList.remove('active');
            m.classList.add('hidden');
            document.body.style.overflow = '';
        }
    };

    window.openReportModal = function(appId) {
        const m = document.getElementById('reportModal') || document.getElementById('aicteScrutinyReportModal');
        if (m) {
            if (m.style.setProperty) m.style.setProperty('display', 'flex', 'important');
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
            if (m.style.setProperty) m.style.setProperty('display', 'none', 'important');
            m.style.display = 'none';
            m.classList.remove('active');
            m.classList.add('hidden');
            document.body.style.overflow = '';
        }
    };

    // Aliases
    window.openCvInspectionModal = window.openCvEvidenceModal;
    window.closeCvInspectionModal = window.closeCvEvidenceModal;
    window.openAicteScrutinyReportModal = window.openReportModal;
    window.closeAicteScrutinyReportModal = window.closeReportModal;

    // Scope helper functions for inline callers without window.
    function openCvEvidenceModal(a, b) { window.openCvEvidenceModal(a, b); }
    function closeCvEvidenceModal() { window.closeCvEvidenceModal(); }
    function openForensicDiffModal(a, b) { window.openForensicDiffModal(a, b); }
    function closeForensicDiffModal() { window.closeForensicDiffModal(); }
    function openReportModal(a) { window.openReportModal(a); }
    function closeReportModal() { window.closeReportModal(); }
    function openAicteScrutinyReportModal(a) { window.openReportModal(a); }
    function closeAicteScrutinyReportModal() { window.closeReportModal(); }
    function openCvInspectionModal(a, b) { window.openCvEvidenceModal(a, b); }
    function closeCvInspectionModal() { window.closeCvEvidenceModal(); }

    // Global escape key to close any open modal
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            window.closeCvEvidenceModal();
            window.closeForensicDiffModal();
            window.closeReportModal();
        }
    });

    // Global Click Event Interceptor - ensures ANY button with matching text opens modal
    document.addEventListener('click', function(e) {
        const btn = e.target.closest('button, a');
        if (!btn) return;
        const txt = (btn.innerText || btn.textContent || '').trim();
        if (txt.includes('View CV Evidence Inspection') || txt.includes('Launch CV Evidence Audit') || txt.includes('View CV Evidence')) {
            window.openCvEvidenceModal('APP-2026-101', 'Computer Center / Lab');
        } else if (txt.includes('Inspect Forensic Diff') || txt.includes('Launch Forensic Diff Viewer')) {
            window.openForensicDiffModal('APP-2026-101', 'Land Ownership Deed');
        } else if (txt.includes('Generate AICTE Scrutiny Report') || txt.includes('Preview AICTE Scrutiny Report')) {
            if (!txt.includes('Print') && !txt.includes('Save PDF')) {
                window.openReportModal('APP-2026-101');
            }
        }
    });"""

    js_marker_start = "// Bulletproof Window Modal Controllers"
    js_marker_end = "window.toggleCvBoundingBoxes = function(val) {"
    idx_js = html.find(js_marker_start)
    if idx_js != -1:
        idx_js_e = html.find(js_marker_end, idx_js)
        if idx_js_e != -1:
            html = html[:idx_js] + exact_js_controllers + "\n\n    " + html[idx_js_e:]
            print(f"Updated JS controllers and global click interceptor in {fpath}")

    # =========================================================================
    # 4. STATIC MODALS INJECTION RIGHT BEFORE </body> (STEP 1)
    # =========================================================================
    modals_markup = """<!-- =====================================================================
       STATIC MODALS (OUTSIDE DYNAMIC TABS, RIGHT BEFORE </body>)
       ===================================================================== -->

  <!-- 1. AI COMPUTER VISION INFRASTRUCTURE AUDIT MODAL -->
  <div id="cvEvidenceModal" class="modal-backdrop" style="display:none;" onclick="if(event.target===this)window.closeCvEvidenceModal()">
    <div class="modal-container card-surface max-w-[1040px] w-[95%] p-5 space-y-4 shadow-2xl border border-line rounded-[6px] flex flex-col max-h-[90vh] overflow-y-auto" onclick="event.stopPropagation()">
      <!-- Header -->
      <div class="flex items-center justify-between border-b border-line pb-3">
        <div class="flex items-center gap-2">
          <span class="px-2 py-0.5 rounded text-[10px] font-bold font-mono bg-[#0284c7]/10 text-[#0284c7] border border-[#0284c7]/30">YOLOv8 Edge CV</span>
          <h3 class="text-sm font-bold text-main">AI Computer Vision Infrastructure Audit | APH 2024-27 Norm Validation</h3>
        </div>
        <div class="flex items-center gap-2">
          <span class="tag-neutral text-[10px] font-semibold">Apex Institute (AP-2026-101) - Lab 03</span>
          <button onclick="window.closeCvEvidenceModal()" class="btn-close" title="Close Modal">&times;</button>
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

      <!-- Body Grid (2 Columns: 65% Left Canvas, 35% Right Analytics) -->
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-4 flex-1 overflow-y-auto">
        <!-- Left Column: Canvas Viewport (65% width / 8 Cols) -->
        <div class="lg:col-span-8 flex flex-col space-y-2">
          <div class="relative w-full rounded border border-slate-700 overflow-hidden bg-slate-900 shadow-inner flex items-center justify-center">
            <canvas id="cvInspectionCanvas" width="760" height="380" class="w-full h-[380px] block" style="background:#0f172a;"></canvas>
            
            <!-- Live Model Inference Pill -->
            <div class="absolute top-2.5 left-2.5 px-2 py-0.5 rounded bg-black/75 text-emerald-400 font-mono text-[10px] border border-emerald-500/40 flex items-center gap-1.5">
              <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
              <span>LIVE INFERENCE: YOLOv8x-SIH26261 (18.4ms)</span>
            </div>
          </div>

          <!-- Checkbox controls below canvas -->
          <div class="flex flex-wrap items-center justify-between gap-3 p-2.5 rounded subtle-surface border border-line text-xs">
            <div class="flex flex-wrap items-center gap-3">
              <label class="inline-flex items-center gap-1.5 cursor-pointer font-medium text-main text-xs">
                <input type="checkbox" id="cvBoxesCheckbox" checked onchange="toggleCvBoundingBoxes(this.checked)" class="rounded text-[#0284c7]">
                <span>YOLOv8 Bounding Boxes</span>
              </label>
              <label class="inline-flex items-center gap-1.5 cursor-pointer font-medium text-main text-xs">
                <input type="checkbox" id="cvHeatmapCheckbox" onchange="toggleCvHeatmap(this.checked)" class="rounded text-[#0284c7]">
                <span>Thermal Heatmap</span>
              </label>
              <label class="inline-flex items-center gap-1.5 cursor-pointer font-medium text-main text-xs">
                <input type="checkbox" id="cvGeofenceCheckbox" checked onchange="toggleCvGeofence(this.checked)" class="rounded text-[#0284c7]">
                <span>Campus Geofence (18.5204 N, 73.8567 E)</span>
              </label>
            </div>
            <span class="text-[10px] font-mono text-emerald-600 font-bold">142 Terminals Identified</span>
          </div>

          <!-- Legend Bar -->
          <div class="flex flex-wrap items-center gap-3 text-[11px] font-mono text-sub pt-1 px-1">
            <span class="flex items-center gap-1"><span class="w-2.5 h-2.5 rounded-sm bg-emerald-500 inline-block"></span> 142 Green: terminal: 0.94</span>
            <span class="flex items-center gap-1"><span class="w-2.5 h-2.5 rounded-sm border border-dashed border-red-500 bg-red-500/20 inline-block"></span> 8 Dashed red: unoccupied_bay: 0.88</span>
            <span class="flex items-center gap-1"><span class="w-2.5 h-2.5 rounded-sm bg-blue-500 inline-block"></span> 2 Blue: fire_extinguisher_ab: 0.97</span>
            <span class="flex items-center gap-1"><span class="w-2.5 h-2.5 rounded-sm border border-dashed border-amber-500 bg-amber-500/20 inline-block"></span> 1 Dashed amber: missing_exit_luminaire: 0.91</span>
          </div>
        </div>

        <!-- Right Column: Analytics Drawer (35% width / 4 Cols) -->
        <div class="lg:col-span-4 flex flex-col space-y-3">
          <!-- Terminal Count Card -->
          <div class="p-3 rounded border border-line bg-card space-y-1.5">
            <div class="flex items-center justify-between text-xs">
              <span class="text-sub font-medium">Terminal Count Card</span>
              <span class="px-2 py-0.5 rounded text-[10px] font-bold bg-red-100 text-red-800 border border-red-300">DEFICIENT (-8 Units)</span>
            </div>
            <div class="text-lg font-bold text-main font-mono">142 / 150 <span class="text-xs font-normal text-sub">Units</span></div>
            <div class="text-[11px] text-sub">
              Detected: 142 | APH Requirement: 150 units for 180 approved intake (1:1 norm).
            </div>
          </div>

          <!-- Carpet Area Density -->
          <div class="p-3 rounded border border-line bg-card space-y-1.5">
            <div class="flex items-center justify-between text-xs">
              <span class="text-sub font-medium">Carpet Area Density</span>
              <span class="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-100 text-emerald-800 border border-emerald-300">COMPLIANT</span>
            </div>
            <div class="text-lg font-bold text-main font-mono">1.25 <span class="text-xs font-normal text-sub">sq.m / terminal</span></div>
            <div class="text-[11px] text-sub">
              APH Norm requires minimum &gt;= 1.20 sq.m per terminal. Lab 3 carpet area: 177.5 sq.m.
            </div>
          </div>

          <!-- Safety Equipment -->
          <div class="p-3 rounded border border-line bg-card space-y-1.5">
            <div class="flex items-center justify-between text-xs">
              <span class="text-sub font-medium">Safety Equipment Audit</span>
              <span class="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-800 border border-amber-300">WARNING</span>
            </div>
            <ul class="text-[11px] space-y-1 text-main">
              <li class="flex items-center justify-between">
                <span>Fire Extinguishers (CO2/AB):</span>
                <span class="text-emerald-600 font-bold font-mono">2 Detected (Compliant)</span>
              </li>
              <li class="flex items-center justify-between">
                <span>Emergency Exit Luminaire:</span>
                <span class="text-amber-600 font-bold font-mono">Missing (Warning)</span>
              </li>
            </ul>
          </div>

          <!-- Action Button -->
          <div class="pt-2">
            <button onclick="appendCvFindingToDossier()" class="btn-primary w-full py-2 text-xs flex items-center justify-center gap-1.5" style="border-radius:4px;">
              <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>Append Finding to Dossier</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 2. CRYPTOGRAPHIC & FORENSIC DOCUMENT DIFF VIEWER MODAL -->
  <div id="forensicDiffModal" class="modal-backdrop" style="display:none;" onclick="if(event.target===this)window.closeForensicDiffModal()">
    <div class="modal-container card-surface max-w-[1040px] w-[95%] p-5 space-y-4 shadow-2xl border border-line rounded-[6px] flex flex-col max-h-[90vh] overflow-y-auto" onclick="event.stopPropagation()">
      <!-- Header -->
      <div class="flex items-center justify-between border-b border-line pb-3">
        <div>
          <div class="flex items-center gap-2">
            <span class="px-2 py-0.5 rounded text-[10px] font-bold font-mono bg-red-100 text-red-700 border border-red-300">Forensic Analysis</span>
            <h3 class="text-sm font-bold text-main">Cryptographic &amp; Forensic Document Diff Viewer</h3>
          </div>
          <p class="text-xs text-sub mt-0.5">Comparison: Institution Submitted PDF vs. DigiLocker State Registry</p>
        </div>
        <div class="flex items-center gap-2">
          <button onclick="window.closeForensicDiffModal()" class="btn-close" title="Close Modal">&times;</button>
        </div>
      </div>

      <!-- Split View Body -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 flex-1 overflow-y-auto">
        <!-- Left Box: Institution Submitted Land Deed -->
        <div class="p-3.5 rounded border-2 border-red-300 bg-red-50/20 space-y-2.5">
          <div class="flex items-center justify-between border-b border-red-200 pb-2">
            <div>
              <span class="text-xs font-bold text-red-900 block">Submitted Land Deed</span>
              <span class="text-[10px] text-red-700 font-mono">SYNTH_LAND_DEED_PUNE_ALTERED.pdf</span>
            </div>
            <span class="px-2 py-0.5 rounded text-[10px] font-bold bg-red-100 text-red-800 border border-red-300">Altered Layer Detected</span>
          </div>

          <!-- Document Snippet Mock -->
          <div class="p-3 rounded bg-white border border-red-200 text-xs font-mono space-y-2 shadow-inner">
            <div class="text-[10px] text-slate-500 uppercase font-bold tracking-wider">Deed Extract (Page 1):</div>
            <div class="p-2 rounded border border-slate-200 bg-slate-50 text-slate-800 text-[11px] leading-relaxed">
              This Indenture made on 12th January 2021 between Pune District Revenue Authority and Apex Institute of Technology...
            </div>
            <!-- Discrepancy Highlight Box -->
            <div class="p-2.5 rounded border-2 border-red-500 bg-red-100/70 text-red-950 font-bold text-xs space-y-1">
              <div class="flex items-center justify-between text-[10px] uppercase tracking-wider text-red-800">
                <span>Altered Field Highlighted:</span>
                <span class="font-mono">Font: Helvetica-Bold (Raster Overlay)</span>
              </div>
              <div class="text-sm">Survey No: 42/B (Contiguous Land Area: 5.20 Acres)</div>
            </div>
          </div>

          <!-- Callout -->
          <div class="p-2.5 rounded bg-red-100/60 border border-red-300 text-red-900 text-xs flex items-start gap-2">
            <svg class="w-4 h-4 text-red-600 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <div class="text-[11px] leading-relaxed">
              <strong>Altered Layer Detected | ELA: 88.4%</strong> (Post-scan digital raster modification detected in acreage and survey number streams).
            </div>
          </div>
        </div>

        <!-- Right Box: DigiLocker / State Revenue Department Record -->
        <div class="p-3.5 rounded border-2 border-emerald-300 bg-emerald-50/20 space-y-2.5">
          <div class="flex items-center justify-between border-b border-emerald-200 pb-2">
            <div>
              <span class="text-xs font-bold text-emerald-900 block">DigiLocker / State Registry</span>
              <span class="text-[10px] text-emerald-700 font-mono">API CTS Property Card Record</span>
            </div>
            <span class="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-100 text-emerald-800 border border-emerald-300">Verified Authenticated Record</span>
          </div>

          <!-- Document Snippet Mock -->
          <div class="p-3 rounded bg-white border border-emerald-200 text-xs font-mono space-y-2 shadow-inner">
            <div class="text-[10px] text-slate-500 uppercase font-bold tracking-wider">Official Registry Extract (Live Query):</div>
            <div class="p-2 rounded border border-slate-200 bg-slate-50 text-slate-800 text-[11px] leading-relaxed">
              Government of Maharashtra, Revenue Department CTS Record No: 2021-MH-PUN-091218, Registered Holder: Apex Educational Trust...
            </div>
            <!-- Official Clean Record -->
            <div class="p-2.5 rounded border-2 border-emerald-500 bg-emerald-100/70 text-emerald-950 font-bold text-xs space-y-1">
              <div class="flex items-center justify-between text-[10px] uppercase tracking-wider text-emerald-800">
                <span>Authenticated Field:</span>
                <span class="font-mono">Font: Courier-New (Original Scanned)</span>
              </div>
              <div class="text-sm">Survey No: 42/A (Contiguous Land Area: 2.10 Acres)</div>
            </div>
          </div>

          <!-- Callout -->
          <div class="p-2.5 rounded bg-emerald-100/60 border border-emerald-300 text-emerald-900 text-xs flex items-start gap-2">
            <svg class="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <div class="text-[11px] leading-relaxed">
              <strong>Verified Authenticated Record</strong> | Maharashtra Land Registry API (HTTP 200 OK, Signed DSC Serial: 5a9b8f102c).
            </div>
          </div>
        </div>
      </div>

      <!-- Forensic Findings Table -->
      <div class="space-y-1.5 pt-1">
        <h4 class="text-xs font-bold uppercase tracking-wider text-slate-700">Forensic Discrepancy Matrix</h4>
        <div class="border border-line rounded overflow-hidden">
          <table class="w-full text-left text-xs font-mono">
            <thead class="subtle-surface text-slate-600 border-b border-line text-[11px]">
              <tr>
                <th class="py-2 px-3">Inspection Parameter</th>
                <th class="py-2 px-3">Submitted PDF Layer</th>
                <th class="py-2 px-3">DigiLocker Ground Truth</th>
                <th class="py-2 px-3">Status</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-line text-[11px]">
              <tr>
                <td class="py-2 px-3 font-semibold font-sans">Font Stream</td>
                <td class="py-2 px-3 text-red-600">Helvetica-Bold Overlay</td>
                <td class="py-2 px-3 text-emerald-600">Courier Scanned</td>
                <td class="py-2 px-3"><span class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-red-100 text-red-800">MISMATCH</span></td>
              </tr>
              <tr>
                <td class="py-2 px-3 font-semibold font-sans">Metadata</td>
                <td class="py-2 px-3 text-amber-600">Adobe Acrobat Pro v24.2</td>
                <td class="py-2 px-3 text-slate-600">Canon DR-G2140 Scanner</td>
                <td class="py-2 px-3"><span class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-800">ANOMALY</span></td>
              </tr>
              <tr>
                <td class="py-2 px-3 font-semibold font-sans">Land Acreage</td>
                <td class="py-2 px-3 text-red-600 font-bold">5.20 Acres Claimed (42/B)</td>
                <td class="py-2 px-3 text-emerald-600 font-bold">2.10 Acres Actual (42/A)</td>
                <td class="py-2 px-3"><span class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-red-100 text-red-800">REGULATORY DEFICIENCY (-3.10 Acres)</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <!-- 3. AICTE OFFICIAL SCRUTINY ASSESSMENT REPORT MODAL -->
  <div id="reportModal" class="modal-backdrop" style="display:none;" onclick="if(event.target===this)window.closeReportModal()">
    <div class="modal-container card-surface max-w-[850px] w-[90%] p-6 space-y-4 shadow-2xl border border-line rounded-[6px] flex flex-col max-h-[90vh] overflow-y-auto" onclick="event.stopPropagation()">
      <!-- Header -->
      <div class="flex items-center justify-between border-b border-line pb-3">
        <h3 class="text-sm font-bold text-main">AICTE Official Scrutiny Assessment Report (AY 2026-27)</h3>
        <div class="flex items-center gap-2">
          <button onclick="window.print()" class="btn btn-secondary btn-sm flex items-center gap-1.5 text-xs px-2.5 py-1" style="border-radius:4px;">
            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 002 2h2m2 4h6a2 2 0 002-2v-4a2 2 0 00-2-2H9a2 2 0 00-2 2v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z" />
            </svg>
            <span>Print / Save PDF</span>
          </button>
          <button onclick="window.closeReportModal()" class="btn-close" title="Close Modal">&times;</button>
        </div>
      </div>

      <!-- Printable Report View -->
      <div id="printableScrutinyReportArea" class="bg-white text-slate-900 p-5 rounded border border-slate-300 space-y-4 font-sans text-xs">
        <!-- AICTE Emblem & Ministry Header -->
        <div class="border-b-2 border-slate-800 pb-3 flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded border border-slate-800 flex items-center justify-center font-bold text-slate-800 text-xs">
              AICTE
            </div>
            <div>
              <div class="font-bold text-sm tracking-wide uppercase text-slate-900">ALL INDIA COUNCIL FOR TECHNICAL EDUCATION</div>
              <div class="text-[11px] text-slate-600 font-medium">Ministry of Education, Government of India | Approval Process AY 2026-27</div>
              <div class="text-[10px] text-slate-500 font-mono">Regulatory Decision-Support System (Problem Statement: SIH 26261)</div>
            </div>
          </div>
          <div class="text-right font-mono text-[11px]">
            <span class="px-2 py-0.5 rounded font-bold bg-red-100 text-red-800 border border-red-300">DEFICIENT</span>
            <div class="text-slate-500 text-[10px] mt-0.5">Date: 2026-10-25</div>
          </div>
        </div>

        <!-- Institution Summary -->
        <div class="grid grid-cols-2 sm:grid-cols-4 gap-2 p-2.5 rounded bg-slate-50 border border-slate-200 text-[11px] font-mono">
          <div>
            <span class="text-slate-500 block text-[10px]">Institution PID:</span>
            <span class="font-bold text-slate-900">1-33249112</span>
          </div>
          <div>
            <span class="text-slate-500 block text-[10px]">Institution Name:</span>
            <span class="font-bold text-slate-900">Apex Institute of Technology</span>
          </div>
          <div>
            <span class="text-slate-500 block text-[10px]">Application ID:</span>
            <span class="font-bold text-slate-900">APP-2026-101</span>
          </div>
          <div>
            <span class="text-slate-500 block text-[10px]">Region / State:</span>
            <span class="font-bold text-slate-900">WRO / Maharashtra</span>
          </div>
        </div>

        <!-- 6 Mandatory Documents Summary Table -->
        <div class="space-y-1.5">
          <div class="text-xs font-bold uppercase tracking-wider text-slate-800 border-b border-slate-300 pb-1">1. Mandatory Regulatory Documents Audit (6 Dossiers)</div>
          <table class="w-full text-left font-mono text-[11px] border border-slate-200">
            <thead class="bg-slate-100 border-b border-slate-200 text-slate-700">
              <tr>
                <th class="py-1.5 px-2">Document Type</th>
                <th class="py-1.5 px-2">Uploaded File Name</th>
                <th class="py-1.5 px-2">Verification Status</th>
                <th class="py-1.5 px-2">SHA-256 Digest</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-200">
              <tr>
                <td class="py-1.5 px-2 font-sans font-semibold">Fire Safety NOC</td>
                <td class="py-1.5 px-2">SYNTH_FIRE_NOC_MH_2026.pdf</td>
                <td class="py-1.5 px-2 text-emerald-700 font-bold">Valid (Active)</td>
                <td class="py-1.5 px-2 text-slate-500">e3b0c442...b855</td>
              </tr>
              <tr class="bg-red-50/50">
                <td class="py-1.5 px-2 font-sans font-semibold text-red-900">Land Ownership Deed</td>
                <td class="py-1.5 px-2 text-red-800">SYNTH_LAND_DEED_PUNE_ALTERED.pdf</td>
                <td class="py-1.5 px-2 text-red-700 font-bold">Altered (ELA: 88.4%)</td>
                <td class="py-1.5 px-2 text-slate-500">9b8769a4...3319</td>
              </tr>
              <tr class="bg-amber-50/50">
                <td class="py-1.5 px-2 font-sans font-semibold text-amber-900">University Affiliation Letter</td>
                <td class="py-1.5 px-2 text-amber-800">SYNTH_SPPU_AFFILIATION_2026.pdf</td>
                <td class="py-1.5 px-2 text-amber-700 font-bold">Intake Mismatch (120 vs 180)</td>
                <td class="py-1.5 px-2 text-slate-500">4a2d81f0...5521</td>
              </tr>
              <tr class="bg-red-50/50">
                <td class="py-1.5 px-2 font-sans font-semibold text-red-900">Faculty Roster &amp; PAN Dossier</td>
                <td class="py-1.5 px-2 text-red-800">SYNTH_FACULTY_ROSTER_DUPLICATE_PAN.pdf</td>
                <td class="py-1.5 px-2 text-red-700 font-bold">Duplicate PAN Detected</td>
                <td class="py-1.5 px-2 text-slate-500">7c9d4a19...9942</td>
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

        <!-- Flagged Deficiencies Section -->
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

  <!-- Fallback ID alias for legacy scripts -->
  <div id="aicteScrutinyReportModal" style="display:none;" data-alias="reportModal"></div>

  """

    # Locate where the static modals should go: right before </body>
    idx_modals = -1
    for m_cand in [
        "<!-- =====================================================================\n       STATIC MODALS",
        "<!-- =====================================================================\n       MODAL 1: INTERACTIVE COMPUTER VISION",
        "<!-- =====================================================================\n       MODULE 1: INTERACTIVE COMPUTER VISION",
        '<div id="cvEvidenceModal"'
    ]:
        idx = html.find(m_cand)
        if idx != -1:
            idx_modals = idx
            break

    idx_body_close = html.rfind("</body>")
    if idx_modals != -1 and idx_body_close != -1:
        html = html[:idx_modals] + modals_markup + html[idx_body_close:]
        print(f"Injected static modals right before </body> in {fpath}")

    # Write out updated file
    with open(fpath, "w", encoding="utf-8") as f:
        f.write(html)
    print(f"Successfully processed {fpath}")

for f in target_files:
    apply_fixes_to_file(f)

print("All target files updated.")
