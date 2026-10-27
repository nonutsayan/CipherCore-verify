with open('index.html', 'r', encoding='utf-8') as f:
    text = f.read()

import re
for m in re.finditer(r'sheet', text, re.IGNORECASE):
    start = max(0, m.start() - 100)
    end = min(len(text), m.end() + 200)
    print("Match 'sheet' at", m.start(), ":")
    print(text[start:end])
    print("=" * 60)
