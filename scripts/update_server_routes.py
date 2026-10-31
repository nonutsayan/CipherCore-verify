with open('server.js', 'r', encoding='utf-8') as f:
    text = f.read()

old = "if (reqPath === '/' || reqPath === '') reqPath = '/index.html';"
new = "if (reqPath === '/' || reqPath === '') reqPath = '/index.html';\n  if (reqPath === '/portal' || reqPath === '/portal/') reqPath = '/portal.html';\n  if (reqPath === '/landing' || reqPath === '/landing/') reqPath = '/landing.html';"

text = text.replace(old, new)
with open('server.js', 'w', encoding='utf-8') as f:
    f.write(text)
print('Updated server.js routing successfully!')
