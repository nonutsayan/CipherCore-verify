fetch('https://nonutsayan.github.io/CipherCore-verify/?t=' + Date.now()).then(r => {
  console.log('GitHub Pages Status:', r.status);
  return r.text();
}).then(html => {
  console.log('GitHub Pages length:', html.length);
  console.log('Has Launch Forensic Diff Viewer?', html.includes('Launch Forensic Diff Viewer'));
  console.log('Has Launch CV Evidence Audit?', html.includes('Launch CV Evidence Audit'));
  console.log('Has getCvModal in JS?', html.includes('getCvModal'));
});
