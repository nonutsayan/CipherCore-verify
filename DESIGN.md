---
name: CipherCore Regulatory Design System
description: High-contrast regulatory design system for AICTE Decision-Support Portal. Strictly 4px/6px rectangular geometry, solid blue accents, neutral surfaces.
colors:
  # Base Surfaces
  bg-page: "#f8fafc"
  bg-card: "#ffffff"
  bg-subtle: "#f1f5f9"
  bg-dark: "#0f172a"
  bg-dark-card: "#1e293b"
  backdrop-overlay: "rgba(0, 0, 0, 0.75)"
  backdrop-dark: "rgba(0, 0, 0, 0.88)"

  # Borders & Separators
  border-color: "#e2e8f0"
  border-dark: "#334155"

  # Regulatory Primary Accent
  primary: "#0284c7"
  primary-hover: "#0369a1"
  primary-light: "#e0f2fe"
  primary-border: "#bae6fd"

  # Status Indicators
  status-valid: "#16a34a"
  status-valid-bg: "#dcfce7"
  status-valid-border: "#86efac"
  status-warning: "#d97706"
  status-warning-bg: "#fef3c7"
  status-warning-border: "#fde68a"
  status-danger: "#dc2626"
  status-danger-bg: "#fee2e2"
  status-danger-border: "#fca5a5"
  tamper-red-subtle: "rgba(185, 28, 28, 0.2)"
  tamper-border-subtle: "rgba(248, 113, 113, 0.4)"

  # Text Content
  text-main: "#0f172a"
  text-sub: "#475569"
  text-muted: "#64748b"
  text-light: "#f8fafc"
  text-dark-sub: "#94a3b8"

rounded:
  none: "0px"
  sm: "4px"
  base: "5px"
  md: "6px"

typography:
  font-sans: "Inter, -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Helvetica, Arial, sans-serif"
  font-mono: "ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, Liberation Mono, monospace"
  scale:
    xxs: "10px"
    xs: "11px"
    sm: "12px"
    base: "13px"
    md: "14px"
    nav: "15px"
    lg: "16px"
    xl: "18px"
    2xl: "20px"
    3xl: "24px"
---

## Overview

CipherCore Verify's design system adheres strictly to the official AICTE SIH 2026 Problem Statement 26261 regulatory guidelines. It prioritizes data integrity, clarity, high contrast, and ergonomic scanability for technical scrutiny officers.

## Colors

- **Page Ground**: `#f8fafc` (light mode), `#0f172a` (dark mode).
- **Cards & Surfaces**: `#ffffff` with `1px solid #e2e8f0` border.
- **Regulatory Accent**: Solid `#0284c7` (Sky-600) with hover state `#0369a1` (Sky-700).
- **Status Codes**: `#16a34a` (Verified/Pass), `#d97706` (Deficiency/Warning), `#dc2626` (Altered/Tampered/Critical).
- **Strict Prohibition**: No purple/violet gradients, no glowing colored halos, no dark-mode neon cyan washes.

## Typography

- **Primary Typeface**: Inter / System Sans-Serif (`-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto`).
- **Code & Cryptographic Hashes**: System Monospace (`ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas`).
- **Minimum Functional Size**: 11px for metadata tags, badges, and micro-labels (10px legal smallprint); 13px/14px for body text and table cells.

## Layout

- **Header**: Fixed 64px non-wrapping horizontal navigation with 3 strict zones (Brand, Search & Filters, Action Controls).
- **Grid Architecture**: 1440px maximum container width, centered layout with 16px/24px gutters.
- **Modal Viewports**: Responsive dialogs constrained to 90vh maximum height with internal scrollable content drawers.

## Elevation & Depth

- **Subtle Elevation**: Flat surfaces with 1px border definition; card shadows restricted to `0 1px 3px rgba(0,0,0,0.05)`.
- **Modals**: High z-index overlay (`z-index: 999999`) with dark backdrop `rgba(0, 0, 0, 0.75)` and crisp card boundaries.

## Shapes

- **Border Radius**: Strictly 4px (`rounded-[4px]`) or 6px (`rounded-[6px]`), with 5px for intermediate container panels.
- **Zero Pill Shapes**: All buttons, badges, and status chips use rectangular or rounded-rectangle corners.

## Components

- **Action Buttons**: Rectangular corners (4px radius), 1px solid border on secondary buttons, solid blue fill on primary buttons.
- **Evidence Modals**: Two-column split drawers (`cvEvidenceModal`, `forensicDiffModal`, `reportModal`).
- **Interactive Canvases**: Hardware-accelerated HTML5 canvas rendering YOLOv8 bounding boxes and forensic diffs.

## Do's and Don'ts

- **DO**: Use inline SVG icons with explicit `stroke-width="2"` and crisp viewboxes.
- **DO**: Maintain high contrast text ratios exceeding WCAG 2.1 AA standards.
- **DON'T**: Use emojis in UI elements or headings.
- **DON'T**: Use em dashes (`—`) or auto-scrolling marquee widgets.
- **DON'T**: Use CSS pill-shaped radiuses (`rounded-full` on buttons).
