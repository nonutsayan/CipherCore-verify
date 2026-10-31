with open('portal.html', 'r', encoding='utf-8') as f:
    text = f.read()

count = text.count('window.initLabImage = function()')
print(f'Found {count} occurrences of window.initLabImage in portal.html')

text = text.replace(
    'window.initLabImage = function() {\n    if (window.cvLabImage) return;',
    'window.initLabImage = function() {\n    if (typeof Image === "undefined" || window.cvLabImage) return;'
)

with open('portal.html', 'w', encoding='utf-8') as f:
    f.write(text)

print('Updated portal.html directly.')
