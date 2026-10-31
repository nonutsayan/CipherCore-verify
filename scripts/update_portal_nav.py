with open("portal.html", "r", encoding="utf-8") as f:
    c = f.read()

target1 = '''      <!-- Zone 3: Actions (Right) -->\n      <div class="header-actions">\n        <button onclick="toggleColorTheme()"'''

replace1 = '''      <!-- Zone 3: Actions (Right) -->\n      <div class="header-actions">\n        <a href="index.html" class="px-2.5 py-1 text-xs font-mono text-slate-400 hover:text-slate-200 border border-slate-700/60 rounded flex items-center gap-1 transition-colors" title="Return to 3D Landing Page">\n          <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>\n          <span>Landing</span>\n        </a>\n        <button onclick="toggleColorTheme()"'''

target2 = '''      <!-- ZONE 3: ACTIONS (RIGHT) - EXACTLY USER BADGE + THEME TOGGLE + SIGN OUT -->\n      <div class="header-actions">\n        <!-- a) User Profile Badge -->'''

replace2 = '''      <!-- ZONE 3: ACTIONS (RIGHT) - EXACTLY USER BADGE + THEME TOGGLE + SIGN OUT -->\n      <div class="header-actions">\n        <a href="index.html" class="px-2.5 py-1 text-xs font-mono text-slate-400 hover:text-slate-200 border border-slate-700/60 rounded flex items-center gap-1 transition-colors" title="Return to 3D Landing Page">\n          <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>\n          <span>Landing</span>\n        </a>\n        <!-- a) User Profile Badge -->'''

c = c.replace(target1.replace("\n", "\r\n"), replace1.replace("\n", "\r\n"))
c = c.replace(target1, replace1)
c = c.replace(target2.replace("\n", "\r\n"), replace2.replace("\n", "\r\n"))
c = c.replace(target2, replace2)

with open("portal.html", "w", encoding="utf-8") as f:
    f.write(c)

with open("frontend/portal.html", "w", encoding="utf-8") as f:
    f.write(c)

with open("backend/src/main/resources/static/portal.html", "w", encoding="utf-8") as f:
    f.write(c)

print("Updated portal.html with Landing button successfully!")
