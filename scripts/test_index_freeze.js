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

  console.log('1. Loading index.html...');
  await page.goto('http://localhost:3000/index.html', { waitUntil: 'domcontentloaded' });
  await sleep(400);

  // Check interactive elements on index.html
  console.log('2. Clicking nav links in header...');
  const navLinks = ['#engines', '#workflow', '#documents', '#safeguard', '#preview'];
  for (const n of navLinks) {
    const link = await page.$(`header nav a[href="${n}"]`);
    if (link) {
      console.log(`  Clicking ${n}...`);
      await link.click();
      await sleep(150);
    }
  }

  console.log('3. Clicking preview tabs (#tabs .tab)...');
  const tabs = await page.$$('#tabs .tab');
  for (let i = 0; i < tabs.length; i++) {
    const text = await (await tabs[i].getProperty('innerText')).jsonValue();
    console.log(`  Clicking tab: "${text}"...`);
    await tabs[i].click();
    await sleep(150);
  }

  console.log('4. Clicking document cards (#dlist li)...');
  const docCards = await page.$$('#dlist li');
  for (let i = 0; i < docCards.length; i++) {
    console.log(`  Clicking doc card ${i}...`);
    await docCards[i].click();
    await sleep(100);
  }

  console.log('5. Clicking "Inspect Forensic Diff" button...');
  const diffBtns = await page.$$('button');
  let diffBtnFound = false;
  for (const b of diffBtns) {
    const txt = await (await b.getProperty('innerText')).jsonValue();
    if (txt.includes('Inspect Forensic Diff')) {
      diffBtnFound = true;
      console.log('  Found button, clicking...');
      await b.click();
      await sleep(300);

      const modalStatus = await page.evaluate(() => {
        const m1 = document.querySelectorAll('#forensicDiffModal');
        return {
          count: m1.length,
          m1_display: m1[0] ? window.getComputedStyle(m1[0]).display : null,
          m2_display: m1[1] ? window.getComputedStyle(m1[1]).display : null,
          bodyOverflow: document.body.style.overflow
        };
      });
      console.log('  Modal status after click:', modalStatus);

      console.log('  Testing Escape key to dismiss...');
      await page.keyboard.press('Escape');
      await sleep(300);

      const postEscape = await page.evaluate(() => {
        const m1 = document.querySelectorAll('#forensicDiffModal');
        return {
          m1_display: m1[0] ? window.getComputedStyle(m1[0]).display : null,
          m2_display: m1[1] ? window.getComputedStyle(m1[1]).display : null,
          bodyOverflow: document.body.style.overflow
        };
      });
      console.log('  Modal status after Escape:', postEscape);
      break;
    }
  }

  await browser.close();
  console.log('index.html test finished.');
}

run().catch(err => console.error(err));
