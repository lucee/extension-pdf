component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	// action="transform" - rotate/scale pages
	// PDFBox: PDPage.setRotation(), media box scaling

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFTransform/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path );

		// Create a test PDF
		document fileName="#path#source.pdf" overwrite=true {
			writeOutput( "<h1>Transform Test</h1><p>This page should be rotated.</p>" );
		}
	}

	function run( testResults, testBox ) {
		describe( "cfpdf action=transform", function() {

			it( title="rotate page 90 degrees", body=function( currentSpec ) {
				pdf action="transform" source="#path#source.pdf" rotation=90
					destination="#path#rotated90.pdf" overwrite=true;

				expect( isPDFFile( "#path#rotated90.pdf" ) ).toBeTrue();
				// TODO: verify rotation via getInfo or PDFBox
			});

			it( title="rotate page 180 degrees", body=function( currentSpec ) {
				pdf action="transform" source="#path#source.pdf" rotation=180
					destination="#path#rotated180.pdf" overwrite=true;

				expect( isPDFFile( "#path#rotated180.pdf" ) ).toBeTrue();
			});

			it( title="rotate page 270 degrees", body=function( currentSpec ) {
				pdf action="transform" source="#path#source.pdf" rotation=270
					destination="#path#rotated270.pdf" overwrite=true;

				expect( isPDFFile( "#path#rotated270.pdf" ) ).toBeTrue();
			});

			it( title="rotate specific pages only", body=function( currentSpec ) {
				// First create a multi-page PDF
				document fileName="#path#multipage.pdf" overwrite=true {
					writeOutput( "<h1>Page 1</h1>" );
					documentItem type="pagebreak";
					writeOutput( "<h1>Page 2</h1>" );
					documentItem type="pagebreak";
					writeOutput( "<h1>Page 3</h1>" );
				}

				pdf action="transform" source="#path#multipage.pdf" pages="2" rotation=90
					destination="#path#partial.pdf" overwrite=true;

				expect( isPDFFile( "#path#partial.pdf" ) ).toBeTrue();
			});

			it( title="scale page with hscale/vscale", body=function( currentSpec ) {
				pdf action="transform" source="#path#source.pdf" hscale=0.5 vscale=0.5
					destination="#path#scaled.pdf" overwrite=true;

				expect( isPDFFile( "#path#scaled.pdf" ) ).toBeTrue();
			});

			it( title="transform with name attribute returns PDF variable", body=function( currentSpec ) {
				pdf action="transform" source="#path#source.pdf" rotation=90 name="local.result";

				expect( isPDFObject( result ) ).toBeTrue();
			});

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
