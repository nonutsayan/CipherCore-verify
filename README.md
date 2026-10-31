# CipherCore Verify — Regulatory Intelligence & Blockchain Notarization Engine

<div align="center">

[![Smart India Hackathon 2026](https://img.shields.io/badge/SIH-2026_Grand_Finale-00F0FF?style=for-the-badge&logo=target&logoColor=white)](https://nonutsayan.github.io/CipherCore-verify/)
[![Problem Statement SIH26261](https://img.shields.io/badge/AICTE-Problem_SIH26261-00FF88?style=for-the-badge&logo=shield&logoColor=black)](https://nonutsayan.github.io/CipherCore-verify/)
[![Live Deployment](https://img.shields.io/badge/Live_Demo-GitHub_Pages-38BDF8?style=for-the-badge&logo=github&logoColor=white)](https://nonutsayan.github.io/CipherCore-verify/)
[![License Apache-2.0](https://img.shields.io/badge/License-Apache_2.0-F59E0B?style=for-the-badge)](LICENSE)

**AI-Powered Comprehensive Institutional Verification, Forensic Document Authentication & Regulatory Automation Engine**

[Live Web Application](https://nonutsayan.github.io/CipherCore-verify/) • [Cyber Command Portal](https://nonutsayan.github.io/CipherCore-verify/portal.html) • [SIH Jury Master Video (2m 42s)](assets/video/sih_jury_showcase.mp4) • [Architecture Report](docs/ROOT_CAUSE_AND_REPORT_GENERATOR_IMPLEMENTATION_REPORT.md)

</div>

---

## 1. Executive Summary

**CipherCore Verify** is a high-performance, zero-trust regulatory decision-support infrastructure engineered for the **All India Council for Technical Education (AICTE)** under **Smart India Hackathon 2026 Problem Statement SIH26261**.

The platform automates the scrutiny of engineering, management, and technical institution approval applications across India by uniting:
1. **Multi-Vector Cryptographic Ingestion**: 100% client-side WebAssembly byte-level extraction with bitwise Shannon entropy validation ($7.98\text{ bits/byte}$) and zero raw data egress.
2. **Forensic Error Level Analysis (ELA)**: Magenta compression variance heatmap exposing statutory land deed and NOC document tampering ($88.4\%$ discrepancy detection).
3. **Computer Vision Facility Audit**: YOLOv8-powered neural workstation detection ($99.4\%$ confidence), spatial floor carpet density validation ($1.32\text{ m}^2 \ge 1.20\text{ m}^2$ APH norms), and EXIF GPS geofence verification.
4. **Authority Gateway Cross-Checking**: Live API attestation against sovereign DigiLocker, State Fire Services, and University Affiliation registries.
5. **Consortium Proof-of-Authority (PoA) Blockchain Ledger**: Immutable SHA-256 block notarization with Class 3 Digital Signature Certificate (DSC) stamping by statutory regulatory officers.

---

## 2. Quick Start & Execution

### Prerequisites
- **Node.js**: v18.0.0 or higher
- **Java**: JDK 17 or JDK 21 (for Spring Boot backend)
- **Maven**: 3.8+ (for backend compilation)
- **Python**: 3.9+ (for Report Generator service)

---

### Step 1: Clone and Enter Repository
```bash
git clone https://github.com/nonutsayan/CipherCore-verify.git
cd CipherCore-verify
```

### Step 2: Start the Web Proxy Server (Port 3000)
```bash
npm start
# or: node server.js
```
- Open **[http://localhost:3000](http://localhost:3000)** in any modern browser.
- Direct Cyber Command Portal: **[http://localhost:3000/portal.html](http://localhost:3000/portal.html)**.

### Step 3 (Optional): Start the Spring Boot Backend (Port 8080)
```bash
cd backend
mvn spring-boot:run
```
- Embedded H2 Console: **[http://localhost:8080/h2-console](http://localhost:8080/h2-console)** (`jdbc:h2:file:./data/ciphercore_db`, User: `sa`, Password: *empty*).
- OpenAPI / Swagger UI: **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**.

### Step 4: Run Automated Verification Tests
```bash
npm test
# Verifies live endpoints, modal controllers, and forensic engines
```

---

## 3. Organized Project Directory Structure

```text
CipherCore-verify/
├── index.html                   # Live Landing Page (Hero, 3D Document Fan, Architecture Overview)
├── portal.html                  # Cyber Command Verification Portal (MailScope Slide-Over Drawer UI)
├── server.js                    # Node.js Static Server & Reverse Proxy (Port 3000)
├── package.json                 # Standard npm manifest (start, test, sync, package)
├── LICENSE                      # Apache-2.0 Open Source License
├── README.md                    # Master Project Documentation & Evaluation Guide
├── PRODUCT.md                   # Complete Product Requirements & Specification
├── DESIGN.md                    # Design System, Palette Tokens & Typography Standards
│
├── frontend/                    # Dedicated Frontend Mirror for Node Server
│   ├── index.html               # Frontend landing page mirror
│   ├── portal.html              # Frontend portal mirror
│   ├── css/                     # Modular Stylesheets
│   │   ├── mailscope-theme.css  # Dark carbon-obsidian theme & glassmorphism
│   │   ├── drawer-engine.css    # Right slide-over drawer with spring physics
│   │   └── cyber-components.css # Tables, metrics badges, modal backdrops
│   ├── js/                      # Modular Frontend Controllers
│   │   ├── app.js               # Master application router & 127 backend integration routines
│   │   ├── drawer.js            # Universal Slide-Over Drawer Controller
│   │   ├── blockchain-viz.js    # 2D PoA 7-validator consensus orbital ring
│   │   └── three-core.js        # Three.js 3D Intelligence Core
│   └── assets/                  # Frontend static assets, reports & video mirrors
│
├── backend/                     # Spring Boot Java 21 Backend
│   ├── pom.xml                  # Maven dependencies & build configuration
│   ├── report_generator.py      # AICTE Scrutiny PDF/JSON Report Generator
│   ├── data/                    # Persistent embedded H2 Database
│   ├── uploads/                 # Generated audit reports & certificates
│   └── src/main/java/           # Spring Boot REST APIs, Services & Blockchain Models
│
├── assets/                      # Production Media & Static Resources
│   ├── video/                   # Full HD Walkthrough Videos & Poster Frames
│   │   ├── sih_jury_showcase.mp4 # 162s SIH Grand Finale Master Cut (Neural VO)
│   │   ├── sih_jury_showcase.jpg # High-resolution poster frame
│   │   ├── brag.mp4             # 42s SaaS Product Walkthrough Video
│   │   └── brag.jpg             # Walkthrough poster frame
│   └── reports/                 # Sample pre-rendered AICTE scrutiny reports
│
├── css/                         # Root stylesheets (GitHub Pages direct resolution)
├── js/                          # Root JavaScript controllers (GitHub Pages direct resolution)
├── docs/                        # Technical Architecture, Audit Reports & Analysis
│   ├── ROOT_CAUSE_AND_REPORT_GENERATOR_IMPLEMENTATION_REPORT.md
│   ├── TAB_FREEZE_DEBUG_LOG.md
│   └── TAB_FREEZE_ROOT_CAUSE_REPORT.md
│
├── scripts/                     # Automated Testing, Sync & Verification Utilities
│   ├── sync_all_mirrors.py      # Automated mirror synchronization across root, frontend & backend
│   ├── verify_live_pages.js     # Live HTTP/DOM regression test suite
│   ├── test_header_signout.js   # Header flexbox & navigation hardening test
│   └── generate_report_pdf.py   # PDF generation utility
│
├── brag-sih-jury/               # SIH Grand Finale Video Production Workspace (Hyperframes)
│   ├── brag.mp4                 # Master 162-second walkthrough video
│   ├── composition/             # Hyperframes timeline composition source & audio layers
│   └── brag-plan.md             # Complete 6-act storyboard specification
│
├── archive/                     # Preserved Historical Development Scripts & Snippets
│   ├── scratch_scripts/         # 90 development, inspection & patch scripts
│   └── snippets/                # Historical component snippets & backup drafts
│
└── tools/                       # Development & Tunneling Utilities
    └── bin/                     # Local binaries (cloudflared)
```

---

## 4. Live Application Endpoints & Test Matrix

| Component | Live URL / Endpoint | Protocol / Port | Purpose |
| :--- | :--- | :--- | :--- |
| **Production Web Portal** | [https://nonutsayan.github.io/CipherCore-verify/](https://nonutsayan.github.io/CipherCore-verify/) | HTTPS | Live GitHub Pages Production Host |
| **Cyber Command Centre** | [https://nonutsayan.github.io/CipherCore-verify/portal.html](https://nonutsayan.github.io/CipherCore-verify/portal.html) | HTTPS | Institutional Dossier Queue & Inspection Drawer |
| **Local Proxy Server** | `http://localhost:3000` | HTTP :3000 | Node.js proxy server with integrated report generator |
| **Spring Boot Backend** | `http://localhost:8080` | HTTP :8080 | Embedded Spring Boot static resource mirror |
| **Swagger / OpenAPI** | `http://localhost:8080/swagger-ui.html` | HTTP :8080 | Interactive REST API documentation |
| **Consortium PoA Stats**| `GET /api/blockchain/stats` | JSON REST | 7 Nodes, 0.0 Gwei, 2.0s block time |
| **Audit Integrity API** | `GET /api/audit/integrity` | JSON REST | SHA-256 hash-chain verification (`valid: true`) |
| **Report Generation** | `GET /api/reports/generate?appId=APP-2026-101` | PDF Stream | Generates official statutory scrutiny certificate |

---

## 5. Walkthrough Demonstration Videos

The project includes two high-definition demonstration videos rendered with Hyperframes:

1. **SIH Grand Finale Master Cut (2m 42s)** — [`assets/video/sih_jury_showcase.mp4`](assets/video/sih_jury_showcase.mp4)
   - Authoritative studio neural voiceover (*Microsoft Christopher Neural*).
   - 6-act deep dive: Enclave boot, staggered physics, multi-vector file parsing, 3-tier validation sequence, single-bit adversarial tamper breach refusal, and architectural wrap-up.
2. **SaaS Product Walkthrough (42s)** — [`assets/video/brag.mp4`](assets/video/brag.mp4)
   - Fast-paced product walkthrough featuring the 3D statutory document fan, operations queue, ELA diff, YOLOv8 lab scan, and Class 3 DSC blockchain notarization.

---

## 6. SIH Evaluation Jury Verification Guide

### Test Application Dossier: `APP-2026-101` (Pune Institute of Advanced Technology)
1. **Open Verification Portal**: Navigate to `portal.html` or click **"Enter Verification Portal"** on the landing page.
2. **Inspect Case Drawer**: Click row `APP-2026-101` in the institutional queue. The right-side cyber drawer slides out smoothly (`cubic-bezier(0.22, 1, 0.36, 1)`).
3. **Forensic ELA Tamper Diff**: Under the *Forensic Anomalies* section, click **"Inspect Forensic ELA Variance"**. The modal renders the submitted land deed side-by-side with the DigiLocker sovereign copy, highlighting an **88.4% tamper discrepancy** on Survey Plot #402/A.
4. **Computer Vision Lab Facility Audit**: Click **"Inspect Computer Vision Evidence"**. View the YOLOv8 object detection overlay locking onto PC terminals ($99.4\%$ confidence), density calculations ($1.32\text{ m}^2 / \text{workstation}$), and GPS coordinates ($18.5204^\circ\text{ N}, 73.8567^\circ\text{ E}$).
5. **Human Decision Gate & Blockchain Stamping**: Regulatory Officer Dr. Arvind Kumar reviews statutory findings, confirms DSC Class 3 signing, and commits the decision to PoA Block `#104,289`.
6. **Download Statutory Scrutiny Report**: Click **"Export AICTE Scrutiny Report (PDF)"** to trigger the embedded report generator.

---

## 7. License

This project is licensed under the **Apache License 2.0** — see the [LICENSE](LICENSE) file for details.

*Developed with pride for the Smart India Hackathon (SIH 2026) · Problem Statement SIH26261 · AICTE Regulatory Automation.*
