const fs = require('fs');

function checkTargets(filePath) {
  const content = fs.readFileSync(filePath, 'utf8');
  console.log(`Checking ${filePath}... Length: ${content.length}`);

  const targets = [
    '<span class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-800 border border-amber-300">DEFICIENT (-8 Units)</span>',
    'DIGITAL TAMPER CONFIRMED',
    'Consortium Network: BharatVidya PoA Trust Network (AICTE Sovereign Notary Protocol)',
    '7/7 PoA Active',
    'AICTE-HQ (New Delhi)'
  ];

  targets.forEach(t => {
    console.log(`Found "${t.substring(0, 30)}...":`, content.includes(t));
  });
}

checkTargets('portal.html');
checkTargets('frontend/portal.html');
