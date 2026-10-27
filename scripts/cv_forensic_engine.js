// =========================================================================
// CIPHERCORE VERIFY - EXECUTIVE GOVTECH CV & FORENSIC DIFF ENGINE
// AICTE SIH 2026 Problem Statement 26261
// =========================================================================

(function() {
  // Global modal state
  window.cvLabImage = null;
  window.cvLabImageLoaded = false;
  window.cvBoundingBoxesVisible = true;
  window.cvHeatmapVisible = false;
  window.cvGeofenceVisible = true;
  window.cvDetectionFilter = 'all';
  window.forensicDiffActiveView = 'split'; // 'split', 'ela', 'hash'

  // Preload Lab Photo
  window.initLabImage = function() {
    if (typeof Image === "undefined" || window.cvLabImage) return;
    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = function() {
      window.cvLabImageLoaded = true;
      if (typeof window.drawCvCanvas === 'function') {
        window.drawCvCanvas();
      }
    };
    img.onerror = function() {
      console.warn('Relative lab_facility_photo.jpg failed to load, falling back to embedded B64.');
      if (window.LAB_FACILITY_PHOTO_B64 && img.src !== window.LAB_FACILITY_PHOTO_B64) {
        img.src = window.LAB_FACILITY_PHOTO_B64;
      }
    };
    img.src = 'lab_facility_photo.jpg';
    window.cvLabImage = img;
  };

  // Immediate Modal Controllers
  window.openCvEvidenceModal = function(appId, facilityType) {
    const m = document.getElementById('cvEvidenceModal') || document.getElementById('cvInspectionModal');
    if (m) {
      m.style.setProperty('display', 'flex', 'important');
      m.classList.add('active');
      m.classList.remove('hidden');
      document.body.style.overflow = 'hidden';
      window.initLabImage();
      setTimeout(window.drawCvCanvas, 20);
      setTimeout(window.drawCvCanvas, 100);
      setTimeout(window.drawCvCanvas, 300);
    } else {
      console.error('cvEvidenceModal element not found in DOM');
    }
  };

  window.closeCvEvidenceModal = function() {
    const m = document.getElementById('cvEvidenceModal') || document.getElementById('cvInspectionModal');
    if (m) {
      m.style.setProperty('display', 'none', 'important');
      m.classList.remove('active');
      m.classList.add('hidden');
      document.body.style.overflow = '';
    }
  };

  window.openForensicDiffModal = function(appId, docType) {
    const m = document.getElementById('forensicDiffModal');
    if (m) {
      m.style.setProperty('display', 'flex', 'important');
      m.classList.add('active');
      m.classList.remove('hidden');
      document.body.style.overflow = 'hidden';
      window.setForensicDiffViewMode('split');
    } else {
      console.error('forensicDiffModal element not found in DOM');
    }
  };

  window.closeForensicDiffModal = function() {
    const m = document.getElementById('forensicDiffModal');
    if (m) {
      m.style.setProperty('display', 'none', 'important');
      m.classList.remove('active');
      m.classList.add('hidden');
      document.body.style.overflow = '';
    }
  };

  window.openReportModal = function(appId) {
    const m = document.getElementById('reportModal') || document.getElementById('aicteScrutinyReportModal');
    if (m) {
      m.style.setProperty('display', 'flex', 'important');
      m.classList.add('active');
      m.classList.remove('hidden');
      document.body.style.overflow = 'hidden';
    }
  };

  window.closeReportModal = function() {
    const m = document.getElementById('reportModal') || document.getElementById('aicteScrutinyReportModal');
    if (m) {
      m.style.setProperty('display', 'none', 'important');
      m.classList.remove('active');
      m.classList.add('hidden');
      document.body.style.overflow = '';
    }
  };

  // Aliases
  window.openCvInspectionModal = window.openCvEvidenceModal;
  window.closeCvInspectionModal = window.closeCvEvidenceModal;
  window.openAicteScrutinyReportModal = window.openReportModal;
  window.closeAicteScrutinyReportModal = window.closeReportModal;

  // CV Toggle Controllers
  window.toggleCvBoundingBoxes = function(val) {
    window.cvBoundingBoxesVisible = (typeof val === 'boolean') ? val : !window.cvBoundingBoxesVisible;
    const chk = document.getElementById('cvBoxesCheckbox');
    if (chk) chk.checked = window.cvBoundingBoxesVisible;
    window.drawCvCanvas();
  };

  window.toggleCvHeatmap = function(val) {
    window.cvHeatmapVisible = (typeof val === 'boolean') ? val : !window.cvHeatmapVisible;
    const chk = document.getElementById('cvHeatmapCheckbox');
    if (chk) chk.checked = window.cvHeatmapVisible;
    window.drawCvCanvas();
  };

  window.toggleCvGeofence = function(val) {
    window.cvGeofenceVisible = (typeof val === 'boolean') ? val : !window.cvGeofenceVisible;
    const chk = document.getElementById('cvGeofenceCheckbox');
    if (chk) chk.checked = window.cvGeofenceVisible;
    window.drawCvCanvas();
  };

  window.setCvFilter = function(filterVal) {
    window.cvDetectionFilter = filterVal;
    window.drawCvCanvas();
  };

  // FORENSIC DIFF VIEW MODE CONTROLLER
  window.setForensicDiffViewMode = function(mode) {
    window.forensicDiffActiveView = mode;
    const btnSplit = document.getElementById('forensicModeBtnSplit');
    const btnEla = document.getElementById('forensicModeBtnEla');
    const btnHash = document.getElementById('forensicModeBtnHash');

    const viewSplit = document.getElementById('forensicViewSplit');
    const viewEla = document.getElementById('forensicViewEla');
    const viewHash = document.getElementById('forensicViewHash');

    const activeClasses = ['bg-[#0284c7]', 'text-white'];
    const inactiveClasses = ['bg-slate-100', 'dark:bg-slate-800', 'text-slate-700', 'dark:text-slate-300'];

    [btnSplit, btnEla, btnHash].forEach(b => {
      if (!b) return;
      b.classList.remove(...activeClasses);
      b.classList.add(...inactiveClasses);
    });

    if (viewSplit) viewSplit.style.display = 'none';
    if (viewEla) viewEla.style.display = 'none';
    if (viewHash) viewHash.style.display = 'none';

    if (mode === 'split') {
      if (btnSplit) {
        btnSplit.classList.add(...activeClasses);
        btnSplit.classList.remove(...inactiveClasses);
      }
      if (viewSplit) viewSplit.style.display = 'grid';
    } else if (mode === 'ela') {
      if (btnEla) {
        btnEla.classList.add(...activeClasses);
        btnEla.classList.remove(...inactiveClasses);
      }
      if (viewEla) {
        viewEla.style.display = 'block';
        window.drawElaCanvas();
      }
    } else if (mode === 'hash') {
      if (btnHash) {
        btnHash.classList.add(...activeClasses);
        btnHash.classList.remove(...inactiveClasses);
      }
      if (viewHash) viewHash.style.display = 'block';
    }
  };

  // ELA HEATMAP CANVAS RENDERER
  window.drawElaCanvas = function() {
    const canvas = document.getElementById('elaViewerCanvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const w = canvas.width;
    const h = canvas.height;

    // Dark base for ELA
    ctx.fillStyle = '#060a12';
    ctx.fillRect(0, 0, w, h);

    // Document outline
    ctx.fillStyle = '#0e1726';
    ctx.fillRect(20, 20, w - 40, h - 40);
    ctx.strokeStyle = '#1e293b';
    ctx.lineWidth = 1;
    ctx.strokeRect(20, 20, w - 40, h - 40);

    // Subtle background noise for uniform scanned text
    ctx.fillStyle = 'rgba(148, 163, 184, 0.08)';
    for (let y = 50; y < h - 60; y += 14) {
      const lineLen = (y % 28 === 0) ? (w - 180) : (w - 100);
      ctx.fillRect(40, y, lineLen, 4);
    }

    // High Error Level Analysis (ELA) thermal anomaly around the doctored region
    // Anomaly region: Survey 42/B - 5.20 Acres
    const ax = 120, ay = 130, aw = 480, ah = 70;

    // Outer glow
    const grad = ctx.createRadialGradient(ax + aw/2, ay + ah/2, 20, ax + aw/2, ay + ah/2, 260);
    grad.addColorStop(0, 'rgba(239, 68, 68, 0.65)');
    grad.addColorStop(0.35, 'rgba(245, 158, 11, 0.45)');
    grad.addColorStop(0.7, 'rgba(6, 182, 212, 0.15)');
    grad.addColorStop(1, 'rgba(6, 10, 18, 0)');
    ctx.fillStyle = grad;
    ctx.fillRect(ax - 60, ay - 40, aw + 120, ah + 80);

    // Intense high-frequency noise cluster in doctored box
    ctx.strokeStyle = '#ef4444';
    ctx.lineWidth = 2;
    if (ctx.setLineDash) ctx.setLineDash([4, 3]);
    ctx.strokeRect(ax, ay, aw, ah);
    if (ctx.setLineDash) ctx.setLineDash([]);

    // Heatmap grid overlay
    for (let px = ax; px < ax + aw; px += 8) {
      for (let py = ay; py < ay + ah; py += 6) {
        const r = Math.floor(200 + Math.random() * 55);
        const g = Math.floor(40 + Math.random() * 80);
        ctx.fillStyle = `rgba(${r}, ${g}, 30, ${0.4 + Math.random() * 0.45})`;
        ctx.fillRect(px, py, 6, 4);
      }
    }

    // Label on ELA anomaly
    ctx.fillStyle = '#ef4444';
    ctx.fillRect(ax, ay - 24, 280, 20);
    ctx.fillStyle = '#ffffff';
    ctx.font = 'bold 11px monospace';
    ctx.fillText('ELA ANOMALY: 88.4% RESAVE DELTA', ax + 8, ay - 10);

    ctx.fillStyle = '#fca5a5';
    ctx.font = 'bold 12px monospace';
    ctx.fillText('Survey No: 42/B [5.20 Acres Claimed] - RASTER OVERLAY', ax + 14, ay + 38);

    // Bottom ELA metrics
    ctx.fillStyle = '#94a3b8';
    ctx.font = '10px monospace';
    ctx.fillText('RGB Difference Laplacian Variance: 428.6 | Quantization Table: Photoshop 12.0 / Acrobat Pro', 30, h - 28);
  };

  // FULL PHOTOREALISTIC CV CANVAS RENDERER
  window.drawCvCanvas = function() {
    const canvas = document.getElementById('cvInspectionCanvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const w = canvas.width;
    const h = canvas.height;

    // 1. BASE LAYER: Render Real Lab Photograph
    if (window.cvLabImageLoaded && window.cvLabImage && window.cvLabImage.complete && window.cvLabImage.naturalWidth > 0) {
      ctx.drawImage(window.cvLabImage, 0, 0, w, h);
      // Subtle contrast grading for institutional CAD HUD
      ctx.fillStyle = 'rgba(15, 23, 42, 0.12)';
      ctx.fillRect(0, 0, w, h);
    } else {
      // Procedural photographic simulation if image is loading
      drawProceduralLabFallback(ctx, w, h);
    }

    // 2. CAD PERSPECTIVE GRID LINES
    ctx.strokeStyle = 'rgba(2, 132, 199, 0.18)';
    ctx.lineWidth = 0.5;
    for (let gy = 80; gy < h; gy += 40) {
      ctx.beginPath();
      ctx.moveTo(0, gy);
      ctx.lineTo(w, gy);
      ctx.stroke();
    }

    // 3. THERMAL DENSITY HEATMAP LAYER
    if (window.cvHeatmapVisible) {
      const heatClusters = [
        { x: 140, y: 380, r: 70, weight: 0.6 },
        { x: 300, y: 380, r: 75, weight: 0.55 },
        { x: 480, y: 380, r: 70, weight: 0.65 },
        { x: 660, y: 380, r: 65, weight: 0.5 },
        { x: 220, y: 280, r: 55, weight: 0.45 },
        { x: 380, y: 280, r: 60, weight: 0.5 },
        { x: 540, y: 280, r: 55, weight: 0.48 },
        { x: 280, y: 200, r: 45, weight: 0.4 },
        { x: 440, y: 200, r: 48, weight: 0.42 }
      ];

      heatClusters.forEach(hc => {
        const grad = ctx.createRadialGradient(hc.x, hc.y, 4, hc.x, hc.y, hc.r);
        grad.addColorStop(0, `rgba(16, 185, 129, ${hc.weight})`);
        grad.addColorStop(0.4, `rgba(6, 182, 212, ${hc.weight * 0.7})`);
        grad.addColorStop(0.8, `rgba(245, 158, 11, ${hc.weight * 0.3})`);
        grad.addColorStop(1, 'rgba(15, 23, 42, 0)');
        ctx.fillStyle = grad;
        ctx.beginPath();
        ctx.arc(hc.x, hc.y, hc.r, 0, Math.PI * 2);
        ctx.fill();
      });
    }

    // 4. CAMPUS GEOFENCE OVERLAY
    if (window.cvGeofenceVisible) {
      ctx.strokeStyle = '#0284c7';
      ctx.lineWidth = 1.8;
      if (ctx.setLineDash) ctx.setLineDash([6, 4]);
      ctx.beginPath();
      ctx.moveTo(35, 110);
      ctx.lineTo(w - 35, 110);
      ctx.lineTo(w - 15, h - 25);
      ctx.lineTo(15, h - 25);
      ctx.closePath();
      ctx.stroke();
      if (ctx.setLineDash) ctx.setLineDash([]);

      ctx.fillStyle = 'rgba(2, 132, 199, 0.05)';
      ctx.fill();

      // Geofence Coordinate Tags
      ctx.fillStyle = '#0284c7';
      ctx.fillRect(18, h - 42, 340, 18);
      ctx.fillStyle = '#ffffff';
      ctx.font = 'bold 9.5px monospace';
      ctx.fillText('GEOFENCE LOCKED: 18.5204 N, 73.8567 E | ACCURACY: ±0.8m', 24, h - 30);
    }

    // 5. YOLOv8 OBJECT DETECTION BOUNDING BOXES
    if (window.cvBoundingBoxesVisible) {
      const showTerminals = window.cvDetectionFilter === 'all' || window.cvDetectionFilter === 'terminals';
      const showVacant = window.cvDetectionFilter === 'all' || window.cvDetectionFilter === 'vacant';
      const showSafety = window.cvDetectionFilter === 'all' || window.cvDetectionFilter === 'safety';

      // --- ROW 1 (Foreground Desk Terminals) ---
      const r1Terminals = [
        { x: 50, y: 320, w: 90, h: 85, conf: 0.97, id: 'T-01' },
        { x: 165, y: 320, w: 90, h: 85, conf: 0.95, id: 'T-02' },
        { x: 280, y: 320, w: 90, h: 85, conf: 0.96, id: 'T-03' },
        { x: 395, y: 320, w: 90, h: 85, conf: 0.94, id: 'T-04' },
        { x: 510, y: 320, w: 90, h: 85, conf: 0.98, id: 'T-05' },
        { x: 625, y: 320, w: 90, h: 85, conf: 0.96, id: 'T-06' }
      ];

      // --- ROW 2 (Mid-front Desk Terminals) ---
      const r2Terminals = [
        { x: 75, y: 240, w: 75, h: 68, conf: 0.95, id: 'T-07' },
        { x: 170, y: 240, w: 75, h: 68, conf: 0.94, id: 'T-08' },
        { x: 265, y: 240, w: 75, h: 68, conf: 0.92, id: 'T-09', vacant: true }, // Vacant Shortfall
        { x: 360, y: 240, w: 75, h: 68, conf: 0.96, id: 'T-10' },
        { x: 455, y: 240, w: 75, h: 68, conf: 0.91, id: 'T-11', vacant: true }, // Vacant Shortfall
        { x: 550, y: 240, w: 75, h: 68, conf: 0.95, id: 'T-12' },
        { x: 645, y: 240, w: 75, h: 68, conf: 0.94, id: 'T-13' }
      ];

      // --- ROW 3 (Mid-back Desk Terminals) ---
      const r3Terminals = [
        { x: 95, y: 175, w: 62, h: 54, conf: 0.93, id: 'T-14' },
        { x: 172, y: 175, w: 62, h: 54, conf: 0.95, id: 'T-15' },
        { x: 250, y: 175, w: 62, h: 54, conf: 0.92, id: 'T-16' },
        { x: 328, y: 175, w: 62, h: 54, conf: 0.89, id: 'T-17', vacant: true }, // Vacant Shortfall
        { x: 405, y: 175, w: 62, h: 54, conf: 0.94, id: 'T-18' },
        { x: 482, y: 175, w: 62, h: 54, conf: 0.96, id: 'T-19' },
        { x: 560, y: 175, w: 62, h: 54, conf: 0.93, id: 'T-20' },
        { x: 638, y: 175, w: 62, h: 54, conf: 0.95, id: 'T-21' }
      ];

      // --- ROW 4 (Background Near Whiteboard) ---
      const r4Terminals = [
        { x: 120, y: 128, w: 50, h: 42, conf: 0.91, id: 'T-22' },
        { x: 185, y: 128, w: 50, h: 42, conf: 0.93, id: 'T-23' },
        { x: 250, y: 128, w: 50, h: 42, conf: 0.94, id: 'T-24' },
        { x: 315, y: 128, w: 50, h: 42, conf: 0.92, id: 'T-25' },
        { x: 380, y: 128, w: 50, h: 42, conf: 0.88, id: 'T-26', vacant: true }, // Vacant Shortfall
        { x: 445, y: 128, w: 50, h: 42, conf: 0.95, id: 'T-27' },
        { x: 510, y: 128, w: 50, h: 42, conf: 0.93, id: 'T-28' },
        { x: 575, y: 128, w: 50, h: 42, conf: 0.94, id: 'T-29' }
      ];

      const allBays = [...r1Terminals, ...r2Terminals, ...r3Terminals, ...r4Terminals];

      allBays.forEach(box => {
        if (!box.vacant && showTerminals) {
          // Green Active Terminal
          ctx.strokeStyle = '#10b981';
          ctx.lineWidth = 1.8;
          if (ctx.setLineDash) ctx.setLineDash([]);
          ctx.strokeRect(box.x, box.y, box.w, box.h);

          ctx.fillStyle = '#10b981';
          ctx.fillRect(box.x, box.y - 15, 68, 14);
          ctx.fillStyle = '#ffffff';
          ctx.font = 'bold 8.5px monospace';
          ctx.fillText(`terminal: ${box.conf}`, box.x + 3, box.y - 4);
        } else if (box.vacant && showVacant) {
          // Red Dashed Vacant Bay Shortfall
          ctx.strokeStyle = '#ef4444';
          ctx.lineWidth = 2;
          if (ctx.setLineDash) ctx.setLineDash([4, 3]);
          ctx.strokeRect(box.x, box.y, box.w, box.h);
          if (ctx.setLineDash) ctx.setLineDash([]);

          ctx.fillStyle = '#ef4444';
          ctx.fillRect(box.x, box.y - 17, 88, 15);
          ctx.fillStyle = '#ffffff';
          ctx.font = 'bold 8.5px monospace';
          ctx.fillText(`unoccupied: ${box.conf}`, box.x + 3, box.y - 5);
        }
      });

      // --- LIFE SAFETY & FACILITY OBJECTS ---
      if (showSafety) {
        // Fire Extinguisher 1 (Right Wall)
        ctx.strokeStyle = '#0284c7';
        ctx.lineWidth = 2;
        if (ctx.setLineDash) ctx.setLineDash([]);
        ctx.strokeRect(w - 48, 160, 26, 55);
        ctx.fillStyle = '#0284c7';
        ctx.fillRect(w - 48, 145, 120, 14);
        ctx.fillStyle = '#ffffff';
        ctx.font = 'bold 8.5px monospace';
        ctx.fillText('fire_extinguisher: 0.99', w - 46, 155);

        // Fire Extinguisher 2 (Left Front Entrance)
        ctx.strokeStyle = '#0284c7';
        ctx.lineWidth = 2;
        ctx.strokeRect(22, 195, 24, 50);
        ctx.fillStyle = '#0284c7';
        ctx.fillRect(22, 180, 115, 14);
        ctx.fillStyle = '#ffffff';
        ctx.font = 'bold 8.5px monospace';
        ctx.fillText('fire_extinguisher: 0.98', 24, 190);

        // Emergency Exit Luminaire (Upper Left Wall)
        ctx.strokeStyle = '#f59e0b';
        ctx.lineWidth = 2;
        ctx.strokeRect(28, 48, 56, 22);
        ctx.fillStyle = '#f59e0b';
        ctx.fillRect(28, 33, 130, 14);
        ctx.fillStyle = '#000000';
        ctx.font = 'bold 8.5px monospace';
        ctx.fillText('emergency_exit: 0.92 [DEFICIT]', 30, 43);

        // 42U Server Rack (Rear Corner)
        ctx.strokeStyle = '#059669';
        ctx.lineWidth = 1.8;
        ctx.strokeRect(w - 110, 115, 48, 90);
        ctx.fillStyle = '#059669';
        ctx.fillRect(w - 110, 100, 105, 14);
        ctx.fillStyle = '#ffffff';
        ctx.font = 'bold 8.5px monospace';
        ctx.fillText('server_rack: 0.97 (42U)', w - 108, 110);
      }
    }

    // 6. TOP HUD STATUS STRIP
    ctx.fillStyle = 'rgba(15, 23, 42, 0.85)';
    ctx.fillRect(0, 0, w, 28);
    ctx.strokeStyle = '#334155';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(0, 28);
    ctx.lineTo(w, 28);
    ctx.stroke();

    ctx.fillStyle = '#10b981';
    ctx.beginPath();
    ctx.arc(14, 14, 4, 0, Math.PI * 2);
    ctx.fill();

    ctx.fillStyle = '#e2e8f0';
    ctx.font = 'bold 10px monospace';
    ctx.fillText('YOLOv8x-SIH26261 | APEX IT LAB-03 (COMPUTING FACILITY)', 26, 17);

    ctx.fillStyle = '#ef4444';
    ctx.font = 'bold 10px monospace';
    ctx.fillText('DEFICIT: -8 TERMINALS (142 DETECTED / 150 REQUIRED)', w - 350, 17);
  };

  // Procedural Fallback if image network load delayed
  function drawProceduralLabFallback(ctx, w, h) {
    ctx.fillStyle = '#090d16';
    ctx.fillRect(0, 0, w, h);

    // Floor tiles
    ctx.strokeStyle = '#1e293b';
    ctx.lineWidth = 1;
    for (let y = 140; y < h; y += 32) {
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(w, y);
      ctx.stroke();
    }
    for (let x = 0; x < w; x += 48) {
      ctx.beginPath();
      ctx.moveTo(x, 140);
      ctx.lineTo(x * 1.15 - 50, h);
      ctx.stroke();
    }

    // Walls & Board
    ctx.fillStyle = '#1e293b';
    ctx.fillRect(0, 0, w, 140);
    ctx.fillStyle = '#f8fafc';
    ctx.fillRect(180, 20, 400, 90);
    ctx.strokeStyle = '#64748b';
    ctx.lineWidth = 2;
    ctx.strokeRect(180, 20, 400, 90);

    ctx.fillStyle = '#0284c7';
    ctx.font = 'bold 12px monospace';
    ctx.fillText('AICTE CCF LAB-03 | 180 INTAKE B.TECH CSE', 205, 55);
    ctx.fillStyle = '#64748b';
    ctx.font = '10px monospace';
    ctx.fillText('REQUIRED TERMINALS: 150 | DETECTED UNITS: 142', 205, 75);
  }

  // Blockchain Notarization Handlers
  window.attachCvFindingToDossier = function() {
    if (window.cipherBlockchain && typeof window.cipherBlockchain.addBlock === 'function') {
      window.cipherBlockchain.addBlock('APP-2026-101', 'CV_AUDIT_NOTARIZED', window.activeUserSession?.name || 'Scrutiny Officer', {
        facility: 'Apex IT - Lab 03 (Computing Facility)',
        detectedTerminals: 142,
        requiredNorm: 150,
        deficiency: '8 terminals deficit (-5.3%) under APH 2024-27 Clause 4.1',
        geofenceCoordinates: '18.5204 N, 73.8567 E',
        notarizedAt: new Date().toISOString()
      });
      if (typeof window.renderBlockchainLedgerUI === 'function') {
        window.renderBlockchainLedgerUI();
      }
    }
    if (typeof window.showToastBanner === 'function') {
      window.showToastBanner('Attached CV finding to official scrutiny dossier & notarized on blockchain.');
    } else {
      alert('Attached CV finding to official scrutiny dossier & notarized on blockchain.');
    }
    window.closeCvEvidenceModal();
  };

  window.attachForensicProofToDossier = function() {
    if (window.cipherBlockchain && typeof window.cipherBlockchain.addBlock === 'function') {
      window.cipherBlockchain.addBlock('APP-2026-101', 'FORENSIC_TAMPER_FLAGGED', window.activeUserSession?.name || 'Scrutiny Officer', {
        document: 'SYNTH_LAND_DEED_PUNE_ALTERED.pdf',
        parameter: 'Survey No: 42/B Claimed 5.20 Acres vs DigiLocker Actual 2.10 Acres',
        elaScore: '88.4% Tamper Overlay Detected',
        fontMismatch: 'Helvetica-Bold Raster Layer Overlaid on Scanned Courier',
        notarizedAt: new Date().toISOString()
      });
      if (typeof window.renderBlockchainLedgerUI === 'function') {
        window.renderBlockchainLedgerUI();
      }
    }
    if (typeof window.showToastBanner === 'function') {
      window.showToastBanner('Attached Forensic Land Deed proof to Official Dossier & notarized on blockchain.');
    } else {
      alert('Attached Forensic Land Deed proof to Official Dossier & notarized on blockchain.');
    }
    window.closeForensicDiffModal();
  };

  // Global Click Event Interceptor - ensures ANY button across the whole document opens the modal
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

  // Global Escape Key Listener
  document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
      window.closeCvEvidenceModal();
      window.closeForensicDiffModal();
      window.closeReportModal();
    }
  });

  // Pre-initialize lab image as soon as DOM is ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', window.initLabImage);
  } else {
    window.initLabImage();
  }
})();
