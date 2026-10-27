# Script to update portal.html with bulletproof CV photo and Forensic Diff engine

with open('portal.html', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Fix tailwind reference
old_tw = "tailwind.config = {"
new_tw = "window.tailwind = window.tailwind || {};\n    window.tailwind.config = {"
if old_tw in content:
    content = content.replace(old_tw, new_tw, 1)
    print("Fixed Tailwind config safety.")

# 2. Check modal backdrop CSS
old_css_rule = """.modal-backdrop[style*="display: none"],
    .modal-backdrop[style*="display:none"],
    #cvEvidenceModal[style*="display: none"],
    #cvEvidenceModal[style*="display:none"],
    #forensicDiffModal[style*="display: none"],
    #forensicDiffModal[style*="display:none"],
    #reportModal[style*="display: none"],
    #reportModal[style*="display:none"] {
      display: none !important;
    }"""

new_css_rule = """.modal-backdrop:not(.active):not([style*="display: flex"]):not([style*="display:flex"]),
    #cvEvidenceModal:not(.active):not([style*="display: flex"]):not([style*="display:flex"]),
    #forensicDiffModal:not(.active):not([style*="display: flex"]):not([style*="display:flex"]),
    #reportModal:not(.active):not([style*="display: flex"]):not([style*="display:flex"]) {
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
    #forensicDiffModal[style*="display:flex"],
    #reportModal.active,
    #reportModal[style*="display: flex"],
    #reportModal[style*="display:flex"] {
      display: flex !important;
      visibility: visible !important;
      opacity: 1 !important;
      pointer-events: auto !important;
      z-index: 999999 !important;
    }"""

if old_css_rule in content:
    content = content.replace(old_css_rule, new_css_rule, 1)
    print("Updated CSS modal visibility rules.")
else:
    print("Could not find exact old_css_rule, searching pattern...")

with open('portal.html', 'w', encoding='utf-8') as f:
    f.write(content)
print("Saved stage 1 of portal.html")
