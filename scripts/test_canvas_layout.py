docs_data = [
    {
        "code": "EoA APH 2.1 DOC 01",
        "title": "Fire Safety Certificate (NOC)",
        "hash": "e3b0c44298fc1c",
        "norm_label": "STATUTORY NORM",
        "norm_val": "Valid clearance from Competent State Fire Authority covering AY 2026-27.",
        "gw_label": "GATEWAY VERIFICATION",
        "gw_val": "State Fire Directorate Portal API (HTTP 200 OK | Active).",
        "check_label": "CHECKS PERFORMED",
        "check_val": "Class-3 DSC digital stamp verified, multi-storey egress norms met.",
        "badge_text": "VERIFIED: Compliant NBC Part IV",
        "badge_type": "ok", # green
        "footer_status": "Verified"
    },
    {
        "code": "EoA APH 2.1 DOC 02",
        "title": "Land Ownership Deed",
        "hash": "9b8769a4a74295",
        "norm_label": "STATUTORY NORM",
        "norm_val": "Registered sale deed / 30-year uninterrupted contiguous land lease.",
        "gw_label": "GATEWAY VERIFICATION",
        "gw_val": "DigiLocker State Land Registry (RoR / 7/12 CTS Property Card).",
        "check_label": "FORENSIC ALERT",
        "check_val": "ELA Score 88.4%: Survey 42/B digital raster overlay detected over 42/A scan.",
        "badge_text": "FLAGGED: Acreage Mismatch 2.10 vs 5.20",
        "badge_type": "flag", # red
        "footer_status": "Flagged"
    },
    {
        "code": "EoA APH 2.1 DOC 03",
        "title": "University Affiliation Letter",
        "hash": "4a2d81f0872658",
        "norm_label": "STATUTORY NORM",
        "norm_val": "Valid affiliation issued by affiliating State/Central University for all branches.",
        "gw_label": "GATEWAY VERIFICATION",
        "gw_val": "University Affiliation Registry Database (SPPU Portal).",
        "check_label": "CHECKS PERFORMED",
        "check_val": "Cross-reconciliation of sanctioned course intake against AICTE quota.",
        "badge_text": "DISCREPANCY: 120 vs 180 Intake",
        "badge_type": "warn", # amber
        "footer_status": "Mismatch"
    },
    {
        "code": "EoA APH 2.1 DOC 04",
        "title": "Faculty Roster & PAN List",
        "hash": "8c43aa2cfd991b",
        "norm_label": "STATUTORY NORM",
        "norm_val": "Minimum 1:20 Faculty-to-Student Ratio (FSR) with 1:2:6 cadre distribution.",
        "gw_label": "GATEWAY VERIFICATION",
        "gw_val": "Central Income Tax PAN & AICTE National Faculty Database.",
        "check_label": "CHECKS PERFORMED",
        "check_val": "Automated collision detection against nationwide technical faculty rosters.",
        "badge_text": "FLAGGED: 2 Duplicate PANs Detected",
        "badge_type": "flag", # red
        "footer_status": "Flagged"
    },
    {
        "code": "EoA APH 2.1 DOC 05",
        "title": "Approved Building Plan",
        "hash": "81fa023e981bc8",
        "norm_label": "STATUTORY NORM",
        "norm_val": "Sanctioned municipal master plan with built-up carpet area breakdown.",
        "gw_label": "VISION AUDIT",
        "gw_val": "YOLOv8 Computer Vision scan of Central Computing Lab-03.",
        "check_label": "CHECKS PERFORMED",
        "check_val": "142 terminals detected (150 required); ramp gradient 1:10 (1:12 norm).",
        "badge_text": "DEFICIENCY: -8 Workstation Shortfall",
        "badge_type": "warn", # amber
        "footer_status": "Deficient"
    },
    {
        "code": "EoA APH 2.1 DOC 06",
        "title": "Fee Payment Receipt (TER)",
        "hash": "12a4b8890cb1e2",
        "norm_label": "STATUTORY NORM",
        "norm_val": "Proof of statutory Technical Education Regulatory (TER) fee payment.",
        "gw_label": "GATEWAY VERIFICATION",
        "gw_val": "Bharatkosh Central Treasury / PFMS Payment Reconciliation.",
        "check_label": "CHECKS PERFORMED",
        "check_val": "Challan UTR AICTE2026998124 settled; institutional PAN matched.",
        "badge_text": "VERIFIED: Treasury Settlement Confirmed",
        "badge_type": "ok", # green
        "footer_status": "Verified"
    }
]

print("Loaded", len(docs_data), "documents")
for d in docs_data:
    print(d["code"], "-", d["title"])
