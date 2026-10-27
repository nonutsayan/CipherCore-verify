import re

with open('backend/report_generator.py', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update Land Deed findings in APP-2026-101
content = content.replace(
    '"findings": "Tampering detected (ELA 88.4%). Claimed 5.20 Acres vs Registry 2.10 Acres."',
    '"findings": "Potential anomaly flagged (Finding FIND-2026-DOC-02-ELA, ELA 88.4%). Claimed 5.20 Acres vs Registry 2.10 Acres. Manual forensic review required."'
)

# 2. Update cvAudit in APP-2026-101
target_cv = """        "cvAudit": {
            "facility": "Central Computing Facility (Lab 03)",
            "geoCoordinates": "18.5204 N, 73.8567 E (Hinjawadi)",
            "detectedTerminals": 142,
            "prescribedNorm": 150,
            "shortfall": 8,
            "shortfallPercentage": "-5.3%",
            "model": "YOLOv8x Deep Neural Detector (v8.2.1)",
            "confidence": "94.2% mean IoU confidence",
            "safetyApparatus": "Fire extinguisher present; Emergency exit signage luminaire uninstalled.",
            "status": "Deficient"
        },"""

replacement_cv = """        "cvAudit": {
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
        },"""

content = content.replace(target_cv.replace('\n', '\r\n'), replacement_cv.replace('\n', '\r\n'))
if target_cv in content:
    content = content.replace(target_cv, replacement_cv)

# 3. Update forensics in APP-2026-101
target_forensics = """        "forensics": {
            "document": "Land Ownership Deed (SYNTH_LAND_DEED_PUNE_ALTERED.pdf)",
            "elaScore": "88.4% Tamper Overlay Detected",
            "producerMismatch": "Adobe Acrobat Pro v24.2 overlay on Scanned Courier bitmap",
            "statutoryShortfall": "Deficit of 3.10 contiguous acres below APH Table 4.1 minimums",
            "status": "Tampered"
        },"""

replacement_forensics = """        "forensics": {
            "document": "Land Ownership Deed (SYNTH_LAND_DEED_PUNE_ALTERED.pdf)",
            "findingId": "FIND-2026-DOC-02-ELA",
            "method": "Error Level Analysis (ELA) & Pixel Grid Residual Variance",
            "elaScore": "88.4% Variance Index (Pixel Discontinuity)",
            "classification": "Potential Inconsistency / Discontinuity Detected",
            "producerMismatch": "Adobe Acrobat Pro v24.2 overlay on Scanned Courier bitmap",
            "statutoryShortfall": "Deficit of 3.10 contiguous acres below APH Table 4.1 minimums (2.10 A actual vs 5.20 A claimed)",
            "status": "DEMO_SIMULATED / MANUAL_REVIEW_REQUIRED",
            "statutoryLimitation": "Automated forensic heuristic indicators are screening aids and do not constitute legal proof of forgery without certified document examination."
        },"""

content = content.replace(target_forensics.replace('\n', '\r\n'), replacement_forensics.replace('\n', '\r\n'))
if target_forensics in content:
    content = content.replace(target_forensics, replacement_forensics)

# 4. Update blockchain in APP-2026-101
target_bc = """        "blockchain": {
            "ledgerType": "Consortium Proof-of-Authority (PoA) Blockchain",
            "genesisHash": "0000000000000000000000000000000000000000000000000000000000000000",
            "currentBlock": 4,
            "merkleRoot": "7b8e1f2a3c9d4e5f6a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f",
            "notarizedHash": "d8e7c6b5a4f3e2d1c0b9a8f7e6d5c4b3a2f1e0d9c8b7a6f5e4d3c2b1a0f9e8d7",
            "nodes": [
                "AICTE-HQ (New Delhi)", "IIT-Bombay Notary Node", "IIT-Madras Validator",
                "IISc-Bangalore Anchor Node", "AICTE-WRO (Mumbai)", "MoE-Gov Gateway", "NIC-DataHub"
            ]
        }"""

replacement_bc = """        "blockchain": {
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
        }"""

content = content.replace(target_bc.replace('\n', '\r\n'), replacement_bc.replace('\n', '\r\n'))
if target_bc in content:
    content = content.replace(target_bc, replacement_bc)

# 5. Update Section 5 Title in PDF Story
content = content.replace(
    'cv_forensic_story.append(Paragraph("5. CONSORTIUM PROOF-OF-AUTHORITY BLOCKCHAIN AUDIT PROOF", style_section_h1))',
    'cv_forensic_story.append(Paragraph("5. CRYPTOGRAPHIC AUDIT LEDGER & HASH-CHAIN PROOF", style_section_h1))'
)

# 6. Update validator nodes text in PDF Story
content = content.replace(
    'bc_nodes = ", ".join(bc.get("nodes", ["AICTE-HQ", "IIT-Bombay", "IISc-Bangalore", "NIC"]))',
    'bc_nodes = ", ".join(bc.get("notaryAgents", bc.get("nodes", ["AICTE-HQ Central Root Notary", "AICTE-WRO Directorate", "Institutional Scrutiny Cell"])))'
)

# 7. Update bc_rows header
content = content.replace(
    'Paragraph("<b>Validator Nodes:</b>", style_body),',
    'Paragraph("<b>Notary Agents:</b>", style_body),'
)

with open('backend/report_generator.py', 'w', encoding='utf-8') as f:
    f.write(content)

print('Updated backend/report_generator.py successfully')
