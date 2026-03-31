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
				expect( bookmarks[2].get( "Title" ) ).toBe( "Methods" );
				expect( bookmarks[3].get( "Title" ) ).toBe( "Conclusion" );
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
