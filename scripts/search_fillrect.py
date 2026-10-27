with open('index.html', 'r', encoding='utf-8') as f:
    text = f.read()

import re
matches = re.finditer(r'fillRect', text)
for m in matches:
    start = max(0, m.start() - 60)
    end = min(len(text), m.end() + 60)
    print(text[start:end])
    print("-" * 50)
