component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentHeaderFooter/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path );
	}

	function run( testResults, testBox ) {
		describe( "cfdocument headers", function() {

			it( title="document with header", body=function( currentSpec ) {
				document format="pdf" filename="#path#with_header.pdf" overwrite=true {
					documentItem type="header" {
						writeOutput( "<div style='text-align:center;'>Document Header</div>" );
					}
					writeOutput( "<h1>Content</h1><p>This document has a header.</p>" );
				}

				expect( isPDFFile( "#path#with_header.pdf" ) ).toBeTrue();
			});

			it( title="header appears on all pages", body=function( currentSpec ) {
				document format="pdf" filename="#path#header_multipage.pdf" overwrite=true {
					documentItem type="header" {
						writeOutput( "<div>Page Header</div>" );
					}
					writeOutput( "<h1>Page 1</h1>" );
					documentItem type="pagebreak";
					writeOutput( "<h1>Page 2</h1>" );
					documentItem type="pagebreak";
					writeOutput( "<h1>Page 3</h1>" );
				}

				expect( isPDFFile( "#path#header_multipage.pdf" ) ).toBeTrue();
				pdf action="getInfo" source="#path#header_multipage.pdf" name="local.info";
				expect( info.totalPages ).toBe( 3 );
			});

		});

		describe( "cfdocument footers", function() {

			it( title="document with footer", body=function( currentSpec ) {
				document format="pdf" filename="#path#with_footer.pdf" overwrite=true {
					documentItem type="footer" {
						writeOutput( "<div style='text-align:center;'>Document Footer</div>" );
					}
					writeOutput( "<h1>Content</h1><p>This document has a footer.</p>" );
				}

				expect( isPDFFile( "#path#with_footer.pdf" ) ).toBeTrue();
			});

			it( title="footer appears on all pages", body=function( currentSpec ) {
				document format="pdf" filename="#path#footer_multipage.pdf" overwrite=true {
					documentItem type="footer" {
						writeOutput( "<div>Page Footer</div>" );
					}
					writeOutput( "<h1>Page 1</h1>" );
					documentItem type="pagebreak";
					writeOutput( "<h1>Page 2</h1>" );
				}

				expect( isPDFFile( "#path#footer_multipage.pdf" ) ).toBeTrue();
				pdf action="getInfo" source="#path#footer_multipage.pdf" name="local.info";
				expect( info.totalPages ).toBe( 2 );
			});

		});

		describe( "cfdocument header and footer combined", function() {

			it( title="document with both header and footer", body=function( currentSpec ) {
				document format="pdf" filename="#path#header_footer.pdf" overwrite=true {
					documentItem type="header" {
						writeOutput( "<div style='border-bottom:1px solid black;'>Header Content</div>" );
					}
					documentItem type="footer" {
						writeOutput( "<div style='border-top:1px solid black;'>Footer Content</div>" );
					}
					writeOutput( "<h1>Main Content</h1><p>This document has both header and footer.</p>" );
				}

				expect( isPDFFile( "#path#header_footer.pdf" ) ).toBeTrue();
			});

			it( title="header and footer with page numbers", body=function( currentSpec ) {
				document format="pdf" filename="#path#page_numbers.pdf" overwrite=true {
					documentItem type="header" {
						writeOutput( "<div>Report Title</div>" );
					}
					documentItem type="footer" evalAtPrint=true {
						writeOutput( "<div style='text-align:center;'>Page ##cfdocument.currentpagenumber## of ##cfdocument.totalpagecount##</div>" );
					}
					writeOutput( "<h1>Page 1 Content</h1>" );
					documentItem type="pagebreak";
					writeOutput( "<h1>Page 2 Content</h1>" );
					documentItem type="pagebreak";
					writeOutput( "<h1>Page 3 Content</h1>" );
				}

				expect( isPDFFile( "#path#page_numbers.pdf" ) ).toBeTrue();
				pdf action="getInfo" source="#path#page_numbers.pdf" name="local.info";
				expect( info.totalPages ).toBe( 3 );
			});

		});
	}

	function afterAll() {
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
	}
}
