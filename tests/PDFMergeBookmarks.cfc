component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFMergeBookmarks/generated/";
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		directoryCreate( variables.path, true, true );

		// Create first PDF with bookmarks (2 pages)
		document format="pdf" bookmark=true filename="#path#doc1.pdf" overwrite=true {
			documentItem type="bookmark" name="Doc1 Chapter 1";
			writeOutput( "<h1>Document 1 - Chapter 1</h1><p>Content for chapter 1</p>" );
			documentItem type="pagebreak";
			documentItem type="bookmark" name="Doc1 Chapter 2";
			writeOutput( "<h1>Document 1 - Chapter 2</h1><p>Content for chapter 2</p>" );
		}

		// Create second PDF with bookmarks (3 pages)
		document format="pdf" bookmark=true filename="#path#doc2.pdf" overwrite=true {
			documentItem type="bookmark" name="Doc2 Intro";
			writeOutput( "<h1>Document 2 - Intro</h1><p>Introduction content</p>" );
			documentItem type="pagebreak";
			documentItem type="bookmark" name="Doc2 Body";
			writeOutput( "<h1>Document 2 - Body</h1><p>Body content</p>" );
			documentItem type="pagebreak";
			documentItem type="bookmark" name="Doc2 End";
			writeOutput( "<h1>Document 2 - End</h1><p>Ending content</p>" );
		}

		// Create a PDF without bookmarks
		document format="pdf" filename="#path#nobookmarks.pdf" overwrite=true {
			writeOutput( "<h1>No Bookmarks</h1><p>This PDF has no bookmarks</p>" );
		}

		// Verify source PDFs have bookmarks
		pdf action="extractBookmarks" source="#path#doc1.pdf" name="variables.doc1Bookmarks";
		pdf action="extractBookmarks" source="#path#doc2.pdf" name="variables.doc2Bookmarks";

		systemOutput( "doc1 bookmarks: #doc1Bookmarks.len()#", true );
		for ( var bm in doc1Bookmarks ) {
			systemOutput( "  source doc1: #bm.Title# page=#bm.PageNumber#", true );
		}
		systemOutput( "doc2 bookmarks: #doc2Bookmarks.len()#", true );
		for ( var bm in doc2Bookmarks ) {
			systemOutput( "  source doc2: #bm.Title# page=#bm.PageNumber#", true );
		}
	}

	function run( testResults, testBox ) {

		describe( "cfpdf merge bookmark preservation", function() {

			it( title="merge two PDFs preserves all bookmarks", body=function( currentSpec ) {
				pdf action="merge" keepbookmark=true
					source="#path#doc1.pdf,#path#doc2.pdf"
					destination="#path#merged_both.pdf" overwrite=true;

				pdf action="extractBookmarks" source="#path#merged_both.pdf" name="local.bookmarks";

				systemOutput( "merged bookmarks: #bookmarks.len()#", true );
				for ( var bm in bookmarks ) {
					systemOutput( "  merged: #bm.Title# page=#bm.PageNumber#", true );
				}

				expect( bookmarks ).toHaveLength( 5, "should have 2 from doc1 + 3 from doc2" );
				expect( bookmarks[1].get( "Title" ) ).toBe( "Doc1 Chapter 1" );
				expect( bookmarks[2].get( "Title" ) ).toBe( "Doc1 Chapter 2" );
				expect( bookmarks[3].get( "Title" ) ).toBe( "Doc2 Intro" );
				expect( bookmarks[4].get( "Title" ) ).toBe( "Doc2 Body" );
				expect( bookmarks[5].get( "Title" ) ).toBe( "Doc2 End" );
			});

			it( title="merged bookmark page numbers are offset correctly", body=function( currentSpec ) {
				pdf action="merge" keepbookmark=true
					source="#path#doc1.pdf,#path#doc2.pdf"
					destination="#path#merged_pages.pdf" overwrite=true;

				pdf action="extractBookmarks" source="#path#merged_pages.pdf" name="local.bookmarks";

				// doc1 chapter 1 on page 1, chapter 2 on page 2
				expect( bookmarks[1].get( "PageNumber" ) ).toBe( 1 );
				expect( bookmarks[2].get( "PageNumber" ) ).toBe( 2 );
				// doc2 bookmarks offset by doc1's 2 pages: pages 3, 4, 5
				expect( bookmarks[3].get( "PageNumber" ) ).toBe( 3, "doc2 first bookmark should be offset by doc1's page count" );
				expect( bookmarks[4].get( "PageNumber" ) ).toBe( 4 );
				expect( bookmarks[5].get( "PageNumber" ) ).toBe( 5 );
			});

			it( title="merge with keepbookmark=false strips bookmarks", body=function( currentSpec ) {
				pdf action="merge" keepbookmark=false
					source="#path#doc1.pdf,#path#doc2.pdf"
					destination="#path#merged_nobm.pdf" overwrite=true;

				pdf action="extractBookmarks" source="#path#merged_nobm.pdf" name="local.bookmarks";

				expect( bookmarks ).toHaveLength( 0, "bookmarks should be stripped" );
			});

			it( title="merge PDF with bookmarks and PDF without bookmarks", body=function( currentSpec ) {
				pdf action="merge" keepbookmark=true
					source="#path#doc1.pdf,#path#nobookmarks.pdf"
					destination="#path#merged_mixed.pdf" overwrite=true;

				pdf action="extractBookmarks" source="#path#merged_mixed.pdf" name="local.bookmarks";

				expect( bookmarks ).toHaveLength( 2, "should only have bookmarks from doc1" );
				expect( bookmarks[1].get( "Title" ) ).toBe( "Doc1 Chapter 1" );
				expect( bookmarks[2].get( "Title" ) ).toBe( "Doc1 Chapter 2" );
			});

			it( title="merge with cfpdfparam preserves bookmarks", body=function( currentSpec ) {
				pdf action="merge" keepbookmark=true
					destination="#path#merged_param.pdf" overwrite=true {
					pdfparam source="#path#doc1.pdf";
					pdfparam source="#path#doc2.pdf";
				}

				pdf action="extractBookmarks" source="#path#merged_param.pdf" name="local.bookmarks";

				expect( bookmarks ).toHaveLength( 5 );
			});

			it( title="merge with page filtering removes bookmarks for excluded pages", body=function( currentSpec ) {
				// Merge doc1 (only page 1) with doc2
				pdf action="merge" keepbookmark=true
					destination="#path#merged_filtered.pdf" overwrite=true {
					pdfparam source="#path#doc1.pdf" pages="1";
					pdfparam source="#path#doc2.pdf";
				}

				pdf action="extractBookmarks" source="#path#merged_filtered.pdf" name="local.bookmarks";

				systemOutput( "filtered merge bookmarks: #bookmarks.len()#", true );
				for ( var bm in bookmarks ) {
					systemOutput( "  filtered: #bm.Title# page=#bm.PageNumber#", true );
				}

				// doc1 chapter 2 (page 2) is filtered out, only chapter 1 (page 1) survives
				// doc2 bookmarks offset by 1 kept page from doc1
				expect( bookmarks ).toHaveLength( 4, "1 from doc1 + 3 from doc2" );
				expect( bookmarks[1].get( "Title" ) ).toBe( "Doc1 Chapter 1" );
				expect( bookmarks[1].get( "PageNumber" ) ).toBe( 1 );
				expect( bookmarks[2].get( "Title" ) ).toBe( "Doc2 Intro" );
				expect( bookmarks[2].get( "PageNumber" ) ).toBe( 2, "doc2 first bookmark offset by 1 kept page from doc1" );
			});

		});

	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
