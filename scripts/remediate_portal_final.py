import re

with open('portal.html', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. CSS Token & Variable Adjustments
# Add --text-inverted and --modal-backdrop-bg to :root
root_tokens_add = """      --text-inverted: #f8fafc;
      --modal-backdrop-bg: rgba(15, 23, 42, 0.70);
      --radius-xs: 3px;"""

content = content.replace("      --radius-xs: 3px;", root_tokens_add, 1)

# Change [data-theme="dark"], html.dark selector to [data-theme="dark"]
content = content.replace(
    '[data-theme="dark"], html.dark {',
    '[data-theme="dark"] {'
)

# Add --text-inverted and --modal-backdrop-bg to [data-theme="dark"]
dark_tokens_add = """      --text-inverted: #f8fafc;
      --modal-backdrop-bg: rgba(11, 17, 32, 0.85);
      --bg-page: #0b1120;"""

content = content.replace("      --bg-page: #0b1120;", dark_tokens_add, 1)

# Ensure toggleColorTheme sets data-theme attribute
old_toggle = """function toggleColorTheme() {
      const htmlEl = document.documentElement;
      const isDark = htmlEl.classList.toggle('dark');"""

new_toggle = """function toggleColorTheme() {
      const htmlEl = document.documentElement;
      const isDark = htmlEl.classList.toggle('dark');
      htmlEl.setAttribute('data-theme', isDark ? 'dark' : 'light');"""

content = content.replace(old_toggle, new_toggle)

# 2. Gate 7a & CSS Gate 48 in component styles
content = content.replace(
    ".top-nav-btn.active {\n      color: #ffffff !important;",
    ".top-nav-btn.active {\n      color: var(--text-inverted) !important;"
)

content = content.replace(
    ".user-badge-avatar {\n      width: 24px;\n      height: 24px;\n      border-radius: var(--radius-sm);\n      background-color: var(--brand-blue);\n      color: #ffffff;",
    ".user-badge-avatar {\n      width: 24px;\n      height: 24px;\n      border-radius: var(--radius-sm);\n      background-color: var(--brand-blue);\n      color: var(--text-inverted);"
)

content = content.replace(
    ".btn-primary {\n      background-color: var(--brand-blue);\n      color: #ffffff !important;",
    ".btn-primary {\n      background-color: var(--brand-blue);\n      color: var(--text-inverted) !important;"
)

content = content.replace(
    ".btn-secondary.active {\n      background-color: var(--brand-blue);\n      color: #ffffff !important;",
    ".btn-secondary.active {\n      background-color: var(--brand-blue);\n      color: var(--text-inverted) !important;"
)

content = content.replace(
    ".sub-tab-btn.active {\n      color: #ffffff !important;",
    ".sub-tab-btn.active {\n      color: var(--text-inverted) !important;"
)

content = content.replace(
    "background-color: rgba(15, 23, 42, 0.70) !important;",
    "background-color: var(--modal-backdrop-bg) !important;"
)

content = content.replace(
    """        background: #ffffff !important;
        color: #000000 !important;""",
    """        background: var(--bg-surface) !important;
        color: var(--text-primary) !important;"""
)

# 3. Gate 54 (Eyebrows)
content = re.sub(
    r'<div class="text-xs font-mono uppercase text-\[#0284c7\] font-bold">Platform Architecture</div>\s*',
    '', content
)
content = re.sub(
    r'<div class="text-xs font-mono uppercase text-\[#0284c7\] font-bold">End-to-End Pipeline</div>\s*',
    '', content
)
content = re.sub(
    r'<div class="text-xs font-mono uppercase text-\[#0284c7\] font-bold">Statutory Requirement</div>\s*',
    '', content
)

# 4. Gate 48 HTML Arbitrary Classes & Inline Styles
content = content.replace("bg-[#0284c7]/10", "bg-sky-500/10")
content = content.replace("border-[#0284c7]/30", "border-sky-500/30")
content = content.replace("bg-[#0284c7]", "bg-sky-600")
content = content.replace("text-[#0284c7]", "text-sky-600")
content = content.replace("border-[#0284c7]", "border-sky-600")
content = content.replace("border-l-[#0284c7]", "border-l-sky-600")
content = content.replace("hover:border-[#0284c7]", "hover:border-sky-600")
content = content.replace("focus:border-[#0284c7]", "focus:border-sky-600")
content = content.replace("hover:border-[#4285F4]", "hover:border-blue-500")
content = content.replace("text-[#b91c1c]", "text-red-700")

# Canvases inline background style
content = content.replace(
    'class="w-full h-auto max-h-[440px] block" style="background:#090d16;"',
    'class="w-full h-auto max-h-[440px] block bg-slate-950"'
)
content = content.replace(
    'class="w-full h-auto max-h-[360px] block" style="background:#060a12;"',
    'class="w-full h-auto max-h-[360px] block bg-slate-950"'
)

# 5. Gate 67 Uppercase & Tracking
# Line 697: AICTE APH tag
content = content.replace(
    '<span class="hidden md:inline-block tag-neutral font-mono text-[10px] ml-1">AICTE APH 2026-27</span>',
    '<span class="hidden md:inline-block tag-neutral font-mono text-[10px] ml-1"><abbr title="Approval Process Handbook">AICTE APH 2026-27</abbr></span>'
)

# Line 810: userRoleBadge
content = content.replace(
    'id="userRoleBadge" class="text-[10px] text-tertiary uppercase font-mono tracking-wider font-semibold"',
    'id="userRoleBadge" class="text-[10px] text-tertiary font-mono font-semibold"'
)

# Lines 880-895: Architecture summary cards
content = content.replace('text-[11px] font-mono text-sub uppercase', 'text-[11px] font-mono text-sub')

# Lines 1128-1143: Stats bar cards
content = content.replace('text-[11px] uppercase font-bold tracking-wider text-secondary', 'text-[11px] font-bold text-secondary')

# Table headers (lines 1187, 1465, 1513, 7246)
content = content.replace('text-sub font-mono text-[11px] uppercase', 'text-sub font-mono text-[11px]')

# Line 1361
content = content.replace('text-[11px] font-mono uppercase text-sub mb-2', 'text-[11px] font-mono text-sub mb-2')

# Line 1554
content = content.replace('font-mono text-[11px] text-sub uppercase font-semibold', 'font-mono text-[11px] text-sub font-semibold')

# Lines 1627-1681: Terms/Privacy h2s
content = content.replace('text-xs font-bold uppercase text-main mb-1', 'text-xs font-bold text-main mb-1')

# Line 1848
content = content.replace('text-[10px] font-semibold text-sub uppercase tracking-wider mb-2', 'text-[10px] font-semibold text-sub mb-2')

# Line 1984
content = content.replace('text-[11px] font-mono uppercase text-sub', 'text-[11px] font-mono text-sub')

# Lines 6548-6560: Modal specs
content = content.replace('text-sub block text-[10px] uppercase font-semibold', 'text-sub block text-[10px] font-semibold')

# Line 6626
content = content.replace('text-[10px] uppercase font-bold text-sub tracking-wider', 'text-[10px] font-bold text-sub')

# Line 6688
content = content.replace('font-bold text-[11px] uppercase tracking-wider text-amber-800', 'font-bold text-[11px] text-amber-800')

# Lines 6750, 6787
content = content.replace('text-[10px] text-slate-500 uppercase font-bold tracking-wider', 'text-[10px] text-slate-500 font-bold')

# Lines 6756, 6793
content = content.replace('text-[10px] uppercase tracking-wider text-red-800', 'text-[10px] text-red-800')
content = content.replace('text-[10px] uppercase tracking-wider text-emerald-800', 'text-[10px] text-emerald-800')

# Line 6858
content = content.replace('text-xs font-bold uppercase tracking-wider text-slate-700', 'text-xs font-bold text-slate-700')

# Line 6938: All India Council
content = content.replace(
    'font-bold text-sm tracking-wide uppercase text-slate-900">ALL INDIA COUNCIL FOR TECHNICAL EDUCATION</div>',
    'font-bold text-sm text-slate-900">All India Council for Technical Education</div>'
)

# Lines 6971, 7024
content = content.replace('text-xs font-bold uppercase tracking-wider text-slate-800', 'text-xs font-bold text-slate-800')

# Lines 7168, 7187
content = content.replace('text-xs font-bold text-main uppercase tracking-wider', 'text-xs font-bold text-main')

# Save updated portal.html
with open('portal.html', 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated portal.html successfully.")
