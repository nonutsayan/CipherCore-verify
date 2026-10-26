# Product: CipherCore Verify

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

AICTE Regulatory Scrutiny Officers, Technical Evaluation Committee Members, Institutional Liaisons, and Higher Education Administrators processing Extension of Approval (EoA) applications under Smart India Hackathon (SIH 2026) Problem Statement 26261.

## Product Purpose

CipherCore Verify is an AI-powered regulatory decision-support and document verification portal designed for AICTE. It provides autonomous regulatory scrutiny and forensic authentication for technical institutions through:
1. Automated forensic tamper detection and error level analysis (ELA) on uploaded statutory PDFs.
2. Issuing authority API validation against State Fire Services, DigiLocker State Revenue Records, and University Affiliation Registries.
3. Edge computer vision (YOLOv8x) audit of laboratory infrastructure, workstation density, and safety norm compliance.
4. Tamper-evident cryptographic blockchain ledger (SHA-256 with Proof-of-Authority consensus) ensuring immutability of scrutiny assessments and audit trails.

## Architecture

- **Frontend Core**: High-contrast, single-page, self-contained regulatory interface without external build tool dependencies.
- **Security & Cryptography**: Web Crypto API (`window.crypto.subtle`) client-side SHA-256 hashing and PoA consortium ledger emulation.
- **Forensic Engine**: Side-by-side split visual diffing (Submitted PDFs vs DigiLocker Ground Truth) and Error Level Analysis (ELA: 88.4%).
- **Vision Analytics**: HTML5 Canvas vector simulation of YOLOv8x inference with bounding box detection, geofencing (18.5204° N, 73.8567° E), and thermal occupancy overlays.
- **Backend Services**: Spring Boot REST microservices (`/api/blockchain`, `/api/verification`, `/api/audit`) with failover to resilient client-side execution.

## Core Design Constraints

- **Strict High-Contrast Regulatory UI**: Light mode default (`#f8fafc` background, `#ffffff` card surfaces, `1px solid #e2e8f0` borders, `#0f172a` text/dark mode, `#0284c7` solid regulatory blue accents).
- **Rectangular Geometry**: Strictly 4px or 6px border radii on buttons and cards. Zero pill-shaped buttons.
- **Zero Decorative Slop**: Zero purple/violet gradients, zero glowing colored halos, zero auto-scrolling marquees, zero emojis (inline SVGs only), zero em dashes.
- **Legibility & Accessibility**: WCAG 2.1 AA compliant text contrast ratios, minimum 11px functional UI text, keyboard accessibility with Escape key modal dismissals.

## Operating Context

Scrutiny committee hearings, institutional appeal workflows, and online regulatory validation sessions where evidence must be verifiable, auditable, and immutable.

## Capabilities and Constraints

- Client-side deterministic execution guaranteeing complete functionality on static GitHub Pages as well as live Spring Boot backend integration.
- Full 6-document synthetic test dataset preloaded for immediate validation across Fire NOC, Land Deeds, SPPU Affiliation, Faculty PAN Rosters, Building Plans, and TER Fee Receipts.
- Interactive modal dialogs outside dynamic tabs with persistent DOM integrity (`cvEvidenceModal`, `forensicDiffModal`, `reportModal`).

## Brand Commitments

Authoritative, transparent, immutable. Every visual element conveys institutional gravitas, regulatory compliance, and tamper evidence.
