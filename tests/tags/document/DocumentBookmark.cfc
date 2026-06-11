component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentBookmark/generated/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );
	}

	function run( testResults, testBox ) {

		describe( "cfdocumentitem type=bookmark", function() {

			it( title="single bookmark via documentItem", body=function( currentSpec ) {
				document format="pdf" filename="#path#single_bookmark.pdf" overwrite=true {
					documentItem type="bookmark" name="Chapter 1";
					writeOutput( "<h1>Chapter 1</h1><p>Content here</p>" );
				}

				pdf action="extractBookmarks" source="#path#single_bookmark.pdf" name="local.bookmarks";

				expect( isArray( bookmarks ) ).toBeTrue();
				expect( bookmarks ).toHaveLength( 1 );
				expect( bookmarks[1].get( "Title" ) ).toBe( "Chapter 1" );
			});

			it( title="multiple bookmarks via documentItem", body=function( currentSpec ) {
				document format="pdf" filename="#path#multi_bookmark.pdf" overwrite=true {
					documentItem type="bookmark" name="Introduction";
					writeOutput( "<h1>Introduction</h1><p>Intro content</p>" );
					documentItem type="pagebreak";
					documentItem type="bookmark" name="Methods";
					writeOutput( "<h1>Methods</h1><p>Methods content</p>" );
					documentItem type="pagebreak";
					documentItem type="bookmark" name="Conclusion";
					writeOutput( "<h1>Conclusion</h1><p>Conclusion content</p>" );
				}

				pdf action="extractBookmarks" source="#path#multi_bookmark.pdf" name="local.bookmarks";

				expect( isArray( bookmarks ) ).toBeTrue();
				expect( bookmarks ).toHaveLength( 3 );
				expect( bookmarks[1].get( "Title" ) ).toBe( "Introduction" );
				expect( bookmarks[1].get( "PageNumber" ) ).toBe( 1 );
				expect( bookmarks[2].get( "Title" ) ).toBe( "Methods" );
				expect( bookmarks[2].get( "PageNumber" ) ).toBe( 2 );
				expect( bookmarks[3].get( "Title" ) ).toBe( "Conclusion" );
				expect( bookmarks[3].get( "PageNumber" ) ).toBe( 3 );
			});

			it( title="bookmark requires name attribute", body=function( currentSpec ) {
				expect( function() {
					document format="pdf" name="local.result" {
						documentItem type="bookmark";
						writeOutput( "<p>No name</p>" );
					}
				}).toThrow();
			});

		});

		describe( "cfdocument bookmark attribute with HTML headings", function() {

			it( title="bookmark=true with htmlbookmark=true creates bookmarks from headings", body=function( currentSpec ) {
				document format="pdf" bookmark=true htmlbookmark=true filename="#path#html_bookmarks.pdf" overwrite=true {
					writeOutput( "<h1>First Heading</h1><p>Content</p>" );
					writeOutput( "<h1>Second Heading</h1><p>More content</p>" );
				}

				pdf action="extractBookmarks" source="#path#html_bookmarks.pdf" name="local.bookmarks";

				expect( isArray( bookmarks ) ).toBeTrue();
				expect( bookmarks ).toHaveLength( 2 );
				expect( bookmarks[1].get( "Title" ) ).toBe( "First Heading" );
				expect( bookmarks[2].get( "Title" ) ).toBe( "Second Heading" );
			});

			it( title="html heading bookmarks point to correct pages", body=function( currentSpec ) {
				document format="pdf" bookmark=true htmlbookmark=true filename="#path#html_bookmarks_pages.pdf" overwrite=true {
					writeOutput( "<h1>Chapter One</h1><p>Content</p>" );
					documentItem type="pagebreak";
					writeOutput( "<h1>Chapter Two</h1><p>More content</p>" );
					documentItem type="pagebreak";
					writeOutput( "<h1>Chapter Three</h1><p>Even more content</p>" );
				}

				pdf action="extractBookmarks" source="#path#html_bookmarks_pages.pdf" name="local.bookmarks";

				expect( bookmarks ).toHaveLength( 3 );
				expect( bookmarks[1].get( "Title" ) ).toBe( "Chapter One" );
				expect( bookmarks[1].get( "PageNumber" ) ).toBe( 1 );
				expect( bookmarks[2].get( "Title" ) ).toBe( "Chapter Two" );
				expect( bookmarks[2].get( "PageNumber" ) ).toBe( 2 );
				expect( bookmarks[3].get( "Title" ) ).toBe( "Chapter Three" );
				expect( bookmarks[3].get( "PageNumber" ) ).toBe( 3 );
			});

		});

		describe( "cfdocument htmlbookmark edge cases", function() {

			it( title="htmlbookmark=true alone (without bookmark=true) still creates heading bookmarks", body=function( currentSpec ) {
				document format="pdf" htmlbookmark=true filename="#path#html_only.pdf" overwrite=true {
					writeOutput( "<h1>Alone Heading</h1><p>Content</p>" );
				}

				pdf action="extractBookmarks" source="#path#html_only.pdf" name="local.bookmarks";
				expect( bookmarks ).toHaveLength( 1 );
				expect( bookmarks[ 1 ].get( "Title" ) ).toBe( "Alone Heading" );
			});

			it( title="htmlbookmark=false produces no bookmarks from headings", body=function( currentSpec ) {
				document format="pdf" bookmark=true htmlbookmark=false filename="#path#no_html_bm.pdf" overwrite=true {
					writeOutput( "<h1>Should Not Appear</h1><p>Body</p>" );
				}

				pdf action="extractBookmarks" source="#path#no_html_bm.pdf" name="local.bookmarks";
				expect( arrayLen( bookmarks ) ).toBe( 0 );
			});

			it( title="mixed heading levels (h1/h2/h3) all become bookmarks in document order", body=function( currentSpec ) {
				document format="pdf" htmlbookmark=true filename="#path#mixed_levels.pdf" overwrite=true {
					writeOutput( "<h1>Top</h1><p>a</p>" );
					writeOutput( "<h2>Middle</h2><p>b</p>" );
					writeOutput( "<h3>Deep</h3><p>c</p>" );
					writeOutput( "<h1>Top Again</h1><p>d</p>" );
				}

				pdf action="extractBookmarks" source="#path#mixed_levels.pdf" name="local.bookmarks";
				expect( bookmarks ).toHaveLength( 4 );
				expect( bookmarks[ 1 ].get( "Title" ) ).toBe( "Top" );
				expect( bookmarks[ 2 ].get( "Title" ) ).toBe( "Middle" );
				expect( bookmarks[ 3 ].get( "Title" ) ).toBe( "Deep" );
				expect( bookmarks[ 4 ].get( "Title" ) ).toBe( "Top Again" );
			});

			it( title="empty heading text is skipped", body=function( currentSpec ) {
				document format="pdf" htmlbookmark=true filename="#path#empty_heading.pdf" overwrite=true {
					writeOutput( "<h1>Real Heading</h1><p>a</p>" );
					writeOutput( "<h1></h1><p>b</p>" );
					writeOutput( "<h1>   </h1><p>c</p>" );
					writeOutput( "<h1>Another Real</h1><p>d</p>" );
				}

				pdf action="extractBookmarks" source="#path#empty_heading.pdf" name="local.bookmarks";
				expect( bookmarks ).toHaveLength( 2 );
				expect( bookmarks[ 1 ].get( "Title" ) ).toBe( "Real Heading" );
				expect( bookmarks[ 2 ].get( "Title" ) ).toBe( "Another Real" );
			});

			it( title="HTML entities in heading text are decoded in bookmark title", body=function( currentSpec ) {
				document format="pdf" htmlbookmark=true filename="#path#entity_heading.pdf" overwrite=true {
					writeOutput( "<h1>R&amp;D Department</h1><p>a</p>" );
					writeOutput( "<h1>Caf&##233; Menu</h1><p>b</p>" );
				}

				pdf action="extractBookmarks" source="#path#entity_heading.pdf" name="local.bookmarks";
				expect( bookmarks ).toHaveLength( 2 );
				expect( bookmarks[ 1 ].get( "Title" ) ).toBe( "R&D Department" );
				expect( bookmarks[ 2 ].get( "Title" ) ).toBe( "Caf" & chr( 233 ) & " Menu" );
			});

			it( title="inline markup inside heading is flattened to text", body=function( currentSpec ) {
				document format="pdf" htmlbookmark=true filename="#path#inline_heading.pdf" overwrite=true {
					writeOutput( "<h1>Bold <strong>here</strong> and <em>italic</em></h1><p>a</p>" );
				}

				pdf action="extractBookmarks" source="#path#inline_heading.pdf" name="local.bookmarks";
				expect( bookmarks ).toHaveLength( 1 );
				expect( bookmarks[ 1 ].get( "Title" ) ).toBe( "Bold here and italic" );
			});

			it( title="cfdocumentitem bookmark and htmlbookmark headings coexist", body=function( currentSpec ) {
				document format="pdf" htmlbookmark=true filename="#path#mixed_bm.pdf" overwrite=true {
					documentItem type="bookmark" name="Explicit One";
					writeOutput( "<h1>Heading One</h1><p>a</p>" );
					documentItem type="pagebreak";
					documentItem type="bookmark" name="Explicit Two";
					writeOutput( "<h1>Heading Two</h1><p>b</p>" );
				}

				pdf action="extractBookmarks" source="#path#mixed_bm.pdf" name="local.bookmarks";
				// 2 explicit + 2 heading = 4 bookmarks
				expect( bookmarks ).toHaveLength( 4 );
				var titles = [];
				for ( var bm in bookmarks ) titles.append( bm.get( "Title" ) );
				expect( titles ).toInclude( "Explicit One" );
				expect( titles ).toInclude( "Explicit Two" );
				expect( titles ).toInclude( "Heading One" );
				expect( titles ).toInclude( "Heading Two" );
			});

		});

		describe( "cfdocumentsection with bookmark attribute", function() {

			it( title="named sections create bookmarks", body=function( currentSpec ) {
				document format="pdf" bookmark=true filename="#path#section_bookmarks.pdf" overwrite=true {
					documentSection name="Section A" {
						writeOutput( "<p>Section A content</p>" );
					}
					documentSection name="Section B" {
						writeOutput( "<p>Section B content</p>" );
					}
				}

				pdf action="extractBookmarks" source="#path#section_bookmarks.pdf" name="local.bookmarks";

				expect( isArray( bookmarks ) ).toBeTrue();
				expect( bookmarks ).toHaveLength( 2 );
				expect( bookmarks[1].get( "Title" ) ).toBe( "Section A" );
				expect( bookmarks[2].get( "Title" ) ).toBe( "Section B" );
			});

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
