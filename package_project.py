import zipfile
import os
import shutil

src_dir = r'C:\Users\Anisa Shaikh\.gemini\antigravity\scratch\aicte-sih-portal'
zip_filename = 'aicte-sih-portal.zip'
zip_filepath = os.path.join(src_dir, zip_filename)

# Exclude patterns to keep zip clean and compact
exclude_dirs = {'.git', 'target', '.mvn', 'node_modules', '.idea', '__pycache__'}
exclude_files = {zip_filename, 'ciphercore_db.mv.db', 'ciphercore_db.trace.db', 'ciphercore_db.lock.db'}

print(f"Creating zip archive at {zip_filepath}...")
file_count = 0
with zipfile.ZipFile(zip_filepath, 'w', zipfile.ZIP_DEFLATED) as zipf:
    for root, dirs, files in os.walk(src_dir):
        # Exclude directories
        dirs[:] = [d for d in dirs if d not in exclude_dirs and not d.startswith('.git')]
        
        for file in files:
            if file in exclude_files or file.endswith('.tmp') or file.endswith('.log') or file.endswith('.exe'):
                continue
            full_path = os.path.join(root, file)
            rel_path = os.path.relpath(full_path, src_dir)
            try:
                zipf.write(full_path, rel_path)
                file_count += 1
            except Exception as e:
                print(f"Skipping {rel_path} due to error: {e}")

zip_size = os.path.getsize(zip_filepath)
print(f"Zip created successfully! Files: {file_count}, Size: {zip_size:,} bytes ({zip_size / (1024*1024):.2f} MB)")

# Copy to destinations
destinations = [
    os.path.join(r'C:\Users\Anisa Shaikh\OneDrive\Desktop', zip_filename),
    os.path.join(r'C:\Users\Anisa Shaikh\Downloads', zip_filename),
    os.path.join(r'C:\Users\Anisa Shaikh\.gemini\antigravity\brain\ea11f410-d472-43ab-a053-161d73c5371b', zip_filename)
]

for dest in destinations:
    try:
        shutil.copyfile(zip_filepath, dest)
        print(f"Copied zip to: {dest}")
    except Exception as e:
        print(f"Warning: Could not copy to {dest}: {e}")
