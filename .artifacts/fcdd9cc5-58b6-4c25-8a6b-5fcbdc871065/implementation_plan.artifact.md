# Add Text Overlay to Saved Image Plan

Implement a text overlay on images saved to the gallery. The overlay will display the classification grade and the classification timestamp (date and time).

## Proposed Changes

### [Core Utilities]

#### [MODIFY] [Utils.kt](file:///C:/FOLDER_PRIBADI/Belajar/Programming/Android/AndroidProject/KlasifikasiGradeTembakau/app/src/main/java/com/alkindi/klasifikasigradetembakau/Utils.kt)
- Add `addTextToBitmap` private helper function to draw text on a `Bitmap`.
    - It will draw two lines of text at the bottom-left:
        1. **Grade Label** (e.g., "Grade A") - Large, Bold.
        2. **Timestamp** (e.g., "Waktu Klasifikasi: 07.00 / 16 September 2026") - Standard size.
    - Both will be black as per the user's reference image.
    - Text size and margins will scale relative to the image width to ensure consistency across different resolutions.
- Update `saveImageToGallery` to:
    1. Decode the source image `Uri` into a `Bitmap`.
    2. Format the current date using `SimpleDateFormat("HH.mm / dd MMMM yyyy", Locale("id", "ID"))`.
    3. Call `addTextToBitmap` to apply the overlay.
    4. Save the modified `Bitmap` to the gallery using `Bitmap.compress()`.

## Verification Plan

### Manual Verification
1. Run the app.
2. Perform an image classification.
3. Click **Simpan Hasil Klasifikasi**.
4. Check the gallery for the saved image.
5. Verify the image contains the text overlay at the bottom-left corner with:
    - The correct Grade label.
    - The "Waktu Klasifikasi: [time] / [date]" text in the requested format.
    - Correct styling (Black text, Bold Grade).
