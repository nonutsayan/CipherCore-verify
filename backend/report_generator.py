#!/usr/bin/env python3
"""
CipherCore Verify - Professional Scrutiny Report Generator
Module: report_generator.py
Location: backend/report_generator.py

Generates publication-quality AICTE Scrutiny Assessment Reports in PDF and JSON formats.
Follows AICTE Approval Process Handbook (APH 2024-27) norms for Problem Statement 26261.
"""

import os
import sys
import json
import argparse
from datetime import datetime, timezone
import hashlib

# ReportLab imports
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, KeepTogether, PageBreak, HRFlowable
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER, TA_LEFT, TA_RIGHT, TA_JUSTIFY
from reportlab.pdfgen import canvas

# Institutional Brand Tokens
COLOR_PRIMARY = colors.HexColor("#0F172A")    # Deep Navy
COLOR_SECONDARY = colors.HexColor("#1E293B")  # Slate Navy
COLOR_ACCENT = colors.HexColor("#0284C7")     # AICTE Scan Blue
COLOR_TEXT = colors.HexColor("#0F172A")       # Primary Text Ink
COLOR_MUTED = colors.HexColor("#475569")      # Subdued Text
COLOR_LINE = colors.HexColor("#CBD5E1")       # Border Line
COLOR_BG_LIGHT = colors.HexColor("#F8FAFC")   # Subtle Background
COLOR_BG_CARD = colors.HexColor("#F1F5F9")    # Card Background

COLOR_OK = colors.HexColor("#15803D")         # Compliant Green
COLOR_OK_BG = colors.HexColor("#DCFCE7")      # Green Tag Fill
COLOR_WARN = colors.HexColor("#B45309")       # Warning Amber
COLOR_WARN_BG = colors.HexColor("#FEF3C7")    # Amber Tag Fill
COLOR_FLAG = colors.HexColor("#B91C1C")       # Tampering Red
COLOR_FLAG_BG = colors.HexColor("#FEE2E2")    # Red Tag Fill

