# FORMAL ENGINEERING IMPLEMENTATION REPORT: FRONTEND FREEZING RESOLUTION AND PROFESSIONAL SCRUTINY REPORT GENERATOR

**Project:** CipherCore Verify (AICTE SIH Problem Statement 26261)  
**Engineering Discipline:** Full-Stack Web Architecture, Performance Engineering, Document Generation  
**Date:** October 28, 2026  
**Status:** Complete and Verified  

---

## EXECUTIVE SUMMARY

This engineering report documents the diagnosis, root-cause elimination, and architectural implementation of two critical systems for the CipherCore Verify platform:

1. **Task A (Frontend Freezing Root-Cause Elimination):** A permanent architectural resolution to the issue where navigating tabs, opening modals, or uploading files rendered the user interface unresponsive, requiring users to press the `Escape` key to regain interactivity.
2. **Task B (Professional Report Generator Module):** The implementation of a maintainable, high-density Python document generation module located at `backend/report_generator.py`. This module replaces temporary client-side print dialogs with vector-rendered, publication-quality AICTE Scrutiny Assessment Reports adhering strictly to AICTE Approval Process Handbook (APH 2024-27) norms.

All implementations were verified via automated test suites in Node.js/JSDOM and Python 3.15, achieving a 100% pass rate.

---

## PART I: COMPREHENSIVE ROOT-CAUSE ANALYSIS OF FRONTEND FREEZING

### 1.1 Symptoms and Observed Behavior
During standard user operation across `portal.html` and `index.html`:
- Clicking navigation tabs (such as switching from "Scrutiny Queue" to "Document Checker" or "Rules & Blockchain Audit") left the UI seemingly unclickable.
- Opening modals (such as "Upload Dossier" or "Record Official Decision") and attempting to click the dark backdrop to dismiss failed to close the modal.
- Uploading files or cancelling modal dialogs resulted in mouse clicks failing to trigger any buttons or table rows.
- Pressing the keyboard `Escape` key immediately restored responsiveness across all interface elements.

### 1.2 Root Cause 1: CSS Selector Asymmetry and Inactive Display Rules
In `portal.html` (lines 497 to 560) and `index.html` (lines 240 to 275), modal backdrop rules were written with asymmetric selector lists:

```css
/* Styling selector that positioned elements fixed across the entire viewport */
.modal-backdrop,
#cvEvidenceModal,
#cvInspectionModal,
#forensicDiffModal,
#reportModal,
#aicteScrutinyReportModal {
  position: fixed !important;
  top: 0 !important; left: 0 !important; right: 0 !important; bottom: 0 !important;
  width: 100vw !important; height: 100vh !important;
  z-index: 999999 !important;
  ...
}

/* Hiding selector applied when inactive */
.modal-backdrop:not(.active):not([style*="display: flex"]),
#cvEvidenceModal:not(.active)...,
#forensicDiffModal:not(.active)...,
#reportModal:not(.active)... {
  display: none !important;
  pointer-events: none !important;
}
```

Critical defect: `#cvInspectionModal` and `#aicteScrutinyReportModal` were explicitly styled as `position: fixed; width: 100vw; height: 100vh; z-index: 999999`, but were omitted from the `:not(.active)` hiding rule. Consequently, whenever those IDs existed in the DOM, they formed an invisible 100vw x 100vh overlay intercepting all pointer events across the page.

### 1.3 Root Cause 2: Conflicting Utility Classes in Modal Containers
In `portal.html`:
- Line 1728: `<div id="authModal" class="fixed inset-0 z-50 hidden flex ...">`
- Line 1814: `<div id="uploadDossierModal" class="fixed inset-0 z-50 hidden bg-black/60 flex ...">`
- Line 1903: `<div id="officialDecisionModal" class="fixed inset-0 z-50 hidden bg-black/60 flex ...">`

These elements contained both `hidden` (which declares `display: none`) and `flex` (which declares `display: flex`) in their static HTML `class` attribute. In Tailwind CSS and compiled stylesheets, rule precedence is determined by stylesheet order. When `.flex` overrides `.hidden`, the modal backdrop remains permanently rendered in the DOM, blocking underlying clicks.

### 1.4 Root Cause 3: Missing Backdrop Dismissal Class and Click Interceptor Failure
The global click handler designed to dismiss modals on outside clicks evaluated:

```javascript
document.addEventListener('click', function(e) {
  if (e.target && e.target.classList && e.target.classList.contains('modal-backdrop')) {
    ...
  }
});
```

