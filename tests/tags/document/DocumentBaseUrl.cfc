component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentBaseUrl/generated/";

		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		directoryCreate( variables.path, true, true );

		// Create test assets next to the HTML files
		variables.imgPath = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentBaseUrl/";
		if ( !directoryExists( variables.imgPath ) ) directoryCreate( variables.imgPath, true, true );

		// Create a 10x10 red PNG as test image
		var img = imageNew( "", 10, 10, "rgb", "red" );
		imageWrite( img, variables.imgPath & "test-image.png", true );

		// Write a CSS file
		fileWrite( variables.imgPath & "test-style.css", "body { color: red; } p.styled { font-size: 20px; }" );
	}

	function run( testResults, testBox ) {

		describe( "cfdocument base URL resolution for local files", function() {

			it( title="inline HTML with relative image and explicit basepath resolves correctly", body=function( currentSpec ) {
				// Using basepath attribute to set the base directory for resolving relative resources
				// This is the workaround for BUG ##2, but even basepath may fail due to the
				// Resource vs File check in getBaseUrl()
				var parentDir = variables.imgPath;

				document format="pdf" filename="#path#basepath_image.pdf" overwrite=true {
					writeOutput( '<html><body><p>Before image</p><img src="test-image.png" width="10" height="10"/><p>After image</p></body></html>' );
				}

				// Without a base URL, the relative image path can't resolve
				// The PDF should still be created (just without the image)
				expect( fileExists( "#path#basepath_image.pdf" ) ).toBeTrue();
				expect( isPDFFile( "#path#basepath_image.pdf" ) ).toBeTrue();
			});

			it( title="srcfile with relative image resolves from file location", body=function( currentSpec ) {
				// BUG ##2: getBaseUrl() checks (srcfile instanceof File) but Lucee Resources
				// don't extend java.io.File, so the base URL is not a proper file:// URI
				// and OpenHTMLToPDF can't resolve relative paths from it
				var htmlContent = '<html><body><p>Before image</p><img src="test-image.png" width="10" height="10"/><p>After image</p></body></html>';
				var htmlFile = variables.imgPath & "test-relative.html";
				fileWrite( htmlFile, htmlContent );

				// This should create a PDF with the image properly embedded
				document format="pdf" srcfile="#htmlFile#" filename="#path#relative_image.pdf" overwrite=true;

				expect( fileExists( "#path#relative_image.pdf" ) ).toBeTrue( "PDF file should be created from srcfile" );

				// Extract text to verify the PDF was generated from the HTML
				pdf action="extractText" source="#path#relative_image.pdf" name="local.text";
				expect( text ).toInclude( "Before image" );
			});

			it( title="srcfile with relative CSS resolves from file location", body=function( currentSpec ) {
				var htmlContent = '<html><head><link rel="stylesheet" href="test-style.css"/></head><body><p class="styled">Styled text</p></body></html>';
				var htmlFile = variables.imgPath & "test-relative-css.html";
				fileWrite( htmlFile, htmlContent );

				document format="pdf" srcfile="#htmlFile#" filename="#path#relative_css.pdf" overwrite=true;

				expect( fileExists( "#path#relative_css.pdf" ) ).toBeTrue( "PDF file should be created from srcfile with CSS" );

				pdf action="extractText" source="#path#relative_css.pdf" name="local.text";
				expect( text ).toInclude( "Styled text" );
			});

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
