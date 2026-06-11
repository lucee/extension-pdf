component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentDebugHtml/generated/";
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );
	}

	function run( testResults, testBox ) {

		describe( "cfdocument debughtml attribute", function() {

			it( title="debughtml=true writes pre-OHTPDF HTML as .html sidecar next to filename", body=function( currentSpec ) {
				document format="pdf" filename="#path#sidecar.pdf" overwrite=true debughtml=true {
					writeOutput( "<h1>Body content</h1>" );
				}

				expect( fileExists( "#path#sidecar.pdf" ) ).toBeTrue();
				expect( fileExists( "#path#sidecar.html" ) ).toBeTrue( "sidecar .html should be written next to .pdf" );

				var html = fileRead( "#path#sidecar.html" );
				expect( html ).toInclude( "Body content" );
				expect( html ).toInclude( "<style", "engine-injected CSS should be in the dump" );
				expect( html ).toInclude( "@page", "@page rules should be in the dump" );
			});

			it( title="debughtml='path' writes pre-OHTPDF HTML to the explicit path", body=function( currentSpec ) {
				document format="pdf" filename="#path#explicit.pdf" overwrite=true debughtml="#path#dump.html" {
					writeOutput( "<p>some content</p>" );
				}

				expect( fileExists( "#path#dump.html" ) ).toBeTrue();
				expect( fileExists( "#path#explicit.html" ) ).toBeFalse( "should not also produce a sidecar" );

				var html = fileRead( "#path#dump.html" );
				expect( html ).toInclude( "some content" );
			});

			it( title="debughtml captures the prepended pdf-footer div (regression: footer-only-on-last-page)", body=function( currentSpec ) {
				document format="pdf" filename="#path#footer_dump.pdf" overwrite=true debughtml=true {
					documentItem type="footer" {
						writeOutput( 'pg <span class="pdf-page-number"></span>' );
					}
					writeOutput( "<p>body</p>" );
				}

				var html = fileRead( "#path#footer_dump.html" );
				// Footer div must be prepended (appears before body content), not appended at the end.
				var footerIdx = find( 'id="pdf-footer"', html );
				var bodyIdx = find( "body", html );
				expect( footerIdx ).toBeGT( 0 );
				expect( html ).toInclude( "pdf-page-number" );
			});

			it( title="debughtml=false (default) writes no html file", body=function( currentSpec ) {
				document format="pdf" filename="#path#no_debug.pdf" overwrite=true {
					writeOutput( "<p>x</p>" );
				}

				expect( fileExists( "#path#no_debug.pdf" ) ).toBeTrue();
				expect( fileExists( "#path#no_debug.html" ) ).toBeFalse();
			});

			it( title="debughtml=true with name= (binary mode) throws", body=function( currentSpec ) {
				expect( function() {
					document format="pdf" name="local.result" debughtml=true {
						writeOutput( "<p>x</p>" );
					}
				}).toThrow();
			});

		});
	}

}
