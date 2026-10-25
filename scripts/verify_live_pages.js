fetch('https://nonutsayan.github.io/CipherCore-verify/?t=' + Date.now()).then(r => {
  console.log('GitHub Pages Status:', r.status);
  return r.text();
}).then(html => {
  console.log('GitHub Pages length:', html.length);
  console.log('Has cvEvidenceModal?', html.includes('id="cvEvidenceModal"'));
  console.log('Has forensicDiffModal?', html.includes('id="forensicDiffModal"'));
  console.log('Has reportModal?', html.includes('id="reportModal"'));
  console.log('Has openCvEvidenceModal?', html.includes('openCvEvidenceModal'));
  console.log('Has openForensicDiffModal?', html.includes('openForensicDiffModal'));
  console.log('Has openReportModal?', html.includes('openReportModal'));
  console.log('Has SYNTH_LAND_DEED_PUNE_ALTERED?', html.includes('SYNTH_LAND_DEED_PUNE_ALTERED.pdf'));
});
