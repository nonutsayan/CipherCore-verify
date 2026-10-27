# TAB-CLICK UI FREEZING FORENSIC INVESTIGATION AND DEBUGGING LOG

**Target Application:** CipherCore Verify (`portal.html`, `index.html`, `server.js`)  
**Runtime Environment:** Windows, Node.js v24.22.0, Python 3.15, Google Chrome 130+  
**Local Test Server:** `http://localhost:3000`  
**Date Started:** October 28, 2026  
**Status:** Root Causes Conclusively Identified - Implementing Targeted Mitigation  

---

## 1. BASELINE ARCHITECTURAL AUDIT (TASKS 3.1 & 3.2)

### 1.1 Source Repository & Git State
- **Active Branch:** `main` (synchronized with `origin/main` at commit `864a267`).
- **Deployment Branch:** `gh-pages` (synchronized with `origin/gh-pages`).
- **Working Tree:** Clean prior to test execution.

### 1.2 Frontend Entry Points and Architecture
- **Framework:** Vanilla JavaScript (ES6+), HTML5, Tailwind CSS, custom design tokens. No React, Vue, Vite, or Angular build step.
- **Entry Points:**
  - `portal.html` / `frontend/portal.html`: Full verification portal.
  - `index.html` / `frontend/index.html`: Public landing page with Three.js r128 canvas.
- **Proxy Server:** `server.js` listening on port 3000. Proxies `/api` to port 8080 and handles `/api/reports/generate` via Python `backend/report_generator.py`.

### 1.3 Navigation and Tab Architecture
The portal implements a single-page multi-view architecture toggling `hidden` classes:
- **Primary Views (`switchMainView(viewName)`):**
  1. `view-applications`: Ingestion & Scrutiny Queue (`#mainNav-applications`).
  2. `view-inspect`: Institutional Dossier Inspection (`#mainNav-inspect`).
  3. `view-checker`: Standalone Document Tamper Checker (`#mainNav-checker`).
  4. `view-rules`: Rules Engine & Blockchain Audit Ledger (`#mainNav-rules`).
- **Secondary / Sub-Tab Controllers:**
  1. In Dossier Inspection (`switchInspectSubTab(tabNumber)`):
     - Sub-tab 1: Document Checklist (`#inspectSubBtn-1`)
     - Sub-tab 2: OCR & Consistency Check (`#inspectSubBtn-2`)
     - Sub-tab 3: Issuing Authority Check (`#inspectSubBtn-3`)
     - Sub-tab 4: Infrastructure Check (`#inspectSubBtn-4`)
  2. In Rules & Audit (`switchRulesSubTab(mode)`):
     - Sub-tab `'rules-list'`: Statutory Validation Rules (`#rulesSubBtn-rules-list`)
     - Sub-tab `'audit-log'`: Audit Log & Cryptographic Blockchain Ledger (`#rulesSubBtn-audit-log`)
  3. In Document Checker (`selectSampleDoc(docKey)`):
     - Sample buttons: `#sampleDocBtn-SAMPLE-1` through `#sampleDocBtn-SAMPLE-6`.
  4. In Applications Ingestion Queue (`setAppStatusFilter(status)`):
     - Filter tabs: `#filterTab-ALL`, `#filterTab-Needs Review`, `#filterTab-Verified`, `#filterTab-Incomplete`.

### 1.4 Modals, Dialogs, and Overlays
- `#authModal`: Authentication / Access Gateway (enforced if session is null).
- `#uploadDossierModal`: Ingest institutional dossier form.
- `#officialDecisionModal`: Record official officer scrutiny decision form.
- `#cvEvidenceModal`: YOLOv8 CV computer center bounding box & heatmap inspector.
- `#forensicDiffModal`: ELA raster diff viewer for land deeds.
- `#reportModal`: AICTE Official Scrutiny Assessment Report.
- `#toastBanner`: Floating bottom-right notification toast.

### 1.5 Global Event Listeners Identified
- `document.addEventListener('click', ...)`: Global backdrop click dismissal and button click interceptor.
- `document.addEventListener('keydown', ...)`: Global `Escape` key handler.
- `window.addEventListener('DOMContentLoaded', ...)`: Session init, demo data seeding, blockchain ledger load.

---

## 2. FORENSIC RUNTIME REPRODUCTION & EVIDENCE CAPTURE (PHASES 1 - 7)

Controlled reproduction scripts (`scripts/forensic_comprehensive_audit.js`, `scripts/test_portal_fresh.js`, `scripts/test_index_freeze.js`) were executed against Google Chrome using Chrome DevTools Protocol (CDP).

### 2.1 Evidence Item 1: Unauthenticated Gateway Backdrop Occlusion
When `portal.html` loads without an existing session in `sessionStorage`:
- `initUserSession()` invokes `openAuthModal(true)`.
- `#authModal` receives `display: flex !important; z-index: 999999 !important; position: fixed !important; inset: 0 !important; width: 100vw !important; height: 100vh !important;`.
- CDP Hit Test Result:
  ```
  Hit at top-nav-tab: <DIV id="authModal" class="modal-backdrop"> (isBackdrop: true)
  Hit at viewport-center: <DIV id="" class="text-[10px] text-tertiary mt-1"> (isBackdrop: false)
  Hit at content-area: <DIV id="" class="flex items-center justify-between border"> (isBackdrop: false)
  ```
- Because `#authModalCloseBtn` is hidden and `onclick="if(event.target===this && sessionStorage.getItem('ciphercore_user'))closeAuthModal()"` restricts dismissing without authentication, any click on visible background navigation tabs or sections is completely intercepted by the `#authModal` backdrop.
- Furthermore, `closeAuthModal()` contained an early return:
  ```javascript
  if (!activeUserSession) {
    return;
  }
  ```
  Consequently, pressing `Escape` could not close `#authModal` when unauthenticated.