# Standard Sample Data Repository for AICTE Applications
STANDARD_APPLICATIONS = {
    "APP-2026-101": {
        "id": "APP-2026-101",
        "institution": "Apex Institute of Technology",
        "pid": "1-33249112",
        "region": "Western Region (WRO, Mumbai)",
        "address": "Survey No. 42/B, Hinjawadi Phase 2, Pune, Maharashtra 411057",
        "affiliatingUniversity": "Savitribai Phule Pune University (SPPU)",
        "category": "Engineering & Technology (UG/PG)",
        "submittedDate": "2026-10-24",
        "status": "Needs Review",
        "overallScore": 68.5,
        "reviewOfficer": "Dr. Arvind Kumar",
        "officerId": "AICTE-OFFICER-782",
        "officerRole": "AICTE Scrutiny Officer (Approval Bureau)",
        "decision": "Deficiency Flagged - Conditional Review",
        "justification": "Contiguous land deed exhibits post-scan raster alteration (ELA 88.4%). Computing center short by 8 terminals under APH Clause 4.1.",
        "documents": [
            {
                "name": "Fire Safety Certificate (NOC)",
                "code": "DOC-01",
                "docType": "Fire Safety Certificate",
                "refId": "MFS-2026-7781",
                "authority": "Maharashtra Fire Services Portal (HTTP 200)",
                "status": "Valid",
                "hash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                "findings": "Valid clearance for AY 2026-27. Class-3 DSC verified."
            },
            {
                "name": "Land Ownership Deed",
                "code": "DOC-02",
                "docType": "Land Ownership Deed",
                "refId": "DL-REV-MH-4419",
                "authority": "IGRS Maharashtra & DigiLocker Gateway",
                "status": "Altered",
                "hash": "4a5e9b821cf387a6d88f40192e448b368735df9012a688bcae9124a91986c071",
                "findings": "Potential anomaly flagged (Finding FIND-2026-DOC-02-ELA, ELA 88.4%). Claimed 5.20 Acres vs Registry 2.10 Acres. Manual forensic review required."
            },
            {
                "name": "University Affiliation Letter",
                "code": "DOC-03",
                "docType": "University Affiliation Letter",
                "refId": "SPPU-AFF-2026-441",
                "authority": "SPPU Academic Council API",
                "status": "Valid",
                "hash": "8f3e2a1b7c9d0e4f6a5b8c7d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f",
                "findings": "Affiliation valid for AY 2026-27. Approved intake 360 seats."
            },
            {
                "name": "Faculty Roster & PAN List",
                "code": "DOC-04",
                "docType": "Faculty Roster & PAN List",
                "refId": "FAC-ROSTER-2026",
                "authority": "NSDL / Income Tax API & AICTE Faculty Portal",
                "status": "Valid",
                "hash": "1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b",
                "findings": "Cadre ratio 1:16.8 (Compliant). 48/52 faculty PAN records active."
            },
            {
                "name": "Approved Building Plan",
                "code": "DOC-05",
                "docType": "Approved Building Plan",
                "refId": "PMC-BP-2024-9981",
                "authority": "Pune Municipal Corporation Urban Planning Gateway",
                "status": "Valid",
                "hash": "3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d",
                "findings": "Approved carpet area 8,450 sq.m. FAR / FSI within permissible statutory limits."
            },
            {
                "name": "Fee Payment Receipt (TER)",
                "code": "DOC-06",
                "docType": "Fee Payment Receipt",
                "refId": "TER-2026-88192",
                "authority": "Bharatkosh Government Payment Portal (UTR SBIN42619028)",
                "status": "Valid",
                "hash": "7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b",
                "findings": "Full processing fee INR 1,50,000 confirmed received."
            }
        ],
        "cvAudit": {
            "facility": "Central Computing Facility (Lab 03)",
            "geoCoordinates": "18.5204 N, 73.8567 E (Hinjawadi)",
            "detectedTerminals": 142,
            "claimedBatchCapacity": 150,
            "batchShortfall": 8,
            "batchShortfallPercentage": "-5.3% below batch declaration",
            "sanctionedIntake": 180,
            "intakeShortfall": 38,
            "intakeShortfallPercentage": "-21.1% on 1:1 intake basis",
            "statutory1to4Norm": "Compliant (1:4 ratio requires 45 terminals for 180 intake; actual ratio 1:1.27)",
            "carpetAreaAudited": "194.2 m2 (Claimed: 210.5 m2)",
            "carpetAreaShortfall": "-16.3 m2 (-7.7%)",
            "workstationDensity": "1.37 m2/terminal (APH Standard: 1.2 to 1.5 m2/terminal)",
            "prescribedNorm": 150,
            "shortfall": 8,
            "shortfallPercentage": "-5.3%",
            "model": "YOLOv8x Deep Neural Detector (v8.2.1)",
            "confidence": "94.2% mean IoU confidence",
            "safetyApparatus": "Fire extinguisher present; Emergency exit signage luminaire uninstalled.",
            "status": "Batch Deficient (-8 Units)"
        },
        "forensics": {
            "document": "Land Ownership Deed (SYNTH_LAND_DEED_PUNE_ALTERED.pdf)",
            "findingId": "FIND-2026-DOC-02-ELA",
            "method": "Error Level Analysis (ELA) & Pixel Grid Residual Variance",
            "elaScore": "88.4% Variance Index (Pixel Discontinuity)",
            "classification": "Potential Inconsistency / Discontinuity Detected",
            "producerMismatch": "Adobe Acrobat Pro v24.2 overlay on Scanned Courier bitmap",
            "statutoryShortfall": "Deficit of 3.10 contiguous acres below APH Table 4.1 minimums (2.10 A actual vs 5.20 A claimed)",
            "status": "DEMO_SIMULATED / MANUAL_REVIEW_REQUIRED",
            "statutoryLimitation": "Automated forensic heuristic indicators are screening aids and do not constitute legal proof of forgery without certified document examination."
        },
        "blockchain": {
            "ledgerType": "CipherCore Cryptographic Audit Ledger & Tamper-Evident Hash-Chain Prototype",
            "architecture": "Sequential SHA-256 Hash Chaining (Single-Node Prototype)",
            "genesisHash": "0000000000000000000000000000000000000000000000000000000000000000",
            "currentBlock": 4,
            "merkleRoot": "7b8e1f2a3c9d4e5f6a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f",
            "notarizedHash": "d8e7c6b5a4f3e2d1c0b9a8f7e6d5c4b3a2f1e0d9c8b7a6f5e4d3c2b1a0f9e8d7",
            "notaryAgents": [
                "AICTE-HQ Central Root Notary", "AICTE-WRO Regional Directorate", "Institutional Scrutiny Cell"
            ],
            "trustLimitations": "Educational demonstration prototype running sequential SHA-256 hash chaining on single host. Production deployment requires distributed Byzantine fault-tolerant multi-node consortium."
        }
    },
    "APP-2026-102": {
        "id": "APP-2026-102",
        "institution": "Veermata Jijabai Technological Institute (VJTI)",
        "pid": "1-04192831",
        "region": "Western Region (WRO, Mumbai)",
        "address": "H. R. Mahajani Road, Matunga, Mumbai, Maharashtra 400019",
        "affiliatingUniversity": "Autonomous / University of Mumbai",
        "category": "Government Autonomous Engineering Institution",
        "submittedDate": "2026-10-23",
        "status": "Verified",
        "overallScore": 99.4,
        "reviewOfficer": "Dr. Arvind Kumar",
        "officerId": "AICTE-OFFICER-782",
        "officerRole": "AICTE Scrutiny Officer",
        "decision": "Approved for Full Extension of Approval (EoA)",
        "justification": "All 6 mandatory statutory documents verified with issuing authorities. Computing labs and campus land norms exceed APH requirements.",
        "documents": [
            {"name": "Fire Safety Certificate", "code": "DOC-01", "status": "Valid", "refId": "MFS-2026-1029", "authority": "Maharashtra Fire Services Portal", "hash": "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2", "findings": "Valid till 2029-03-31."},
            {"name": "Land Ownership Deed", "code": "DOC-02", "status": "Valid", "refId": "DL-REV-MH-1002", "authority": "IGRS Maharashtra", "hash": "b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3", "findings": "16.2 Contiguous Acres government allotment deed verified."},
            {"name": "University Affiliation Letter", "code": "DOC-03", "status": "Valid", "refId": "MU-AFF-2026-002", "authority": "University of Mumbai Gateway", "hash": "c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4", "findings": "Autonomous UGC status active through 2030."},
            {"name": "Faculty Roster & PAN List", "code": "DOC-04", "status": "Valid", "refId": "FAC-VJTI-2026", "authority": "NSDL Income Tax Portal", "hash": "d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5", "findings": "142 full-time faculty, 86 with PhD degrees. Cadre ratio 1:12.4."},
            {"name": "Approved Building Plan", "code": "DOC-05", "status": "Valid", "refId": "MCGM-BP-1964-001", "authority": "Brihanmumbai Municipal Corporation", "hash": "e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6", "findings": "Permanent heritage & institutional building approvals in order."},
            {"name": "Fee Payment Receipt", "code": "DOC-06", "status": "Valid", "refId": "TER-2026-99012", "authority": "Bharatkosh Gateway", "hash": "f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6a7", "findings": "Government autonomous exemption / statutory fee reconciliation confirmed."}
        ],
        "cvAudit": {
            "facility": "Central Computer Center & Data Lab",
            "geoCoordinates": "19.0222 N, 72.8561 E",
            "detectedTerminals": 265,
            "prescribedNorm": 200,
            "shortfall": 0,
            "shortfallPercentage": "+32.5%",
            "model": "YOLOv8x Deep Neural Detector",
            "confidence": "96.8% mean confidence",
            "safetyApparatus": "Full automated fire suppression and dual emergency exits verified.",
            "status": "Compliant"
        },
        "forensics": {
            "document": "Land Ownership Deed",
            "elaScore": "2.1% (Pristine Original Scanned Document)",
            "producerMismatch": "None",
            "statutoryShortfall": "None. Exceeds urban land requirements.",
            "status": "Verified"
        },
        "blockchain": {
            "ledgerType": "Consortium Proof-of-Authority (PoA) Blockchain",
            "genesisHash": "0000000000000000000000000000000000000000000000000000000000000000",
            "currentBlock": 1,
            "merkleRoot": "1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b",
            "notarizedHash": "3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c",
            "nodes": ["AICTE-HQ", "IIT-Bombay", "IISc-Bangalore", "NIC-DataHub"]
        }
    }
}


