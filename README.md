# CipherCore // Project Chimera — Regulatory Intelligence & Blockchain Command Centre
### Smart India Hackathon 2026 · Problem Statement SIH26261
**Problem Statement:** AI-Powered Application Validation, Document Authentication and Regulatory Compliance Verification

---

## 1. Quick Start Commands

### Prerequisites
- **Java**: JDK 17 or JDK 21 (Maven configured)
- **Node.js**: v18+ (standard library only, zero external npm installs required)

---

### Step 1: Open Project in VS Code
Open your terminal (PowerShell, CMD, or Bash) and run:
```bash
code "C:\Users\Anisa Shaikh\.gemini\antigravity\scratch\aicte-sih-portal"
```
Or open VS Code manually and click **File ➔ Open Folder** and select:
`C:\Users\Anisa Shaikh\.gemini\antigravity\scratch\aicte-sih-portal`

---

### Step 2: Start the Spring Boot Backend (Port 8080)
Open a terminal in the `backend` directory:
```bash
cd "C:\Users\Anisa Shaikh\.gemini\antigravity\scratch\aicte-sih-portal\backend"
mvn spring-boot:run
```
*Backend runs at: [http://localhost:8080](http://localhost:8080)*

---

### Step 3: Start the Frontend Proxy Server (Port 3000)
Open a second terminal in the project root directory:
```bash
cd "C:\Users\Anisa Shaikh\.gemini\antigravity\scratch\aicte-sih-portal"
node server.js
```
*Frontend runs at: [http://localhost:3000](http://localhost:3000)*

---

### Step 4: Run Automated Regression Tests
```bash
cd "C:\Users\Anisa Shaikh\.gemini\antigravity\scratch\aicte-sih-portal\backend"
mvn test
```

---

## 2. Live Application Endpoints & Ports

| Component | URL / Endpoint | Description |
| :--- | :--- | :--- |
| **Primary Web Portal** | [http://localhost:3000](http://localhost:3000) | **MailScope-Inspired Cyber Command UI** with Slide-Over Drawers |
| **Direct Backend Mirror** | [http://localhost:8080](http://localhost:8080) | Embedded Spring Boot static resource mirror |
| **OpenAPI / Swagger** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) | Interactive REST API documentation |
| **H2 Database Console**| [http://localhost:8080/h2-console](http://localhost:8080/h2-console) | JDBC URL: `jdbc:h2:file:./data/ciphercore_db`<br>User: `sa`<br>Password: *(leave empty)* |
| **Consortium PoA Stats**| `GET http://localhost:8080/api/blockchain/stats` | Real-time PoA consensus stats (7 Nodes, 0.0 Gwei, 2.0s block time) |
| **Audit Integrity API** | `GET http://localhost:8080/api/audit/integrity` | Cryptographic SHA-256 hash-chain verification (`valid: true`) |
| **Dashboard Summary** | `GET http://localhost:8080/api/dashboard/summary` | Real-time application metrics (45 total applications) |
| **Zip Download Link** | [http://localhost:3000/aicte-sih-portal.zip](http://localhost:3000/aicte-sih-portal.zip) | Direct browser download of complete project zip (1.69 MB) |

---

## 3. Dedicated `frontend/` Architecture & MailScope Slide-Over Drawer System

### Project Directory Structure
```text
aicte-sih-portal/
├── frontend/                     # Dedicated Modular Frontend
│   ├── index.html                # Main application interface with Slide-Over Drawer Engine
│   ├── css/
│   │   ├── mailscope-theme.css   # Dark glassmorphism, carbon obsidian palette & card states
│   │   ├── drawer-engine.css     # Right slide-over drawer with spring cubic-bezier(0.22,1,0.36,1)
│   │   └── cyber-components.css  # Full component library, tables, metrics, bounding boxes
│   ├── js/
│   │   ├── drawer.js             # Universal Slide-Over Drawer Controller (inspect, tabs, telemetry)
│   │   ├── three-core.js         # Interactive 3D Three.js Intelligence Core
│   │   ├── blockchain-viz.js     # 2D PoA 7-validator consensus orbital ring
│   │   └── app.js                # Master application router & 127 backend integration functions
│   └── assets/
│       └── three.min.js          # Local Three.js library
├── backend/                      # Spring Boot Java 21 Backend (Port 8080)
│   ├── pom.xml
│   ├── data/                     # Embedded persistent H2 database (ciphercore_db.mv.db)
│   └── src/main/
├── server.js                     # Node.js static server (serves frontend/ & proxies /api to 8080)
├── index.html                    # Root mirror / entry point
└── aicte-sih-portal.zip          # Standalone 1.69 MB portable archive
```

### The Slide-Over Drawer Engine (Inspired by MailScope)
Instead of static popups or disruptive modals, clicking **ANY** item in the interface smoothly slides open a **right-side cyber investigation drawer** (`cubic-bezier(0.22, 1, 0.36, 1)`) with:
- **Documents / OCR**: Full spatial coordinate preview, bounding box highlights, extracted field key-values, and inline field correction form.
- **Forensic Anomalies**: Error Level Analysis (ELA) variance graph, clone stamp cosine similarity, EXIF timestamp mismatch, and statutory disclaimer banner.
- **Issuing Authorities**: Live DigiLocker / Fire / University / RoC API attestation, XML digital signature verification, and TLS response latency.
- **AICTE Compliance Deficiencies**: APH 2024-27 chapter and clause citations, mathematical deficit calculations, and officer adjudication actions.
- **Blockchain Blocks**: Raw block hex, 7/7 validator signatures, Keccak-256 hashes, and Merkle tree inclusion proof diagram.
- **Audit Records**: RFC 3161 immutable cryptographic trail, SHA-256 hash continuity pointers, and officer identity sign-offs.
- **Live Notifications**: Instant alert slide-out with one-click "Mark All Read".

---

## 4. Preserved Full-Stack Integrity
- **100% ID Preservation**: 334 element IDs (all 329 original IDs + 5 upgraded metrics).
- **100% Function Preservation**: All 127 JavaScript functions preserved with exact signatures.
- **Zero Framework Migration**: Progressive HTML5 + CSS3 + vanilla JavaScript architecture; no heavy node build steps or runtime framework overhead.
- **19 Modals Preserved**: Field corrections, audit log, JSON reports, adjudication, compliance override, comparator, and dossier inspection.
