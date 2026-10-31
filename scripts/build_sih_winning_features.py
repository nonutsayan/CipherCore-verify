import re
import os
import sys
import shutil

def main():
    with open('frontend/index.html', 'r', encoding='utf-8') as f:
        html = f.read()

    # =========================================================================
    # 1. ADD PRINT CSS FOR SCRUTINY REPORT
    # =========================================================================
    print_css = '''
    /* Print Styles for AICTE Regulatory Scrutiny Report */
    @media print {
      body * {
        visibility: hidden !important;
      }
      #aicteScrutinyReportModal, #aicteScrutinyReportModal * {
        visibility: visible !important;
      }
      #aicteScrutinyReportModal {
        position: absolute !important;
        left: 0 !important;
        top: 0 !important;
        width: 100% !important;
        height: auto !important;
        background: #ffffff !important;
        color: #000000 !important;
        padding: 0 !important;
        margin: 0 !important;
        z-index: 999999 !important;
      }
      .no-print, .no-print * {
        display: none !important;
      }
      .print-page-break {
        page-break-after: always;
      }
    }
'''
    if '@media print' not in html:
        html = html.replace('</style>', print_css + '\n  </style>')

    # =========================================================================
    # 2. UPDATE APPLICATION DOSSIER TOP BAR & OFFICIAL DECISION MODAL (MODULE 4)
    # =========================================================================
    # In Application Dossier Top Bar, add "Generate AICTE Scrutiny Report" button
    report_btn_html = '''          <button onclick="openAicteScrutinyReportModal()" class="btn-primary flex items-center gap-1.5 text-xs bg-[#0284c7] text-white">
            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <span>Generate AICTE Scrutiny Report</span>
          </button>
'''
    if 'openAicteScrutinyReportModal()' not in html:
        # Insert before exportCurrentApplicationJson button
        html = html.replace(
            '<button onclick="exportCurrentApplicationJson()"',
            report_btn_html + '          <button onclick="exportCurrentApplicationJson()"'
        )

    # In officialDecisionModal footer, add preview report button on left
    if 'Preview AICTE Scrutiny Report' not in html:
        decision_modal_footer_target = '''<div class="flex items-center justify-end gap-2 pt-2 border-t border-line">
        <button onclick="closeOfficialDecisionModal()" class="btn-secondary text-xs">Cancel</button>'''
        
        decision_modal_footer_replacement = '''<div class="flex items-center justify-between gap-2 pt-2 border-t border-line">
        <button onclick="openAicteScrutinyReportModal()" type="button" class="btn-secondary text-xs flex items-center gap-1.5">
          <svg class="w-3.5 h-3.5 text-[#0284c7]" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
          <span>Preview AICTE Scrutiny Report</span>
        </button>
        <div class="flex items-center gap-2">
          <button onclick="closeOfficialDecisionModal()" class="btn-secondary text-xs">Cancel</button>'''
        html = html.replace(decision_modal_footer_target, decision_modal_footer_replacement)

    # =========================================================================
    # 3. SUB-TAB 2 (OCR): ADD FORENSIC DIFF BUTTON (MODULE 2)
    # =========================================================================
    # We locate app.ocrChecks rendering in renderInspectView and add the button for Survey / Land Area
    old_ocr_render = '''                  ${app.ocrChecks.map(row => `
                    <tr>
                      <td class="py-3 px-3 font-semibold text-main">${row.field}</td>
                      <td class="py-3 px-3 font-mono text-main">${row.formValue}</td>
                      <td class="py-3 px-3 font-mono text-main">${row.extractedValue}</td>
                      <td class="py-3 px-3">${renderStatusTag(row.status)}</td>
                      <td class="py-3 px-3 text-sub">${row.note}</td>
                    </tr>
                  `).join('')}'''

    new_ocr_render = '''                  ${app.ocrChecks.map(row => {
                    const isLandDiff = row.field.toLowerCase().includes('land') || row.field.toLowerCase().includes('area') || row.field.toLowerCase().includes('survey');
                    return `
                      <tr>
                        <td class="py-3 px-3 font-semibold text-main">${row.field}</td>
                        <td class="py-3 px-3 font-mono text-main">${row.formValue}</td>
                        <td class="py-3 px-3 font-mono text-main">${row.extractedValue}</td>
                        <td class="py-3 px-3">${renderStatusTag(row.status)}</td>
                        <td class="py-3 px-3 text-sub">
                          <div class="flex items-center justify-between gap-2">
                            <span>${row.note}</span>
                            ${isLandDiff ? `
                              <button onclick="openForensicDiffModal()" class="btn-primary py-1 px-2.5 text-xs shrink-0 flex items-center gap-1.5" style="border-radius:4px;">
                                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                  <path stroke-linecap="round" stroke-linejoin="round" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
                                </svg>
                                <span>Inspect Forensic Diff</span>
                              </button>
                            ` : ''}
                          </div>
                        </td>
                      </tr>
                    `;
                  }).join('')}'''

    html = html.replace(old_ocr_render, new_ocr_render)

    # =========================================================================
    # 4. SUB-TAB 4 (INFRA): ADD CV EVIDENCE INSPECTION BUTTON (MODULE 1)
    # =========================================================================
    old_infra_render = '''                  ${app.infraChecks.map(item => `
                    <tr>
                      <td class="py-3 px-3 font-semibold text-main">${item.item}</td>
                      <td class="py-3 px-3 font-mono text-sub">${item.requirement}</td>
                      <td class="py-3 px-3 font-mono text-main">${item.submitted}</td>
                      <td class="py-3 px-3">${renderStatusTag(item.status)}</td>
                    </tr>
                  `).join('')}'''

    new_infra_render = '''                  ${app.infraChecks.map(item => {
                    const isLabWorkstation = item.item.toLowerCase().includes('workstation') || item.item.toLowerCase().includes('computer');
                    return `
                      <tr>
                        <td class="py-3 px-3 font-semibold text-main">${item.item}</td>
                        <td class="py-3 px-3 font-mono text-sub">${item.requirement}</td>
                        <td class="py-3 px-3 font-mono text-main">
                          <div class="flex items-center justify-between gap-2">
                            <span>${item.submitted}</span>
                            ${isLabWorkstation ? `
                              <button onclick="openCvInspectionModal()" class="btn-primary py-1 px-2.5 text-xs shrink-0 flex items-center gap-1.5" style="border-radius:4px;">
                                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                  <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                  <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                </svg>
                                <span>View CV Evidence Inspection</span>
                              </button>
                            ` : ''}
                          </div>
                        </td>
                        <td class="py-3 px-3">${renderStatusTag(item.status)}</td>
                      </tr>
                    `;
                  }).join('')}'''

    html = html.replace(old_infra_render, new_infra_render)

    # =========================================================================
    # 5. ADD THE 3 NEW MODALS BEFORE </body>
    # =========================================================================
    new_modals_html = '''
  <!-- =====================================================================
       MODULE 1: INTERACTIVE COMPUTER VISION (CV) LAB & INFRASTRUCTURE MODAL
       ===================================================================== -->
  <div id="cvInspectionModal" class="fixed inset-0 z-50 hidden bg-black/60 flex items-center justify-center p-3 sm:p-4">
    <div class="card-surface max-w-5xl w-full p-5 space-y-4 shadow-2xl border border-line rounded flex flex-col max-h-[92vh]">
      <!-- Header -->
      <div class="flex items-center justify-between border-b border-line pb-3">
        <div>
          <div class="flex items-center gap-2">
            <span class="px-2 py-0.5 rounded text-[10px] font-bold font-mono bg-[#0284c7]/10 text-[#0284c7] border border-[#0284c7]/30">YOLOv8x Edge CV</span>
            <h3 class="text-sm font-bold text-main">AI Computer Vision Infrastructure Audit</h3>
          </div>
          <p class="text-[11px] text-sub mt-0.5">Automated spatial &amp; workstation asset scrutiny (Edge Object Detection &amp; Geofence Verification).</p>
        </div>
        <button onclick="closeCvInspectionModal()" class="btn-secondary px-2.5 py-1 text-xs" style="border-radius:4px;">Close</button>
      </div>

      <!-- Top Metadata Bar -->
      <div class="p-2.5 rounded subtle-surface border border-line text-xs grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-2 font-mono">
        <div>
          <span class="text-sub block text-[10px] uppercase font-semibold">Target Facility:</span>
          <span class="text-main font-bold">Apex IT - Lab 3 (CCF)</span>
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
        <!-- Left: Interactive Canvas Viewport (8 Cols) -->
        <div class="lg:col-span-8 flex flex-col space-y-2">
          <div class="relative w-full rounded border border-slate-700 overflow-hidden bg-slate-900 shadow-inner flex items-center justify-center">
            <canvas id="cvInspectionCanvas" width="760" height="440" class="w-full h-auto block"></canvas>
            
            <!-- Live Model Pill -->
            <div class="absolute top-2.5 left-2.5 px-2 py-0.5 rounded bg-black/75 text-emerald-400 font-mono text-[10px] border border-emerald-500/40 flex items-center gap-1.5">
              <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
              <span>LIVE INFERENCE: YOLOv8x-SIH26261 (18.4ms)</span>
            </div>
          </div>

          <!-- Canvas Viewport Controls -->
          <div class="flex flex-wrap items-center justify-between gap-2 p-2 rounded subtle-surface border border-line text-xs">
            <div class="flex items-center gap-2">
              <button id="cvToggleBoxesBtn" onclick="toggleCvBoundingBoxes()" class="btn-primary py-1 px-2.5 text-xs" style="border-radius:4px;">
                Toggle Bounding Boxes (Active)
              </button>
              <button id="cvToggleHeatmapBtn" onclick="toggleCvHeatmap()" class="btn-secondary py-1 px-2.5 text-xs" style="border-radius:4px;">
                Toggle Heatmap Overlay
              </button>
            </div>
            <div class="text-[11px] font-mono text-sub">
              Confidence Threshold: <span class="font-bold text-main">&gt;= 0.85</span>
            </div>
          </div>
        </div>

        <!-- Right: CV Inference Stats Drawer (4 Cols) -->
        <div class="lg:col-span-4 space-y-3 text-xs">
          <!-- Metric 1: Workstation Count -->
          <div class="p-3 rounded subtle-surface border border-line space-y-1.5">
            <div class="flex items-center justify-between">
              <span class="text-sub font-semibold">1. Workstation Count:</span>
              <span class="tag-red font-bold">Deficient (-8 Units)</span>
            </div>
            <div class="text-lg font-mono font-bold text-main">
              142 Terminals <span class="text-xs text-sub font-normal">/ 150 Req.</span>
            </div>
            <div class="w-full bg-slate-200 h-1.5 rounded overflow-hidden">
              <div class="bg-amber-500 h-full" style="width: 94.6%;"></div>
            </div>
            <div class="text-[11px] text-sub">
              APH Intake Norm requires 150 active terminals for 180 CSE intake. Shortfall of 8 units detected.
            </div>
          </div>

          <!-- Metric 2: Terminal Spacing -->
          <div class="p-3 rounded subtle-surface border border-line space-y-1">
            <div class="flex items-center justify-between">
              <span class="text-sub font-semibold">2. Terminal Spacing:</span>
              <span class="tag-green font-bold">Compliant</span>
            </div>
            <div class="text-sm font-mono font-bold text-main">
              1.2m Carpet Area / Terminal
            </div>
            <div class="text-[11px] text-sub">
              Calculated floor area complies with AICTE minimum specification (1.0m per workstation).
            </div>
          </div>

          <!-- Metric 3: Safety Assets -->
          <div class="p-3 rounded subtle-surface border border-line space-y-1">
            <div class="flex items-center justify-between">
              <span class="text-sub font-semibold">3. Safety Assets:</span>
              <span class="tag-amber font-bold">Warning</span>
            </div>
            <ul class="text-[11px] space-y-1 text-main">
              <li class="flex items-center gap-1.5 text-emerald-700">
                <svg class="w-3.5 h-3.5 text-emerald-600 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/></svg>
                <span>2 Fire Extinguishers (Class ABC, Hydrotested)</span>
              </li>
              <li class="flex items-center gap-1.5 text-amber-700">
                <svg class="w-3.5 h-3.5 text-amber-600 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>
                <span>Missing Emergency Exit Luminaire above rear door</span>
              </li>
            </ul>
          </div>

          <!-- Action Button -->
          <button onclick="attachCvFindingToDossier()" class="btn-primary w-full py-2 text-xs flex items-center justify-center gap-1.5" style="border-radius:4px;">
            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
            </svg>
            <span>Attach Finding to Official Scrutiny Dossier</span>
          </button>
        </div>
      </div>

      <!-- Footer -->
      <div class="flex items-center justify-between pt-2 border-t border-line text-xs">
        <span class="text-[11px] text-sub font-mono">SIH Problem ID: 26261 | AICTE Regulatory Decision-Support</span>
        <button onclick="closeCvInspectionModal()" class="btn-secondary text-xs" style="border-radius:4px;">Close Inspection</button>
      </div>
    </div>
  </div>

  <!-- =====================================================================
       MODULE 2: SIDE-BY-SIDE FORENSIC DOCUMENT DIFF MODAL
       ===================================================================== -->
  <div id="forensicDiffModal" class="fixed inset-0 z-50 hidden bg-black/60 flex items-center justify-center p-3 sm:p-4">
    <div class="card-surface max-w-5xl w-full p-5 space-y-4 shadow-2xl border border-line rounded flex flex-col max-h-[92vh]">
      <!-- Header -->
      <div class="flex items-center justify-between border-b border-line pb-3">
        <div>
          <div class="flex items-center gap-2">
            <span class="px-2 py-0.5 rounded text-[10px] font-bold font-mono bg-red-100 text-red-800 border border-red-300">Forensic Tamper Detection</span>
            <h3 class="text-sm font-bold text-main">Cryptographic &amp; Forensic Document Diff Inspector</h3>
          </div>
          <p class="text-[11px] text-sub mt-0.5">Dual-layer side-by-side raster overlay &amp; font metrics discrepancy analysis (DocID: DEED-REV-MH-4419).</p>
        </div>
        <button onclick="closeForensicDiffModal()" class="btn-secondary px-2.5 py-1 text-xs" style="border-radius:4px;">Close</button>
      </div>

      <!-- Side-by-Side Comparison Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 flex-1 overflow-y-auto">
        <!-- Left: Submitted PDF (Altered) -->
        <div class="p-4 rounded border-2 border-red-400 bg-red-50/20 space-y-3">
          <div class="flex items-center justify-between border-b border-red-200 pb-2">
            <div>
              <span class="tag-red font-bold text-[10px]">Institution Submitted PDF</span>
              <div class="font-mono text-[11px] text-main font-semibold mt-0.5">land_ownership_deed.pdf</div>
            </div>
            <span class="px-2 py-0.5 rounded text-[10px] font-mono bg-red-100 text-red-800 border border-red-300 font-bold">Altered Layer</span>
          </div>

          <!-- Document Snippet Mock -->
          <div class="p-3 bg-white rounded border border-line text-xs font-serif space-y-2 text-slate-800 shadow-xs">
            <div class="text-center border-b border-line pb-1.5">
              <div class="font-bold text-[11px] uppercase tracking-wider text-slate-900">Government of Maharashtra - Revenue Dept</div>
              <div class="text-[10px] text-slate-500 font-sans">Index-II Property Registration Certificate</div>
            </div>

            <div class="space-y-1.5 text-[11px]">
              <div><span class="text-slate-500 font-sans">Village / Taluka:</span> Pashan / Haveli, Pune</div>
              <div><span class="text-slate-500 font-sans">Registered Holder:</span> Apex Educational Trust, Pune</div>
              
              <!-- Tampered Area Highlight -->
              <div class="p-2.5 rounded border-2 border-red-500 bg-red-50 space-y-1">
                <div class="text-[10px] font-mono text-red-700 font-bold uppercase">Altered Text Stream (Post-Issuance):</div>
                <div class="font-mono font-bold text-red-900 text-xs">
                  Survey No: 42/B (Acreage: 5.20 Acres)
                </div>
                <div class="text-[10px] text-red-700 font-sans">
                  Forensic Finding: Helvetica-Bold overlay applied over scanned Courier New baseline.
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

        <!-- Right: DigiLocker Record (Authentic) -->
        <div class="p-4 rounded border-2 border-emerald-400 bg-emerald-50/20 space-y-3">
          <div class="flex items-center justify-between border-b border-emerald-200 pb-2">
            <div>
              <span class="tag-green font-bold text-[10px]">DigiLocker / State Registry</span>
              <div class="font-mono text-[11px] text-main font-semibold mt-0.5">DL-REV-MH-4419 (Live Government API)</div>
            </div>
            <span class="px-2 py-0.5 rounded text-[10px] font-mono bg-emerald-100 text-emerald-800 border border-emerald-300 font-bold">Authentic Source</span>
          </div>

          <!-- Document Snippet Mock -->
          <div class="p-3 bg-white rounded border border-line text-xs font-serif space-y-2 text-slate-800 shadow-xs">
            <div class="text-center border-b border-line pb-1.5">
              <div class="font-bold text-[11px] uppercase tracking-wider text-slate-900">Government of Maharashtra - Revenue Dept</div>
              <div class="text-[10px] text-slate-500 font-sans">State Land Registry Portal (Mahabhulekh 7/12 Extract)</div>
            </div>

            <div class="space-y-1.5 text-[11px]">
              <div><span class="text-slate-500 font-sans">Village / Taluka:</span> Pashan / Haveli, Pune</div>
              <div><span class="text-slate-500 font-sans">Registered Holder:</span> Apex Educational Trust, Pune</div>

              <!-- Authentic Area Highlight -->
              <div class="p-2.5 rounded border-2 border-emerald-500 bg-emerald-50 space-y-1">
                <div class="text-[10px] font-mono text-emerald-700 font-bold uppercase">Official Verified Registry Extract:</div>
                <div class="font-mono font-bold text-emerald-900 text-xs">
                  Survey No: 42/A (Acreage: 2.10 Acres)
                </div>
                <div class="text-[10px] text-emerald-700 font-sans">
                  Cryptographically verified via State Revenue Directorate digital signature.
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

      <!-- Bottom Forensic Proof Cards -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-2.5 text-xs">
        <div class="p-2.5 rounded subtle-surface border border-line space-y-1">
          <div class="font-bold text-main text-[11px]">1. Font Encoding Mismatch</div>
          <p class="text-[11px] text-sub">Helvetica-Bold overlay applied over scanned Courier New baseline. Glyph baseline offset: +4.2pt.</p>
        </div>
        <div class="p-2.5 rounded subtle-surface border border-line space-y-1">
          <div class="font-bold text-main text-[11px]">2. PDF Metadata Discrepancy</div>
          <p class="text-[11px] text-sub">Modified with "Adobe Acrobat Pro v24.2" 14 days after issuing date (Creation: 2024-03-10 | Mod: 2024-03-24).</p>
        </div>
        <div class="p-2.5 rounded subtle-surface border border-line space-y-1">
          <div class="font-bold text-main text-[11px]">3. Hash &amp; Layer Discrepancy</div>
          <p class="text-[11px] text-sub">Submitted PDF hash does not match DigiLocker issuance hash. Root signature broken.</p>
        </div>
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
       MODULE 4: 1-CLICK OFFICIAL AICTE SCRUTINY DEFICIENCY REPORT MODAL
       ===================================================================== -->
  <div id="aicteScrutinyReportModal" class="fixed inset-0 z-50 hidden bg-black/60 flex items-center justify-center p-3 sm:p-4">
    <div class="card-surface max-w-4xl w-full p-6 space-y-5 shadow-2xl border border-line rounded flex flex-col max-h-[94vh]">
      <!-- Controls Bar (Hidden in Print) -->
      <div class="flex items-center justify-between border-b border-line pb-3 no-print">
        <div>
          <span class="px-2 py-0.5 rounded text-[10px] font-bold font-mono bg-[#0284c7]/10 text-[#0284c7] border border-[#0284c7]/30">Official Regulatory Document</span>
          <h3 class="text-sm font-bold text-main">AICTE Scrutiny Committee Assessment Report</h3>
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
              <path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
            </svg>
            <span>Export JSON</span>
          </button>
          <button onclick="closeAicteScrutinyReportModal()" class="btn-secondary px-2.5 py-1 text-xs" style="border-radius:4px;">Close</button>
        </div>
      </div>

      <!-- Printable Regulatory Document Body -->
      <div id="aictePrintableDocument" class="bg-white p-6 rounded border border-line text-slate-900 space-y-5 overflow-y-auto font-sans leading-normal">
        <!-- Official Government Header -->
        <div class="flex items-center justify-between border-b-2 border-slate-900 pb-4">
          <div class="flex items-center gap-3">
            <!-- AICTE Emblem Vector Shield -->
            <div class="w-12 h-12 flex-shrink-0 flex items-center justify-center rounded bg-slate-900 text-white font-bold text-xl">
              <svg class="w-8 h-8 text-amber-400" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2L3 7v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V7l-9-5zm0 4a3 3 0 110 6 3 3 0 010-6zm5 11.2c-1.3-.8-3.1-1.2-5-1.2s-3.7.4-5 1.2V15c1.4-.9 3.3-1.4 5-1.4s3.6.5 5 1.4v2.2z"/>
              </svg>
            </div>
            <div>
              <div class="text-sm font-bold tracking-wide uppercase text-slate-950 font-serif">All India Council for Technical Education</div>
              <div class="text-[11px] text-slate-600 font-serif">A Statutory Body of the Ministry of Education, Government of India</div>
              <div class="text-xs font-bold text-[#0284c7] mt-0.5">Scrutiny Committee Assessment Report (AY 2026-27)</div>
            </div>
          </div>
          <div class="text-right text-[11px] font-mono">
            <div class="font-bold text-slate-900">SIH PS ID: 26261</div>
            <div class="text-slate-500">Ref: AICTE-WRO/2026/SCR-8812</div>
            <div class="text-slate-500">Date: 2026-10-25 11:24 UTC</div>
          </div>
        </div>

        <!-- Section 1: Institution & Application Summary Box -->
        <div class="p-3.5 bg-slate-50 border border-slate-300 rounded text-xs space-y-2">
          <div class="font-bold uppercase tracking-wider text-slate-800 text-[11px] border-b border-slate-200 pb-1">
            Section I: Institution &amp; Application Profile
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-x-4 gap-y-1.5 text-[11px]">
            <div><span class="text-slate-500">Permanent Institute ID:</span> <span class="font-mono font-bold text-slate-900">1-3329012411</span></div>
            <div><span class="text-slate-500">Regional Office:</span> <span class="font-semibold text-slate-900">Western Regional Office (WRO, Mumbai)</span></div>
            <div><span class="text-slate-500">Institution Name:</span> <span class="font-bold text-slate-950">Apex Institute of Technology, Pune</span></div>
            <div><span class="text-slate-500">Application Category:</span> <span class="font-semibold text-slate-900">Extension of Approval (EoA) &amp; CSE Intake Increase</span></div>
            <div><span class="text-slate-500">Scrutiny Assessment Status:</span> <span class="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-900 border border-amber-300">Needs Review (Conditional Deficiencies Flagged)</span></div>
            <div><span class="text-slate-500">Inspecting Officer ID:</span> <span class="font-mono font-bold text-[#0284c7]">AICTE-OFFICER-782 (Dr. Arvind Kumar)</span></div>
          </div>
        </div>

        <!-- Section 2: 6 Mandatory Documents Summary Table -->
        <div class="space-y-1.5 text-xs">
          <div class="font-bold uppercase tracking-wider text-slate-800 text-[11px]">
            Section II: Mandatory Statutory Documents Verification Status
          </div>
          <table class="w-full text-left border-collapse text-[11px] border border-slate-300">
            <thead>
              <tr class="bg-slate-100 border-b border-slate-300 text-slate-700 font-mono text-[10px] uppercase">
                <th class="p-2 border-r border-slate-300">#</th>
                <th class="p-2 border-r border-slate-300">Mandatory Document</th>
                <th class="p-2 border-r border-slate-300">Issuing Authority Gateway</th>
                <th class="p-2 border-r border-slate-300">SHA-256 Fingerprint</th>
                <th class="p-2">Scrutiny Verdict</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-200">
              <tr>
                <td class="p-2 font-mono border-r border-slate-300">1</td>
                <td class="p-2 font-semibold border-r border-slate-300">Fire Safety NOC</td>
                <td class="p-2 font-mono text-slate-600 border-r border-slate-300">Maharashtra Fire Portal (MFS-PUN-2023)</td>
                <td class="p-2 font-mono text-slate-500 text-[10px] border-r border-slate-300">e82a...91b2</td>
                <td class="p-2 font-bold text-red-600">Expired (2024-12-31)</td>
              </tr>
              <tr>
                <td class="p-2 font-mono border-r border-slate-300">2</td>
                <td class="p-2 font-semibold border-r border-slate-300">Land Ownership Deed</td>
                <td class="p-2 font-mono text-slate-600 border-r border-slate-300">DigiLocker / State Revenue Gateway</td>
                <td class="p-2 font-mono text-slate-500 text-[10px] border-r border-slate-300">3a9e...8b42</td>
                <td class="p-2 font-bold text-red-600">Acreage Mismatch (1.80 vs 2.50)</td>
              </tr>
              <tr>
                <td class="p-2 font-mono border-r border-slate-300">3</td>
                <td class="p-2 font-semibold border-r border-slate-300">University Affiliation NOC</td>
                <td class="p-2 font-mono text-slate-600 border-r border-slate-300">SPPU Affiliation API Gateway</td>
                <td class="p-2 font-mono text-slate-500 text-[10px] border-r border-slate-300">9b3c...4a21</td>
                <td class="p-2 font-bold text-emerald-600">Verified &amp; Valid</td>
              </tr>
              <tr>
                <td class="p-2 font-mono border-r border-slate-300">4</td>
                <td class="p-2 font-semibold border-r border-slate-300">Faculty Roster &amp; Form 16</td>
                <td class="p-2 font-mono text-slate-600 border-r border-slate-300">TRACES / DSC Trust Service</td>
                <td class="p-2 font-mono text-slate-500 text-[10px] border-r border-slate-300">c41a...7102</td>
                <td class="p-2 font-bold text-emerald-600">Verified (1:15 Ratio Pass)</td>
              </tr>
              <tr>
                <td class="p-2 font-mono border-r border-slate-300">5</td>
                <td class="p-2 font-semibold border-r border-slate-300">Building Sanction Plan</td>
                <td class="p-2 font-mono text-slate-600 border-r border-slate-300">PMC Town Planning Directorate</td>
                <td class="p-2 font-mono text-slate-500 text-[10px] border-r border-slate-300">f910...2281</td>
                <td class="p-2 font-bold text-emerald-600">Compliant (1,240 Sq.m)</td>
              </tr>
              <tr>
                <td class="p-2 font-mono border-r border-slate-300">6</td>
                <td class="p-2 font-semibold border-r border-slate-300">Scrutiny Fee Receipt</td>
                <td class="p-2 font-mono text-slate-600 border-r border-slate-300">Bharat Kosh Payment Gateway</td>
                <td class="p-2 font-mono text-slate-500 text-[10px] border-r border-slate-300">d120...9911</td>
                <td class="p-2 font-bold text-emerald-600">Reconciled (Rs. 2,00,000)</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Section 3: Flagged Deficiencies -->
        <div class="p-3.5 bg-red-50/50 border border-red-200 rounded text-xs space-y-2">
          <div class="font-bold uppercase tracking-wider text-red-900 text-[11px] border-b border-red-200 pb-1">
            Section III: Summary of Flagged Deficiencies &amp; Forensic Findings
          </div>
          <ul class="space-y-1.5 text-[11px] text-red-900 list-disc list-inside">
            <li><strong>Land Acreage Shortfall &amp; Layer Alteration:</strong> Revenue land deed states 1.80 Acres contiguous land whereas 2.50 Acres was claimed. Forensic font inspection revealed a Helvetica-Bold raster overlay on Survey No 42/B.</li>
            <li><strong>Computer Center Workstation Shortfall:</strong> Computer Vision inspection of CCF Lab-3 detected 142 operational terminals against the prescribed APH norm of 150 terminals (-8 terminals deficit).</li>
            <li><strong>Fire NOC Validity Gap:</strong> State Fire Portal indicates Fire Certificate expired on 2024-12-31 without proof of statutory renewal.</li>
            <li><strong>Safety Asset Warning:</strong> Missing emergency luminaire above rear egress path in computing facility.</li>
          </ul>
        </div>

        <!-- Section 4: Blockchain Stamp & Verification QR Code -->
        <div class="flex flex-col sm:flex-row items-center justify-between gap-4 p-3.5 bg-slate-50 border border-slate-300 rounded text-xs">
          <!-- Blockchain Notarization Stamp -->
          <div class="space-y-1 text-[11px] font-mono">
            <div class="font-bold text-slate-800 uppercase tracking-wider">Blockchain PoA Notarization Stamp:</div>
            <div>Notarized Block: <span class="font-bold text-[#0284c7]">Block #4 (Consortium Ledger)</span></div>
            <div>Transaction Hash: <span class="text-slate-600 text-[10px] break-all">0x7a8f192b8d0c41258671e982103847a82b910123</span></div>
            <div>Signed By: <span class="font-bold text-slate-900">Dr. Arvind Kumar (AICTE Scrutiny Officer)</span></div>
            <div>Proof-of-Authority Network: <span class="text-emerald-700 font-semibold">BharatVidya Consortium (7 Nodes Active)</span></div>
          </div>

          <!-- Verifiable SVG QR Code -->
          <div class="flex flex-col items-center shrink-0 text-center space-y-1">
            <div class="p-1.5 bg-white border border-slate-300 rounded shadow-xs">
              <svg class="w-20 h-20 text-slate-900" viewBox="0 0 100 100" fill="currentColor">
                <!-- Outer Corners -->
                <rect x="5" y="5" width="28" height="28" fill="black"/>
                <rect x="9" y="9" width="20" height="20" fill="white"/>
                <rect x="13" y="13" width="12" height="12" fill="black"/>
                
                <rect x="67" y="5" width="28" height="28" fill="black"/>
                <rect x="71" y="9" width="20" height="20" fill="white"/>
                <rect x="75" y="13" width="12" height="12" fill="black"/>
                
                <rect x="5" y="67" width="28" height="28" fill="black"/>
                <rect x="9" y="71" width="20" height="20" fill="white"/>
                <rect x="13" y="75" width="12" height="12" fill="black"/>

                <!-- QR Data Matrix Pixels -->
                <rect x="37" y="10" width="6" height="6" fill="black"/>
                <rect x="47" y="10" width="6" height="6" fill="black"/>
                <rect x="57" y="10" width="6" height="6" fill="black"/>
                <rect x="37" y="20" width="6" height="6" fill="black"/>
                <rect x="47" y="24" width="6" height="6" fill="black"/>
                <rect x="10" y="37" width="6" height="6" fill="black"/>
                <rect x="20" y="37" width="6" height="6" fill="black"/>
                <rect x="37" y="37" width="8" height="8" fill="black"/>
                <rect x="53" y="37" width="6" height="6" fill="black"/>
                <rect x="67" y="37" width="6" height="6" fill="black"/>
                <rect x="83" y="37" width="6" height="6" fill="black"/>
                <rect x="43" y="47" width="6" height="6" fill="black"/>
                <rect x="63" y="47" width="6" height="6" fill="black"/>
                <rect x="73" y="47" width="6" height="6" fill="black"/>
                <rect x="37" y="57" width="6" height="6" fill="black"/>
                <rect x="53" y="57" width="8" height="8" fill="black"/>
                <rect x="73" y="57" width="6" height="6" fill="black"/>
                <rect x="37" y="73" width="6" height="6" fill="black"/>
                <rect x="47" y="73" width="6" height="6" fill="black"/>
                <rect x="63" y="73" width="6" height="6" fill="black"/>
                <rect x="73" y="73" width="6" height="6" fill="black"/>
                <rect x="83" y="73" width="6" height="6" fill="black"/>
                <rect x="47" y="83" width="8" height="8" fill="black"/>
                <rect x="63" y="83" width="6" height="6" fill="black"/>
              </svg>
            </div>
            <span class="text-[9px] font-mono text-slate-500">verify.ciphercore.in/audit?id=APP-2026-101</span>
          </div>
        </div>

        <!-- Mandatory Statutory Footer -->
        <div class="border-t border-slate-300 pt-3 text-[10px] text-slate-500 flex flex-col sm:flex-row items-center justify-between gap-1">
          <div>This is an official AICTE Scrutiny Committee Assessment record generated under SIH 2026.</div>
          <div>Page 1 of 1 | Authorized Electronic Signature</div>
        </div>
      </div>
    </div>
  </div>
'''

    if 'id="cvInspectionModal"' not in html:
        html = html.replace('</body>', new_modals_html + '\n</body>')

    # =========================================================================
    # 6. ENHANCE MODULE 3: BLOCKCHAIN DATA & TAMPER SIMULATION CASCADE
    # =========================================================================
    # Let's update `seedDemoBlockchainBlocks()` to match the exact 5 blocks specified in Module 3
    new_seed_blockchain_blocks = '''    async function seedDemoBlockchainBlocks() {
      if (cipherBlockchain.chain.length <= 1) {
        // Block #1: Ingestion
        await cipherBlockchain.addBlock('INGEST-5-APPS', 'DOSSIER_INGESTION', 'AICTE_ROOT_NOTARY', {
          title: 'Ingested 5 Application Dossiers',
          batchId: 'BATCH-SIH2026-AY2627',
          institutionsCount: 5,
          dossiers: ['APP-2026-101', 'APP-2026-102', 'APP-2026-103', 'APP-2026-104', 'APP-2026-105'],
          ingestionTimestamp: '2026-10-25 02:00:00 UTC'
        });

        // Block #2: Authority Match
        await cipherBlockchain.addBlock('FIRE-PORTAL-MH', 'AUTHORITY_MATCH', 'AICTE_GATEWAY_DAEMON', {
          title: 'State Fire Services API Validation',
          targetInstitution: 'Apex Institute of Technology (APP-2026-101)',
          refId: 'MFS-PUN-2023-4419',
          verifiedStatus: 'EXPIRED_REVOKED',
          issuingAuthority: 'Maharashtra Fire Services Gateway',
          expiryDate: '2024-12-31'
        });

        // Block #3: Anomaly Flagged
        await cipherBlockchain.addBlock('APP-2026-101', 'ANOMALY_FLAGGED', 'AICTE_FORENSIC_ENGINE', {
          title: 'Apex Institute Land Deed Alteration Flagged',
          documentId: 'DEED-REV-MH-4419',
          finding: 'Survey No 42/B (5.20 Acres) altered from original 42/A (2.10 Acres)',
          forensicConfidence: '99.8% Raster & Font Overlay Detected',
          producerAnomaly: 'Adobe Acrobat Pro v24.2'
        });

        // Block #4: Official Decision
        await cipherBlockchain.addBlock('APP-2026-101', 'DECISION_RECORDED', 'Dr. Arvind Kumar (AICTE Scrutiny Officer)', {
          title: 'Officer Dr. Arvind Kumar recorded decision on APP-2026-101',
          officialDecision: 'Needs Review',
          statutoryAction: 'Hearing notice issued for Land Deed alteration & Fire NOC expiry',
          officerSignoff: true,
          decisionTimestamp: '2026-10-25 02:30:12 UTC'
        });
      }
    }'''

    # Replace seedDemoBlockchainBlocks
    seed_pattern = re.compile(r'async function seedDemoBlockchainBlocks\(\)\s*\{.*?^\s*\}', re.DOTALL | re.MULTILINE)
    if seed_pattern.search(html):
        html = seed_pattern.sub(new_seed_blockchain_blocks, html)

    # Update simulateTamper to target Block #2 specifically and trigger cascade across #2, #3, and #4
    new_simulate_tamper = '''      simulateTamper(blockIndex = 2) {
        if (this.chain.length <= 2) return false;
        const targetBlock = this.chain[2];

        // Tamper Block #2 payload maliciously without recomputing the SHA-256 hash
        targetBlock.payloadData = {
          ...targetBlock.payloadData,
          verifiedStatus: 'VALID_CLEARED_FRAUDULENT',
          tamperExploit: 'MALICIOUS_OFFLINE_FIRE_NOC_OVERRIDE',
          auditViolation: 'CRITICAL_INTEGRITY_BREACH',
          unauthorizedBypass: 'Adverse Regulatory Sanction Illegally Suppressed'
        };

        this.isTampered = true;
        this.tamperedIndex = 2;
        return true;
      }'''

    sim_pattern = re.compile(r'simulateTamper\(blockIndex\s*=\s*\d+\)\s*\{.*?^\s*\}', re.DOTALL | re.MULTILINE)
    if sim_pattern.search(html):
        html = sim_pattern.sub(new_simulate_tamper, html)

    # Update triggerSimulateTamper and triggerChainIntegrityCheck for the prompt's exact alert text:
    # "TAMPER DETECTED: Hash mismatch at Block #2! Stored hash does not match computed payload hash. Chained cryptographic link broken."
    new_tamper_handlers = '''    async function triggerSimulateTamper() {
      const success = cipherBlockchain.simulateTamper(2);
      if (!success) {
        showToastBanner('Please ensure blockchain has blocks populated first.');
        return;
      }
      const result = await cipherBlockchain.verifyChainIntegrity();
      cipherBlockchain.isTampered = true;
      cipherBlockchain.tamperDetails = result;
      renderBlockchainLedgerUI();
      showToastBanner('TAMPER DETECTED: Hash mismatch at Block #2! Stored hash does not match computed payload hash. Chained cryptographic link broken.');
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
        showToastBanner('TAMPER DETECTED: Hash mismatch at Block #2! Stored hash does not match computed payload hash. Chained cryptographic link broken.');
      }
      renderBlockchainLedgerUI();
    }'''

    tamper_handlers_pattern = re.compile(r'async function triggerChainIntegrityCheck\(\)\s*\{.*?async function triggerSimulateTamper\(\)\s*\{.*?^\s*\}', re.DOTALL | re.MULTILINE)
    if tamper_handlers_pattern.search(html):
        html = tamper_handlers_pattern.sub(new_tamper_handlers, html)

    # Update renderBlockchainLedgerUI to flash RED on blocks >= 2 when tampered
    old_render_ui_loop = '''container.innerHTML = cipherBlockchain.chain.map((b, idx) => {
        const isTamperedBlock = cipherBlockchain.isTampered && b.index === cipherBlockchain.tamperedIndex;'''

    new_render_ui_loop = '''container.innerHTML = cipherBlockchain.chain.map((b, idx) => {
        // When Block #2 is tampered, the cryptographic link cascade breaks for Block #2, #3, and #4
        const isTamperedBlock = cipherBlockchain.isTampered && b.index >= (cipherBlockchain.tamperedIndex || 2);'''

    html = html.replace(old_render_ui_loop, new_render_ui_loop)

    # Also update the alert banner text in renderBlockchainLedgerUI
    old_alert_msg = '''CRITICAL ALERT: Block #${td.brokenIndex} Hash Mismatch!'''
    new_alert_msg = '''TAMPER DETECTED: Hash Mismatch at Block #2! Stored hash does not match computed payload hash. Chained cryptographic link broken.'''
    html = html.replace(old_alert_msg, new_alert_msg)

    # =========================================================================
    # 7. ADD JAVASCRIPT FUNCTIONS FOR MODULE 1, 2, 4 AND ESCAPE KEY HANDLING
    # =========================================================================
    module_scripts = '''
    // =====================================================================
    // MODULE 1: INTERACTIVE COMPUTER VISION (CV) CANVAS & MODAL ENGINE
    // =====================================================================
    let cvBoundingBoxesVisible = true;
    let cvHeatmapVisible = false;

    function openCvInspectionModal() {
      document.getElementById('cvInspectionModal').classList.remove('hidden');
      setTimeout(drawCvCanvas, 60);
    }

    function closeCvInspectionModal() {
      document.getElementById('cvInspectionModal').classList.add('hidden');
    }

    function toggleCvBoundingBoxes() {
      cvBoundingBoxesVisible = !cvBoundingBoxesVisible;
      const btn = document.getElementById('cvToggleBoxesBtn');
      if (btn) {
        btn.textContent = cvBoundingBoxesVisible ? 'Toggle Bounding Boxes (Active)' : 'Toggle Bounding Boxes (Off)';
        btn.className = cvBoundingBoxesVisible ? 'btn-primary py-1 px-2.5 text-xs' : 'btn-secondary py-1 px-2.5 text-xs';
      }
      drawCvCanvas();
      showToastBanner(`Computer Vision Bounding Boxes: ${cvBoundingBoxesVisible ? 'ON' : 'OFF'}`);
    }

    function toggleCvHeatmap() {
      cvHeatmapVisible = !cvHeatmapVisible;
      const btn = document.getElementById('cvToggleHeatmapBtn');
      if (btn) {
        btn.textContent = cvHeatmapVisible ? 'Toggle Heatmap (Active)' : 'Toggle Heatmap Overlay';
        btn.className = cvHeatmapVisible ? 'btn-primary py-1 px-2.5 text-xs' : 'btn-secondary py-1 px-2.5 text-xs';
      }
      drawCvCanvas();
      showToastBanner(`Heatmap Density View: ${cvHeatmapVisible ? 'ON' : 'OFF'}`);
    }

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
      ctx.fillRect(200, 20, 360, 90);
      ctx.strokeStyle = '#64748b';
      ctx.lineWidth = 3;
      ctx.strokeRect(200, 20, 360, 90);
      ctx.fillStyle = '#0284c7';
      ctx.font = 'bold 12px monospace';
      ctx.fillText('AICTE CCF LAB-3 | 180 INTAKE B.TECH CSE', 230, 65);

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
          const isEmptyBay = (r.y === 250 && (i === 2 || i === 4));
          if (!isEmptyBay) {
            ctx.fillStyle = '#0284c7';
            ctx.fillRect(bx + 12, by - 5, r.w - 24, r.h - 22);
            ctx.strokeStyle = '#94a3b8';
            ctx.lineWidth = 1.5;
            ctx.strokeRect(bx + 12, by - 5, r.w - 24, r.h - 22);

            // Stand
            ctx.fillStyle = '#64748b';
            ctx.fillRect(bx + r.w / 2 - 4, by + r.h - 27, 8, 8);
          } else {
            // Empty slot marker
            ctx.strokeStyle = '#f59e0b';
            ctx.setLineDash([4, 4]);
            ctx.strokeRect(bx + 10, by - 5, r.w - 20, r.h - 20);
            ctx.setLineDash([]);
          }

          // Chair
          ctx.fillStyle = '#1e293b';
          ctx.beginPath();
          ctx.arc(bx + r.w / 2, by + r.h + 5, 12, 0, Math.PI * 2);
          ctx.fill();
        }
      });

      // 2. Heatmap Density Overlay (If Enabled)
      if (cvHeatmapVisible) {
        ctx.save();
        ctx.globalAlpha = 0.45;
        const heatGradients = [
          { x: 200, y: 220, r: 120, color: 'rgba(239, 68, 68, 0.8)' },
          { x: 420, y: 240, r: 140, color: 'rgba(245, 158, 11, 0.8)' },
          { x: 600, y: 360, r: 130, color: 'rgba(16, 185, 129, 0.8)' }
        ];
        heatGradients.forEach(hg => {
          const rad = ctx.createRadialGradient(hg.x, hg.y, 10, hg.x, hg.y, hg.r);
          rad.addColorStop(0, hg.color);
          rad.addColorStop(1, 'rgba(0,0,0,0)');
          ctx.fillStyle = rad;
          ctx.beginPath();
          ctx.arc(hg.x, hg.y, hg.r, 0, Math.PI * 2);
          ctx.fill();
        });
        ctx.restore();
      }

      // 3. YOLOv8 Object Detection Bounding Boxes (If Enabled)
      if (cvBoundingBoxesVisible) {
        // Draw workstation boxes
        rows.forEach(r => {
          for (let i = 0; i < r.count; i++) {
            const bx = r.startX + i * r.spacing;
            const by = r.y;
            const isEmpty = (r.y === 250 && (i === 2 || i === 4));

            if (!isEmpty) {
              // Green Box: Terminal
              ctx.strokeStyle = '#10b981';
              ctx.lineWidth = 1.8;
              ctx.strokeRect(bx + 6, by - 10, r.w - 12, r.h + 20);

              ctx.fillStyle = '#10b981';
              ctx.fillRect(bx + 6, by - 22, 74, 12);
              ctx.fillStyle = '#ffffff';
              ctx.font = 'bold 9px monospace';
              ctx.fillText('terminal: 0.94', bx + 8, by - 13);
            } else {
              // Orange / Red Box: Unoccupied Bay Shortfall
              ctx.strokeStyle = '#f59e0b';
              ctx.lineWidth = 2;
              ctx.strokeRect(bx + 4, by - 12, r.w - 8, r.h + 24);

              ctx.fillStyle = '#f59e0b';
              ctx.fillRect(bx + 4, by - 24, 100, 12);
              ctx.fillStyle = '#000000';
              ctx.font = 'bold 9px monospace';
              ctx.fillText('unoccupied_bay: 0.88', bx + 6, by - 15);
            }
          }
        });

        // Blue Box: Fire Extinguisher
        ctx.strokeStyle = '#0284c7';
        ctx.lineWidth = 2;
        ctx.strokeRect(w - 62, 34, 32, 68);
        ctx.fillStyle = '#0284c7';
        ctx.fillRect(w - 62, 22, 114, 12);
        ctx.fillStyle = '#ffffff';
        ctx.font = 'bold 9px monospace';
        ctx.fillText('fire_extinguisher_ab: 0.96', w - 60, 31);

        // Dashed Yellow Box: Missing Exit Luminaire (above doorway)
        ctx.strokeStyle = '#eab308';
        ctx.lineWidth = 2;
        ctx.setLineDash([5, 4]);
        ctx.strokeRect(20, 8, 68, 24);
        ctx.setLineDash([]);
        ctx.fillStyle = '#eab308';
        ctx.fillRect(20, -2, 126, 12);
        ctx.fillStyle = '#000000';
        ctx.font = 'bold 9px monospace';
        ctx.fillText('missing_exit_luminaire: 0.91', 22, 7);
      }
    }

    function attachCvFindingToDossier() {
      logAuditEvent(activeUserSession?.name || 'CV Scrutiny Engine', 'APP-2026-101', 'CV Infrastructure Finding Attached', 'Attached shortfall (-8 workstations) and missing exit luminaire to Official Dossier.');
      showToastBanner('Attached CV infrastructure findings to Official Scrutiny Dossier.');
      closeCvInspectionModal();
    }

    // =====================================================================
    // MODULE 2: SIDE-BY-SIDE FORENSIC DOCUMENT DIFF VIEWER
    // =====================================================================
    function openForensicDiffModal() {
      document.getElementById('forensicDiffModal').classList.remove('hidden');
    }

    function closeForensicDiffModal() {
      document.getElementById('forensicDiffModal').classList.add('hidden');
    }

    function attachForensicProofToDossier() {
      logAuditEvent(activeUserSession?.name || 'Forensic Officer', 'APP-2026-101', 'Forensic Diff Proof Attached', 'Land Deed font overlay and acreage gap (1.80 vs 2.50) notarized as regulatory deficiency.');
      showToastBanner('Attached Forensic Land Deed proof to Official Dossier.');
      closeForensicDiffModal();
    }

    // =====================================================================
    // MODULE 4: 1-CLICK OFFICIAL AICTE SCRUTINY DEFICIENCY REPORT
    // =====================================================================
    function openAicteScrutinyReportModal() {
      document.getElementById('aicteScrutinyReportModal').classList.remove('hidden');
    }

    function closeAicteScrutinyReportModal() {
      document.getElementById('aicteScrutinyReportModal').classList.add('hidden');
    }

    function printAicteScrutinyReport() {
      window.print();
    }

    function exportScrutinyReportJson() {
      const app = applicationsData.find(a => a.id === currentInspectAppId) || applicationsData[0];
      const reportData = {
        title: 'AICTE Scrutiny Committee Assessment Report (AY 2026-27)',
        problemStatementId: '26261',
        regulatoryAuthority: 'All India Council for Technical Education',
        generatedAt: new Date().toISOString(),
        institution: app ? app.institution : 'Apex Institute of Technology, Pune',
        applicationId: app ? app.id : 'APP-2026-101',
        category: 'Extension of Approval (EoA) & Intake Increase',
        status: app ? app.status : 'Needs Review',
        scrutinyOfficer: activeUserSession?.name || 'Dr. Arvind Kumar (AICTE Scrutiny Officer)',
        officerId: 'AICTE-OFFICER-782',
        mandatoryDocumentsVerification: app ? app.documents : [],
        ocrChecks: app ? app.ocrChecks : [],
        infrastructureFindings: [
          { item: 'Central Computing Facility Workstations', detected: 142, norm: 150, deficit: -8, status: 'Deficient' },
          { item: 'Emergency Luminaire', status: 'Missing over rear egress door' }
        ],
        forensicDiffFindings: [
          { doc: 'Land Ownership Deed', discrepancy: 'Survey No 42/B (5.20 Acres) altered from 42/A (2.10 Acres)', confidence: '99.8%' }
        ],
        blockchainNotarization: {
          consortiumNetwork: 'BharatVidya PoA Trust Network (7 Nodes)',
          blockNumber: 4,
          transactionHash: '0x7a8f192b8d0c41258671e982103847a82b910123'
        }
      };

      const blob = new Blob([JSON.stringify(reportData, null, 2)], { type: 'application/json;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `AICTE_Scrutiny_Report_${reportData.applicationId}.json`;
      a.click();
      URL.revokeObjectURL(url);
      showToastBanner(`Exported AICTE_Scrutiny_Report_${reportData.applicationId}.json`);
    }

    // Global Escape Key & Modal Backdrop Handling
    window.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') {
        closeCvInspectionModal();
        closeForensicDiffModal();
        closeAicteScrutinyReportModal();
        closeOfficialDecisionModal();
        closeUploadDossierModal();
        closeAuthModal();
      }
    });
'''

    # Insert module_scripts right above window.addEventListener('DOMContentLoaded'
    if 'window.addEventListener(\'DOMContentLoaded\'' not in html:
        print("Error: Could not find DOMContentLoaded in html", file=sys.stderr)
        return False

    html = html.replace('window.addEventListener(\'DOMContentLoaded\'', module_scripts + '\n    window.addEventListener(\'DOMContentLoaded\'')

    # Save to frontend/index.html
    with open('frontend/index.html', 'w', encoding='utf-8') as f:
        f.write(html)

    # Sync to root index.html
    shutil.copyfile('frontend/index.html', 'index.html')

    # Sync to backend static
    backend_static = 'backend/src/main/resources/static/index.html'
    if os.path.exists(os.path.dirname(backend_static)):
        shutil.copyfile('frontend/index.html', backend_static)

    print("Successfully built & synced all 4 game-winning features across all targets.")
    return True

if __name__ == '__main__':
    main()
