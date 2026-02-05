component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFWrite/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path );

		// Create a PDF in memory
		document name="variables.pdfVar" {
			writeOutput( "<h1>Test PDF Content</h1><p>This is test content for write action.</p>" );
		}
	}

	function run( testResults, testBox ) {
		describe( "cfpdf action=write", function() {

			it( title="write PDF variable to file", body=function( currentSpec ) {
				pdf action="write" source="pdfVar" destination="#path#written.pdf" overwrite=true;

				expect( isPDFFile( "#path#written.pdf" ) ).toBeTrue();
			});

			it( title="write PDF with overwrite=false should error on existing file", body=function( currentSpec ) {
				pdf action="write" source="pdfVar" destination="#path#nooverwrite.pdf" overwrite=true;

				expect( function() {
					pdf action="write" source="pdfVar" destination="#path#nooverwrite.pdf" overwrite=false;
				}).toThrow();
			});

			it( title="write preserves PDF content", body=function( currentSpec ) {
				pdf action="write" source="pdfVar" destination="#path#content.pdf" overwrite=true;

				pdf action="extractText" source="#path#content.pdf" name="local.text" type="string";
				expect( text ).toInclude( "Test PDF Content" );
			});

			it( title="write after modification preserves changes", body=function( currentSpec ) {
				// Add a header then write
				pdf action="addHeader" source="pdfVar" text="Added Header" name="local.modified";
				pdf action="write" source="modified" destination="#path#modified.pdf" overwrite=true;

				expect( isPDFFile( "#path#modified.pdf" ) ).toBeTrue();
			});

		});
	}

	function afterAll() {
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
	}
}
