import os

files = [
    "frontend/index.html",
    "index.html",
    "backend/src/main/resources/static/index.html"
]

bento1_target = """              <p class="text-xs text-sub mt-1 leading-relaxed">
                Extracts machine-readable text layers, compares font encodings, and identifies post-issuance raster overlays. Cross-compares document dates, institutional names, and land acreage across all uploaded PDFs to catch doctored scans.
              </p>"""

bento1_replacement = """              <p class="text-xs text-sub mt-1 leading-relaxed">
                Extracts machine-readable text layers, compares font encodings, and identifies post-issuance raster overlays. Cross-compares document dates, institutional names, and land acreage across all uploaded PDFs to catch doctored scans.
              </p>
              <div class="pt-2">
                <button onclick="window.openForensicDiffModal('APP-2026-101', 'Land Deed')" class="btn-primary py-1.5 px-3 text-xs flex items-center gap-1.5" style="border-radius:4px;">
                  <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
                  </svg>
                  <span>Launch Forensic Diff Viewer</span>
                </button>
              </div>"""

bento3_target = """              <p class="text-xs text-sub mt-1 leading-relaxed">
                Evaluates institutional capacity against AICTE Approval Process Handbook (APH 2024-27) norms. Validates contiguous land area (Rural vs Metro), computer terminal density (1:1 per intake), and barrier-free ramp standards.
              </p>"""

bento3_replacement = """              <p class="text-xs text-sub mt-1 leading-relaxed">
                Evaluates institutional capacity against AICTE Approval Process Handbook (APH 2024-27) norms. Validates contiguous land area (Rural vs Metro), computer terminal density (1:1 per intake), and barrier-free ramp standards.
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

for fpath in files:
    with open(fpath, "r", encoding="utf-8") as f:
        content = f.read()

    if bento1_target in content:
        content = content.replace(bento1_target, bento1_replacement)
        print(f"Added Card 1 button in {fpath}")

    if bento3_target in content:
        content = content.replace(bento3_target, bento3_replacement)
        print(f"Added Card 3 button in {fpath}")

    with open(fpath, "w", encoding="utf-8") as f:
        f.write(content)

print("Done updating Bento cards across all files.")
