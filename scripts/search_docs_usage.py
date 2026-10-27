with open('index.html', 'r', encoding='utf-8') as f:
    text = f.read()

import re
matches = re.finditer(r'\bDOCS\b', text)
for m in matches:
    start = max(0, m.start() - 50)
    end = min(len(text), m.end() + 50)
    print(text[start:end])
    print("-" * 50)
