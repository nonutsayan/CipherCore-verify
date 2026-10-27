const fs = require('fs');

function checkDivs(filename) {
  const html = fs.readFileSync(filename, 'utf8');
  // Strip comments and scripts
  const clean = html
    .replace(/<!--[\s\S]*?-->/g, '')
    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
    .replace(/<style\b[^<]*(?:(?!<\/style>)<[^<]*)*<\/style>/gi, '');

  const tagRegex = /<\/?([a-zA-Z0-9]+)(?:\s+[^>]*)?>/g;
  const stack = [];
  let match;
  const voidTags = new Set(['area', 'base', 'br', 'col', 'embed', 'hr', 'img', 'input', 'link', 'meta', 'param', 'source', 'track', 'wbr']);

  let mismatches = 0;
  while ((match = tagRegex.exec(clean)) !== null) {
    const fullTag = match[0];
    const tagName = match[1].toLowerCase();
    const isClosing = fullTag.startsWith('</');
    const isSelfClosing = fullTag.endsWith('/>') || voidTags.has(tagName);

    if (isSelfClosing) continue;

    if (!isClosing) {
      const idMatch = fullTag.match(/id=["']([^"']+)["']/);
      stack.push({ tag: tagName, id: idMatch ? idMatch[1] : null });
    } else {
      if (stack.length === 0) {
        console.log(filename, 'Extra closing tag:', fullTag);
        mismatches++;
      } else {
        const top = stack.pop();
        if (top.tag !== tagName) {
          console.log(filename, 'Mismatched closing tag:', fullTag, 'expected closing for <' + top.tag + (top.id ? ' id="' + top.id + '"' : '') + '>');
          mismatches++;
        }
      }
    }
  }
  console.log(filename, 'Done. Remaining unclosed tags:', stack.length, 'Mismatches:', mismatches);
  if (stack.length > 0) {
    console.log(filename, 'Unclosed tags:', stack);
  }
}

checkDivs('portal.html');
checkDivs('index.html');