However, `#uploadDossierModal` and `#officialDecisionModal` were never assigned the `modal-backdrop` class in their markup. Consequently, clicking outside the dialog card did not match `e.target.classList.contains('modal-backdrop')`. The full-screen overlay remained active, trapping the user in the open modal.

### 1.5 Root Cause 4: Persistent Body Scroll Locks (`overflow: hidden`)
When opening modal dialogs, scripts executed `document.body.style.overflow = 'hidden'`. However, `closeUploadDossierModal()` and `closeOfficialDecisionModal()` merely ran:

```javascript
function closeUploadDossierModal() {
  document.getElementById('uploadDossierModal').classList.add('hidden');
}
```

Neither function reset `document.body.style.overflow = ''`. As a result, the body scroll lock remained permanently active across the entire document, freezing page scrolling.

### 1.6 Root Cause 5: Tab Navigation Desynchronization
When a user switched tabs via `switchMainView(viewName)`, the active view container was toggled, but no check was performed to determine if any modal or backdrop overlay was active. If a modal was open, the tab content changed behind the modal, but the modal and its scroll lock remained active, rendering the new tab unclickable.

### 1.7 Diagnosis: Why Pressing `Escape` Restored Responsiveness
The global keyboard listener registered on `Escape` executed:

```javascript
document.addEventListener('keydown', function(e) {
  if (e.key === 'Escape') {
    window.closeCvEvidenceModal();
    window.closeForensicDiffModal();
    window.closeReportModal();
    closeOfficialDecisionModal();
    closeUploadDossierModal();
    closeAuthModal();
  }
});
```

Because `window.closeCvEvidenceModal()`, `window.closeForensicDiffModal()`, and `window.closeReportModal()` explicitly executed:
```javascript
m.style.setProperty('display', 'none', 'important');
m.classList.remove('active');
m.classList.add('hidden');
document.body.style.overflow = '';
```
Pressing `Escape` was the only user interaction that called `document.body.style.overflow = ''` and forced all fixed overlays to `display: none !important`.

---

## PART II: IMPLEMENTATION OF THE FRONTEND DEFECT RESOLUTION

### 2.1 Unified Modal Lifecycle Manager
A centralized modal controller was implemented in `portal.html` and mirrored across all frontend distributions:

```javascript
function isAnyModalOpen() {
  const modalIds = [
    'cvEvidenceModal', 'cvInspectionModal', 'forensicDiffModal',
    'reportModal', 'aicteScrutinyReportModal', 'officialDecisionModal',
    'uploadDossierModal', 'authModal'
  ];
  return modalIds.some(id => {
    const el = document.getElementById(id);
    if (!el) return false;
    if (el.classList.contains('active')) return true;
    if (el.style.display && el.style.display !== 'none' && !el.classList.contains('hidden')) return true;
    return false;
  });
}

function checkAndResetBodyOverflow() {
  if (!isAnyModalOpen()) {
    document.body.style.overflow = '';
  }
}

function closeAllActiveModals() {
  if (typeof window.closeCvEvidenceModal === 'function') window.closeCvEvidenceModal();
  if (typeof window.closeForensicDiffModal === 'function') window.closeForensicDiffModal();
  if (typeof window.closeReportModal === 'function') window.closeReportModal();
  if (typeof closeOfficialDecisionModal === 'function') closeOfficialDecisionModal();
  if (typeof closeUploadDossierModal === 'function') closeUploadDossierModal();
  if (sessionStorage.getItem('ciphercore_user') && typeof closeAuthModal === 'function') {
    closeAuthModal();
  }
  document.body.style.overflow = '';
}
```

### 2.2 Tab Switch Auto-Dismissal
In `switchMainView(viewName)`, `closeAllActiveModals()` is invoked as the initial operation:

```javascript
function switchMainView(viewName) {
  closeAllActiveModals();
  activeMainView = viewName;
  ...
}
```
This guarantees that navigating to any tab automatically cleans up lingering backdrops, dismisses active dialogs, and releases body scroll locks.

### 2.3 Resilient HTML Markup and Outside-Click Handler
1. Removed `hidden flex` contradictory classes from `#authModal`, `#uploadDossierModal`, and `#officialDecisionModal`.
2. Assigned `.modal-backdrop` to all modal roots with `style="display:none;"`.
3. Updated the global backdrop listener to detect backdrop clicks across all modal IDs:

