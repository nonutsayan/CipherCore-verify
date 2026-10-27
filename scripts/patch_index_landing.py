import re

print("Patching index.html with CV and Forensic buttons and modals...")
with open('index.html', 'r', encoding='utf-8') as f:
    html = f.read()

with open('scripts/cv_forensic_engine.js', 'r', encoding='utf-8') as f:
    engine_js = f.read()

with open('scripts/lab_photo_b64.js', 'r', encoding='utf-8') as f:
    photo_b64_js = f.read()

# 1. Add Forensic Diff button to Engine 1
old_engine1_tags = '<div class="tags"><span>ELA Forensics</span><span>Font Descriptor Matrix</span><span>Metadata Timeline</span><span>SHA-256</span></div>\n            </article>'
new_engine1_tags = '''<div class="tags"><span>ELA Forensics</span><span>Font Descriptor Matrix</span><span>Metadata Timeline</span><span>SHA-256</span></div>
              <div style="margin-top: 14px;">
                <button type="button" onclick="window.openForensicDiffModal('APP-2026-101', 'Land Ownership Deed')" class="btn btn-sm" style="display:inline-flex; align-items:center; gap:6px; cursor:pointer;">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"/></svg>
                  <span>Inspect Forensic Diff</span>
                </button>
              </div>
            </article>'''

if old_engine1_tags in html:
    html = html.replace(old_engine1_tags, new_engine1_tags, 1)
    print("Added Forensic button to Engine 1 in index.html")
else:
    print("Warning: old_engine1_tags not found directly, trying regex")
    html = re.sub(
        r'(<span class="note">Engine 1 of 4 · Forensic Analysis</span>.*?<div class="tags">.*?</div>)(\s*</article>)',
        r'\1\n              <div style="margin-top: 14px;"><button type="button" onclick="window.openForensicDiffModal(\'APP-2026-101\', \'Land Ownership Deed\')" class="btn btn-sm" style="display:inline-flex; align-items:center; gap:6px; cursor:pointer;"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"/></svg><span>Inspect Forensic Diff</span></button></div>\2',
        html,
        flags=re.DOTALL
    )

# 2. Add CV Evidence button to Engine 3
old_engine3_tags = '<div class="tags"><span>YOLOv8x Edge CV</span><span>APH Chapter II</span><span>Spatial Geofence</span><span>Terminal Counting</span></div>\n            </article>'
new_engine3_tags = '''<div class="tags"><span>YOLOv8x Edge CV</span><span>APH Chapter II</span><span>Spatial Geofence</span><span>Terminal Counting</span></div>
              <div style="margin-top: 14px;">
                <button type="button" onclick="window.openCvEvidenceModal('APP-2026-101', 'Computer Center / Lab')" class="btn btn-sm" style="display:inline-flex; align-items:center; gap:6px; cursor:pointer;">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
                  <span>View CV Evidence Inspection</span>
                </button>
              </div>
            </article>'''

if old_engine3_tags in html:
    html = html.replace(old_engine3_tags, new_engine3_tags, 1)
    print("Added CV button to Engine 3 in index.html")
else:
    print("Warning: old_engine3_tags not found directly, trying regex")
    html = re.sub(
        r'(<span class="note">Engine 3 of 4 · Infrastructure Vision</span>.*?<div class="tags">.*?</div>)(\s*</article>)',
        r'\1\n              <div style="margin-top: 14px;"><button type="button" onclick="window.openCvEvidenceModal(\'APP-2026-101\', \'Computer Center / Lab\')" class="btn btn-sm" style="display:inline-flex; align-items:center; gap:6px; cursor:pointer;"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg><span>View CV Evidence Inspection</span></button></div>\2',
        html,
        flags=re.DOTALL
    )

# 3. Read modal HTML containers from portal.html
with open('portal.html', 'r', encoding='utf-8') as f:
    portal_text = f.read()

cv_modal_start = portal_text.find('id="cvEvidenceModal"')
cv_backdrop_start = portal_text.rfind('<div', 0, cv_modal_start)
cv_modal_end = portal_text.find('<!-- 2. CRYPTOGRAPHIC & FORENSIC DOCUMENT DIFF VIEWER MODAL', cv_modal_start)

forensic_modal_start = portal_text.find('id="forensicDiffModal"')
forensic_backdrop_start = portal_text.rfind('<div', 0, forensic_modal_start)
forensic_modal_end = portal_text.find('<!-- 3. AICTE OFFICIAL SCRUTINY ASSESSMENT REPORT MODAL', forensic_modal_start)

