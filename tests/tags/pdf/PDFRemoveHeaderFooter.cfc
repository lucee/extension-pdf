component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" skip=true {

	// SKIP: action="removeHeaderFooter" not yet implemented
	// See: FEATURES.md - Low priority, Hard effort
	// Requires content stream parsing - headers/footers have no standard location

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFRemoveHeaderFooter/generated/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );

		// Create a PDF with headers and footers
		document fileName="#path#source.pdf" overwrite=true {
			writeOutput( "<h1>Page 1</h1><p>Content</p>" );
			documentItem type="pagebreak";
			writeOutput( "<h1>Page 2</h1><p>More content</p>" );
		}

		// Add header/footer
		pdf action="addHeader" source="#path#source.pdf" text="Document Header - Page _PAGENUMBER";
		pdf action="addFooter" source="#path#source.pdf" text="Footer - _PAGENUMBER of _LASTPAGENUMBER";
	}

	function run( testResults, testBox ) {
		describe( "cfpdf action=removeHeaderFooter", function() {

			it( title="remove header and footer from all pages", body=function( currentSpec ) {
				pdf action="removeHeaderFooter" source="#path#source.pdf"
					destination="#path#clean.pdf" overwrite=true;

				expect( isPDFFile( "#path#clean.pdf" ) ).toBeTrue();
				// TODO: verify headers/footers actually removed
			});

			it( title="remove header/footer from specific pages", body=function( currentSpec ) {
				pdf action="removeHeaderFooter" source="#path#source.pdf" pages="1"
					destination="#path#partial.pdf" overwrite=true;

				expect( isPDFFile( "#path#partial.pdf" ) ).toBeTrue();
			});

			it( title="remove with name attribute", body=function( currentSpec ) {
				pdf action="removeHeaderFooter" source="#path#source.pdf" name="local.result";

				expect( isPDFObject( result ) ).toBeTrue();
			});

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
