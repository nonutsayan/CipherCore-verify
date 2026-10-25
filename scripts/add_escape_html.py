def main():
    with open('frontend/index.html', 'r', encoding='utf-8') as f:
        text = f.read()

    helper = """    function escapeHtml(str) {
      if (typeof str !== 'string') str = String(str || '');
      return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }
"""

    target = 'function renderPayloadSummary(payload) {'
    if target in text and 'function escapeHtml' not in text:
        text = text.replace(target, helper + '\n    ' + target)

    with open('frontend/index.html', 'w', encoding='utf-8') as f:
        f.write(text)

    with open('index.html', 'w', encoding='utf-8') as f:
        f.write(text)

    with open('backend/src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
        f.write(text)

    print("escapeHtml added successfully.")

if __name__ == '__main__':
    main()