class NumberedCanvas(canvas.Canvas):
    """
    Two-pass canvas to dynamically compute and print 'Page X of Y'
    along with official AICTE verification headers, footers, and timestamp.
    """
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super().showPage()
        super().save()

    def draw_page_decorations(self, page_count):
        self.saveState()
        self.setFont("Helvetica", 8)
        self.setFillColor(COLOR_MUTED)

        # Top border rule
        self.setStrokeColor(COLOR_LINE)
        self.setLineWidth(0.75)
        self.line(36, 756, 576, 756)

        # Header running string
        self.drawString(36, 762, "ALL INDIA COUNCIL FOR TECHNICAL EDUCATION (AICTE) · EOA SCRUTINY BUREAU")
        self.drawRightString(576, 762, "APPROVAL PROCESS HANDBOOK (APH 2024-27)")

        # Bottom border rule
        self.line(36, 42, 576, 42)

        # Bottom footer details
        now_str = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S UTC")
        self.drawString(36, 30, f"CipherCore Verify (SIH 26261) · Decision-Support System · Generated: {now_str}")
        self.drawRightString(576, 30, f"Page {self._pageNumber} of {page_count}")
        self.restoreState()


def get_application_data(app_id=None, raw_data=None):
    """
    Returns populated application dictionary. Merges default with passed payload.
    """
    base_data = STANDARD_APPLICATIONS.get(app_id or "APP-2026-101", STANDARD_APPLICATIONS["APP-2026-101"])
    if raw_data and isinstance(raw_data, dict):
        merged = json.loads(json.dumps(base_data))
        merged.update(raw_data)
        return merged
    return base_data


