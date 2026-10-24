# Lucee PDF Extension - Changelog

## v3.0

Requires **Lucee 7.1+** and **Java 21**. `cfdocument` keeps the Flying Saucer renderer, updated to 10.3 and now on OpenPDF instead of iText 2. All `cfpdf`/`cfpdfform` work is done with PDFBox 3. The commercial PD4ML ("classic") engine is gone.

### Breaking Changes

- **Lucee 7.1+ and Java 21 required.** The extension is built for Jakarta EE (Tomcat 10+). For Lucee 6, use PDF Extension 1.0 or 2.0.
- **`type="classic"` (PD4ML) is now an error.** `cfdocument type="classic"`, `type="pd4ml"` and `this.pdf.type = "classic"` throw `PDF engine [classic] is no longer supported, please use [modern] instead.` In 2.x they rendered with PD4ML. `type="modern"` (or no type) is the only engine.
- **iText 2 removed.** The renderer now writes through OpenPDF 3. Code that reached into the old `com.lowagie` classes via `createObject` no longer works.
- **`cfpdf` without `action`** throws `attribute [action] is required for tag [cfpdf]` instead of silently doing nothing (LDEV-6654).

### Library Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| Flying Saucer | 10.3.0 | HTML/CSS rendering for `cfdocument` |
| OpenPDF | 3.0.5 | PDF output of the renderer, merge/concat, watermark/header/footer stamping |
| PDFBox | 3.0.7 | `cfpdf` / `cfpdfform` actions (text, images, forms, attachments, signatures, optimize, ...) |
| xmpbox | 3.0.7 | PDF/A detection (XMP metadata) |
| TagSoup | 1.2.1 | HTML clean-up before rendering |
| Bouncy Castle | 1.84 | encryption and signature validation |

### New cfpdf Actions

#### action="transform"
Scale PDF pages using `hscale` and `vscale` attributes.

```cfml
<cfpdf action="transform" source="input.pdf" destination="scaled.pdf" hscale="0.5" vscale="0.5">
```

#### action="addAttachments"
Attach files to a PDF document.

```cfml
<cfpdf action="addAttachments" source="doc.pdf" destination="with-attachments.pdf">
    <cfpdfparam file="attachment.txt">
</cfpdf>
```

#### action="extractAttachments"
Extract attached files from a PDF (Lucee extension - not available in ACF).

```cfml
<cfpdf action="extractAttachments" source="doc.pdf" directory="./attachments/">
```

#### action="removeAttachments"
Remove all attachments from a PDF.

```cfml
<cfpdf action="removeAttachments" source="doc.pdf" destination="clean.pdf">
```

#### action="readSignatureFields"
Read signature field information from a PDF.

```cfml
<cfpdf action="readSignatureFields" source="signed.pdf" name="fields">
```

Returns a query with columns: `name`, `signable`, `isSigned`, `certifiable`

#### action="validateSignature"
Validate digital signatures in a PDF.

```cfml
<cfpdf action="validateSignature" source="signed.pdf" name="result">
```

Returns a struct with:
- `hasSignatures` (boolean)
- `signatureCount` (numeric)
- `allValid` (boolean)
- `signatures` (array of signature details)

#### action="optimize"
Reduce PDF file size by removing specified elements.

```cfml
<cfpdf action="optimize" source="doc.pdf" destination="optimized.pdf"
    noBookmarks=true noMetadata=true noJavaScript=true noAttachments=true
    noThumbnails=true noComments=true noForms=true noLinks=true>
```

**Attributes:**
- `noBookmarks` - Remove document outline/bookmarks
- `noMetadata` - Remove document info (author, title, etc.)
- `noJavaScript` - Remove embedded JavaScript
- `noAttachments` - Remove embedded files
- `noThumbnails` - Remove page thumbnails
- `noComments` - Remove annotations/comments
- `noForms` - Remove form fields
- `noLinks` - Remove hyperlinks

#### action="sanitize"
Remove potentially dangerous elements for security.

```cfml
<cfpdf action="sanitize" source="untrusted.pdf" destination="safe.pdf">
```

Always removes: JavaScript, attachments, metadata, links with actions. Optionally removes forms with `noForms=true`.

#### action="addStamp"
Add a stamp image to PDF pages (uses same API as watermark).

```cfml
<cfpdf action="addStamp" source="doc.pdf" destination="stamped.pdf"
    image="stamp.png" position="50,50" opacity=0.8>
```

### New cfpdfform Tag

Read and populate PDF form fields.

#### action="read"
Extract form field values from a PDF.

```cfml
<cfpdfform action="read" source="form.pdf" result="fields">
<cfdump var="#fields#">

<!--- Or export as XML --->
<cfpdfform action="read" source="form.pdf" xmldata="xmlOutput">
```

#### action="populate"
Fill form fields with values.

```cfml
<cfpdfform action="populate" source="form.pdf" destination="filled.pdf">
    <cfpdfformparam name="firstName" value="John">
    <cfpdfformparam name="lastName" value="Doe">
</cfpdfform>

<!--- Or from XML --->
<cfpdfform action="populate" source="form.pdf" xmldata="data.xml" destination="filled.pdf">
```

