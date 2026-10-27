const fs = require('fs');

// We will test the exact restoration script
const testScript = `
  var DOCS = [
    'Fire Safety Certificate (NOC)',
    'Land Ownership Deed',
    'University Affiliation Letter',
    'Faculty Roster & PAN List',
    'Approved Building Plan',
    'Fee Payment Receipt'
  ];
  var FLAG = 3;
  var W = 1.25, H = 1.65;
  var seed = 5;
  function rnd(){
    seed = (seed * 16807) % 2147483647;
    return (seed - 1) / 2147483646;
  }
  var hashes = [
    'e3b0c44298fc1c',
    '9b8769a4a74295',
    '4a2d81f0872658',
    '8c43aa2cfd991b',
    '81fa023e981bc8',
    '12a4b8890cb1e2'
  ];

  var DOC_SPECS = [
    {
      code: 'EoA APH 2.1 DOC 01',
      title: 'Fire Safety Certificate (NOC)',
      normLabel: 'STATUTORY NORM',
      normValue: 'Valid clearance from Competent State Fire Authority covering AY 2026-27.',
      gwLabel: 'GATEWAY VERIFICATION',
      gwValue: 'State Fire Directorate Portal API (HTTP 200 OK | Active).',
      checkLabel: 'CHECKS PERFORMED',
      checkValue: 'Class-3 DSC digital stamp verified, multi-storey egress norms met.',
      statusTitle: '● VERIFIED',
      statusSub: 'Compliant with NBC Part IV Norms',
      statusType: 'ok',
      footerBadge: '[Verified]'
    },
    {
      code: 'EoA APH 2.1 DOC 02',
      title: 'Land Ownership Deed',
      normLabel: 'STATUTORY NORM',
      normValue: 'Registered sale deed / 30-year uninterrupted contiguous land lease.',
      gwLabel: 'GATEWAY VERIFICATION',
      gwValue: 'DigiLocker State Land Registry (RoR / 7/12 CTS Property Card).',
      checkLabel: 'FORENSIC ALERT',
      checkValue: 'ELA Score 88.4%: Survey 42/B digital raster overlay detected over 42/A scan.',
      statusTitle: '■ ANOMALY FLAGGED',
      statusSub: 'Acreage Mismatch 2.10 vs 5.20 Acres',
      statusType: 'flag',
      footerBadge: '[Flagged]'
    },
    {
      code: 'EoA APH 2.1 DOC 03',
      title: 'University Affiliation Letter',
      normLabel: 'STATUTORY NORM',
      normValue: 'Valid affiliation issued by affiliating State/Central University for all branches.',
      gwLabel: 'GATEWAY VERIFICATION',
      gwValue: 'University Affiliation Registry Database (SPPU Portal).',
      checkLabel: 'CHECKS PERFORMED',
      checkValue: 'Cross-reconciliation of sanctioned course intake against AICTE quota.',
      statusTitle: '▲ DISCREPANCY',
      statusSub: '120 Affiliated vs 180 Claimed Intake',
      statusType: 'warn',
      footerBadge: '[Mismatch]'
    },
    {
      code: 'EoA APH 2.1 DOC 04',
      title: 'Faculty Roster & PAN List',
      normLabel: 'STATUTORY NORM',
      normValue: 'Minimum 1:20 Faculty-to-Student Ratio (FSR) with 1:2:6 cadre distribution.',
      gwLabel: 'GATEWAY VERIFICATION',
      gwValue: 'Central Income Tax PAN & AICTE National Faculty Database.',
      checkLabel: 'CHECKS PERFORMED',
      checkValue: 'Automated collision detection against nationwide technical faculty rosters.',
      statusTitle: '■ FLAGGED',
      statusSub: '2 Duplicate PANs at Neighbor Institute',
      statusType: 'flag',
      footerBadge: '[Flagged]'
    },
    {
      code: 'EoA APH 2.1 DOC 05',
      title: 'Approved Building Plan',
      normLabel: 'STATUTORY NORM',
      normValue: 'Sanctioned municipal master plan with built-up carpet area breakdown.',
      gwLabel: 'VISION AUDIT',
      gwValue: 'YOLOv8 Computer Vision scan of Central Computing Lab-03.',
      checkLabel: 'CHECKS PERFORMED',
      checkValue: '142 terminals detected (150 required); ramp gradient 1:10 (1:12 norm).',
      statusTitle: '▲ DEFICIENCY',
      statusSub: '-8 Terminal Deficit & Ramp Violation',
      statusType: 'warn',
      footerBadge: '[Deficient]'
    },
    {
      code: 'EoA APH 2.1 DOC 06',
      title: 'Fee Payment Receipt (TER)',
      normLabel: 'STATUTORY NORM',
      normValue: 'Proof of statutory Technical Education Regulatory (TER) fee payment.',
      gwLabel: 'GATEWAY VERIFICATION',
      gwValue: 'Bharatkosh Central Treasury / PFMS Payment Reconciliation.',
      checkLabel: 'CHECKS PERFORMED',
      checkValue: 'Challan UTR AICTE2026998124 settled; institutional PAN matched.',
      statusTitle: '● VERIFIED',
      statusSub: 'Treasury Settlement Confirmed',
      statusType: 'ok',
      footerBadge: '[Verified]'
    }
  ];

  function wrapText(ctx, text, x, y, maxWidth, lineHeight, maxLines) {
    var words = text.split(' ');
    var line = '';
    var lines = [];
    for (var n = 0; n < words.length; n++) {
      var testLine = line + (line ? ' ' : '') + words[n];
      var metrics = ctx.measureText(testLine);
      if (metrics.width > maxWidth && n > 0) {
        lines.push(line);
        line = words[n];
      } else {
        line = testLine;
      }
    }
    lines.push(line);
    if (maxLines && lines.length > maxLines) {
      lines = lines.slice(0, maxLines);
      if (lines[maxLines - 1].length > 3) {
        var last = lines[maxLines - 1];
        while (ctx.measureText(last + '...').width > maxWidth && last.length > 0) {
          last = last.slice(0, -1);
        }
        lines[maxLines - 1] = last + '...';
      }
    }
    for (var k = 0; k < lines.length; k++) {
      ctx.fillText(lines[k], x, y + (k * lineHeight));
    }
    return y + (lines.length * lineHeight);
  }

  function paint(cv, title, idx){
    var c = cv.getContext('2d'), w = cv.width, h = cv.height;
    var doc = DOC_SPECS[idx] || DOC_SPECS[0];

    // Card background
    c.fillStyle = '#F4F6FA';
    c.fillRect(0, 0, w, h);

    // Dark Header (exact original geometry)
    c.fillStyle = '#0E1622';
    c.fillRect(0, 0, w, 76);
    c.fillStyle = '#9FB2C8';
    c.font = '500 11px "JetBrains Mono", Menlo, monospace';
    c.fillText('EoA  APH 2.1  DOC 0' + (idx + 1), 18, 22);

    c.fillStyle = '#FFFFFF';
    c.font = '700 18px "Familjen Grotesk", "Public Sans", Arial, sans-serif';
    var words = title.split(' '), line = '', ly = 46, lines = [];
    words.forEach(function(wd){
      var t = line ? line + ' ' + wd : wd;
      if(c.measureText(t).width > w - 36 && line){
        lines.push(line);
        line = wd;
      } else {
        line = t;
      }
    });
    lines.push(line);
    lines.slice(0, 2).forEach(function(l, k){ c.fillText(l, 18, ly + k * 20); });

    // Body Info (replaces dummy horizontal gray bars)
    // 1. STATUTORY NORM
    c.fillStyle = '#64748B';
    c.font = '700 8.5px "JetBrains Mono", monospace';
    c.fillText(doc.normLabel, 18, 96);
    c.fillStyle = '#0F172A';
    c.font = '500 10px "Public Sans", sans-serif';
    wrapText(c, doc.normValue, 18, 108, w - 36, 13, 2);

    // 2. GATEWAY VERIFICATION / VISION AUDIT
    c.fillStyle = '#64748B';
    c.font = '700 8.5px "JetBrains Mono", monospace';
    c.fillText(doc.gwLabel, 18, 142);
    c.fillStyle = '#0F172A';
    c.font = '500 10px "Public Sans", sans-serif';
    wrapText(c, doc.gwValue, 18, 154, w - 36, 13, 2);

    // 3. CHECKS PERFORMED / FORENSIC ALERT
    c.fillStyle = (doc.checkLabel.indexOf('ALERT') !== -1 || doc.checkLabel.indexOf('FLAG') !== -1) ? '#DC2626' : '#64748B';
    c.font = '700 8.5px "JetBrains Mono", monospace';
    c.fillText(doc.checkLabel, 18, 188);
    c.fillStyle = '#0F172A';
    c.font = '500 10px "Public Sans", sans-serif';
    wrapText(c, doc.checkValue, 18, 200, w - 36, 13, 2);

    // 4. Metric / Status Badge (beside the seal)
    var badgeBg = (doc.statusType === 'flag' ? '#FEF2F2' : (doc.statusType === 'warn' ? '#FFFBEB' : '#ECFDF5'));
    var badgeBorder = (doc.statusType === 'flag' ? '#EF4444' : (doc.statusType === 'warn' ? '#F59E0B' : '#10B981'));
    var badgeText = (doc.statusType === 'flag' ? '#B91C1C' : (doc.statusType === 'warn' ? '#B45309' : '#047857'));

    c.fillStyle = badgeBg;
    c.fillRect(18, 252, 155, 46);
    c.strokeStyle = badgeBorder;
    c.lineWidth = 1.2;
    c.strokeRect(18, 252, 155, 46);

    c.fillStyle = badgeText;
    c.font = '700 9px "JetBrains Mono", monospace';
    c.fillText(doc.statusTitle, 24, 268);

    c.fillStyle = '#0F172A';
    c.font = '500 8px "Public Sans", sans-serif';
    wrapText(c, doc.statusSub, 24, 283, 143, 10, 1);

    // Exact original circular official seal stamp
    c.strokeStyle = '#4B6584';
    c.lineWidth = 2.2;
    c.beginPath();
    c.arc(w - 58, 284, 30, 0, Math.PI * 2);
    c.stroke();
    c.beginPath();
    c.arc(w - 58, 284, 23, 0, Math.PI * 2);
    c.stroke();
    c.fillStyle = '#4B6584';
    c.font = '700 9px "JetBrains Mono", monospace';
    c.textAlign = 'center';
    c.fillText('SEAL', w - 58, 288);
    c.textAlign = 'left';

    // Exact original footer divider line & sha256 hash
    c.fillStyle = '#CBD5E1';
    c.fillRect(18, h - 38, w - 36, 1.5);
    c.fillStyle = '#64748B';
    c.font = '400 10px "JetBrains Mono", Menlo, monospace';
    c.fillText('sha256 ' + hashes[idx] + '...', 18, h - 16);

    // Status indicator on right of footer
    c.fillStyle = badgeText;
    c.font = '700 10px "JetBrains Mono", monospace';
    c.textAlign = 'right';
    c.fillText(doc.footerBadge, w - 18, h - 16);
    c.textAlign = 'left';
  }
`;

console.log('Restoration script length:', testScript.length);
`;
