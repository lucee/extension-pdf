# PDF Extension for Lucee

Update `CHANGELOG.md` when adding new features, bug fixes

## Test Approach

leave artifacts in place for visual inspection, cleanup before each test run

place all generated artifacts in a `generated/` subdirectory (ignored globally via `**/generated/` in `.gitignore`)

## Libraries used

- **cfdocument**: [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer) (HTML/CSS rendering) + [OpenPDF](https://github.com/LibrePDF/OpenPDF) (PDF output)
- **cfpdf**: [Apache PDFBox](https://pdfbox.apache.org/)

## Build and test

Build with Java 21:

```bash
mvn package
```

Run the tests with [script-runner](https://github.com/lucee/script-runner) against a Lucee 7.1+ checkout (`lucee/lucee`, the `test` folder is the webroot). Set `testAdditional` to this repo's `tests` folder; `B737ABC4-D43F-4D91-8E8E973E37C40D1B` is the Image extension, which the tests need:

```bash
export testLabels=pdf
export testAdditional="$PWD/tests"
export testFilter=            # optional, e.g. DocumentRendering,PDFWrite
ant -buildfile /path/to/script-runner/build.xml \
  -DluceeVersionQuery="7.1/snapshot/light" \
  -Dwebroot=/path/to/lucee/test \
  -Dexecute=/bootstrap-tests.cfm \
  -DextensionDir="$PWD/target" \
  -Dextensions="B737ABC4-D43F-4D91-8E8E973E37C40D1B"
```

CI (`.github/workflows/main.yml`) runs the same suite against the latest Lucee 7.1 and 8.0 builds.
