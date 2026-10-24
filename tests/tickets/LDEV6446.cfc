component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	// the source image is SIZE x SIZE pixels, decoding it at full resolution needs at least SIZE*SIZE*3 bytes
	variables.SIZE = 5000;

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6446/generated/";
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		directoryCreate( variables.path, true, true );

		// a large image with some content, so it is not trivially compressible
		var img = imageNew( "", variables.SIZE, variables.SIZE, "rgb", "white" );
		imageSetDrawingColor( img, "blue" );
		for ( var i = 0; i < variables.SIZE; i += 250 ) {
			imageDrawLine( img, i, 0, variables.SIZE - i, variables.SIZE );
			imageDrawLine( img, 0, i, variables.SIZE, variables.SIZE - i );
		}
		imageWrite( img, variables.path & "large.jpg", 0.8, true );
		img = "";

		// first page holds the large image, scaled down to the page width
		document fileName="#variables.path#large-image.pdf" overwrite=true {
			writeOutput( '<img src="file://#variables.path#large.jpg" style="width:100%">' );
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6446 cfpdf action=thumbnail with a large page image", function() {

			it( title="creates the thumbnail without decoding the image at full resolution", body=function( currentSpec ) {
				var thumbDir = variables.path & "thumbs/";
				var oldGen = getOldGenPools();
				expect( arrayLen( oldGen ) ).toBeGT( 0, "no old generation heap memory pool found" );

				// make sure the PDF was rendered with the image in it
				pdf action="getInfo" source="#variables.path#large-image.pdf" name="local.info";
				expect( info.totalPages ).toBe( 1 );

				var before = 0;
				for ( var pool in oldGen ) {
					pool.resetPeakUsage();
					before += pool.getUsage().getUsed();
				}

				pdf action="thumbnail" source="#variables.path#large-image.pdf" destination="#thumbDir#" overwrite=true;

				var peak = 0;
				for ( var pool in oldGen ) {
					peak += pool.getPeakUsage().getUsed();
				}

				var files = directoryList( thumbDir, false, "name", "*.jpg" );
				expect( arrayLen( files ) ).toBe( 1 );
				var thumb = imageRead( thumbDir & files[ 1 ] );
				expect( imageGetWidth( thumb ) ).toBeLT( 500 );
				expect( imageGetHeight( thumb ) ).toBeLT( 500 );

				// a full resolution decode of the page image puts a raster of at least SIZE*SIZE*3 bytes on the heap
				var fullRaster = variables.SIZE * variables.SIZE * 3;
				var growth = peak - before;
				expect( growth ).toBeLT( fullRaster, "old generation heap grew by [#numberFormat( growth / 1024 / 1024 )#MB] while creating the thumbnail, a full resolution decode of the page image needs [#numberFormat( fullRaster / 1024 / 1024 )#MB]" );
			});

		});
	}

	private array function getOldGenPools() {
		var pools = [];
		var MemoryType = createObject( "java", "java.lang.management.MemoryType" );
		for ( var pool in createObject( "java", "java.lang.management.ManagementFactory" ).getMemoryPoolMXBeans() ) {
			if ( pool.getType().equals( MemoryType.HEAP ) && ( findNoCase( "old", pool.getName() ) || findNoCase( "tenured", pool.getName() ) ) ) {
				arrayAppend( pools, pool );
			}
		}
		return pools;
	}

	function afterAll() {
		// leave artifacts for inspection
	}
}