```javascript
document.addEventListener('click', function(e) {
  const target = e.target;
  if (!target) return;
  const isBackdrop = (target.classList && target.classList.contains('modal-backdrop')) ||
                     target.id === 'uploadDossierModal' ||
                     target.id === 'officialDecisionModal' ||
                     target.id === 'cvEvidenceModal' ||
                     target.id === 'cvInspectionModal' ||
                     target.id === 'forensicDiffModal' ||
                     target.id === 'reportModal' ||
                     target.id === 'aicteScrutinyReportModal' ||
                     target.id === 'authModal';

  if (isBackdrop) {
    if (target.id === 'cvEvidenceModal' || target.id === 'cvInspectionModal') window.closeCvEvidenceModal();
    else if (target.id === 'forensicDiffModal') window.closeForensicDiffModal();
    else if (target.id === 'aicteScrutinyReportModal' || target.id === 'reportModal') window.closeReportModal();
    else if (target.id === 'officialDecisionModal') closeOfficialDecisionModal();
    else if (target.id === 'uploadDossierModal') closeUploadDossierModal();
    else if (target.id === 'authModal' && sessionStorage.getItem('ciphercore_user')) closeAuthModal();
  }
});
```

### 2.4 Resilient File Upload Handlers
In `handleStandaloneFileUpload` and `handleCreateNewDossier`, all logic was enclosed in `try ... catch ... finally` blocks. Crucially:
- `event.target.value = '';` was placed in the `finally` block so that selecting the same file consecutively triggers the browser's `change` event.
- `checkAndResetBodyOverflow()` was placed in the `finally` block to prevent persistent scroll locks.

---

## PART III: ARCHITECTURE OF THE PROFESSIONAL REPORT GENERATOR (`report_generator.py`)

### 3.1 Design Principles
The report generator module was engineered to fulfill the following architectural requirements:
- **No Mockups or Browser Printing:** Bypasses `window.print()` and client-side HTML-to-image canvas captures in favor of a vector PDF engine.
- **Strict APH 2024-27 Regulatory Conformance:** Contains authentic scrutiny sections covering institutional profile, the 6 mandatory documents, YOLOv8 computer vision facility inspection, forensic ELA tampering checks, and Proof-of-Authority blockchain notarization.
- **Dual-Mode Execution:** Functions as a command-line utility (`CLI`) for headless batch jobs and as an importable Python library (`API`).
- **Two-Pass Numbered Canvas:** Dynamically calculates total pages to print institutional headers, timestamps, and "Page X of Y" footers across all pages.

### 3.2 Report Structure
The generated report contains 6 structured sections:
1. **Institutional Header and Regulatory Identification:** Official Council header, academic cycle (AY 2026-27), problem statement code (SIH 26261), and overall status badge.
2. **Institutional Dossier Metadata:** Table displaying Institution Name, Permanent Institute ID (PID), Region, Campus Address, Affiliating University, Application Category, and Submission Date.
3. **Statutory Scrutiny Executive Summary:** Compliance score metric, reviewing officer credentials, regulatory decision, justification, and statutory directives.
4. **Mandatory Six-Document Scrutiny Matrix:** Detailed audit table covering:
   - DOC 01: Fire Safety Certificate (NOC)
   - DOC 02: Land Ownership Deed
   - DOC 03: University Affiliation Letter
   - DOC 04: Faculty Roster and PAN List
   - DOC 05: Approved Building Plan
   - DOC 06: Fee Payment Receipt (TER)
5. **Computer Vision & Forensic Engine Findings:**
   - YOLOv8 Central Computing Facility inspection: 142 detected terminals vs 150 prescribed norm (-8 deficit / -5.3% shortfall), geofence tag (18.5204 N, 73.8567 E), and missing emergency exit luminaire.
   - Forensic Land Deed analysis: Error Level Analysis (ELA) score of 88.4%, detected post-issuance raster alteration, and contiguous acreage deficit.
6. **Consortium Proof-of-Authority Blockchain Audit Trail:**
   - Merkle root, block index, previous block hash, notarized block hash, and validator node consensus (AICTE-HQ, IIT-Bombay, IIT-Madras, IISc-Bangalore, MoE-Gov, NIC).
7. **Statutory Officer Digital Endorsement Block:** Official Scrutiny Bureau seal, digital signature hash, and verification timestamp.

### 3.3 Programmatic API and CLI Interface
`backend/report_generator.py` exposes:
- `generate_pdf_report(app_data, output_path)`: Writes publication-quality PDF.
- `generate_json_report(app_data, output_path)`: Writes schema-compliant JSON.
- `get_application_data(app_id, raw_data)`: Retrieves pre-configured authentic application data or merges dynamic payloads.
- CLI arguments:
  ```bash
  python backend/report_generator.py --app-id APP-2026-101 --format both --output ./uploads/report
  ```

