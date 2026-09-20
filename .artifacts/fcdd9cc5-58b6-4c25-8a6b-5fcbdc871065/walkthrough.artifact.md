# Save to Gallery with Text Overlay Walkthrough

I have updated the "Save to Gallery" functionality to include a text overlay on the saved image, showing the classification grade and timestamp.

## Changes Made

### Utility Enhancement
- **Text Overlay Helper**: Added `addTextToBitmap` in `Utils.kt`.
    - Draws the classification grade (e.g., "Grade A") in **bold black text**.
    - Draws the timestamp in the format: `Waktu Klasifikasi: HH.mm / dd MMMM yyyy` (Indonesian locale).
    - Scales text size and margins relative to the image width for consistent appearance across resolutions.
- **Save Logic Update**: Modified `saveImageToGallery` to:
    - Decode the image into a `Bitmap`.
    - Apply the text overlay.
    - Save the annotated bitmap as a JPEG to the gallery.

### UI Integration
- (Already implemented) The "Simpan Hasil Klasifikasi" button in `ClassifierResultActivity` triggers this process.

## Verification Results

### Automated Tests
- Build successful: `:app:assembleDebug`

### Manual Verification Required
1. Open the app and classify a tobacco image.
2. Tap **Simpan Hasil Klasifikasi**.
3. Verify the "Berhasil menyimpan gambar ke galeri" Toast.
4. Open the Gallery and check the `Pictures/KlasifikasiTembakau` folder.
5. **Check Overlay**: Ensure the image has "Grade [X]" and the timestamp at the bottom-left corner, exactly as in your reference image.

render_diffs(file:///C:/FOLDER_PRIBADI/Belajar/Programming/Android/AndroidProject/KlasifikasiGradeTembakau/app/src/main/java/com/alkindi/klasifikasigradetembakau/Utils.kt)
