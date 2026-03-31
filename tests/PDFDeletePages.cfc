component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFDeletePages/generated/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );

		// Create a 3-page PDF
		document fileName="#path#source.pdf" overwrite=true {
			writeOutput( "<h1>Page 1</h1>" );
			documentItem type="pagebreak";
			writeOutput( "<h1>Page 2</h1>" );
			documentItem type="pagebreak";
			writeOutput( "<h1>Page 3</h1>" );
		}
	}

	function run( testResults, testBox ) {
		describe( "cfpdf action=deletePages", function() {

			it( title="delete single page from PDF", body=function( currentSpec ) {
				fileCopy( "#path#source.pdf", "#path#test1.pdf" );

				pdf action="deletePages" source="#path#test1.pdf" pages="2";

				pdf action="getInfo" source="#path#test1.pdf" name="local.info";
				expect( info.totalPages ).toBe( 2 );
			});

			it( title="delete multiple pages from PDF", body=function( currentSpec ) {
				fileCopy( "#path#source.pdf", "#path#test2.pdf" );

				pdf action="deletePages" source="#path#test2.pdf" pages="1,3";

				pdf action="getInfo" source="#path#test2.pdf" name="local.info";
				expect( info.totalPages ).toBe( 1 );
			});

			it( title="delete page range from PDF", body=function( currentSpec ) {
				fileCopy( "#path#source.pdf", "#path#test3.pdf" );

				pdf action="deletePages" source="#path#test3.pdf" pages="1-2";

				pdf action="getInfo" source="#path#test3.pdf" name="local.info";
				expect( info.totalPages ).toBe( 1 );
			});

			it( title="delete pages with destination attribute", body=function( currentSpec ) {
				pdf action="deletePages" source="#path#source.pdf" pages="1" destination="#path#dest.pdf" overwrite=true;

				expect( isPDFFile( "#path#dest.pdf" ) ).toBeTrue();
				pdf action="getInfo" source="#path#dest.pdf" name="local.info";
				expect( info.totalPages ).toBe( 2 );
				// Original should be unchanged
				pdf action="getInfo" source="#path#source.pdf" name="local.origInfo";
				expect( origInfo.totalPages ).toBe( 3 );
			});

			it( title="delete pages with name attribute", body=function( currentSpec ) {
				pdf action="deletePages" source="#path#source.pdf" pages="1" name="local.result";

				expect( isPDFObject( result ) ).toBeTrue();
			});

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
