import shutil, os

portal_src = 'portal.html'
portal_destinations = [
    'frontend/portal.html',
    'backend/src/main/resources/static/portal.html'
]

index_src = 'index.html'
index_destinations = [
    'frontend/index.html',
    'backend/src/main/resources/static/index.html',
    'landing.html',
    'frontend/landing.html',
    'backend/src/main/resources/static/landing.html',
    '../ciphercore-landing/index.html'
]

photo_src = 'lab_facility_photo.jpg'
photo_destinations = [
    'frontend/lab_facility_photo.jpg',
    'backend/src/main/resources/static/lab_facility_photo.jpg',
    '../ciphercore-landing/lab_facility_photo.jpg'
]

print("--- Syncing portal.html ---")
for dst in portal_destinations:
    if os.path.exists(os.path.dirname(dst)):
        shutil.copyfile(portal_src, dst)
        print(f"Synced {portal_src} -> {dst} ({os.path.getsize(dst)} bytes)")

print("\n--- Syncing index.html ---")
for dst in index_destinations:
    if os.path.exists(os.path.dirname(dst)):
        shutil.copyfile(index_src, dst)
        print(f"Synced {index_src} -> {dst} ({os.path.getsize(dst)} bytes)")

print("\n--- Syncing lab_facility_photo.jpg ---")
for dst in photo_destinations:
    if os.path.exists(os.path.dirname(dst)):
        shutil.copyfile(photo_src, dst)
        print(f"Synced {photo_src} -> {dst} ({os.path.getsize(dst)} bytes)")

print("\nAll mirrors synced successfully!")
