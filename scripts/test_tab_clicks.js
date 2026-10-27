const puppeteer = require('puppeteer-core');
const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const sleep = ms => new Promise(r => setTimeout(r, ms));

async function run() {
  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: 'new',
    args: ['--no-sandbox', '--disable-gpu']
  });
  const page = await browser.newPage();
  await page.setViewport({ width: 1400, height: 900 });

  page.on('console', msg => {
    if (msg.type() === 'error') console.log(`[Console Error] ${msg.text()}`);
  });
  page.on('pageerror', err => console.error(`[PageError] ${err.message}`));

  console.log('1. Loading portal.html...');
  await page.goto('http://localhost:3000/portal.html', { waitUntil: 'domcontentloaded' });
  await sleep(400);

  console.log('2. Logging in as Scrutiny Officer...');
  await page.evaluate(() => loginAsOfficer());
  await sleep(400);

  // Helper to click and check responsiveness
  async function testClick(label, selector) {
    const t0 = Date.now();
    console.log(`\nTesting click on ${label} (${selector})...`);
    
    // Check hit test before click
    const hitBefore = await page.evaluate(sel => {
      const el = document.querySelector(sel);
      if (!el) return { found: false };
      const r = el.getBoundingClientRect();
      const topEl = document.elementFromPoint(r.left + r.width / 2, r.top + r.height / 2);
      return {
        found: true,
        rect: { x: r.x, y: r.y, w: r.width, h: r.height },
        topElementTag: topEl?.tagName,
        topElementId: topEl?.id,
        isSameOrChild: topEl === el || el.contains(topEl)
      };
    }, selector);

    console.log(`  Hit test before click:`, hitBefore);

    if (!hitBefore.found) {
      console.error(`  ERROR: Element ${selector} not found!`);
      return;
    }
    if (!hitBefore.isSameOrChild) {
      console.warn(`  WARNING: Element is covered by <${hitBefore.topElementTag} id="${hitBefore.topElementId}">!`);
    }

    try {
      await page.click(selector);
    } catch (e) {
      console.error(`  CLICK FAILED: ${e.message}`);
    }

    await sleep(200);

    // Measure main thread responsiveness
    const tStart = Date.now();
    const threadCheck = await page.evaluate(() => {
      return {
        bodyOverflow: document.body.style.overflow,
        activeView: window.activeMainView,
        visibleModals: Array.from(document.querySelectorAll('.modal-backdrop, [id*="Modal"]'))
          .filter(m => window.getComputedStyle(m).display !== 'none' && window.getComputedStyle(m).visibility !== 'hidden')
          .map(m => m.id)
      };
    });
    const latency = Date.now() - tStart;
    console.log(`  Result after click: Latency=${latency}ms | Overflow="${threadCheck.bodyOverflow}" | VisibleModals=[${threadCheck.visibleModals.join(',')}]`);
  }

  // Primary Nav Tabs
  await testClick('Nav Tab: Inspect', '#mainNav-inspect');
  await testClick('Inspect Sub-Tab 1', '#inspectSubBtn-1');
  await testClick('Inspect Sub-Tab 2', '#inspectSubBtn-2');
  await testClick('Inspect Sub-Tab 3', '#inspectSubBtn-3');
  await testClick('Inspect Sub-Tab 4', '#inspectSubBtn-4');

  await testClick('Nav Tab: Checker', '#mainNav-checker');
  await testClick('Checker Sample 1', '#sampleDocBtn-SAMPLE-1');
  await testClick('Checker Sample 2', '#sampleDocBtn-SAMPLE-2');
  await testClick('Checker Sample 3', '#sampleDocBtn-SAMPLE-3');
  await testClick('Checker Sample 4', '#sampleDocBtn-SAMPLE-4');

  await testClick('Nav Tab: Rules', '#mainNav-rules');
  await testClick('Rules Sub-Tab: Audit Log', '#rulesSubBtn-audit-log');
  await testClick('Rules Sub-Tab: Validation Rules', '#rulesSubBtn-rules-list');

  await testClick('Nav Tab: Applications', '#mainNav-applications');
  await testClick('Filter Tab: Verified', '#filterTab-Verified');
  await testClick('Filter Tab: Incomplete', '#filterTab-Incomplete');
  await testClick('Filter Tab: ALL', '#filterTab-ALL');

  console.log('\n--- Stress test: 10 Rapid consecutive tab clicks ---');
  for (let i = 0; i < 10; i++) {
    const tab = ['#mainNav-inspect', '#mainNav-checker', '#mainNav-rules', '#mainNav-applications'][i % 4];
    await page.click(tab);
    await sleep(40);
  }

  const finalCheck = await page.evaluate(() => {
    return {
      bodyOverflow: document.body.style.overflow,
      activeView: window.activeMainView
    };
  });
  console.log('Final State after rapid clicks:', finalCheck);

  await browser.close();
  console.log('Tab click audit finished successfully.');
}

run().catch(err => console.error('Tab click audit failed:', err));
