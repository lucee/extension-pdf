component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentRendering/generated/";
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );
	}

	function run( testResults, testBox ) {

		describe( "cfdocument basic rendering", function() {

			it( title="minimal HTML renders to valid PDF", body=function( currentSpec ) {
				document format="pdf" name="local.result" {
					writeOutput( "<p>Hello World</p>" );
				}
				expect( isPDFObject( result ) ).toBeTrue();
			});

			it( title="empty body renders to valid PDF", body=function( currentSpec ) {
				document format="pdf" name="local.result" {
					writeOutput( "<html><body></body></html>" );
				}
				expect( isPDFObject( result ) ).toBeTrue();
			});

			it( title="plain text without HTML tags", body=function( currentSpec ) {
				document format="pdf" name="local.result" {
					writeOutput( "Just plain text, no tags" );
				}
				expect( isPDFObject( result ) ).toBeTrue();
			});

		});

		describe( "cfdocument text content integrity", function() {

			it( title="headings and paragraphs preserved", body=function( currentSpec ) {
				document format="pdf" filename="#path#headings.pdf" overwrite=true {
					writeOutput( "<h1>Main Title</h1><h2>Subtitle</h2><p>Body text here</p>" );
				}

				pdf action="extractText" source="#path#headings.pdf" name="local.text";
				expect( text ).toInclude( "Main Title" );
				expect( text ).toInclude( "Subtitle" );
				expect( text ).toInclude( "Body text here" );
			});

			it( title="HTML entities rendered correctly", body=function( currentSpec ) {
				document format="pdf" filename="#path#entities.pdf" overwrite=true {
					writeOutput( "<p>Ampersand &amp; less-than &lt; greater-than &gt; mdash &##8212; copyright &##169;</p>" );
				}

				pdf action="extractText" source="#path#entities.pdf" name="local.text";
				expect( text ).toInclude( "&" );
				expect( text ).toInclude( "<" );
				expect( text ).toInclude( ">" );
				expect( text ).toInclude( chr( 8212 ), "mdash should be rendered" );
				expect( text ).toInclude( chr( 169 ), "copyright should be rendered" );
			});

			it( title="unicode characters preserved", body=function( currentSpec ) {
				var eAcute = chr( 233 );
				var iUmlaut = chr( 239 );
				var euro = chr( 8364 );
				var pound = chr( 163 );
				var registered = chr( 174 );
				document format="pdf" filename="#path#unicode.pdf" overwrite=true {
					writeOutput( '<html><head><meta charset="UTF-8"/></head><body>' );
					writeOutput( "<p>Accented: caf#eAcute#, na#iUmlaut#ve, r#eAcute#sum#eAcute#</p>" );
					writeOutput( "<p>Symbols: #euro# #pound# #registered#</p>" );
					writeOutput( "</body></html>" );
				}

				pdf action="extractText" source="#path#unicode.pdf" name="local.text";
				expect( text ).toInclude( "caf" & eAcute, "é should survive" );
				expect( text ).toInclude( "na" & iUmlaut & "ve", "ï should survive" );
				expect( text ).toInclude( euro, "€ should survive" );
				expect( text ).toInclude( pound, "£ should survive" );
				expect( text ).toInclude( registered, "® should survive" );
			});

			it( title="ordered and unordered lists", body=function( currentSpec ) {
				document format="pdf" filename="#path#lists.pdf" overwrite=true {
					writeOutput( "<ul><li>Bullet One</li><li>Bullet Two</li></ul>" );
					writeOutput( "<ol><li>Number One</li><li>Number Two</li></ol>" );
				}

				pdf action="extractText" source="#path#lists.pdf" name="local.text";
				expect( text ).toInclude( "Bullet One" );
				expect( text ).toInclude( "Bullet Two" );
				expect( text ).toInclude( "Number One" );
				expect( text ).toInclude( "Number Two" );
			});

			it( title="nested tables render all content", body=function( currentSpec ) {
				document format="pdf" filename="#path#nested_table.pdf" overwrite=true {
					writeOutput( '
						<table border="1">
							<tr><td>Outer Cell</td><td>
								<table border="1">
									<tr><td>Inner A</td><td>Inner B</td></tr>
								</table>
							</td></tr>
						</table>
					' );
				}

				pdf action="extractText" source="#path#nested_table.pdf" name="local.text";
				expect( text ).toInclude( "Outer Cell" );
				expect( text ).toInclude( "Inner A" );
				expect( text ).toInclude( "Inner B" );
			});

			it( title="HTML5 semantic elements rendered", body=function( currentSpec ) {
				document format="pdf" filename="#path#html5.pdf" overwrite=true {
					writeOutput( '
						<article>
							<header><h1>Article Title</h1></header>
							<section><p>Section content</p></section>
							<nav><p>Nav content</p></nav>
							<footer><p>Footer content</p></footer>
						</article>
					' );
				}

				pdf action="extractText" source="#path#html5.pdf" name="local.text";
				expect( text ).toInclude( "Article Title" );
				expect( text ).toInclude( "Section content" );
				expect( text ).toInclude( "Nav content" );
				expect( text ).toInclude( "Footer content" );
			});

		});

		describe( "cfdocument CSS rendering", function() {

			it( title="inline style block applied", body=function( currentSpec ) {
				document format="pdf" filename="#path#style_block.pdf" overwrite=true {
					writeOutput( '
						<html><head><style>
							.big { font-size: 24px; }
							.hidden { display: none; }
						</style></head><body>
							<p class="big">Visible Big Text</p>
							<p class="hidden">Hidden Text</p>
							<p>Normal Text</p>
						</body></html>
					' );
				}

				pdf action="extractText" source="#path#style_block.pdf" name="local.text";
				expect( text ).toInclude( "Visible Big Text" );
				expect( text ).toInclude( "Normal Text" );
				expect( text ).notToInclude( "Hidden Text" );
			});

			it( title="inline styles applied", body=function( currentSpec ) {
				document format="pdf" filename="#path#inline_style.pdf" overwrite=true {
					writeOutput( '<p style="display:none">Should Be Hidden</p>' );
					writeOutput( '<p>Should Be Visible</p>' );
				}

				pdf action="extractText" source="#path#inline_style.pdf" name="local.text";
				expect( text ).notToInclude( "Should Be Hidden" );
				expect( text ).toInclude( "Should Be Visible" );
			});

		});

		describe( "cfdocument page breaks", function() {

			it( title="CSS page-break-before forces new page", body=function( currentSpec ) {
				document format="pdf" filename="#path#page_break_before.pdf" overwrite=true {
					writeOutput( '<p>Page one content</p>' );
					writeOutput( '<p style="page-break-before: always;">Page two content</p>' );
				}

				pdf action="getInfo" source="#path#page_break_before.pdf" name="local.info";
				expect( info.totalPages ).toBe( 2 );

				pdf action="extractText" source="#path#page_break_before.pdf" name="local.text";
				expect( text ).toInclude( "Page one content" );
				expect( text ).toInclude( "Page two content" );
			});

			it( title="CSS page-break-after forces new page", body=function( currentSpec ) {
				document format="pdf" filename="#path#page_break_after.pdf" overwrite=true {
					writeOutput( '<p style="page-break-after: always;">Page one content</p>' );
					writeOutput( '<p>Page two content</p>' );
				}

				pdf action="getInfo" source="#path#page_break_after.pdf" name="local.info";
				expect( info.totalPages ).toBe( 2 );
			});

			it( title="multiple CSS page breaks", body=function( currentSpec ) {
				document format="pdf" filename="#path#multi_break.pdf" overwrite=true {
					writeOutput( '<div style="page-break-after: always;"><p>Page 1</p></div>' );
					writeOutput( '<div style="page-break-after: always;"><p>Page 2</p></div>' );
					writeOutput( '<p>Page 3</p>' );
				}

				pdf action="getInfo" source="#path#multi_break.pdf" name="local.info";
				expect( info.totalPages ).toBe( 3 );

				pdf action="extractText" source="#path#multi_break.pdf" name="local.text";
				expect( text ).toInclude( "Page 1" );
				expect( text ).toInclude( "Page 2" );
				expect( text ).toInclude( "Page 3" );
			});

		});

		describe( "cfdocument page settings", function() {

			it( title="letter page type produces 612x792 page", body=function( currentSpec ) {
				document format="pdf" pagetype="letter" filename="#path#letter.pdf" overwrite=true {
					writeOutput( "<p>Letter size</p>" );
				}
				pdf action="getInfo" source="#path#letter.pdf" name="local.info";
				expect( round( info.pagesize[ 1 ].width ) ).toBe( 612, "letter width should be 612 points" );
				expect( round( info.pagesize[ 1 ].height ) ).toBe( 792, "letter height should be 792 points" );
			});

			it( title="legal page type produces 612x1008 page", body=function( currentSpec ) {
				document format="pdf" pagetype="legal" filename="#path#legal.pdf" overwrite=true {
					writeOutput( "<p>Legal size</p>" );
				}
				pdf action="getInfo" source="#path#legal.pdf" name="local.info";
				expect( round( info.pagesize[ 1 ].width ) ).toBe( 612, "legal width should be 612 points" );
				expect( round( info.pagesize[ 1 ].height ) ).toBe( 1008, "legal height should be 1008 points" );
			});

			it( title="landscape orientation swaps width and height", body=function( currentSpec ) {
				document format="pdf" pagetype="letter" orientation="landscape" filename="#path#landscape.pdf" overwrite=true {
					writeOutput( "<p>Landscape content</p>" );
				}
				pdf action="getInfo" source="#path#landscape.pdf" name="local.info";
				expect( round( info.pagesize[ 1 ].width ) ).toBe( 792, "landscape letter width should be 792" );
				expect( round( info.pagesize[ 1 ].height ) ).toBe( 612, "landscape letter height should be 612" );
			});

			it( title="custom margins", body=function( currentSpec ) {
				document format="pdf" marginTop=2 marginBottom=2 marginLeft=2 marginRight=2 name="local.result" {
					writeOutput( "<p>Custom margins</p>" );
				}
				expect( isPDFObject( result ) ).toBeTrue();
			});

		});

		describe( "cfdocument images", function() {

			it( title="inline base64 data URI image", body=function( currentSpec ) {
				// 1x1 red PNG as base64
				var b64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
				document format="pdf" filename="#path#base64_img.pdf" overwrite=true {
					writeOutput( '<p>Before image</p><img src="data:image/png;base64,#b64#" width="10" height="10"/><p>After image</p>' );
				}

				expect( isPDFFile( "#path#base64_img.pdf" ) ).toBeTrue();

				pdf action="extractText" source="#path#base64_img.pdf" name="local.text";
				expect( text ).toInclude( "Before image" );
				expect( text ).toInclude( "After image" );
			});

			it( title="local file image", body=function( currentSpec ) {
				// Create a test image
				var img = imageNew( "", 20, 20, "rgb", "green" );
				imageWrite( img, "#path#test_img.png", true );
				var imgPath = replace( path, "\", "/", "all" );

				document format="pdf" filename="#path#local_img.pdf" overwrite=true {
					writeOutput( '<p>With local image</p><img src="file:///#imgPath#test_img.png" width="20" height="20"/>' );
				}

				expect( isPDFFile( "#path#local_img.pdf" ) ).toBeTrue();

				pdf action="extractText" source="#path#local_img.pdf" name="local.text";
				expect( text ).toInclude( "With local image" );
			});

			it( title="missing image does not crash rendering", body=function( currentSpec ) {
				document format="pdf" filename="#path#missing_img.pdf" overwrite=true {
					writeOutput( '<p>Before</p><img src="http://nonexistent.invalid/missing.png" width="10" height="10"/><p>After</p>' );
				}

				expect( isPDFFile( "#path#missing_img.pdf" ) ).toBeTrue();

				pdf action="extractText" source="#path#missing_img.pdf" name="local.text";
				expect( text ).toInclude( "Before" );
				expect( text ).toInclude( "After" );
			});

		});

		describe( "cfdocument HTML form elements create AcroFields", function() {

			it( title="text inputs become form fields", body=function( currentSpec ) {
				document format="pdf" filename="#path#form_fields.pdf" overwrite=true {
					writeOutput( '
						<form>
							<input type="text" name="username" value="testuser"/>
							<input type="text" name="email" value="test@example.com"/>
						</form>
					' );
				}

				pdfform action="read" source="#path#form_fields.pdf" result="local.fields";
				expect( fields ).toHaveKey( "username" );
				expect( fields.username ).toBe( "testuser" );
				expect( fields.email ).toBe( "test@example.com" );
			});

			// PDFBOX-5963: checkbox font with null name causes NPE during PDFMergerUtility.mergeDocuments()
			// Fixed on PDFBox trunk (2025-02-25), expected in 3.0.8
			it( title="checkbox inputs become form fields", skip=true, body=function( currentSpec ) {
				document format="pdf" filename="#path#form_checkbox.pdf" overwrite=true {
					writeOutput( '
						<form>
							<input type="checkbox" name="agree" checked=""/>
						</form>
					' );
				}

				pdfform action="read" source="#path#form_checkbox.pdf" result="local.fields";
				expect( fields ).toHaveKey( "agree" );
			});

		});

		describe( "cfdocument error handling", function() {

			it( title="scale=0 throws", body=function( currentSpec ) {
				expect( function() {
					document format="pdf" scale=0 name="local.result" {
						writeOutput( "<p>zero scale</p>" );
					}
				}).toThrow();
			});

			it( title="scale over 100 throws", body=function( currentSpec ) {
				expect( function() {
					document format="pdf" scale=101 name="local.result" {
						writeOutput( "<p>too big</p>" );
					}
				}).toThrow();
			});

		});

	}

	function afterAll() {
		// leave artifacts for inspection
	}
}