**Attributes:**
- `source` - PDF file path or variable
- `destination` - output file path
- `name` - variable name to store result (returns PDF object)
- `result` - variable name for field values struct (read action)
- `xmldata` - XML file path, XML string, or XML object (both read and populate)
- `password` - PDF password if protected
- `overwrite` - overwrite destination file (default: false)
- `overwritedata` - overwrite existing field values (default: false)
- `flatten` - burn field values into page content, removes interactivity (default: false)

### New cfpdfformparam Tag

Child tag of cfpdfform for specifying form field values.

```cfml
<cfpdfformparam name="fieldName" value="fieldValue">
```

### IsPDFArchive / PDF/A Detection

`IsPDFArchive()` now checks XMP metadata for PDF/A conformance declarations (pdfaid:part) instead of just validating that the file is a PDF. Works with all PDF/A flavours (1a/1b, 2a/2b/2u, 3a/3b/3u, 4e/4f).

`getInfo()` now includes a `PDFAVersion` key — returns e.g. `"1b"`, `"2a"`, `"3b"`, or `""` if not PDF/A.

### Bookmarks

- **HTML heading bookmarks**: `htmlbookmark=true` creates the outline from `<h1>`–`<h6>`, using Flying Saucer's `<bookmarks>` support, so entries point to the rendered page.
- **Merge keeps bookmarks**: `cfpdf action="merge"` with `keepbookmark=true` keeps the bookmarks of all sources, shifts them by each source's page offset, and drops bookmarks to pages that are excluded.

### cfdocument

- **`scale`** (1–100) shrinks the content to that percentage while the page size stays as requested (LDEV-6641).
- **`resourceHandler`**: a Component (with `onResourceFetch( url, parsedUrl )`) or a UDF that can supply remote (http/https) resources, such as images, CSS and the `src` document. Return binary or string content to use it, or null to fall through to the default fetch. `parsedUrl` has the keys `protocol`, `host`, `port`, `path`, `query` and `fragment`. If the handler throws, the error is logged to the `pdf` log and the default fetch is used (LDEV-6640).

```cfml
<cfdocument format="pdf" resourceHandler="#function( url, parsedUrl ) {
    if ( arguments.parsedUrl.host == "internal" ) return myCustomFetch( arguments.url, session.authToken );
}#">
    <img src="http://internal/protected.png"/>
</cfdocument>
```

- **`debughtml`**: writes the fully assembled HTML that is passed to the renderer (after CSS injection, header/footer and bookmark injection). `debughtml=true` writes a `.html` file next to `filename`; `debughtml="path/to/dump.html"` writes to that path.
- **CSS in the document wins over the tag defaults**: the generated defaults (page size and margins from the tag attributes, page counters, header/footer running elements) are placed first in `<head>`, so `@page { size: …; margin: … }` and other document CSS override them (LDEV-6649).
- **Table headers repeat**: `<thead>` and `<tfoot>` are repeated on every page a table spans (LDEV-6650).
- **Remote fetch timeout**: the `src` document, images and stylesheets are fetched with a connect/read timeout of 30s. It can be changed with the system property `lucee.pdf.remote.timeout` or the env var `LUCEE_PDF_REMOTE_TIMEOUT` (milliseconds). Before, an unresponsive host could block the request forever (LDEV-6651).
- **Font load diagnostics**: a font in `fontdirectory` that can't be loaded is logged as a WARN to the `pdf` log instead of being ignored silently (LDEV-6655).

### Bug Fixes

- `cfpdf action="write" flatten=true` flattens form fields (LDEV-6643).
- `cfpdf action="merge" directory=… filter=…` applies the filter: `*` and `?` wildcards, `|` for several patterns, case-insensitive (LDEV-6644).
- `cfpdf` `extractText`, `thumbnail`, `extractImage` and PDF watermarks close the source document, so there are no more leaked file handles, and `extractText type="xml"` returns well-formed XML (LDEV-6642).
- `cfdocument saveAsName` is sanitised in the `Content-Disposition` header (CR/LF and quotes removed, RFC 5987 `filename*` for non-ASCII names) (LDEV-6646).
- `cfdocument filename` is checked against the security manager's file access, like `srcfile` (LDEV-6647).
- The header/footer template index is bounds checked for `evalAtPrint` headers/footers (LDEV-6653).
- `cfpdf action="thumbnail"` decodes page images near the thumbnail resolution instead of at full size, so a page with a large image no longer needs hundreds of MB of heap for a small thumbnail (LDEV-6446).

### Removed

- PD4ML engine and related classes (see Breaking Changes).
- iText 2 (replaced by OpenPDF 3 for the renderer and by PDFBox 3 for `cfpdf`).
- `FontsJarExtractor` / `initDefaultFontDirectory()`: these seeded `{lucee-config}/fonts` from a `fonts.jar` that is no longer shipped. The font directory fallback chain (`cfdocument fontdirectory` → `this.pdf.fontDirectory` → `{lucee-config}/fonts`) is unchanged; populate `{lucee-config}/fonts` yourself if you want a default set (LDEV-6656).
- The test-only functions `GetPDFLinkURIs`, `CountPDFLinkAnnotations` and `GetPDFFontNamesList`, which had been registered as BIFs in 3.0 snapshots (LDEV-6652).

### Not supported (known gaps)

- `cfpdf action="processDDX"` is not implemented.
- HTML `<input>` elements in `cfdocument` are not turned into AcroForm fields (Flying Saucer limitation). Use `cfpdfform action="populate"` on an existing form PDF instead.
