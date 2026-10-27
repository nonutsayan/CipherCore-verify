portal_files = [
    'portal.html',
    'frontend/portal.html',
    'backend/src/main/resources/static/portal.html'
]

for pf in portal_files:
    with open(pf, 'r', encoding='utf-8') as f:
        content = f.read()

    if 'window.rulesList = rulesList;' not in content:
        content = content.replace(
            '      { code: \'RULE-05\', category: \'Infrastructure Norm\', description: \'Flag computer workstation shortfalls or geo-coordinate deviations above 500 meters\', enabled: true }\n    ];',
            '      { code: \'RULE-05\', category: \'Infrastructure Norm\', description: \'Flag computer workstation shortfalls or geo-coordinate deviations above 500 meters\', enabled: true }\n    ];\n    window.rulesList = rulesList;'
        )

    with open(pf, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Exposed window.rulesList in {pf}")
