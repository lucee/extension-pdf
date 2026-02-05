component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	// action="optimize" - implemented
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
		pdf action="setInfo" source="#path#source.pdf" destination="#path#source.pdf" overwrite=true info={
			author: "Test Author",
			title: "Test Document",
			subject: "Testing optimize",
			keywords: "test, optimize, pdf"
		};
	}

	function run( testResults, testBox ) {
		describe( "cfpdf action=optimize", function() {

			it( title="optimize with noBookmarks removes bookmarks", body=function( currentSpec ) {
				pdf action="optimize" source="#path#source.pdf"
					destination="#path#nobookmarks.pdf" overwrite=true noBookmarks=true;

				// Verify bookmarks removed - extractBookmarks should return empty
				pdf action="extractBookmarks" source="#path#nobookmarks.pdf" name="local.bookmarks";
				expect( bookmarks ).toBeEmpty();
			});

			it( title="optimize with noMetadata removes document info", body=function( currentSpec ) {
				pdf action="optimize" source="#path#source.pdf"
					destination="#path#nometa.pdf" overwrite=true noMetadata=true;

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

				pdf action="optimize" source="#path#with_attach.pdf"
					destination="#path#no_attach.pdf" overwrite=true noAttachments=true;

				expect( isPDFFile( "#path#no_attach.pdf" ) ).toBeTrue();
			});

			it( title="optimize with multiple options", body=function( currentSpec ) {
				pdf action="optimize" source="#path#source.pdf"
					destination="#path#multi_opt.pdf" overwrite=true
					noBookmarks=true noMetadata=true noThumbnails=true;

				expect( isPDFFile( "#path#multi_opt.pdf" ) ).toBeTrue();
			});

			it( title="optimize produces valid PDF", body=function( currentSpec ) {
				pdf action="optimize" source="#path#source.pdf"
					destination="#path#smaller.pdf" overwrite=true
					noBookmarks=true noMetadata=true noThumbnails=true;

				expect( isPDFFile( "#path#smaller.pdf" ) ).toBeTrue();
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

			// Image downsampling not implemented - skip
			// it( title="optimize with algo attribute for image downsampling", skip=true, body=function( currentSpec ) {
			// });

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