cv_modal_markup = portal_text[cv_backdrop_start:cv_modal_end]
forensic_modal_markup = portal_text[forensic_backdrop_start:forensic_modal_end]

# 4. Modal CSS styles for index.html (self-contained, matching light/dark themes)
modal_css = """
  <style>
    /* =========================================================================
       MODAL STYLES FOR LANDING PAGE
       ========================================================================= */
    .modal-backdrop:not(.active):not([style*="display: flex"]):not([style*="display:flex"]),
    #cvEvidenceModal:not(.active):not([style*="display: flex"]):not([style*="display:flex"]),
    #forensicDiffModal:not(.active):not([style*="display: flex"]):not([style*="display:flex"]) {
      display: none !important;
      visibility: hidden !important;
      opacity: 0 !important;
      pointer-events: none !important;
    }

    .modal-backdrop.active,
    .modal-backdrop[style*="display: flex"],
    .modal-backdrop[style*="display:flex"],
    #cvEvidenceModal.active,
    #cvEvidenceModal[style*="display: flex"],
    #cvEvidenceModal[style*="display:flex"],
    #forensicDiffModal.active,
    #forensicDiffModal[style*="display: flex"],
    #forensicDiffModal[style*="display:flex"] {
      position: fixed !important;
      top: 0 !important;
      left: 0 !important;
      right: 0 !important;
      bottom: 0 !important;
      width: 100vw !important;
      height: 100vh !important;
      z-index: 999999 !important;
      background-color: rgba(15, 23, 42, 0.72) !important;
      backdrop-filter: blur(6px) !important;
      -webkit-backdrop-filter: blur(6px) !important;
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
      padding: 1rem !important;
      visibility: visible !important;
      opacity: 1 !important;
      pointer-events: auto !important;
    }

    .modal-container {
      background: var(--bg-surface, #ffffff);
      color: var(--text-primary, #0f172a);
      border: 1px solid var(--border-subtle, #cbd5e1);
      border-radius: 6px;
      font-family: var(--font-sans, 'Inter', sans-serif);
    }
    [data-theme="dark"] .modal-container {
      background: #0e1520;
      color: #e9eff7;
      border-color: rgba(170, 200, 235, 0.2);
    }

    .card-surface { background: var(--bg-surface, #ffffff); }
    [data-theme="dark"] .card-surface { background: #0e1520; }
    .subtle-surface { background: var(--bg-surface-subtle, #f1f5f9); }
    [data-theme="dark"] .subtle-surface { background: #162030; }

    .btn-close {
      width: 28px;
      height: 28px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 4px;
      border: 1px solid var(--border-subtle, #cbd5e1);
      background: transparent;
      color: var(--text-secondary, #64748b);
      font-size: 18px;
      cursor: pointer;
      line-height: 1;
    }
    .btn-close:hover {
      background: var(--bg-surface-subtle, #e2e8f0);
      color: var(--text-primary, #0f172a);
    }

    .btn-primary {
      background: #0284c7;
      color: #ffffff;
      font-weight: 600;
      border-radius: 4px;
      padding: 6px 14px;
      border: none;
      cursor: pointer;
    }
    .btn-primary:hover { background: #0369a1; }

    .tag-neutral {
      padding: 2px 6px;
      border-radius: 3px;
      font-size: 11px;
      font-family: monospace;
      background: #f1f5f9;
      color: #475569;
      border: 1px solid #cbd5e1;
    }
    [data-theme="dark"] .tag-neutral {
      background: #1e293b;
      color: #94a3b8;
      border-color: #334155;
    }
  </style>
"""

# Insert modal CSS in head
if '</head>' in html:
    html = html.replace('</head>', f'{modal_css}\n</head>', 1)
    print("Inserted modal CSS in index.html head")

# Insert modal markup and script before </body>
modals_and_script = f"""
  <!-- EMBEDDED MODALS FOR EVIDENCE INSPECTION -->
{cv_modal_markup}

{forensic_modal_markup}

  <!-- LAB PHOTO B64 & CV/FORENSIC ENGINE -->
  <script>
{photo_b64_js}
{engine_js}
  </script>
"""

if '</body>' in html:
    html = html.replace('</body>', f'{modals_and_script}\n</body>', 1)
    print("Inserted modals and engine script before </body> in index.html")

with open('index.html', 'w', encoding='utf-8') as f:
    f.write(html)

print("Saved updated index.html successfully!")
