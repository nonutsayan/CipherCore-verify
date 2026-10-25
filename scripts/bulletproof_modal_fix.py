import os
import sys

def main():
    target_files = [
        "frontend/index.html",
        "index.html",
        "backend/src/main/resources/static/index.html"
    ]

    with open("frontend/index.html", "r", encoding="utf-8") as f:
        html = f.read()

    # 1. HARDEN CSS FOR MODAL BACKDROPS & VISIBILITY
    css_target = "    /* Modal Backdrop & Bulletproof Visibility */"
    css_end = "html.dark .modal-backdrop {\n      background-color: rgba(0, 0, 0, 0.85);\n    }"
    
    hardened_css = """    /* Modal Backdrop & Bulletproof Visibility */
    .modal-backdrop,
    #cvEvidenceModal,
    #cvInspectionModal,
    #forensicDiffModal,
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
    #aicteScrutinyReportModal.active {
      display: flex !important;
      visibility: visible !important;
      opacity: 1 !important;
    }
    .modal-backdrop:not(.active),
    #cvEvidenceModal:not(.active),
    #cvInspectionModal:not(.active),
    #forensicDiffModal:not(.active),
    #aicteScrutinyReportModal:not(.active) {
      display: none !important;
    }
    html.dark .modal-backdrop {
      background-color: rgba(0, 0, 0, 0.88) !important;
    }"""

    idx_css_start = html.find(css_target)
    if idx_css_start != -1:
        idx_css_end = html.find(css_end, idx_css_start) + len(css_end)
        html = html[:idx_css_start] + hardened_css + html[idx_css_end:]
        print("Updated CSS with bulletproof !important modal selectors.")

    # 2. HARDEN JAVASCRIPT CONTROLLERS
    js_start_marker = "// Bulletproof Window Modal Controllers"
    js_end_marker = "window.toggleCvBoundingBoxes = function(val) {"
    
    hardened_js = """// Bulletproof Window Modal Controllers
    function getCvModal() {
      return document.getElementById('cvEvidenceModal') || document.getElementById('cvInspectionModal');
    }
    function getDiffModal() {
      return document.getElementById('forensicDiffModal');
    }
    function getReportModal() {
      return document.getElementById('aicteScrutinyReportModal') || document.getElementById('reportModal');
    }

    window.openCvEvidenceModal = function(appId, facilityType) {
      console.log('Opening CV Evidence Modal for', appId, facilityType);
      const modal = getCvModal();
      if (!modal) {
        console.error('CV modal element not found!');
        return;
      }
      modal.classList.add('active');
      modal.classList.remove('hidden');
      modal.style.display = 'flex';
      if (modal.style.setProperty) {
        modal.style.setProperty('display', 'flex', 'important');
        modal.style.setProperty('visibility', 'visible', 'important');
        modal.style.setProperty('opacity', '1', 'important');
      }
      document.body.style.overflow = 'hidden';
      setTimeout(drawCvCanvas, 30);
      setTimeout(drawCvCanvas, 150);
    };

    window.closeCvEvidenceModal = function() {
      const modal = getCvModal();
      if (!modal) return;
      modal.classList.remove('active');
      modal.classList.add('hidden');
      modal.style.display = 'none';
      if (modal.style.setProperty) {
        modal.style.setProperty('display', 'none', 'important');
      }
      document.body.style.overflow = '';
    };

    window.openForensicDiffModal = function(appId, docType) {
      console.log('Opening Forensic Diff Modal for', appId, docType);
      const modal = getDiffModal();
      if (!modal) {
        console.error('Forensic Diff modal element not found!');
        return;
      }
      modal.classList.add('active');
      modal.classList.remove('hidden');
      modal.style.display = 'flex';
      if (modal.style.setProperty) {
        modal.style.setProperty('display', 'flex', 'important');
        modal.style.setProperty('visibility', 'visible', 'important');
        modal.style.setProperty('opacity', '1', 'important');
      }
      document.body.style.overflow = 'hidden';
    };

    window.closeForensicDiffModal = function() {
      const modal = getDiffModal();
      if (!modal) return;
      modal.classList.remove('active');
      modal.classList.add('hidden');
      modal.style.display = 'none';
      if (modal.style.setProperty) {
        modal.style.setProperty('display', 'none', 'important');
      }
      document.body.style.overflow = '';
    };

    window.openReportModal = function(appId) {
      console.log('Opening Report Modal for', appId);
      const modal = getReportModal();
      if (!modal) {
        console.error('Report modal element not found!');
        return;
      }
      modal.classList.add('active');
      modal.classList.remove('hidden');
      modal.style.display = 'flex';
      if (modal.style.setProperty) {
        modal.style.setProperty('display', 'flex', 'important');
        modal.style.setProperty('visibility', 'visible', 'important');
        modal.style.setProperty('opacity', '1', 'important');
      }
      document.body.style.overflow = 'hidden';
    };

    window.closeReportModal = function() {
      const modal = getReportModal();
      if (!modal) return;
      modal.classList.remove('active');
      modal.classList.add('hidden');
      modal.style.display = 'none';
      if (modal.style.setProperty) {
        modal.style.setProperty('display', 'none', 'important');
      }
      document.body.style.overflow = '';
    };

    // Global backwards-compatibility aliases
    window.openCvInspectionModal = window.openCvEvidenceModal;
    window.closeCvInspectionModal = window.closeCvEvidenceModal;
    window.openAicteScrutinyReportModal = window.openReportModal;
    window.closeAicteScrutinyReportModal = window.closeReportModal;

    """

    idx_js_start = html.find(js_start_marker)
    if idx_js_start != -1:
        idx_js_end = html.find(js_end_marker, idx_js_start)
        html = html[:idx_js_start] + hardened_js + html[idx_js_end:]
        print("Updated JavaScript controllers with getCvModal and getDiffModal.")

    # 3. ADD BUTTONS TO TOP TOOLBAR OF VIEW-INSPECT
    # Find inspect application top control bar
    old_toolbar_marker = '<button onclick="openAicteScrutinyReportModal()" class="btn-primary flex items-center gap-1.5 text-xs bg-[#0284c7] text-white"'
    new_toolbar_buttons = """<button onclick="window.openForensicDiffModal('APP-2026-101', 'Land Ownership Deed')" class="btn-secondary flex items-center gap-1.5 text-xs" style="border-radius:4px;">
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
          <button onclick="openAicteScrutinyReportModal()" class="btn-primary flex items-center gap-1.5 text-xs bg-[#0284c7] text-white" """

    if old_toolbar_marker in html and "View CV Evidence</span>\n          </button>\n          <button onclick=\"openAicteScrutinyReportModal()\"" not in html:
        html = html.replace(old_toolbar_marker, new_toolbar_buttons)
        print("Added direct buttons in View-Inspect top toolbar.")

    # 4. ADD INTERACTIVE BUTTONS TO BENTO CARDS ON LANDING PAGE
    # Bento Card 1: OCR & Forensic Tamper Check
    card1_marker = "ELA, font mismatch, hash integrity"
    if card1_marker in html and "Launch Forensic Diff Viewer" not in html:
        bento1_old = '<p class="text-xs text-sub mt-2 leading-relaxed">\n              Detect altered acreage, forged dates, and text re-rasterization using multi-layer Error Level Analysis (ELA) and font stream metrics.\n            </p>'
        bento1_new = """<p class="text-xs text-sub mt-2 leading-relaxed">
              Detect altered acreage, forged dates, and text re-rasterization using multi-layer Error Level Analysis (ELA) and font stream metrics.
            </p>
            <div class="pt-2">
              <button onclick="window.openForensicDiffModal('APP-2026-101', 'Land Deed')" class="btn-primary py-1.5 px-3 text-xs flex items-center gap-1.5" style="border-radius:4px;">
                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
                </svg>
                <span>Launch Forensic Diff Viewer</span>
              </button>
            </div>"""
        html = html.replace(bento1_old, bento1_new)
        print("Added Launch Forensic Diff Viewer button to Bento Card 1 on Landing Page.")

    # Bento Card 3: APH Infrastructure Audit
    card3_marker = "3. APH Infrastructure Audit"
    if card3_marker in html and "Launch CV Evidence Audit" not in html:
        bento3_old = '<p class="text-xs text-sub mt-2 leading-relaxed">\n              Computer vision edge models compute lab workstation counts, barrier-free access, and fire safety compliance against APH norms.\n            </p>'
        bento3_new = """<p class="text-xs text-sub mt-2 leading-relaxed">
              Computer vision edge models compute lab workstation counts, barrier-free access, and fire safety compliance against APH norms.
            </p>
            <div class="pt-2">
              <button onclick="window.openCvEvidenceModal('APP-2026-101', 'Computer Center / Lab')" class="btn-primary py-1.5 px-3 text-xs flex items-center gap-1.5" style="border-radius:4px;">
                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                <span>Launch CV Evidence Audit</span>
              </button>
            </div>"""
        html = html.replace(bento3_old, bento3_new)
        print("Added Launch CV Evidence Audit button to Bento Card 3 on Landing Page.")

    # 5. ALSO ADD ACTION IN SUB-TAB 1 NEXT TO LAND DEED & BUILDING PLAN
    sub1_table_code = """                      <td class="py-3 px-3 text-right">
                        ${doc.status !== 'Valid'
                          ? `<button onclick="simulateFixDocument('${app.id}', ${idx})" class="btn-primary py-1 px-2.5 text-xs">Upload / Replace</button>`
                          : `<span class="text-[11px] font-mono text-sub">Verified</span>`
                        }
                      </td>"""

    sub1_new_table_code = """                      <td class="py-3 px-3 text-right">
                        <div class="flex items-center justify-end gap-1.5">
                          ${doc.name.toLowerCase().includes('land') ? `
                            <button onclick="window.openForensicDiffModal('${app.id}', '${doc.name}')" class="btn-secondary py-1 px-2 text-xs flex items-center gap-1" style="border-radius:4px;" title="Inspect Forensic Diff">
                              <svg class="w-3 h-3 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" /></svg>
                              <span>Diff</span>
                            </button>
                          ` : ''}
                          ${doc.name.toLowerCase().includes('building') || doc.name.toLowerCase().includes('plan') ? `
                            <button onclick="window.openCvEvidenceModal('${app.id}', '${doc.name}')" class="btn-secondary py-1 px-2 text-xs flex items-center gap-1" style="border-radius:4px;" title="View CV Evidence">
                              <svg class="w-3 h-3 text-[#0284c7]" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" /></svg>
                              <span>CV</span>
                            </button>
                          ` : ''}
                          ${doc.status !== 'Valid'
                            ? `<button onclick="simulateFixDocument('${app.id}', ${idx})" class="btn-primary py-1 px-2.5 text-xs">Upload / Replace</button>`
                            : `<span class="text-[11px] font-mono text-sub">Verified</span>`
                          }
                        </div>
                      </td>"""

    if sub1_table_code in html:
        html = html.replace(sub1_table_code, sub1_new_table_code)
        print("Added quick action buttons in Sub-tab 1 table rows.")

    # 6. IN STANDALONE DOCUMENT CHECKER, ADD BUTTON IN CARD 3 FOR SAMPLE-2 & SAMPLE-5
    # Let's inspect renderDocumentCheckerCards Card 3
    card3_tamper_marker = '${renderStatusTag(t.status)}</div>\n              </div>\n            `).join(\'\')}'
    card3_tamper_new = """${renderStatusTag(t.status)}</div>
              </div>
            `).join('')}
            ${doc.overallStatus === 'Altered' || doc.fileName.includes('LAND_DEED') ? `
              <div class="pt-2 border-t border-line">
                <button onclick="window.openForensicDiffModal('SAMPLE-2', 'Land Deed')" class="btn-primary w-full py-2 text-xs flex items-center justify-center gap-1.5" style="border-radius:4px;">
                  <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
                  </svg>
                  <span>Open Interactive Side-by-Side Forensic Diff</span>
                </button>
              </div>
            ` : ''}
            ${doc.fileName.includes('BUILDING_PLAN') ? `
              <div class="pt-2 border-t border-line">
                <button onclick="window.openCvEvidenceModal('SAMPLE-5', 'Building Plan')" class="btn-primary w-full py-2 text-xs flex items-center justify-center gap-1.5" style="border-radius:4px;">
                  <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                  <span>Open Interactive CV Infrastructure Inspection</span>
                </button>
              </div>
            ` : ''}"""

    if card3_tamper_marker in html:
        html = html.replace(card3_tamper_marker, card3_tamper_new)
        print("Added action buttons inside Standalone Document Checker Card 3.")

    # 7. WRITE TO ALL TARGETS
    for tgt in target_files:
        os.makedirs(os.path.dirname(os.path.abspath(tgt)), exist_ok=True)
        with open(tgt, "w", encoding="utf-8") as f:
            f.write(html)
        print(f"Successfully wrote {tgt}")

if __name__ == "__main__":
    main()
