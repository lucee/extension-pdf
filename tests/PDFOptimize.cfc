component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" skip=true {

	// SKIP: action="optimize" not yet implemented
	// See: FEATURES.md - Medium effort
	// PDFBox: Remove attachments, bookmarks, metadata, JS, thumbnails

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFOptimize/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path );

		// Create a test PDF with various elements
		document fileName="#path#source.pdf" overwrite=true bookmark=true {
			writeOutput( "<h1 style='bookmark:level 1;'>Chapter 1</h1>" );
			writeOutput( "<p>Content for chapter 1</p>" );
			documentItem type="pagebreak";
			writeOutput( "<h1 style='bookmark:level 1;'>Chapter 2</h1>" );
			writeOutput( "<p>Content for chapter 2</p>" );
		}

		// Set some metadata
		pdf action="setInfo" source="#path#source.pdf" info={
			author: "Test Author",
			title: "Test Document",
			subject: "Testing optimize",
			keywords: "test, optimize, pdf"
		};
	}

	function run( testResults, testBox ) {
		describe( "cfpdf action=optimize", function() {

			it( title="optimize with noBookmarks removes bookmarks", body=function( currentSpec ) {
				fileCopy( "#path#source.pdf", "#path#nobookmarks.pdf" );

				pdf action="optimize" source="#path#nobookmarks.pdf" noBookmarks=true;

				// Verify bookmarks removed - extractBookmarks should return empty
				pdf action="extractBookmarks" source="#path#nobookmarks.pdf" name="local.bookmarks";
				expect( bookmarks ).toBeEmpty();
			});

			it( title="optimize with noMetadata removes document info", body=function( currentSpec ) {
				fileCopy( "#path#source.pdf", "#path#nometa.pdf" );

				pdf action="optimize" source="#path#nometa.pdf" noMetadata=true;

				pdf action="getInfo" source="#path#nometa.pdf" name="local.info";
				expect( info.author ?: "" ).toBeEmpty();
				expect( info.title ?: "" ).toBeEmpty();
			});

			it( title="optimize with noAttachments removes embedded files", body=function( currentSpec ) {
				// First add an attachment
				fileWrite( "#path#attach.txt", "test attachment" );
				pdf action="addAttachments" source="#path#source.pdf"
					destination="#path#with_attach.pdf" overwrite=true {
					pdfparam source="#path#attach.txt";
				}

				pdf action="optimize" source="#path#with_attach.pdf" noAttachments=true;

				// TODO: verify attachments removed
			});

			it( title="optimize with multiple options", body=function( currentSpec ) {
				fileCopy( "#path#source.pdf", "#path#multi_opt.pdf" );

				pdf action="optimize" source="#path#multi_opt.pdf"
					noBookmarks=true noMetadata=true noThumbnails=true;

				expect( isPDFFile( "#path#multi_opt.pdf" ) ).toBeTrue();
			});

			it( title="optimize reduces file size", body=function( currentSpec ) {
				fileCopy( "#path#source.pdf", "#path#smaller.pdf" );
				var originalSize = getFileInfo( "#path#smaller.pdf" ).size;

				pdf action="optimize" source="#path#smaller.pdf"
					noBookmarks=true noMetadata=true noThumbnails=true;

				var newSize = getFileInfo( "#path#smaller.pdf" ).size;
				// Optimized file should be smaller or equal
				expect( newSize ).toBeLTE( originalSize );
			});

			it( title="optimize with destination preserves original", body=function( currentSpec ) {
				var originalSize = getFileInfo( "#path#source.pdf" ).size;

				pdf action="optimize" source="#path#source.pdf"
					destination="#path#optimized.pdf" overwrite=true
					noBookmarks=true noMetadata=true;

				// Original unchanged
				expect( getFileInfo( "#path#source.pdf" ).size ).toBe( originalSize );
				// Optimized version exists
				expect( isPDFFile( "#path#optimized.pdf" ) ).toBeTrue();
			});

			it( title="optimize with algo attribute for image downsampling", body=function( currentSpec ) {
				// Create PDF with images
				document fileName="#path#with_images.pdf" overwrite=true {
					writeOutput( "<img src='data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==' width='100' height='100'>" );
				}

				pdf action="optimize" source="#path#with_images.pdf"
					destination="#path#downsampled.pdf" overwrite=true
					algo="bicubic" hscale=0.5 vscale=0.5;

				expect( isPDFFile( "#path#downsampled.pdf" ) ).toBeTrue();
			});

		});
	}

	function afterAll() {
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
	}
}