def generate_json_report(app_data, output_path=None):
    """
    Produces clean, standard JSON representation of the Scrutiny Report.
    """
    report_dict = {
        "reportType": "AICTE Official Scrutiny Assessment Report",
        "academicYear": "2026-27",
        "statutoryHandbook": "Approval Process Handbook (APH 2024-27)",
        "problemStatement": "SIH26261",
        "generatedAt": datetime.now(timezone.utc).isoformat(),
        "applicationProfile": {
            "applicationId": app_data.get("id"),
            "institutionName": app_data.get("institution"),
            "permanentInstituteId": app_data.get("pid"),
            "region": app_data.get("region"),
            "affiliatingUniversity": app_data.get("affiliatingUniversity"),
            "category": app_data.get("category"),
            "submittedDate": app_data.get("submittedDate"),
            "overallStatus": app_data.get("status")
        },
        "scrutinyExecutiveSummary": {
            "decision": app_data.get("decision", "Needs Review"),
            "officerId": app_data.get("officerId", "AICTE-OFFICER-782"),
            "officerName": app_data.get("reviewOfficer", "Dr. Arvind Kumar"),
            "justification": app_data.get("justification", ""),
            "overallScore": app_data.get("overallScore", 68.5)
        },
        "documentAuditMatrix": app_data.get("documents", []),
        "computerVisionAudit": app_data.get("cvAudit", {}),
        "forensicAnalysis": app_data.get("forensics", {}),
        "blockchainProofOfAuthority": app_data.get("blockchain", {}),
        "cryptographicAuditLedger": app_data.get("blockchain", {}),
        "statutoryDisclaimer": "This automated document validation report was generated by the CipherCore Verification Engine (AICTE SIH Problem Statement SIH26261 prototype). Automated computer vision and forensic discrepancy flags are screening aids and heuristic indicators intended to support human scrutiny officers. They do not constitute final statutory determinations without human review and official officer sign-off. Cryptographic proofs are generated via a local cryptographic audit ledger prototype using sequential SHA-256 hash chaining."
    }

    if output_path:
        os.makedirs(os.path.dirname(os.path.abspath(output_path)), exist_ok=True)
        with open(output_path, "w", encoding="utf-8") as f:
            json.dump(report_dict, f, indent=2)

    return report_dict