---

## PART IV: FULL-STACK INTEGRATION & PROXY PARITY

### 4.1 Node.js Proxy Server (`server.js`)
`server.js` was upgraded to route report requests to the Python engine:
- Intercepts `/api/reports/generate` and `/api/reports/download`.
- Parses `appId` and `format` query parameters.
- Invokes `backend/report_generator.py` via `child_process.execFile` and streams the resulting binary PDF or JSON directly to the HTTP client with appropriate `Content-Disposition` and `Content-Type` headers.
- Provides static asset fallback from `assets/reports/` for offline or embedded environments.
- Maintains full proxy parity for `/api/validation-runs/:id/report.pdf` and `/api/validation-runs/:id/report.json`.

### 4.2 Frontend Integration (`portal.html`)
- Dossier Inspection toolbar updated with direct "Download Official Report (PDF)" button.
- `#reportModal` header updated with "Download Official PDF (Python Engine)" button.
- `downloadOfficialPdfReport(appId)` function implemented to handle asynchronous fetch, Blob instantiation, and automatic browser download triggering.

---

## PART V: AUTOMATED VERIFICATION RESULTS

### 5.1 Freeze Prevention Test Suite (`scripts/test_freeze_prevention.js`)
Executed via Node.js and JSDOM headless environment:

```
Starting Freeze Prevention and Modal Lifecycle Test Suite...

[PASS] Initial state: document.body.style.overflow is clean
[PASS] Open Upload Modal sets active and overflow hidden
[PASS] Close Upload Modal clears active and resets body overflow
[PASS] Re-open Upload Modal sets overflow hidden
[PASS] Clicking outside Upload Modal dismisses it and clears overflow
[PASS] Open Decision Modal sets active and overflow hidden
[PASS] Close Decision Modal clears active and resets body overflow
[PASS] Decision Modal opened before tab switch
[PASS] Tab switch to checker auto-dismisses Decision Modal
[PASS] Tab switch resets body overflow to empty string
[PASS] Tab switch to rules auto-dismisses Upload Modal
[PASS] Open CV Modal sets active and overflow hidden
[PASS] Click outside CV Modal closes it and resets overflow
[PASS] Decision Modal active before Escape key
[PASS] Escape key closes all active modals and clears overflow
[PASS] window.downloadOfficialPdfReport is defined
[PASS] window.exportScrutinyReportJson is defined

Test Summary: 17/17 Passed (Exit Code: 0)
```

### 5.2 Report Generator Test Suite (`scripts/test_report_generator.py`)
Executed via Python 3.15 test harness:

```
Executing Report Generator Test Suite against: .../backend/report_generator.py
[PASS] CLI Standard PDF Generation
[PASS] CLI Standard JSON Generation
[PASS] Custom Data Injection via JSON CLI
[PASS] Direct Python Module Import and Execution

Report Generator Test Summary: 4/4 Passed (Exit Code: 0)
```

### 5.3 Live HTTP Route Verification
Tested against running server on `http://localhost:3000`:
- `GET /portal.html`: HTTP 200 OK (Clean load, responsive)
- `GET /api/reports/generate?appId=APP-2026-101&format=pdf`: HTTP 200 OK (8,904 bytes, valid `%PDF-1.4` magic bytes)
- `GET /api/reports/generate?appId=APP-2026-101&format=json`: HTTP 200 OK (Valid JSON object with `applicationProfile`)
- `GET /api/validation-runs/1/report.pdf`: HTTP 200 OK (Route parity verified)

---

## PART VI: SUMMARY OF DELIVERABLES

| Deliverable | File Path | Status |
|---|---|---|
| Report Generator Module | `backend/report_generator.py` | Complete & Tested |
| Proxy Server Integration | `server.js` | Complete & Active |
| Zero-Freeze Portal | `portal.html` | Complete & Verified |
| Zero-Freeze Landing Page | `index.html` | Complete & Verified |
| Frontend Mirror Bundle | `frontend/portal.html`, `frontend/index.html` | Synced & Verified |
| Pre-generated Reports | `assets/reports/`, `frontend/assets/reports/` | Generated (5 Institutions) |
| Task Matrix Documentation | `docs/ROOT_CAUSE_AND_REPORT_GENERATOR_TASKS.md` | Complete |
| Implementation Report | `docs/ROOT_CAUSE_AND_REPORT_GENERATOR_IMPLEMENTATION_REPORT.md` | Complete |
| Test Suites | `scripts/test_freeze_prevention.js`, `scripts/test_report_generator.py` | 100% Passing (21/21) |
