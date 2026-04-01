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
| OpenHTMLToPDF | 1.1.24 | HTML/CSS to PDF rendering |
| PDFBox | 3.0.5 | PDF manipulation (cfpdf actions) |
| jsoup | 1.18.3 | HTML parsing/cleanup |

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

Returns a query with columns: `name`, `signable`, `isSigned`

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
- `overwritedata` - overwrite existing field values (default: true)

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

### Improvements

- **Smaller Footprint**: Significantly reduced JAR size vs iText
- **No License Issues**: All open source libraries (Apache 2.0, LGPL)
- **Better CSS Support**: CSS 2.1 with some CSS3 support via OpenHTMLToPDF
- **Modern PDFBox**: PDFBox 3.x with improved performance and security

### Removed Features

- PD4ML engine and related classes
- Flying Saucer renderer (replaced by OpenHTMLToPDF)
- iText dependency (replaced by PDFBox)
