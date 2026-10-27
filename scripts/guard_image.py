with open('scripts/cv_forensic_engine.js', 'r', encoding='utf-8') as f:
    code = f.read()

code = code.replace(
    'window.initLabImage = function() {\n    if (window.cvLabImage) return;',
    'window.initLabImage = function() {\n    if (typeof Image === "undefined" || window.cvLabImage) return;'
)

with open('scripts/cv_forensic_engine.js', 'w', encoding='utf-8') as f:
    f.write(code)

print("Updated cv_forensic_engine.js with Image guard.")
