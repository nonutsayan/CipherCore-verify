import os
import re

targets = [
    'index.html',
    'landing.html',
    'frontend/index.html',
    'frontend/landing.html',
    'backend/src/main/resources/static/index.html',
    'backend/src/main/resources/static/landing.html',
    '../ciphercore-landing/index.html'
]

replacement_code = '''  var DOC_SPECS = [
    {
      code: 'EoA APH 2.1 DOC 01',
      title: 'Fire Safety Certificate (NOC)',
      hash: 'e3b0c44298fc1c',
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
      hash: '9b8769a4a74295',
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
      hash: '4a2d81f0872658',
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
      hash: '8c43aa2cfd991b',
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
      hash: '81fa023e981bc8',
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
      hash: '12a4b8890cb1e2',
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

  var hashes = DOC_SPECS.map(function(d){ return d.hash; });

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

  function roundRect(ctx, x, y, width, height, radius, fill, stroke) {
    ctx.beginPath();
    ctx.moveTo(x + radius, y);
    ctx.lineTo(x + width - radius, y);
    ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
    ctx.lineTo(x + width, y + height - radius);
    ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
    ctx.lineTo(x + radius, y + height);
    ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
    ctx.lineTo(x, y + radius);
    ctx.quadraticCurveTo(x, y, x + radius, y);
    ctx.closePath();
    if (fill) ctx.fill();
    if (stroke) ctx.stroke();
  }

  function paint(cv, title, idx){
    var c = cv.getContext('2d'), w = cv.width, h = cv.height;
    var s = w / 300;
    var doc = DOC_SPECS[idx] || {
      code: 'EoA APH 2.1 DOC 0' + (idx + 1),
      title: title || 'Scrutiny Document',
      hash: hashes[idx] || 'e3b0c44298fc1c',
      normLabel: 'STATUTORY NORM',
      normValue: 'Clearance verified against AICTE APH 2024-27 statutory requirements.',
      gwLabel: 'GATEWAY VERIFICATION',
      gwValue: 'Issuing Authority Verification API (HTTP 200 OK | Verified).',
      checkLabel: 'CHECKS PERFORMED',
      checkValue: 'Digital signature validation and norm compliance check executed.',
      statusTitle: '● VERIFIED',
      statusSub: 'Compliant with Prescribed Norms',
      statusType: 'ok',
      footerBadge: '[Verified]'
    };

    var isDark = false;
    try {
      isDark = (document.documentElement.getAttribute('data-theme') === 'dark') ||
               (!document.documentElement.getAttribute('data-theme') && window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches);
    } catch(e){}

    var bgCard = isDark ? '#1E293B' : '#FFFFFF';
    var borderCard = isDark ? '#334155' : '#E2E8F0';
    var bgHeader = isDark ? '#0B0F17' : '#0E1622';
    var headerCodeColor = '#38BDF8';
    var headerTitleColor = '#FFFFFF';
    var labelColor = isDark ? '#94A3B8' : '#64748B';
    var valueColor = isDark ? '#F1F5F9' : '#0F172A';
    var dividerColor = isDark ? '#334155' : '#E2E8F0';

    // 1. Base card surface & border
    c.fillStyle = bgCard;
    c.fillRect(0, 0, w, h);
    c.strokeStyle = borderCard;
    c.lineWidth = 1 * s;
    c.strokeRect(0, 0, w, h);

    // 2. Dark Header
    c.fillStyle = bgHeader;
    c.fillRect(0, 0, w, 70 * s);

    c.fillStyle = headerCodeColor;
    c.font = '700 ' + (9.5 * s) + 'px "JetBrains Mono", Menlo, monospace';
    c.fillText(doc.code, 16 * s, 20 * s);

    c.fillStyle = headerTitleColor;
    c.font = '700 ' + (13.5 * s) + 'px "Familjen Grotesk", sans-serif';
    c.fillText(doc.title, 16 * s, 42 * s);

    c.fillStyle = '#94A3B8';
    c.font = '600 ' + (7.5 * s) + 'px "JetBrains Mono", monospace';
    c.fillText('AICTE APPROVAL PROCESS HANDBOOK 2024-27', 16 * s, 58 * s);

    c.fillStyle = '#334155';
    c.fillRect(0, 70 * s, w, 1 * s);

    // 3. Three-tier regulatory body
    var contentWidth = w - 32 * s;

    // Tier 1: Statutory Norm
    c.fillStyle = labelColor;
    c.font = '700 ' + (8.5 * s) + 'px "JetBrains Mono", monospace';
    c.fillText(doc.normLabel, 16 * s, 85 * s);

    c.fillStyle = valueColor;
    c.font = '500 ' + (10 * s) + 'px "Public Sans", sans-serif';
    wrapText(c, doc.normValue, 16 * s, 97 * s, contentWidth, 13 * s, 2);

    c.fillStyle = dividerColor;
    c.fillRect(16 * s, 126 * s, contentWidth, 1 * s);

    // Tier 2: Gateway Verification / Vision Audit
    c.fillStyle = labelColor;
    c.font = '700 ' + (8.5 * s) + 'px "JetBrains Mono", monospace';
    c.fillText(doc.gwLabel, 16 * s, 139 * s);

    c.fillStyle = valueColor;
    c.font = '500 ' + (10 * s) + 'px "Public Sans", sans-serif';
    wrapText(c, doc.gwValue, 16 * s, 151 * s, contentWidth, 13 * s, 2);

    c.fillStyle = dividerColor;
    c.fillRect(16 * s, 180 * s, contentWidth, 1 * s);

    // Tier 3: Checks Performed / Forensic Alert
    c.fillStyle = (doc.checkLabel.indexOf('ALERT') !== -1 || doc.checkLabel.indexOf('FLAG') !== -1) ? '#DC2626' : labelColor;
    c.font = '700 ' + (8.5 * s) + 'px "JetBrains Mono", monospace';
    c.fillText(doc.checkLabel, 16 * s, 193 * s);

    c.fillStyle = valueColor;
    c.font = '500 ' + (10 * s) + 'px "Public Sans", sans-serif';
    wrapText(c, doc.checkValue, 16 * s, 205 * s, contentWidth, 13 * s, 2);

    c.fillStyle = dividerColor;
    c.fillRect(16 * s, 234 * s, contentWidth, 1 * s);

    // 4. Status Indicator Box & Circular Seal
    c.fillStyle = labelColor;
    c.font = '700 ' + (8.5 * s) + 'px "JetBrains Mono", monospace';
    c.fillText('SCRUTINY STATUS & REGULATORY SEAL', 16 * s, 247 * s);

    var badgeBg = isDark ? 'rgba(16, 185, 129, 0.15)' : '#ECFDF5';
    var badgeBorder = isDark ? '#059669' : '#10B981';
    var badgeTextColor = isDark ? '#34D399' : '#047857';

    if (doc.statusType === 'flag') {
      badgeBg = isDark ? 'rgba(239, 68, 68, 0.15)' : '#FEF2F2';
      badgeBorder = isDark ? '#DC2626' : '#EF4444';
      badgeTextColor = isDark ? '#F87171' : '#B91C1C';
    } else if (doc.statusType === 'warn') {
      badgeBg = isDark ? 'rgba(245, 158, 11, 0.15)' : '#FFFBEB';
      badgeBorder = isDark ? '#D97706' : '#F59E0B';
      badgeTextColor = isDark ? '#FBBF24' : '#B45309';
    }

    c.fillStyle = badgeBg;
    c.strokeStyle = badgeBorder;
    c.lineWidth = 1.2 * s;
    roundRect(c, 16 * s, 256 * s, 180 * s, 40 * s, 4 * s, true, true);

    c.fillStyle = badgeTextColor;
    c.font = '700 ' + (9 * s) + 'px "JetBrains Mono", monospace';
    c.fillText(doc.statusTitle, 24 * s, 271 * s);

    c.fillStyle = valueColor;
    c.font = '500 ' + (8.2 * s) + 'px "Public Sans", sans-serif';
    wrapText(c, doc.statusSub, 24 * s, 286 * s, 164 * s, 11 * s, 1);

    // Official Seal Stamp (clean, circular, on the bottom right)
    var sealColor = isDark ? '#38BDF8' : '#0284C7';
    c.strokeStyle = sealColor;
    c.lineWidth = 1.8 * s;
    c.beginPath();
    c.arc(246 * s, 276 * s, 25 * s, 0, Math.PI * 2);
    c.stroke();

    c.lineWidth = 1 * s;
    c.beginPath();
    c.arc(246 * s, 276 * s, 19 * s, 0, Math.PI * 2);
    c.stroke();

    c.fillStyle = sealColor;
    c.textAlign = 'center';
    c.font = '700 ' + (7 * s) + 'px "JetBrains Mono", monospace';
    c.fillText('AICTE', 246 * s, 270 * s);
    c.font = '700 ' + (7.5 * s) + 'px "JetBrains Mono", monospace';
    c.fillText('SEAL', 246 * s, 279 * s);
    c.font = '700 ' + (7 * s) + 'px "JetBrains Mono", monospace';
    c.fillText('2026', 246 * s, 288 * s);
    c.textAlign = 'left';

    // 5. Footer Line & Hash / Status
    c.fillStyle = dividerColor;
    c.fillRect(16 * s, 356 * s, contentWidth, 1.2 * s);

    c.fillStyle = labelColor;
    c.font = '500 ' + (9.5 * s) + 'px "JetBrains Mono", monospace';
    c.fillText('sha256 ' + doc.hash + '...', 16 * s, 377 * s);

    c.textAlign = 'right';
    c.fillStyle = badgeTextColor;
    c.font = '700 ' + (9.5 * s) + 'px "JetBrains Mono", monospace';
    c.fillText(doc.footerBadge, (w - 16 * s), 377 * s);
    c.textAlign = 'left';
  }'''

