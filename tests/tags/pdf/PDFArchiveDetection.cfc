component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFArchiveDetection/generated/";

		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		directoryCreate( variables.path, true, true );

		// Create a normal (non-PDF/A) PDF
		document format="pdf" filename="#path#normal.pdf" overwrite=true {
			writeOutput( "<p>Normal PDF content</p>" );
		}
	}

	function run( testResults, testBox ) {

		describe( "IsPDFArchive function", function() {

			it( title="returns false for a normal PDF", body=function( currentSpec ) {
				expect( IsPDFArchive( "#path#normal.pdf" ) ).toBeFalse(
					"Normal PDFs should not be identified as PDF/A" );
			});

			it( title="returns false for a non-PDF file", body=function( currentSpec ) {
				var txtFile = path & "notapdf.txt";
				fileWrite( txtFile, "this is not a pdf" );
				expect( IsPDFArchive( txtFile ) ).toBeFalse();
			});

			it( title="returns false for non-existent file", body=function( currentSpec ) {
				expect( IsPDFArchive( "#path#doesnotexist.pdf" ) ).toBeFalse();
			});

		});

		describe( "getInfo PDFAVersion key", function() {

			it( title="normal PDF has empty PDFAVersion", body=function( currentSpec ) {
				pdf action="getInfo" source="#path#normal.pdf" name="local.info";
				expect( info.PDFAVersion ).toBe( "" );
			});

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
