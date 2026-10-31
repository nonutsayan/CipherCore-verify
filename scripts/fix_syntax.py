import re

def fix():
    with open('frontend/index.html', 'r', encoding='utf-8') as f:
        text = f.read()

    # Fix the missing quotes in showToastBanner
    text = re.sub(
        r'showToastBanner\(Cryptographic validation passed:[^\n\r]*',
        'showToastBanner("Cryptographic validation passed: All blocks verified with SHA-256 integrity.");',
        text
    )

    with open('frontend/index.html', 'w', encoding='utf-8') as f:
        f.write(text)

    with open('index.html', 'w', encoding='utf-8') as f:
        f.write(text)

    with open('backend/src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
        f.write(text)

    print("Fix script applied successfully.")

if __name__ == '__main__':
    fix()
