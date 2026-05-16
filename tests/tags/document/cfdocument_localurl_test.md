
# cfdocument localurl Test

This test (`cfdocument_localurl_test.cfm`) verifies that Lucee's `<cfdocument>` tag supports the `localurl` attribute, allowing local file references (such as images) to be embedded in generated PDFs.

## How it works

- The test generates a PDF using `<cfdocument localurl="true">`.
- It references a local image (`tests/LDEV1519/image.jpg`).
- After PDF generation, it checks if the PDF file was created successfully.

## Usage

1. Place the test file in the `tests/` directory.
2. Ensure `LDEV1519/image.jpg` exists (provided in repo).
3. Run the test on your Lucee server:
   - Access `tests/cfdocument_localurl_test.cfm` via your browser or Lucee admin.
4. The output will indicate if the PDF was created successfully.

## Notes

- For deeper validation, you may inspect the generated PDF to confirm the image is embedded.
- This test is intended for manual verification, but can be adapted for automated test harnesses.
