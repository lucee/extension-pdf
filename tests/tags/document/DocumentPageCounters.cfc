component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentPageCounters/generated/";
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );
	}

	function run( testResults, testBox ) {

		describe( "cfdocument CSS page counters via pdf-page-number / pdf-page-count", function() {

			it( title="footer pdf-page-number shows current page on each page", body=function( currentSpec ) {
				document format="pdf" filename="#path#footer_page_num.pdf" overwrite=true {
					documentItem type="footer" {
						writeOutput( 'pg <span class="pdf-page-number"></span>' );
					}
					writeOutput( "<p>page one body</p>" );
					documentItem type="pagebreak";
					writeOutput( "<p>page two body</p>" );
					documentItem type="pagebreak";
					writeOutput( "<p>page three body</p>" );
				}

				pdf action="extractText" source="#path#footer_page_num.pdf" pages="1" name="local.p1";
				pdf action="extractText" source="#path#footer_page_num.pdf" pages="2" name="local.p2";
				pdf action="extractText" source="#path#footer_page_num.pdf" pages="3" name="local.p3";

				expect( p1 ).toInclude( "pg 1" );
				expect( p2 ).toInclude( "pg 2" );
				expect( p3 ).toInclude( "pg 3" );
			});

			it( title="footer pdf-page-count shows total on every page", body=function( currentSpec ) {
				document format="pdf" filename="#path#footer_page_count.pdf" overwrite=true {
					documentItem type="footer" {
						writeOutput( 'total <span class="pdf-page-count"></span>' );
					}
					writeOutput( "<p>page one</p>" );
					documentItem type="pagebreak";
					writeOutput( "<p>page two</p>" );
					documentItem type="pagebreak";
					writeOutput( "<p>page three</p>" );
				}

				pdf action="extractText" source="#path#footer_page_count.pdf" pages="1" name="local.p1";
				pdf action="extractText" source="#path#footer_page_count.pdf" pages="3" name="local.p3";

				expect( p1 ).toInclude( "total 3" );
				expect( p3 ).toInclude( "total 3" );
			});

			it( title="combined Page X of Y in footer renders correctly across multiple pages", body=function( currentSpec ) {
				document format="pdf" filename="#path#page_x_of_y.pdf" overwrite=true {
					documentItem type="footer" {
						writeOutput( 'Page <span class="pdf-page-number"></span> of <span class="pdf-page-count"></span>' );
					}
					writeOutput( "<p>one</p>" );
					documentItem type="pagebreak";
					writeOutput( "<p>two</p>" );
					documentItem type="pagebreak";
					writeOutput( "<p>three</p>" );
				}

				pdf action="extractText" source="#path#page_x_of_y.pdf" pages="1" name="local.p1";
				pdf action="extractText" source="#path#page_x_of_y.pdf" pages="2" name="local.p2";
				pdf action="extractText" source="#path#page_x_of_y.pdf" pages="3" name="local.p3";

				expect( p1 ).toInclude( "Page 1 of 3" );
				expect( p2 ).toInclude( "Page 2 of 3" );
				expect( p3 ).toInclude( "Page 3 of 3" );
			});

			it( title="header pdf-page-number works the same as footer", body=function( currentSpec ) {
				document format="pdf" filename="#path#header_page_num.pdf" overwrite=true {
					documentItem type="header" {
						writeOutput( 'hdr <span class="pdf-page-number"></span>' );
					}
					writeOutput( "<p>first</p>" );
					documentItem type="pagebreak";
					writeOutput( "<p>second</p>" );
				}

				pdf action="extractText" source="#path#header_page_num.pdf" pages="1" name="local.p1";
				pdf action="extractText" source="#path#header_page_num.pdf" pages="2" name="local.p2";

				expect( p1 ).toInclude( "hdr 1" );
				expect( p2 ).toInclude( "hdr 2" );
			});

			it( title="single-page document shows page 1 and total 1", body=function( currentSpec ) {
				document format="pdf" filename="#path#single_page.pdf" overwrite=true {
					documentItem type="footer" {
						writeOutput( 'Page <span class="pdf-page-number"></span> of <span class="pdf-page-count"></span>' );
					}
					writeOutput( "<p>only page</p>" );
				}

				pdf action="extractText" source="#path#single_page.pdf" name="local.text";
				expect( text ).toInclude( "Page 1 of 1" );
			});

		});
	}

}