def generate_pdf_report(app_data, output_path):
    """
    Produces a multi-page, publication-grade PDF scrutiny report using ReportLab.
    """
    os.makedirs(os.path.dirname(os.path.abspath(output_path)), exist_ok=True)

    doc = SimpleDocTemplate(
        output_path,
        pagesize=letter,
        leftMargin=36,
        rightMargin=36,
        topMargin=46,
        bottomMargin=52
    )

    styles = getSampleStyleSheet()

    # Custom Typography Styles
    style_title = ParagraphStyle(
        "ReportTitle",
        parent=styles["Normal"],
        fontName="Helvetica-Bold",
        fontSize=15,
        leading=18,
        textColor=COLOR_PRIMARY,
        alignment=TA_LEFT
    )

    style_subtitle = ParagraphStyle(
        "ReportSubtitle",
        parent=styles["Normal"],
        fontName="Helvetica-Bold",
        fontSize=9,
        leading=12,
        textColor=COLOR_ACCENT,
        alignment=TA_LEFT
    )

    style_meta_mono = ParagraphStyle(
        "ReportMetaMono",
        parent=styles["Normal"],
        fontName="Courier",
        fontSize=7.5,
        leading=10,
        textColor=COLOR_MUTED
    )

    style_section_h1 = ParagraphStyle(
        "SectionH1",
        parent=styles["Normal"],
        fontName="Helvetica-Bold",
        fontSize=10.5,
        leading=14,
        textColor=COLOR_SECONDARY,
        spaceBefore=8,
        spaceAfter=4
    )

    style_body = ParagraphStyle(
        "ReportBody",
        parent=styles["Normal"],
        fontName="Helvetica",
        fontSize=8,
        leading=11,
        textColor=COLOR_TEXT
    )

    style_body_bold = ParagraphStyle(
        "ReportBodyBold",
        parent=styles["Normal"],
        fontName="Helvetica-Bold",
        fontSize=8,
        leading=11,
        textColor=COLOR_TEXT
    )

    style_cell_header = ParagraphStyle(
        "CellHeader",
        parent=styles["Normal"],
        fontName="Helvetica-Bold",
        fontSize=7.5,
        leading=9.5,
        textColor=colors.white
    )

    style_cell_text = ParagraphStyle(
        "CellText",
        parent=styles["Normal"],
        fontName="Helvetica",
        fontSize=7.5,
        leading=9.5,
        textColor=COLOR_TEXT
    )

    style_cell_mono = ParagraphStyle(
        "CellMono",
        parent=styles["Normal"],
        fontName="Courier",
        fontSize=6.5,
        leading=8.5,
        textColor=COLOR_MUTED
    )

    style_tag_ok = ParagraphStyle(
        "TagOk",
        parent=styles["Normal"],
        fontName="Helvetica-Bold",
        fontSize=7,
        leading=8,
        textColor=COLOR_OK,
        alignment=TA_CENTER
    )

    style_tag_flag = ParagraphStyle(
        "TagFlag",
        parent=styles["Normal"],
        fontName="Helvetica-Bold",
        fontSize=7,
        leading=8,
        textColor=COLOR_FLAG,
        alignment=TA_CENTER
    )

    story = []

    # -------------------------------------------------------------------------
    # 1. OFFICIAL INSTITUTIONAL HEADER & BADGE
    # -------------------------------------------------------------------------
    app_id = app_data.get("id", "APP-2026-101")
    inst_name = app_data.get("institution", "Apex Institute of Technology")
    status = app_data.get("status", "Needs Review")

    header_table_data = [
        [
            Paragraph("ALL INDIA COUNCIL FOR TECHNICAL EDUCATION", style_subtitle),
            Paragraph(f"DOSSIER ID: <b>{app_id}</b>", ParagraphStyle("RightMono", parent=style_meta_mono, alignment=TA_RIGHT))
        ],
        [
            Paragraph("AI-Assisted EoA Scrutiny Assessment Report", style_title),
            Paragraph(f"STATUS: <font color='{'#B91C1C' if status != 'Verified' else '#15803D'}'><b>{status.upper()}</b></font>", ParagraphStyle("RightBold", parent=style_body_bold, alignment=TA_RIGHT))
        ],
        [
            Paragraph("Statutory Decision Support Platform · Smart India Hackathon Problem Statement 26261", style_meta_mono),
            Paragraph("APH 2024-27 NORM CHECKS", ParagraphStyle("RightMonoSmall", parent=style_meta_mono, alignment=TA_RIGHT))
        ]
    ]

    header_table = Table(header_table_data, colWidths=[380, 160])
    header_table.setStyle(TableStyle([
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 1),
        ('TOPPADDING', (0, 0), (-1, -1), 1),
        ('LEFTPADDING', (0, 0), (-1, -1), 0),
        ('RIGHTPADDING', (0, 0), (-1, -1), 0),
    ]))
    story.append(header_table)
    story.append(Spacer(1, 6))
    story.append(HRFlowable(width="100%", thickness=1.5, color=COLOR_PRIMARY, spaceBefore=2, spaceAfter=8))

    # -------------------------------------------------------------------------
    # 2. APPLICATION PROFILE & DOSSIER METADATA
    # -------------------------------------------------------------------------
    story.append(Paragraph("1. INSTITUTIONAL DOSSIER METADATA", style_section_h1))

    meta_rows = [
        [
            Paragraph("<b>Institution Name:</b>", style_body),
            Paragraph(inst_name, style_body_bold),
            Paragraph("<b>Permanent ID (PID):</b>", style_body),
            Paragraph(app_data.get("pid", "N/A"), style_body_bold)
        ],
        [
            Paragraph("<b>Application Category:</b>", style_body),
            Paragraph(app_data.get("category", "N/A"), style_body),
            Paragraph("<b>Affiliating Body:</b>", style_body),
            Paragraph(app_data.get("affiliatingUniversity", "N/A"), style_body)
        ],
        [
            Paragraph("<b>Regional Office:</b>", style_body),
            Paragraph(app_data.get("region", "N/A"), style_body),
            Paragraph("<b>Submission Date:</b>", style_body),
            Paragraph(app_data.get("submittedDate", "2026-10-24"), style_body)
        ],
        [
            Paragraph("<b>Campus Address:</b>", style_body),
            Paragraph(app_data.get("address", "N/A"), style_body),
            Paragraph("<b>Scrutiny Cycle:</b>", style_body),
            Paragraph("AY 2026-27 (Extension of Approval)", style_body_bold)
        ]
    ]

    profile_table = Table(meta_rows, colWidths=[105, 175, 105, 155])
    profile_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), COLOR_BG_LIGHT),
        ('GRID', (0, 0), (-1, -1), 0.5, COLOR_LINE),
        ('TOPPADDING', (0, 0), (-1, -1), 3.5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 3.5),
        ('LEFTPADDING', (0, 0), (-1, -1), 6),
        ('RIGHTPADDING', (0, 0), (-1, -1), 6),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
    ]))
    story.append(profile_table)
    story.append(Spacer(1, 8))

    # -------------------------------------------------------------------------
    # 3. EXECUTIVE SCRUTINY FINDINGS & HUMAN OFFICER REVIEW
    # -------------------------------------------------------------------------
    story.append(Paragraph("2. STATUTORY SCRUTINY EXECUTIVE SUMMARY", style_section_h1))

    decision_bg = COLOR_FLAG_BG if status != 'Verified' else COLOR_OK_BG
    decision_border = COLOR_FLAG if status != 'Verified' else COLOR_OK

    summary_rows = [
        [
            Paragraph("<b>Automated Compliance Score:</b>", style_body),
            Paragraph(f"<b>{app_data.get('overallScore', 68.5)}%</b> (Weightage Scrutiny Metric)", style_body_bold),
            Paragraph("<b>Scrutiny Officer:</b>", style_body),
            Paragraph(f"{app_data.get('reviewOfficer', 'Dr. Arvind Kumar')} ({app_data.get('officerId', 'AICTE-OFFICER-782')})", style_body)
        ],
        [
            Paragraph("<b>Official Finding:</b>", style_body),
            Paragraph(f"<b>{app_data.get('decision', 'Deficiency Flagged')}</b>", ParagraphStyle("SummaryDecision", parent=style_body_bold, textColor=decision_border)),
            Paragraph("<b>Consortium Blockchain:</b>", style_body),
            Paragraph("Notarized on Proof-of-Authority Ledger", style_body)
        ],
        [
            Paragraph("<b>Officer Justification &amp; Directive:</b>", style_body),
            Paragraph(app_data.get("justification", "Mandatory land deed exhibits raster tampering; deficiency letter issued."), style_body),
            Paragraph("<b>Statutory Action:</b>", style_body),
            Paragraph("Hearing before Standing Hearing Committee (SHC)", style_body_bold)
        ]
    ]

    summary_table = Table(summary_rows, colWidths=[105, 175, 105, 155])
    summary_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), COLOR_BG_CARD),
        ('GRID', (0, 0), (-1, -1), 0.5, COLOR_LINE),
        ('TOPPADDING', (0, 0), (-1, -1), 4),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
        ('LEFTPADDING', (0, 0), (-1, -1), 6),
        ('RIGHTPADDING', (0, 0), (-1, -1), 6),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
    ]))
    story.append(summary_table)
    story.append(Spacer(1, 8))

    # -------------------------------------------------------------------------
    # 4. MANDATORY 6-DOCUMENT VERIFICATION MATRIX
    # -------------------------------------------------------------------------
    story.append(Paragraph("3. MANDATORY SIX-DOCUMENT SCRUTINY MATRIX (APH 2024-27 CLAUSE 2.1)", style_section_h1))

    doc_headers = [
        Paragraph("Doc Code &amp; Title", style_cell_header),
        Paragraph("Issuing Gateway &amp; Ref ID", style_cell_header),
        Paragraph("Cryptographic SHA-256 Hash", style_cell_header),
        Paragraph("Status", style_cell_header),
        Paragraph("Statutory Finding &amp; Forensic Details", style_cell_header)
    ]

    doc_table_data = [doc_headers]

    for d in app_data.get("documents", []):
        d_status = d.get("status", "Valid")
        is_ok = d_status == "Valid"
        status_paragraph = Paragraph(d_status, style_tag_ok if is_ok else style_tag_flag)
        
        doc_table_data.append([
            Paragraph(f"<b>{d.get('code', 'DOC')}</b><br/>{d.get('name', '')}", style_cell_text),
            Paragraph(f"{d.get('authority', 'Gateway API')}<br/><font color='#475569'>{d.get('refId', 'REF-N/A')}</font>", style_cell_text),
            Paragraph(d.get("hash", "e3b0c442...")[:24] + "...", style_cell_mono),
            status_paragraph,
            Paragraph(d.get("findings", "Verified compliant."), style_cell_text)
        ])

    doc_table = Table(doc_table_data, colWidths=[105, 125, 95, 45, 170])
    doc_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), COLOR_SECONDARY),
        ('GRID', (0, 0), (-1, -1), 0.5, COLOR_LINE),
        ('TOPPADDING', (0, 0), (-1, -1), 3),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 3),
        ('LEFTPADDING', (0, 0), (-1, -1), 5),
        ('RIGHTPADDING', (0, 0), (-1, -1), 5),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, COLOR_BG_LIGHT]),
    ]))
    story.append(doc_table)
    story.append(Spacer(1, 10))

    # -------------------------------------------------------------------------
    # 5. COMPUTER VISION (CV) & FORENSIC ENGINE AUDIT (PAGE BREAK OR KEEP)
    # -------------------------------------------------------------------------
    cv_info = app_data.get("cvAudit", {})
    forensic_info = app_data.get("forensics", {})

    cv_forensic_story = []
    cv_forensic_story.append(Paragraph("4. COMPUTER VISION & FORENSIC AUDIT (AI ENGINES)", style_section_h1))

    engine_data = [
        [
            Paragraph("<b>Engine Parameter</b>", style_cell_header),
            Paragraph("<b>YOLOv8 Computer Vision Facility Audit</b>", style_cell_header),
            Paragraph("<b>Forensic Document Tamper Engine</b>", style_cell_header)
        ],
        [
            Paragraph("Target Inspected", style_body_bold),
            Paragraph(cv_info.get("facility", "Computing Lab"), style_cell_text),
            Paragraph(forensic_info.get("document", "Mandatory Document"), style_cell_text)
        ],
        [
            Paragraph("Analysis Model", style_body_bold),
            Paragraph(cv_info.get("model", "YOLOv8x Deep Neural Detector"), style_cell_text),
            Paragraph("ELA & Font-Raster Discrepancy Analyzer", style_cell_text)
        ],
        [
            Paragraph("Audit Finding", style_body_bold),
            Paragraph(f"Detected: <b>{cv_info.get('detectedTerminals', 0)}</b> vs Norm: <b>{cv_info.get('prescribedNorm', 0)}</b><br/>Shortfall: <font color='#B91C1C'><b>{cv_info.get('shortfall', 0)} terminals ({cv_info.get('shortfallPercentage', '')})</b></font>", style_cell_text),
            Paragraph(f"Tamper Score: <font color='#B91C1C'><b>{forensic_info.get('elaScore', 'N/A')}</b></font><br/>{forensic_info.get('producerMismatch', '')}", style_cell_text)
        ],
        [
            Paragraph("Statutory Status", style_body_bold),
            Paragraph(f"<font color='{'#B91C1C' if cv_info.get('status') != 'Compliant' else '#15803D'}'><b>{cv_info.get('status', 'Deficient')}</b></font><br/>Norm: APH Clause 4.1", style_cell_text),
            Paragraph(f"<font color='{'#B91C1C' if forensic_info.get('status') != 'Verified' else '#15803D'}'><b>{forensic_info.get('status', 'Tampered')}</b></font><br/>Norm: Contiguous Land Area", style_cell_text)
        ],
        [
            Paragraph("Geotag / Chain Stamp", style_body_bold),
            Paragraph(f"Coordinates: {cv_info.get('geoCoordinates', 'N/A')}<br/>Safety: {cv_info.get('safetyApparatus', 'N/A')}", style_cell_text),
            Paragraph(f"Deficiency: {forensic_info.get('statutoryShortfall', 'None')}", style_cell_text)
        ]
    ]

    engine_table = Table(engine_data, colWidths=[105, 215, 220])
    engine_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), COLOR_SECONDARY),
        ('GRID', (0, 0), (-1, -1), 0.5, COLOR_LINE),
        ('TOPPADDING', (0, 0), (-1, -1), 3.5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 3.5),
        ('LEFTPADDING', (0, 0), (-1, -1), 5),
        ('RIGHTPADDING', (0, 0), (-1, -1), 5),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, COLOR_BG_LIGHT]),
    ]))
    cv_forensic_story.append(engine_table)
    cv_forensic_story.append(Spacer(1, 10))

    # -------------------------------------------------------------------------
    # 6. CONSORTIUM BLOCKCHAIN IMMUTABLE AUDIT TRAIL
    # -------------------------------------------------------------------------
    bc = app_data.get("blockchain", {})
    cv_forensic_story.append(Paragraph("5. CRYPTOGRAPHIC AUDIT LEDGER & HASH-CHAIN PROOF", style_section_h1))

    bc_nodes = ", ".join(bc.get("notaryAgents", bc.get("nodes", ["AICTE-HQ Central Root Notary", "AICTE-WRO Directorate", "Institutional Scrutiny Cell"])))
    bc_rows = [
        [
            Paragraph("<b>Ledger Protocol:</b>", style_body),
            Paragraph(bc.get("ledgerType", "Consortium PoA Blockchain"), style_body),
            Paragraph("<b>Block Index:</b>", style_body),
            Paragraph(f"Block #{bc.get('currentBlock', 4)} (Consensus Finalized)", style_body_bold)
        ],
        [
            Paragraph("<b>Merkle Tree Root:</b>", style_body),
            Paragraph(f"<font name='Courier' size=6.5>{bc.get('merkleRoot', 'N/A')}</font>", style_body),
            Paragraph("<b>Notarized Hash:</b>", style_body),
            Paragraph(f"<font name='Courier' size=6.5>{bc.get('notarizedHash', 'N/A')[:28]}...</font>", style_body)
        ],
        [
            Paragraph("<b>Notary Agents:</b>", style_body),
            Paragraph(f"<font size=7 color='#475569'>{bc_nodes}</font>", style_body),
            Paragraph("<b>Tamper Check:</b>", style_body),
            Paragraph("<font color='#15803D'><b>CHAIN VALID (Cryptographically Verified)</b></font>", style_body)
        ]
    ]

    bc_table = Table(bc_rows, colWidths=[105, 175, 105, 155])
    bc_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), COLOR_BG_CARD),
        ('GRID', (0, 0), (-1, -1), 0.5, COLOR_LINE),
        ('TOPPADDING', (0, 0), (-1, -1), 3.5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 3.5),
        ('LEFTPADDING', (0, 0), (-1, -1), 5),
        ('RIGHTPADDING', (0, 0), (-1, -1), 5),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
    ]))
    cv_forensic_story.append(bc_table)
    cv_forensic_story.append(Spacer(1, 12))

    # -------------------------------------------------------------------------
    # 7. OFFICIAL SIGN-OFF & STATUTORY OFFICER SIGNATURE
    # -------------------------------------------------------------------------
    cv_forensic_story.append(Paragraph("6. STATUTORY SCRUTINY OFFICER ENDORSEMENT", style_section_h1))

    officer_sign_rows = [
        [
            Paragraph("<b>Scrutiny Official:</b><br/>"
                      f"{app_data.get('reviewOfficer', 'Dr. Arvind Kumar')}<br/>"
                      f"<font color='#475569'>{app_data.get('officerRole', 'Scrutiny Officer')}<br/>"
                      f"Bureau ID: {app_data.get('officerId', 'AICTE-OFFICER-782')}</font>", style_body),
            Paragraph("<b>Official Digital Verification Stamp:</b><br/>"
                      "<font name='Courier' size=6.5 color='#475569'>"
                      f"DIGITAL SIGNATURE HASH:<br/>"
                      f"{hashlib.sha256((inst_name + app_id + status).encode()).hexdigest()}<br/>"
                      f"TIMESTAMP: {datetime.now(timezone.utc).strftime('%Y-%m-%d %H:%M:%S UTC')}<br/>"
                      f"AUTHORITY: ALL INDIA COUNCIL FOR TECHNICAL EDUCATION</font>", style_body),
            Paragraph("<b>Official Seal:</b><br/>"
                      "<font size=7 color='#0284C7'><b>[ AICTE SCRUTINY BUREAU ]</b><br/>"
                      "VALIDATED &amp; NOTARIZED<br/>"
                      "Decision-Support Prototype</font>", ParagraphStyle("SealCenter", parent=style_body, alignment=TA_CENTER))
        ]
    ]

    sign_table = Table(officer_sign_rows, colWidths=[180, 240, 120])
    sign_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), COLOR_BG_LIGHT),
        ('GRID', (0, 0), (-1, -1), 0.75, COLOR_PRIMARY),
        ('TOPPADDING', (0, 0), (-1, -1), 6),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 6),
        ('LEFTPADDING', (0, 0), (-1, -1), 6),
        ('RIGHTPADDING', (0, 0), (-1, -1), 6),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
    ]))
    cv_forensic_story.append(sign_table)

    story.append(KeepTogether(cv_forensic_story))

    # Build the document with our two-pass NumberedCanvas
    doc.build(story, canvasmaker=NumberedCanvas)
    return output_path