pattern = r'var seed = 5;.*?function paint\(cv, title, idx\)\{.*?c\.fillRect\(18, h - 38, w - 36, 1\.5\);\s*\}'

for fn in targets:
    if not os.path.exists(fn):
        print("Skipping non-existent", fn)
        continue
    with open(fn, 'r', encoding='utf-8') as f:
        content = f.read()

    # Replace paint block
    match = re.search(pattern, content, re.DOTALL)
    if match:
        content = content[:match.start()] + replacement_code + content[match.end():]
        print(f"Replaced paint block in {fn}")
    else:
        print(f"Regex match failed for paint block in {fn}")

    # Set canvas resolution to 600x792 for retina crispness
    content = content.replace('cv.width = 300; cv.height = 396;', 'cv.width = 600; cv.height = 792;')

    # Ensure readTheme re-renders sheets
    target_read_theme_end = '    readTheme.done = true;\n  }'
    replacement_read_theme_end = '''    readTheme.done = true;
    if (typeof sheets !== 'undefined' && sheets.length) {
      sheets.forEach(function(sh, k){
        paint(sh.cv, DOCS[k], k);
        sh.tex.needsUpdate = true;
      });
    }
  }'''
    if target_read_theme_end in content and 'paint(sh.cv, DOCS[k], k)' not in content:
        content = content.replace(target_read_theme_end, replacement_read_theme_end)

    with open(fn, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Saved {fn}")
