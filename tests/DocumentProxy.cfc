component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function run( testResults, testBox ) {

		describe( "cfdocument proxy settings", function() {

			it( title="invalid proxy server causes connection error", body=function( currentSpec ) {
				// Using a bogus proxy should fail when fetching a remote URL
				expect( function() {
					document format="pdf" src="http://example.com"
						proxyserver="127.0.0.1" proxyport="19999"
						name="local.result";
				}).toThrow();
			});

			it( title="proxy is not used for local content", body=function( currentSpec ) {
				// Inline HTML doesn't fetch anything, so proxy settings should be irrelevant
				document format="pdf" proxyserver="127.0.0.1" proxyport="19999" name="local.result" {
					writeOutput( "<p>local content</p>" );
				}
				expect( isPDFObject( result ) ).toBeTrue();
			});

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