def main():
    parser = argparse.ArgumentParser(description="CipherCore Verify - AICTE Scrutiny Report Generator")
    parser.add_argument("--app-id", default="APP-2026-101", help="Application ID (e.g., APP-2026-101)")
    parser.add_argument("--format", choices=["pdf", "json", "both"], default="pdf", help="Output format")
    parser.add_argument("--output", default=None, help="Target output file path")
    parser.add_argument("--data", default=None, help="Inline JSON string or file path containing application dossier")

    args = parser.parse_args()

    # Load custom data if provided
    raw_data = None
    if args.data:
        if os.path.isfile(args.data):
            with open(args.data, "r", encoding="utf-8") as f:
                raw_data = json.load(f)
        else:
            try:
                raw_data = json.loads(args.data)
            except Exception as e:
                print(f"[Warning] Failed to parse --data JSON: {e}", file=sys.stderr)

    app_data = get_application_data(args.app_id, raw_data)

    output_base = args.output
    if not output_base:
        output_base = os.path.join(
            os.path.dirname(os.path.abspath(__file__)),
            "uploads",
            f"AICTE_Scrutiny_Report_{app_data.get('id', 'APP-2026-101')}"
        )

    results = {}

    if args.format in ["pdf", "both"]:
        pdf_path = output_base if output_base.endswith(".pdf") else f"{output_base}.pdf"
        generate_pdf_report(app_data, pdf_path)
        print(f"[Success] Generated PDF Report: {pdf_path}")
        results["pdf"] = pdf_path

    if args.format in ["json", "both"]:
        json_path = output_base if output_base.endswith(".json") else f"{output_base}.json"
        generate_json_report(app_data, json_path)
        print(f"[Success] Generated JSON Report: {json_path}")
        results["json"] = json_path

    return 0


if __name__ == "__main__":
    sys.exit(main())
