component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFThumbnail/generated/";
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );

		// Create a multi-page source PDF
		document fileName="#path#source.pdf" overwrite=true {
			writeOutput( "<h1>Page 1</h1><p>Thumbnail test</p>" );
			documentItem type="pagebreak";
			writeOutput( "<h1>Page 2</h1><p>Second page</p>" );
			documentItem type="pagebreak";
			writeOutput( "<h1>Page 3</h1><p>Third page</p>" );
		}
	}

	function run( testResults, testBox ) {

		describe( "cfpdf action=thumbnail", function() {

			it( title="generate thumbnails for all pages", body=function( currentSpec ) {
				var thumbDir = "#path#thumbs_all/";
				pdf action="thumbnail" source="#path#source.pdf"
					destination="#thumbDir#" overwrite=true;

				expect( directoryExists( thumbDir ) ).toBeTrue();

				var files = directoryList( thumbDir, false, "name", "*.jpg" );
				expect( arrayLen( files ) ).toBe( 3 );
			});

			it( title="generate thumbnails for specific pages", body=function( currentSpec ) {
				var thumbDir = "#path#thumbs_pages/";
				pdf action="thumbnail" source="#path#source.pdf"
					destination="#thumbDir#" pages="1,3" overwrite=true;

				var files = directoryList( thumbDir, false, "name", "*.jpg" );
				expect( arrayLen( files ) ).toBe( 2 );
			});

			it( title="generate PNG thumbnails", body=function( currentSpec ) {
				var thumbDir = "#path#thumbs_png/";
				pdf action="thumbnail" source="#path#source.pdf"
					destination="#thumbDir#" format="png" overwrite=true;

				var files = directoryList( thumbDir, false, "name", "*.png" );
				expect( arrayLen( files ) ).toBe( 3 );
			});

			it( title="generate thumbnails with custom prefix", body=function( currentSpec ) {
				var thumbDir = "#path#thumbs_prefix/";
				pdf action="thumbnail" source="#path#source.pdf"
					destination="#thumbDir#" imagePrefix="page" overwrite=true;

				var files = directoryList( thumbDir, false, "name", "page*" );
				expect( arrayLen( files ) ).toBe( 3 );
			});

			it( title="larger scale produces larger thumbnails", body=function( currentSpec ) {
				// Default scale is 25; use 10 vs 100 to get a clear size difference
				var smallDir = "#path#thumbs_small/";
				var largeDir = "#path#thumbs_large/";
				pdf action="thumbnail" source="#path#source.pdf"
					destination="#smallDir#" scale=10 overwrite=true;
				pdf action="thumbnail" source="#path#source.pdf"
					destination="#largeDir#" scale=100 overwrite=true;

				var smallFiles = directoryList( smallDir, false, "name", "*.jpg" );
				var largeFiles = directoryList( largeDir, false, "name", "*.jpg" );
				expect( arrayLen( smallFiles ) ).toBe( 3 );
				expect( arrayLen( largeFiles ) ).toBe( 3 );

				var smallImg = imageRead( smallDir & smallFiles[ 1 ] );
				var largeImg = imageRead( largeDir & largeFiles[ 1 ] );
				expect( imageGetWidth( largeImg ) ).toBeGT( imageGetWidth( smallImg ), "scale=100 should be wider than scale=10" );
				expect( imageGetHeight( largeImg ) ).toBeGT( imageGetHeight( smallImg ), "scale=100 should be taller than scale=10" );
			});

			it( title="thumbnail with invalid scale throws", body=function( currentSpec ) {
				expect( function() {
					pdf action="thumbnail" source="#path#source.pdf"
						destination="#path#thumbs_fail/" scale=0;
				}).toThrow();
			});

		});
	}

	function afterAll() {
		// leave artifacts for inspection
	}
}
