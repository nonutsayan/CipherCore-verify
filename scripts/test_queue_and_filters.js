const puppeteer = require('puppeteer-core');

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';

(async () => {
  console.log('Starting automated queue and filter lifecycle tests...');
  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: 'new',
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });

  const page = await browser.newPage();
  await page.setViewport({ width: 1400, height: 900 });

  const consoleErrors = [];
  page.on('console', msg => {
    if (msg.type() === 'error') {
      const loc = msg.location();
      consoleErrors.push({ text: msg.text(), url: loc.url, line: loc.lineNumber });
    }
  });
  page.on('response', async res => {
    if (res.status() >= 400) {
      const body = await res.text().catch(() => '');
      console.log(`[HTTP ${res.status()}] ${res.url()} -> ${body}`);
    }
  });

  // 1. Visit Portal
  console.log('\n--- 1. Fresh Portal Visit ---');
  await page.goto('http://localhost:3000/portal.html', { waitUntil: 'networkidle2' });

  // Authenticate as AICTE Scrutiny Officer
  console.log('Authenticating as AICTE Scrutiny Officer...');
  await page.waitForFunction(() => typeof window.loginAsOfficer === 'function');
  await page.evaluate(() => {
    window.loginAsOfficer();
  });
  await new Promise(r => setTimeout(r, 800));

  // Check KPI numbers
  const getKpis = async () => {
    return await page.evaluate(() => ({
      total: document.getElementById('kpiTotalDossiers')?.textContent.trim(),
      verified: document.getElementById('kpiVerifiedCount')?.textContent.trim(),
      flagged: document.getElementById('kpiFlaggedCount')?.textContent.trim(),
      pending: document.getElementById('kpiPendingCount')?.textContent.trim()
    }));
  };

  const getFilters = async () => {
    return await page.evaluate(() => ({
      all: document.getElementById('filterTab-ALL')?.textContent.trim(),
      review: document.getElementById('filterTab-Needs Review')?.textContent.trim(),
      verified: document.getElementById('filterTab-Verified')?.textContent.trim(),
      incomplete: document.getElementById('filterTab-Incomplete')?.textContent.trim()
    }));
  };

  const getTableRowsCount = async () => {
    return await page.evaluate(() => {
      return document.querySelectorAll('#applicationsTableBody tr').length;
    });
  };

  const kpis1 = await getKpis();
  const filters1 = await getFilters();
  const rows1 = await getTableRowsCount();

  console.log('KPIs after login:', kpis1);
  console.log('Filters after login:', filters1);
  console.log('Table rows after login:', rows1);

  if (kpis1.total !== '6' || kpis1.verified !== '3' || kpis1.flagged !== '2' || kpis1.pending !== '1') {
    throw new Error(`KPI mismatch: expected 6, 3, 2, 1 but got ${JSON.stringify(kpis1)}`);
  }
  if (rows1 !== 6) {
    throw new Error(`Table rows mismatch: expected 6 but got ${rows1}`);
  }
  console.log('PASS: Initial load has all 6 demo institutions matching KPIs (6 = 3 + 2 + 1)');

  // 2. Test Filters
  console.log('\n--- 2. Testing Filter Buttons ---');
  await page.click('#filterTab-Needs\\ Review');
  await new Promise(r => setTimeout(r, 400));
  const reviewRows = await getTableRowsCount();
  console.log('Rows when filter is "Needs Review":', reviewRows);
  if (reviewRows !== 2) throw new Error(`Expected 2 review rows, got ${reviewRows}`);

  await page.click('#filterTab-Verified');
  await new Promise(r => setTimeout(r, 400));
  const verifiedRows = await getTableRowsCount();
  console.log('Rows when filter is "Verified":', verifiedRows);
  if (verifiedRows !== 3) throw new Error(`Expected 3 verified rows, got ${verifiedRows}`);

  await page.click('#filterTab-Incomplete');
  await new Promise(r => setTimeout(r, 400));
  const incompleteRows = await getTableRowsCount();
  console.log('Rows when filter is "Incomplete":', incompleteRows);
  if (incompleteRows !== 1) throw new Error(`Expected 1 incomplete row, got ${incompleteRows}`);

  await page.click('#filterTab-ALL');
  await new Promise(r => setTimeout(r, 400));
  const allRows = await getTableRowsCount();
  console.log('Rows when filter is "ALL":', allRows);
  if (allRows !== 6) throw new Error(`Expected 6 rows, got ${allRows}`);
  console.log('PASS: Filter pills filter table records accurately');

  // 3. Test Clear Queue
  console.log('\n--- 3. Testing Clear Queue ---');
  await page.click('#clearQueueBtn');
  await new Promise(r => setTimeout(r, 500));

  const kpisCleared = await getKpis();
  const filtersCleared = await getFilters();
  const isZeroStateVisible = await page.evaluate(() => {
    const el = document.getElementById('applicationsZeroState');
    return el && !el.classList.contains('hidden');
  });

  console.log('KPIs after clear:', kpisCleared);
  console.log('Filters after clear:', filtersCleared);
  console.log('Zero state visible:', isZeroStateVisible);

  if (kpisCleared.total !== '0' || kpisCleared.verified !== '0' || kpisCleared.flagged !== '0' || kpisCleared.pending !== '0') {
    throw new Error(`Expected 0 KPIs after clear, got ${JSON.stringify(kpisCleared)}`);
  }
  if (!isZeroStateVisible) {
    throw new Error('Zero state should be visible after clearing queue');
  }
  console.log('PASS: Clear queue sets zero state and zeroes all counters');

  // 4. Test Page Refresh Preserves Cleared Queue
  console.log('\n--- 4. Refreshing Page (Cleared Queue State) ---');
  await page.reload({ waitUntil: 'networkidle2' });
  await new Promise(r => setTimeout(r, 500));

  const kpisRefreshed = await getKpis();
  const isZeroStateStillVisible = await page.evaluate(() => {
    const el = document.getElementById('applicationsZeroState');
    return el && !el.classList.contains('hidden');
  });
  console.log('KPIs after refresh:', kpisRefreshed);
  console.log('Zero state visible after refresh:', isZeroStateStillVisible);
  if (kpisRefreshed.total !== '0' || !isZeroStateStillVisible) {
    throw new Error('Cleared queue state must persist across refresh');
  }
  console.log('PASS: Cleared queue persisted across page refresh');

  // 5. Test Load Demo Data
  console.log('\n--- 5. Loading Demo Dataset ---');
  await page.click('#loadDemoBtn');
  await new Promise(r => setTimeout(r, 600));

  const kpisReloaded = await getKpis();
  const filtersReloaded = await getFilters();
  const rowsReloaded = await getTableRowsCount();

  console.log('KPIs after reload:', kpisReloaded);
  console.log('Filters after reload:', filtersReloaded);
  console.log('Rows after reload:', rowsReloaded);

  if (kpisReloaded.total !== '6' || rowsReloaded !== 6) {
    throw new Error(`Expected 6 applications after loading demo data, got ${rowsReloaded}`);
  }
  console.log('PASS: Load demo dataset loaded all 6 sample institutions');

  // 6. Test Idempotency (calling loadDemoBtn again)
  console.log('\n--- 6. Testing Idempotent Reload ---');
  await page.click('#loadDemoBtn');
  await new Promise(r => setTimeout(r, 600));
  const rowsIdempotent = await getTableRowsCount();
  if (rowsIdempotent !== 6) {
    throw new Error(`Idempotency failure: expected 6 applications but got ${rowsIdempotent}`);
  }
  console.log('PASS: Demo seeding is completely idempotent (rows remain 6)');

  // 7. Check Console Errors
  console.log('\n--- 7. Console Error Audit ---');
  console.log('Total console errors recorded:', consoleErrors.length);
  if (consoleErrors.length > 0) {
    console.log('Errors:', consoleErrors);
  }

  await browser.close();
  console.log('\nALL QUEUE & FILTER TESTS PASSED SUCCESSFULLY!');
})();
