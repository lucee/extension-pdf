component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	// action="addStamp" - implemented (delegates to watermark)
	// Note: addStamp uses the same approach as addWatermark with image attribute
	// Standard stamp annotations with iconName are not yet supported

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFStamp/generated/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );

		// Create a multi-page test PDF
		document fileName="#path#source.pdf" overwrite=true {
			writeOutput( "<h1>Page 1</h1><p>Content for stamping</p>" );
			documentItem type="pagebreak";
			writeOutput( "<h1>Page 2</h1><p>More content</p>" );
			documentItem type="pagebreak";
			writeOutput( "<h1>Page 3</h1><p>Even more content</p>" );
		}

		// Create a simple stamp image (1x1 pixel PNG)
		variables.stampImageBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
		fileWrite( "#path#stamp.png", binaryDecode( stampImageBase64, "base64" ) );
	}

	function run( testResults, testBox ) {
		describe( "cfpdf action=addStamp", function() {

			it( title="add stamp image to PDF", body=function( currentSpec ) {
				pdf action="addStamp" source="#path#source.pdf"
					destination="#path#stamped.pdf" overwrite=true
					image="#path#stamp.png" position="50,50";

				expect( isPDFFile( "#path#stamped.pdf" ) ).toBeTrue();
			});

			it( title="add stamp with opacity", body=function( currentSpec ) {
				pdf action="addStamp" source="#path#source.pdf"
					destination="#path#stamp_opacity.pdf" overwrite=true
					image="#path#stamp.png" opacity=0.5;

				expect( isPDFFile( "#path#stamp_opacity.pdf" ) ).toBeTrue();
			});

			it( title="add stamp with name attribute", body=function( currentSpec ) {
				pdf action="addStamp" source="#path#source.pdf" name="local.stamped"
					image="#path#stamp.png";

				expect( isPDFObject( stamped ) ).toBeTrue();
			});

			// Standard stamp annotations not yet implemented - skip
			// it( title="add stamp with iconName", skip=true, body=function( currentSpec ) {
			// });

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
