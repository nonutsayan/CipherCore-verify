import base64
with open('lab_facility_photo.jpg', 'rb') as f:
    b64 = base64.b64encode(f.read()).decode('utf-8')
print('Base64 length:', len(b64))
with open('scripts/lab_photo_b64.js', 'w', encoding='utf-8') as out:
    out.write('window.LAB_FACILITY_PHOTO_B64 = "data:image/jpeg;base64,' + b64 + '";\n')
print('Saved scripts/lab_photo_b64.js')
