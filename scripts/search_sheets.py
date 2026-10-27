with open('index.html', 'r', encoding='utf-8') as f:
    text = f.read()

import re
print("Matches for 'seal':", len(re.findall(r'seal', text, re.IGNORECASE)))
print("Matches for 'sheet':", len(re.findall(r'sheet', text, re.IGNORECASE)))
print("Matches for 'e3b0c442':", len(re.findall(r'e3b0c442', text, re.IGNORECASE)))
print("Matches for 'DOC 0':", len(re.findall(r'DOC 0', text, re.IGNORECASE)))

for m in re.finditer(r'DOC 0\d', text):
    start = max(0, m.start() - 100)
    end = min(len(text), m.end() + 200)
    print("Match DOC 0x at", m.start(), ":")
    print(text[start:end])
    print("-" * 50)
