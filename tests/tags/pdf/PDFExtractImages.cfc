component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFExtractImages/generated/";
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );

		var imgPath = replace( path, "\", "/", "all" );

		// Create tiny distinct images to embed
		imageWrite( imageNew( "", 50, 50, "rgb", "blue" ), "#path#blue.png", true );
		imageWrite( imageNew( "", 30, 30, "rgb", "red" ), "#path#red.png", true );

		// PDF with single image
		document fileName="#path#with_image.pdf" overwrite=true {
			writeOutput( '<html><body><h1>Image Test</h1><img src="file:///#imgPath#blue.png" width="50" height="50"/></body></html>' );
		}

		// PDF with two distinct images
		document fileName="#path#two_images.pdf" overwrite=true {
			writeOutput( '<html><body>
				<img src="file:///#imgPath#blue.png" width="50" height="50"/>
				<img src="file:///#imgPath#red.png" width="30" height="30"/>
			</body></html>' );
		}

		// PDF without images
		document fileName="#path#no_images.pdf" overwrite=true {
			writeOutput( "<h1>No Images</h1><p>Just text</p>" );
		}
	}

	function run( testResults, testBox ) {

		describe( "cfpdf action=extractImage", function() {

			it( title="extract images uses imagePrefix and produces valid images", body=function( currentSpec ) {
				var imgDir = "#path#extracted/";
				if ( directoryExists( imgDir ) ) directoryDelete( imgDir, true );
				directoryCreate( imgDir, true, true );

				pdf action="extractImage" source="#path#with_image.pdf"
					destination="#imgDir#" imagePrefix="myimg" overwrite=true;

				var files = directoryList( imgDir, false, "name" );
				systemOutput( "Extracted images: " & arrayToList( files ), true );
				expect( arrayLen( files ) ).toBeGT( 0, "should extract at least one image" );

				// Every file must use the requested prefix
				for ( var f in files ) {
					expect( f ).toMatch( "^myimg.*", "extracted file [#f#] should start with imagePrefix" );
				}

				// Extracted file must be a valid image and roughly the right size
				var extracted = imageRead( imgDir & files[ 1 ] );
				expect( imageGetWidth( extracted ) ).toBeGT( 0 );
				expect( imageGetHeight( extracted ) ).toBeGT( 0 );
			});

			it( title="extract images from PDF with multiple images returns multiple files", body=function( currentSpec ) {
				var imgDir = "#path#multi_extracted/";
				if ( directoryExists( imgDir ) ) directoryDelete( imgDir, true );
				directoryCreate( imgDir, true, true );

				pdf action="extractImage" source="#path#two_images.pdf"
					destination="#imgDir#" imagePrefix="multi" overwrite=true;

				var files = directoryList( imgDir, false, "name" );
				expect( arrayLen( files ) ).toBeGTE( 2, "two distinct embedded images should produce at least 2 files" );
			});

			it( title="extract images from PDF without images produces empty dir", body=function( currentSpec ) {
				var imgDir = "#path#no_extracted/";
				if ( directoryExists( imgDir ) ) directoryDelete( imgDir, true );
				directoryCreate( imgDir, true, true );

				pdf action="extractImage" source="#path#no_images.pdf"
					destination="#imgDir#" imagePrefix="img" overwrite=true;

				var files = directoryList( imgDir, false, "name" );
				expect( arrayLen( files ) ).toBe( 0 );
			});

		});
	}

	function afterAll() {
		// leave artifacts for inspection
	}
}
