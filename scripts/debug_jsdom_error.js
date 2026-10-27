const fs = require('fs');
const { JSDOM, VirtualConsole } = require('jsdom');

const html = fs.readFileSync('portal.html', 'utf8');

const virtualConsole = new VirtualConsole();
virtualConsole.on('error', (err) => {
  console.error('CONSOLE ERROR:', err);
});
virtualConsole.on('warn', (msg) => {
  console.warn('CONSOLE WARN:', msg);
});
virtualConsole.on('jsdomError', (err) => {
  console.error('JSDOM SCRIPT ERROR:', err);
});

try {
  const dom = new JSDOM(html, {
    runScripts: 'dangerously',
    virtualConsole: virtualConsole,
    url: 'http://localhost:3000/portal.html'
  });
  console.log('JSDOM instantiated');
} catch (e) {
  console.error('CAUGHT INSTANTIATION ERROR:', e);
}