### 2.2 Evidence Item 2: Duplicate Modal Stacking & HTML Syntax Errors in `index.html`
Running `test_index_freeze.js` against `index.html` captured:
```
[PageError] Invalid or unexpected token
Modal status after click: { count: 2, m1_display: 'flex', m2_display: 'none', bodyOverflow: 'hidden' }
```
- Line 535 and 536 of `index.html` contained unescaped backslashes inside an inline attribute:
  `onclick="window.openForensicDiffModal(\'APP-2026-101\', \'Land Ownership Deed\')"`
  This triggered an unhandled JavaScript SyntaxError (`Invalid or unexpected token`).
- `index.html` had duplicate blocks of code: lines 1502 to 2475 were duplicated verbatim on lines 2476 to 3450.
  There were two elements with `id="cvEvidenceModal"`, two with `id="forensicDiffModal"`, and two with `id="reportModal"`.
  When opening or closing modals, `document.getElementById` manipulated only the first occurrence, leaving the second DOM node in an orphaned, uncontrolled state.

### 2.3 Evidence Item 3: Competing Global Click Event Interceptors
`portal.html` contained redundant, identical global click listeners at line 2569 and line 5326:
```javascript
document.addEventListener('click', function(e) {
  const btn = e.target.closest('button, a');
  if (!btn) return;
  const txt = (btn.innerText || btn.textContent || '').trim();
  if (txt.includes('View CV Evidence Inspection') || txt.includes('Launch CV Evidence Audit') || txt.includes('View CV Evidence')) {
    e.preventDefault();
    window.openCvEvidenceModal('APP-2026-101', 'Computer Center / Lab');
  } else if (txt.includes('Inspect Forensic Diff') || txt.includes('Launch Forensic Diff Viewer')) {
    e.preventDefault();
    window.openForensicDiffModal('APP-2026-101', 'Land Ownership Deed');
  } else if (txt.includes('Generate AICTE Scrutiny Report') || txt.includes('Preview AICTE Scrutiny Report')) {
    if (!txt.includes('Print') && !txt.includes('Save PDF')) {
      e.preventDefault();
      window.openReportModal('APP-2026-101');
    }
  }
});
```
When a user clicked any button or link whose text matched, both event listeners executed, calling `e.preventDefault()`, which cancelled native event dispatch and opened modals multiple times.

### 2.4 Evidence Item 4: Why Escape Restored Responsiveness
All global `Escape` key listeners in the codebase:
- `portal.html` line 2589: `window.closeCvEvidenceModal(); window.closeForensicDiffModal(); window.closeReportModal();`
- `portal.html` line 5318: `window.closeCvEvidenceModal(); window.closeForensicDiffModal(); window.closeReportModal();`
- `portal.html` line 5772: `closeAllActiveModals();`
- `index.html` line 2458: `window.closeCvEvidenceModal(); window.closeForensicDiffModal(); window.closeReportModal();`
- `index.html` line 3432: `window.closeCvEvidenceModal(); window.closeForensicDiffModal(); window.closeReportModal();`
Escape ONLY dispatches modal closing functions that set `display: none !important;` and reset `document.body.style.overflow = ''`.
Escape does not interact with view tabs, routing, or background processing.
This conclusively proves that the perceived "freeze" was input occlusion caused by a modal backdrop overlay remaining active in the viewport.

### 2.5 Evidence Item 5: Double API Prefix on Blockchain Notarization
In `portal.html` line 1701:
`<input id="backendUrlInput" type="text" value="http://localhost:8080/api" ... />`
In line 4426:
`fetch(`${backendBase}/api/blockchain/notarize`, ...)`
Because `backendBase` already included `/api`, this evaluated to `http://localhost:8080/api/api/blockchain/notarize`, generating recurring `ERR_CONNECTION_REFUSED` errors during tab navigation and sample document selection.

---

## 3. ROOT CAUSE SUMMARY (PHASE 8)

1. **Occlusion by Stacking Backdrops:** Modals assigned `z-index: 999999 !important; position: fixed !important; inset: 0 !important;` remained in the hit-test tree when closed via incomplete class toggling or when unauthenticated gateway was rendered.
2. **Duplicate DOM Structures in `index.html`:** The entire modal and script section was duplicated, leading to ID collisions, orphaned backdrop nodes, and syntax errors in inline attributes.
3. **Duplicate Global Click Interceptors:** Three separate `click` listeners attached to `document` intercepted clicks across all tabs, calling `e.preventDefault()` whenever button text matched substrings.
4. **Asymmetrical Escape Handling:** `closeAuthModal()` returned early when `activeUserSession` was null, preventing Escape from clearing the gateway overlay.
5. **Backend Notarization URL Formatting:** Improper concatenation created broken network calls during document tab switching.

---

## 4. ACTION PLAN FOR MITIGATION (PHASE 9)

1. **Clean `index.html`:**
   - Remove duplicate lines 2476-3450.
   - Fix unescaped backslashes in lines 535-536: replace `\'` with single quotes `'`.
   - Remove duplicate modal CSS block.
2. **Clean `portal.html`:**
   - Consolidate duplicate script blocks (remove duplicate declarations from lines 5250-5785).
   - Ensure `closeAuthModal()` always closes the modal and removes backdrop when invoked by Escape, regardless of authentication state.
   - Fix blockchain URL concatenation in `addBlock()` to prevent double `/api/api`.
   - Ensure clean modal lifecycle with single, centralized backdrop management.
3. **Automated Verification:**
   - Execute regression test suite verifying all tabs, sub-tabs, sample documents, modals, and Escape handling.
