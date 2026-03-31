component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFPageRanges/generated/";

		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		directoryCreate( variables.path, true, true );

		// Create a 5-page PDF
		document fileName="#path#source.pdf" overwrite=true {
			writeOutput( "<h1>Page 1</h1>" );
			documentItem type="pagebreak";
			writeOutput( "<h1>Page 2</h1>" );
			documentItem type="pagebreak";
			writeOutput( "<h1>Page 3</h1>" );
			documentItem type="pagebreak";
			writeOutput( "<h1>Page 4</h1>" );
			documentItem type="pagebreak";
			writeOutput( "<h1>Page 5</h1>" );
		}
	}

	function run( testResults, testBox ) {

		describe( "cfpdf page range syntax", function() {

			it( title="explicit range 1-3 deletes correctly", body=function( currentSpec ) {
				fileCopy( "#path#source.pdf", "#path#explicit_range.pdf" );

				pdf action="deletePages" source="#path#explicit_range.pdf" pages="1-3";

				pdf action="getInfo" source="#path#explicit_range.pdf" name="local.info";
				expect( info.totalPages ).toBe( 2 );
			});

			it( title="open-ended range '3-' means page 3 to last page", body=function( currentSpec ) {
				// BUG ##5: setPages(-1) passes -1 as lastPageNumber to parsePageDefinition,
				// so "3-" resolves to range 3..-1 which produces nothing
				fileCopy( "#path#source.pdf", "#path#open_end.pdf" );

				pdf action="deletePages" source="#path#open_end.pdf" pages="3-";

				pdf action="getInfo" source="#path#open_end.pdf" name="local.info";
				// Should have deleted pages 3,4,5 leaving pages 1,2
				expect( info.totalPages ).toBe( 2, "pages='3-' should delete from page 3 to the last page" );
			});

			it( title="open-ended range '-3' means page 1 to page 3", body=function( currentSpec ) {
				fileCopy( "#path#source.pdf", "#path#open_start.pdf" );

				pdf action="deletePages" source="#path#open_start.pdf" pages="-3";

				pdf action="getInfo" source="#path#open_start.pdf" name="local.info";
				// Should have deleted pages 1,2,3 leaving pages 4,5
				expect( info.totalPages ).toBe( 2, "pages='-3' should delete from page 1 to page 3" );
			});

			it( title="single page number works", body=function( currentSpec ) {
				fileCopy( "#path#source.pdf", "#path#single_page.pdf" );

				pdf action="deletePages" source="#path#single_page.pdf" pages="2";

				pdf action="getInfo" source="#path#single_page.pdf" name="local.info";
				expect( info.totalPages ).toBe( 4 );
			});

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
