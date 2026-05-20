component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.dir = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentResourceHandler/generated/";
		if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
		directoryCreate( variables.dir, true, true );
	}

	function run( testResults, testBox ) {

		describe( "cfdocument resourceHandler attribute", function() {

			it( title="UDF resourceHandler receives URL and returns content", body=function( currentSpec ) {
				var fetchedUrls = [];
				var handler = function( url ) {
					fetchedUrls.append( arguments.url );
					if ( arguments.url contains "custom-image.png" ) {
						// Return a tiny 1x1 red PNG as binary
						var img = imageNew( "", 1, 1, "rgb", "red" );
						var tmpFile = variables.dir & "handler-img.png";
						imageWrite( img, tmpFile, true );
						return fileReadBinary( tmpFile );
					}
					return javacast( "null", "" );
				};

				document format="pdf" resourceHandler="#handler#" filename="#dir#udf_handler.pdf" overwrite=true {
					writeOutput( '<html><body><p>Hello</p><img src="http://example.com/custom-image.png" width="10" height="10"/></body></html>' );
				}

				expect( fileExists( "#dir#udf_handler.pdf" ) ).toBeTrue();
				expect( isPDFFile( "#dir#udf_handler.pdf" ) ).toBeTrue();
				// The handler should have been called with the image URL
				expect( fetchedUrls ).toInclude( "http://example.com/custom-image.png" );
			});

			it( title="UDF resourceHandler receives parsedUrl struct", body=function( currentSpec ) {
				var captured = {};
				var handler = function( url, parsedUrl ) {
					if ( arguments.url contains "test-resource.css" ) {
						captured = arguments.parsedUrl;
						return "body { color: red; }";
					}
					return javacast( "null", "" );
				};

				document format="pdf" resourceHandler="#handler#" filename="#dir#parsed_url.pdf" overwrite=true {
					writeOutput( '<html><head><link rel="stylesheet" href="https://cdn.example.com:8443/assets/test-resource.css?v=2&amp;min=true##anchor"/></head><body><p>Parsed</p></body></html>' );
				}

				expect( fileExists( "#dir#parsed_url.pdf" ) ).toBeTrue();
				expect( captured ).toHaveKey( "protocol" );
				expect( captured.protocol ).toBe( "https" );
				expect( captured.host ).toBe( "cdn.example.com" );
				expect( captured.port ).toBe( 8443 );
				expect( captured.path ).toBe( "/assets/test-resource.css" );
				expect( captured.query ).toInclude( "v=2" );
				expect( captured.fragment ).toBe( "anchor" );
			});

			it( title="UDF resourceHandler returning null falls through to default", body=function( currentSpec ) {
				var handler = function( url ) {
					return javacast( "null", "" );
				};

				document format="pdf" resourceHandler="#handler#" name="local.result" {
					writeOutput( '<html><body><p>Default fallthrough</p></body></html>' );
				}

				expect( isPDFObject( result ) ).toBeTrue();
			});

			it( title="Component resourceHandler with onResourceFetch method", body=function( currentSpec ) {
				var handlerCfc = new testAdditional.tags.document.ResourceHandlerCFC();

				document format="pdf" resourceHandler="#handlerCfc#" filename="#dir#cfc_handler.pdf" overwrite=true {
					writeOutput( '<html><body><p>CFC test</p><img src="http://example.com/cfc-image.png" width="10" height="10"/></body></html>' );
				}

				expect( fileExists( "#dir#cfc_handler.pdf" ) ).toBeTrue();
				expect( isPDFFile( "#dir#cfc_handler.pdf" ) ).toBeTrue();
				expect( handlerCfc.getFetchedUrls() ).toInclude( "http://example.com/cfc-image.png" );
				// Verify parsedUrl was passed to CFC handler
				var parsed = handlerCfc.getParsedUrls();
				expect( arrayLen( parsed ) ).toBeGT( 0 );
				var imgParsed = parsed[ 1 ];
				expect( imgParsed.protocol ).toBe( "http" );
				expect( imgParsed.host ).toBe( "example.com" );
			});

			it( title="resourceHandler works for src attribute fetching", body=function( currentSpec ) {
				var handlerCalled = false;
				var handler = function( url ) {
					handlerCalled = true;
					return '<html><body><p>Intercepted content</p></body></html>';
				};

				document format="pdf" resourceHandler="#handler#" src="http://example.com/page.html" filename="#dir#src_handler.pdf" overwrite=true;

				expect( fileExists( "#dir#src_handler.pdf" ) ).toBeTrue();
				expect( isPDFFile( "#dir#src_handler.pdf" ) ).toBeTrue();
				expect( handlerCalled ).toBeTrue();

				pdf action="extractText" source="#dir#src_handler.pdf" name="local.text";
				expect( text ).toInclude( "Intercepted content" );
			});

			it( title="works without resourceHandler (no regression)", body=function( currentSpec ) {
				document format="pdf" name="local.result" {
					writeOutput( '<html><body><p>No handler</p></body></html>' );
				}
				expect( isPDFObject( result ) ).toBeTrue();
			});

			it( title="external stylesheet supplied by handler actually applies", body=function( currentSpec ) {
				// display:none is observable via extractText — proves the CSS was both fetched AND applied,
				// not merely that the handler was called.
				var handler = function( url ) {
					if ( arguments.url contains "custom.css" ) {
						return ".hidden { display: none; } .visible { display: block; }";
					}
					return javacast( "null", "" );
				};

				document format="pdf" resourceHandler="#handler#" filename="#dir#css_applies.pdf" overwrite=true {
					writeOutput( '<html><head><link rel="stylesheet" href="http://example.com/custom.css"/></head><body>' );
					writeOutput( '<p class="visible">Visible Paragraph</p>' );
					writeOutput( '<p class="hidden">Hidden Paragraph</p>' );
					writeOutput( '</body></html>' );
				}

				pdf action="extractText" source="#dir#css_applies.pdf" name="local.text";
				expect( text ).toInclude( "Visible Paragraph" );
				expect( text ).notToInclude( "Hidden Paragraph", "external stylesheet display:none should hide content" );
			});

			it( title="@media print rules from external stylesheet apply", body=function( currentSpec ) {
				// OHTPDF should treat itself as a print medium. Base rules hide print-only and show screen-only;
				// @media print flips them. If print media is honoured we see the print-only text and NOT the screen-only text.
				var handler = function( url ) {
					if ( arguments.url contains "print.css" ) {
						return "
							.screen-only { display: block; }
							.print-only  { display: none; }
							@media print {
								.screen-only { display: none; }
								.print-only  { display: block; }
							}
						";
					}
					return javacast( "null", "" );
				};

				document format="pdf" resourceHandler="#handler#" filename="#dir#media_print.pdf" overwrite=true {
					writeOutput( '<html><head><link rel="stylesheet" href="http://example.com/print.css"/></head><body>' );
					writeOutput( '<p class="screen-only">Screen Only Text</p>' );
					writeOutput( '<p class="print-only">Print Only Text</p>' );
					writeOutput( '</body></html>' );
				}

				pdf action="extractText" source="#dir#media_print.pdf" name="local.text";
				expect( text ).toInclude( "Print Only Text", "@media print rules should apply when rendering to PDF" );
				expect( text ).notToInclude( "Screen Only Text", "@media print should override base screen rules" );
			});

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
