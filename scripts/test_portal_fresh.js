const puppeteer = require('puppeteer-core');
const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';

async function test() {
  console.log('Launching browser...');
  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: 'new',
    args: ['--no-sandbox', '--disable-gpu']
  });
  const page = await browser.newPage();

  page.on('console', msg => console.log(`[Console] ${msg.type()}: ${msg.text()}`));
  page.on('pageerror', err => console.error(`[PageError] ${err.message}`));
  page.on('requestfailed', req => console.warn(`[RequestFailed] ${req.url()} (${req.failure().errorText})`));

  console.log('Navigating to http://localhost:3000/portal.html...');
  await page.goto('http://localhost:3000/portal.html', { waitUntil: 'domcontentloaded' });
  console.log('DOMContentLoaded reached!');

  await new Promise(r => setTimeout(r, 1000));
  console.log('1s elapsed.');

  const bodyState = await page.evaluate(() => {
    return {
      bodyOverflow: document.body.style.overflow,
      activeUser: window.activeUserSession,
      authModalDisplay: document.getElementById('authModal')?.style?.display,
      authModalClasses: document.getElementById('authModal')?.className,
      portalHeaderHidden: document.getElementById('portalHeader')?.classList?.contains('hidden'),
      publicHeaderHidden: document.getElementById('publicHeader')?.classList?.contains('hidden')
    };
  });
  console.log('Body State:', bodyState);

  await browser.close();
  console.log('Done!');
}

test().catch(err => console.error('Error:', err));
