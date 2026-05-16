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
				var handlerCfc = new testAdditional.ResourceHandlerCFC();

				document format="pdf" resourceHandler="#handlerCfc#" filename="#dir#cfc_handler.pdf" overwrite=true {
					writeOutput( '<html><body><p>CFC test</p><img src="http://example.com/cfc-image.png" width="10" height="10"/></body></html>' );
				}

				expect( fileExists( "#dir#cfc_handler.pdf" ) ).toBeTrue();
				expect( isPDFFile( "#dir#cfc_handler.pdf" ) ).toBeTrue();
				expect( handlerCfc.getFetchedUrls() ).toInclude( "http://example.com/cfc-image.png" );
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

			it( title="resourceHandler can inject CSS content", body=function( currentSpec ) {
				var handler = function( url ) {
					if ( arguments.url contains "custom.css" ) {
						return "body { color: red; } p { font-size: 24px; }";
					}
					return javacast( "null", "" );
				};

				document format="pdf" resourceHandler="#handler#" filename="#dir#css_handler.pdf" overwrite=true {
					writeOutput( '<html><head><link rel="stylesheet" href="http://example.com/custom.css"/></head><body><p>Styled via handler</p></body></html>' );
				}

				expect( fileExists( "#dir#css_handler.pdf" ) ).toBeTrue();
				expect( isPDFFile( "#dir#css_handler.pdf" ) ).toBeTrue();
			});

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
