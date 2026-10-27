/**
 * MASTER FORENSIC DEBUGGING SUITE
 * Exhaustively tests all user paths, tab clicks, sub-tabs, modals, and Escape handling.
 */
const puppeteer = require('puppeteer-core');

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const sleep = ms => new Promise(r => setTimeout(r, ms));

async function main() {
  console.log('=====================================================================');
  console.log(' FORENSIC AUDIT: COMPREHENSIVE TAB & MODAL INTERACTION TRACE');
  console.log('=====================================================================\n');

  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: 'new',
    args: [
      '--no-sandbox',
      '--disable-gpu',
      '--disable-dev-shm-usage',
      '--no-first-run',
      '--no-default-browser-check',
      '--window-size=1400,900'
    ]
  });

  const page = await browser.newPage();
  await page.setViewport({ width: 1400, height: 900 });

  const errors = [];
  page.on('console', msg => {
    if (msg.type() === 'error') {
      errors.push({ type: 'console.error', text: msg.text() });
      console.log(`  [Console Error] ${msg.text()}`);
    }
  });
  page.on('pageerror', err => {
    errors.push({ type: 'pageerror', text: err.message });
    console.error(`  [Page Exception] ${err.message}`);
  });

  async function checkInteractivity(stepName) {
    const status = await page.evaluate(() => {
      const w = window.innerWidth;
      const h = window.innerHeight;
      const points = [
        { label: 'top-nav-tab', x: Math.floor(w * 0.35), y: 35 },
        { label: 'viewport-center', x: Math.floor(w * 0.5), y: Math.floor(h * 0.5) },
        { label: 'content-area', x: Math.floor(w * 0.5), y: 250 }
      ];

      const hits = points.map(pt => {
        const el = document.elementFromPoint(pt.x, pt.y);
        return {
          label: pt.label,
          tag: el ? el.tagName : 'NULL',
          id: el ? el.id : '',
          class: el ? el.className.toString().slice(0, 40) : '',
          isBackdrop: el ? (el.classList.contains('modal-backdrop') || el.id.includes('Modal')) : false
        };
      });

      const openModals = Array.from(document.querySelectorAll('.modal-backdrop, [id*="Modal"]'))
        .filter(m => {
          const s = window.getComputedStyle(m);
          return s.display !== 'none' && s.visibility !== 'hidden' && s.opacity !== '0';
        })
        .map(m => m.id);

      return {
        bodyOverflow: document.body.style.overflow,
        openModals,
        hits
      };
    });

    console.log(`[Check: ${stepName}]`);
    console.log(`  Body overflow: "${status.bodyOverflow}" | Visible Modals: [${status.openModals.join(', ')}]`);
    status.hits.forEach(h => {
      console.log(`  Hit at ${h.label}: <${h.tag} id="${h.id}" class="${h.class}"> (isBackdrop: ${h.isBackdrop})`);
    });
    return status;
  }

  // =========================================================================
  // TEST SCENARIO A: LANDING PAGE (index.html)
  // =========================================================================
  console.log('\n--- SCENARIO A: index.html Landing Page ---');
  await page.goto('http://localhost:3000/index.html', { waitUntil: 'domcontentloaded' });
  await sleep(500);
  await checkInteractivity('Landing Initial Load');

  // Test filter tabs on landing page
  const landingTabs = ['all', 'ok', 'warn', 'flag'];
  for (const f of landingTabs) {
    const sel = `button.tab[data-f="${f}"]`;
    const tabEl = await page.$(sel);
    if (tabEl) {
      console.log(`  Clicking landing preview tab: data-f="${f}"`);
      await tabEl.click();
      await sleep(100);
    }
  }
  await checkInteractivity('Landing After Filter Tabs');

  // Test Forensic Diff button on landing page (if present)
  const diffBtns = await page.$$('button');
  for (const b of diffBtns) {
    const text = await (await b.getProperty('innerText')).jsonValue();
    if (text.includes('Inspect Forensic Diff')) {
      console.log('  Clicking "Inspect Forensic Diff" on landing page...');
      await b.click();
      await sleep(250);
      const postClick = await checkInteractivity('Landing Forensic Diff Opened');
      console.log('  Pressing Escape key...');
      await page.keyboard.press('Escape');
      await sleep(200);
      await checkInteractivity('Landing After Escape');
      break;
    }
  }

  // =========================================================================
  // TEST SCENARIO B: PORTAL PAGE (portal.html) UNAPPROVED / INITIAL STATE
  // =========================================================================
  console.log('\n--- SCENARIO B: portal.html Initial Gateway State ---');
  await page.goto('http://localhost:3000/portal.html', { waitUntil: 'domcontentloaded' });
  await sleep(500);
  const unauthCheck = await checkInteractivity('Portal Fresh Load (Unauthenticated)');

  console.log('  Pressing Escape key while unauthenticated...');
  await page.keyboard.press('Escape');
  await sleep(200);
  const postEscapeUnauth = await checkInteractivity('Portal Unauth After Escape');

  // =========================================================================
  // TEST SCENARIO C: PORTAL PAGE AUTHENTICATED
  // =========================================================================
  console.log('\n--- SCENARIO C: Portal Authentication & Tab Navigation ---');
  console.log('  Authenticating as AICTE Scrutiny Officer...');
  await page.evaluate(() => loginAsOfficer());
  await sleep(400);
  await checkInteractivity('Portal Post-Authentication (Applications View)');

  // 1. Applications Filter Tabs
  console.log('\n  [1. Testing Applications View Filter Tabs]');
  const appFilters = ['#filterTab-ALL', '#filterTab-Needs\\ Review', '#filterTab-Verified', '#filterTab-Incomplete'];
  for (const f of appFilters) {
    const btn = await page.$(f);
    if (btn) {
      console.log(`    Clicking filter tab: ${f}`);
      await btn.click();
      await sleep(100);
      await checkInteractivity(`Filter Tab ${f}`);
    } else {
      console.warn(`    Filter tab ${f} not found`);
    }
  }

  // 2. Primary Navigation Tabs: Switch to Inspect
  console.log('\n  [2. Navigating to View 2: Inspect Application]');
  await page.click('#mainNav-inspect');
  await sleep(200);
  await checkInteractivity('Inspect Application View');

  // Test Sub-Tabs 1-4 in Inspect View
  for (let s = 1; s <= 4; s++) {
    const subBtn = `#inspectSubBtn-${s}`;
    console.log(`    Clicking Inspect Sub-Tab ${s} (${subBtn})...`);
    await page.click(subBtn);
    await sleep(200);
    const subCheck = await checkInteractivity(`Inspect Sub-Tab ${s}`);

    // If Sub-Tab 2: Test Forensic Diff Modal & Escape
    if (s === 2) {
      console.log('    Testing "Inspect Forensic Diff" button in Sub-Tab 2...');
      const fBtn = await page.$('#inspectSubTabContent button[onclick*="openForensicDiffModal"]');
      if (fBtn) {
        await fBtn.click();
        await sleep(250);
        await checkInteractivity('Forensic Diff Modal Open');

        // Test tabs inside Forensic Diff Modal: split, ela, hash
        for (const mode of ['split', 'ela', 'hash']) {
          const mBtnId = mode === 'split' ? '#forensicModeBtnSplit' : mode === 'ela' ? '#forensicModeBtnEla' : '#forensicModeBtnHash';
          console.log(`      Clicking Forensic Diff mode tab: ${mode} (${mBtnId})...`);
          const mBtn = await page.$(mBtnId);
          if (mBtn) {
            await mBtn.click();
            await sleep(150);
            await checkInteractivity(`Forensic Diff Mode ${mode}`);
          }
        }

        console.log('      Testing ESCAPE key to dismiss Forensic Diff Modal...');
        await page.keyboard.press('Escape');
        await sleep(200);
        await checkInteractivity('After Forensic Diff Dismissed via Escape');
      }
    }

    // If Sub-Tab 4: Test CV Evidence Modal & Escape
    if (s === 4) {
      console.log('    Testing "View CV Evidence Inspection" button in Sub-Tab 4...');
      const cvBtn = await page.$('#inspectSubTabContent button[onclick*="openCvEvidenceModal"]');
      if (cvBtn) {
        await cvBtn.click();
        await sleep(250);
        await checkInteractivity('CV Evidence Modal Open');

        console.log('      Testing ESCAPE key to dismiss CV Evidence Modal...');
        await page.keyboard.press('Escape');
        await sleep(200);
        await checkInteractivity('After CV Evidence Dismissed via Escape');
      }
    }
  }

  // 3. Test Preview Scrutiny Report Modal & Escape
  console.log('\n  [3. Testing Scrutiny Report Modal & Escape]');
  const reportBtn = await page.$('#view-inspect button[onclick*="openReportModal"]');
  if (reportBtn) {
    console.log('    Clicking Preview Scrutiny Report button...');
    await reportBtn.click();
    await sleep(250);
    await checkInteractivity('Report Modal Open');
    console.log('    Pressing Escape to dismiss Report Modal...');
    await page.keyboard.press('Escape');
    await sleep(200);
    await checkInteractivity('After Report Modal Dismissed via Escape');
  }

  // 4. Test Official Decision Modal & Escape
  console.log('\n  [4. Testing Official Decision Modal & Escape]');
  const decBtn = await page.$('#officialDecisionActionBtn');
  if (decBtn) {
    console.log('    Clicking Official Decision button...');
    await decBtn.click();
    await sleep(250);
    await checkInteractivity('Official Decision Modal Open');
    console.log('    Pressing Escape to dismiss Official Decision Modal...');
    await page.keyboard.press('Escape');
    await sleep(200);
    await checkInteractivity('After Official Decision Dismissed via Escape');
  }

  // 5. Primary Navigation Tab 3: Document Checker
  console.log('\n  [5. Navigating to View 3: Document Checker]');
  await page.click('#mainNav-checker');
  await sleep(200);
  await checkInteractivity('Checker View Loaded');

  // Test all sample document pills
  for (let docIdx = 1; docIdx <= 6; docIdx++) {
    const sampleBtnId = `#sampleDocBtn-SAMPLE-${docIdx}`;
    console.log(`    Clicking sample document pill: SAMPLE-${docIdx} (${sampleBtnId})...`);
    const sBtn = await page.$(sampleBtnId);
    if (sBtn) {
      await sBtn.click();
      await sleep(150);
      await checkInteractivity(`Checker Sample-${docIdx}`);
    }
  }

  // 6. Primary Navigation Tab 4: Rules & Audit Log
  console.log('\n  [6. Navigating to View 4: Rules & Audit Log]');
  await page.click('#mainNav-rules');
  await sleep(200);
  await checkInteractivity('Rules View Loaded');

  console.log('    Clicking Rules Sub-Tab: Audit Log (#rulesSubBtn-audit-log)...');
  await page.click('#rulesSubBtn-audit-log');
  await sleep(200);
  await checkInteractivity('Audit Log Sub-Tab');

  console.log('    Clicking Rules Sub-Tab: Validation Rules (#rulesSubBtn-rules-list)...');
  await page.click('#rulesSubBtn-rules-list');
  await sleep(200);
  await checkInteractivity('Validation Rules Sub-Tab');

  // 7. Test Ingestion Modal: Upload Dossier Modal
  console.log('\n  [7. Testing Upload Dossier Modal]');
  await page.click('#mainNav-applications');
  await sleep(200);
  const upBtn = await page.$('#uploadDossierBtn');
  if (upBtn) {
    console.log('    Clicking Upload Application Dossier button...');
    await upBtn.click();
    await sleep(250);
    await checkInteractivity('Upload Dossier Modal Open');
    console.log('    Pressing Escape to dismiss Upload Dossier Modal...');
    await page.keyboard.press('Escape');
    await sleep(200);
    await checkInteractivity('After Upload Dossier Dismissed via Escape');
  }

  // 8. Rapid View Switching Stress
  console.log('\n  [8. Rapid Stress View Switching]');
  const mainTabs = ['#mainNav-applications', '#mainNav-inspect', '#mainNav-checker', '#mainNav-rules'];
  for (let cycle = 1; cycle <= 4; cycle++) {
    for (const t of mainTabs) {
      await page.click(t);
      await sleep(50);
    }
  }
  const postStress = await checkInteractivity('Post Stress Test');

  console.log('\n=====================================================================');
  console.log(` AUDIT COMPLETE. Total Errors Recorded: ${errors.length}`);
  console.log('=====================================================================');
  await browser.close();
}

main().catch(err => {
  console.error('Master Forensic Audit Failed:', err);
  process.exit(1);
});
