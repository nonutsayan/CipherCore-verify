with open('portal.html', 'r', encoding='utf-8') as f:
    text = f.read()

import re
style_blocks = re.findall(r'<style[^>]*>([\s\S]*?)</style>', text)
print(f'Total style blocks: {len(style_blocks)}')

for i, sb in enumerate(style_blocks):
    for line in sb.split('\n'):
        if any(k in line for k in ['modal-backdrop', 'cvEvidence', 'forensicDiff', 'reportModal']):
            print(f'Block {i+1}: {line.strip()}')
