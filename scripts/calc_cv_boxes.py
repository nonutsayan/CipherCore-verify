# Calculate realistic perspective bounding boxes for a 960x540 canvas based on 1376x768 photo

cw = 960
ch = 540

# Scale factors from 1376x768
sx = cw / 1376.0
sy = ch / 768.0

print(f'Scale factors: sx={sx:.3f}, sy={sy:.3f}')

# Let's define the 4 rows of student terminals in the lab photo:
# Row 1 (Foreground, largest): ~ y = 340 to 470
# Row 2 (Mid-front): ~ y = 260 to 330
# Row 3 (Mid-back): ~ y = 190 to 250
# Row 4 (Background): ~ y = 140 to 185
print('Calculated perspective rows')
