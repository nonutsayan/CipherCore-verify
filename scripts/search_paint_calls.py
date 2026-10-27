with open('index.html', 'r', encoding='utf-8') as f:
    text = f.read()

import re
print("Matches for paint(:")
for m in re.finditer(r'paint\(', text):
    start = max(0, m.start() - 50)
    end = min(len(text), m.end() + 100)
    print(text[start:end])
    print("-" * 40)
