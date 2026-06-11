# Lucee PDF Extension - Changelog

## v3.0 - OpenHTMLToPDF

Major rewrite switching from Flying Saucer/iText to OpenHTMLToPDF + PDFBox 3.x.

### Breaking Changes

- **Engine Removed**: PD4ML engine no longer available (was commercial/proprietary)
- **Engine Replaced**: Flying Saucer + iText replaced with OpenHTMLToPDF + PDFBox 3.x
- **Java Requirement**: Requires Java 11+

### Library Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| OpenHTMLToPDF | 1.1.37 | HTML/CSS to PDF rendering |
| PDFBox | 3.0.7 | PDF manipulation (cfpdf actions) |
| jsoup | 1.22.1 | HTML parsing/cleanup |

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

- **Accurate page destinations**: Bookmarks now use OpenHTMLToPDF's native `<bookmarks>` support, pointing to exact rendered page positions instead of section start pages
- **Merge preserves bookmarks**: `cfpdf action="merge"` with `keepbookmark=true` now preserves and remaps bookmarks from all source PDFs, with correct page offsets
- **Page filtering**: Bookmarks pointing to excluded pages are automatically removed during merge
- **HTML heading bookmarks**: `htmlbookmark=true` creates bookmarks with correct per-page destinations

### cfdocument scale attribute

The `scale` attribute (1-100) now works, rendering content at the specified percentage of the page size.

```cfml
<cfdocument format="pdf" scale="50">
    <h1>Half-size content</h1>
</cfdocument>
```

### cfdocument resourceHandler attribute

Custom resource fetching for images, CSS, and other resources. Accepts a Component (with `onResourceFetch(url, parsedUrl)` method) or a UDF. Return binary/string content to use, or null to fall through to default fetching. Useful for session-protected resources or custom auth.

The handler receives two arguments:

- `url` (string) — the raw URL
- `parsedUrl` (struct) — with keys: `protocol`, `host`, `port`, `path`, `query`, `fragment`

```cfml
<!--- Component handler --->
<cfdocument format="pdf" resourceHandler="#new my.ResourceHandler()#">
    <img src="http://internal/session-image.png"/>
</cfdocument>

<!--- UDF handler with parsed URL --->
<cfdocument format="pdf" resourceHandler="#function( url, parsedUrl ) {
    if ( arguments.parsedUrl.host == "internal" ) {
        return myCustomFetch( arguments.url, session.authToken );
    }
}#">
    <img src="http://internal/protected.png"/>
</cfdocument>
```

### Improvements

- **Smaller Footprint**: Significantly reduced JAR size vs iText
- **No License Issues**: All open source libraries (Apache 2.0, LGPL)
- **Better CSS Support**: CSS 2.1 with some CSS3 support via OpenHTMLToPDF
- **Modern PDFBox**: PDFBox 3.x with improved performance and security
- **Self-closing HTML tags**: Unknown/custom tags are allowed to self-close for compatibility with real-world HTML
- **Removed legacy v2 dist jars**: Cleaned out old iText, Flying Saucer, TagSoup, and pre-release PDFBox jars
- **OpenHTMLToPDF logging bridge**: Replaced the JUL handler with a native `XRLogger` impl (`LuceeXRLogger`). Engine output now reaches Lucee's pdf log with SLF4J `{}` placeholders correctly substituted, throwables forwarded with stack traces, and per-call PageContext lookup so admin reconfig takes effect without restart.
- **Font load diagnostics**: When a font in a `fontdirectory` can't have its TTF family name read (corrupt file, etc.), `cfdocument` now logs a WARN to the `pdf` log with the file path and the family name it fell back to (the filename). Previously this silently degraded — users would set `fontdirectory`, the font lookup would later miss because the registered family name didn't match what they expected, and there was no signal at all. Successful registrations are logged at DEBUG.

### Bug Fixes

- **`cfdocumentitem type="footer"` only rendered on the last page**: The footer `<div>` was appended at the end of `<body>`, so OpenHTMLToPDF's `position: running(footer)` only picked it up *after* its source position — leaving every earlier page with no footer. Now prepended like the header, so the running element is declared before pagination begins and flows into every page's `@bottom-center` margin box. CSS page counters (`<span class="pdf-page-number">` / `pdf-page-count`, and the `{currentpagenumber}` / `{totalpagecount}` placeholders) now produce correct "Page X of Y" output on every page.

### New cfdocument Attributes

- **`debughtml`**: dump the fully-assembled HTML (after engine CSS injection, header/footer flow, and bookmark injection) that gets passed to OpenHTMLToPDF. Useful when a PDF renders unexpectedly and you want to see what the engine actually fed the renderer. `debughtml=true` writes a `.html` sidecar next to `filename`; `debughtml="path/to/dump.html"` writes to an explicit path. Requires `filename` to be set when used in boolean form.

### Removed Features

- PD4ML engine and related classes
- Flying Saucer renderer (replaced by OpenHTMLToPDF)
- iText dependency (replaced by PDFBox)
- `FontsJarExtractor` and `initDefaultFontDirectory()` — PD4ML-era bootstrap that scanned the classpath for a legacy `fonts.jar` and seeded `{lucee-config}/fonts`. The jar isn't shipped on Lucee 7.x so the call was a no-op. The font-directory fallback chain (`cfdocument fontdirectory` → `this.pdf.fontDirectory` → `{lucee-config}/fonts`) is unchanged; admins now populate `{lucee-config}/fonts` themselves if they want a default set.
- Type/engine selection machinery: `PDFDocument.TYPE_NONE`/`TYPE_PD4ML`/`TYPE_FS` constants, `PDFDocument.newInstance(int)`, `ApplicationSettings.getType()` and the `this.pdf.type` parsing block, and the `selectedType` field in the `cfdocument` tag. The `type` attribute on `cfdocument` and the `this.pdf.type` setting are still accepted (TLD compat) but silently ignored — OpenHTMLToPDF is the only engine.
