files = [
    "frontend/index.html",
    "index.html",
    "backend/src/main/resources/static/index.html"
]

for fpath in files:
    with open(fpath, "r", encoding="utf-8") as f:
        c = f.read()
    
    c = c.replace(
        '<h3 class="text-sm font-bold text-main">No Active Applications in Queue</h3>',
        '<h2 class="text-sm font-bold text-main">No Active Applications in Queue</h2>'
    )
    c = c.replace(
        '<h3 class="text-sm font-bold text-main">CipherCore Access Gateway</h3>',
        '<h2 class="text-sm font-bold text-main">CipherCore Access Gateway</h2>'
    )
    
    with open(fpath, "w", encoding="utf-8") as f:
        f.write(c)
    print("Updated headings in", fpath)
