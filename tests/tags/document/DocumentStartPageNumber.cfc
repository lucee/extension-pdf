component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentStartPageNumber/generated/";
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		directoryCreate( variables.path, true, true );
	}

	private string function pageText( required string file, required numeric page ) {
		pdf action="extractText" source="#arguments.file#" pages="#arguments.page#" name="local.txt";
		return local.txt;
	}

	function run( testResults, testBox ) {

		describe( "cfdocument startPageNumber (LDEV-2910)", function() {

			it( title="default numbering still starts at 1", body=function( currentSpec ) {
				var file = path & "default.pdf";
				document format="pdf" filename="#file#" overwrite=true {
					documentItem type="footer" {
						writeOutput( 'pg #cfdocument.currentpagenumber# of #cfdocument.totalpagecount#' );
					}
					writeOutput( "<p>one</p>" );
					documentItem type="pagebreak";
					writeOutput( "<p>two</p>" );
				}
				expect( pageText( file, 1 ) ).toInclude( "pg 1 of 2" );
				expect( pageText( file, 2 ) ).toInclude( "pg 2 of 2" );
			});

			it( title="cfdocument.currentpagenumber in a footer starts at startPageNumber", body=function( currentSpec ) {
				var file = path & "footer_var.pdf";
				document format="pdf" filename="#file#" overwrite=true startPageNumber=5 {
					documentItem type="footer" {
						writeOutput( 'pg #cfdocument.currentpagenumber#' );
					}
					writeOutput( "<p>one</p>" );
					documentItem type="pagebreak";
					writeOutput( "<p>two</p>" );
					documentItem type="pagebreak";
					writeOutput( "<p>three</p>" );
				}
				expect( pageText( file, 1 ) ).toInclude( "pg 5" );
				expect( pageText( file, 2 ) ).toInclude( "pg 6" );
				expect( pageText( file, 3 ) ).toInclude( "pg 7" );
			});

			it( title="cfdocument.currentpagenumber in a header starts at startPageNumber", body=function( currentSpec ) {
				var file = path & "header_var.pdf";
				document format="pdf" filename="#file#" overwrite=true startPageNumber=10 {
					documentItem type="header" {
						writeOutput( 'hdr #cfdocument.currentpagenumber#' );
					}
					writeOutput( "<p>one</p>" );
					documentItem type="pagebreak";
					writeOutput( "<p>two</p>" );
				}
				expect( pageText( file, 1 ) ).toInclude( "hdr 10" );
				expect( pageText( file, 2 ) ).toInclude( "hdr 11" );
			});

			it( title="totalpagecount is the number of the last page when startPageNumber is used", body=function( currentSpec ) {
				var file = path & "total.pdf";
				document format="pdf" filename="#file#" overwrite=true startPageNumber=5 {
					documentItem type="footer" {
						writeOutput( 'Page #cfdocument.currentpagenumber# of #cfdocument.totalpagecount#' );
					}
					writeOutput( "<p>one</p>" );
					documentItem type="pagebreak";
					writeOutput( "<p>two</p>" );
					documentItem type="pagebreak";
					writeOutput( "<p>three</p>" );
				}
				expect( pageText( file, 1 ) ).toInclude( "Page 5 of 7" );
				expect( pageText( file, 3 ) ).toInclude( "Page 7 of 7" );
			});

			it( title="pdf-page-count shows the number of the last page when startPageNumber is used", body=function( currentSpec ) {
				var file = path & "css_count.pdf";
				document format="pdf" filename="#file#" overwrite=true startPageNumber=5 {
					documentItem type="footer" {
						writeOutput( 'Page <span class="pdf-page-number"></span> of <span class="pdf-page-count"></span>' );
					}
					writeOutput( "<p>one</p>" );
					documentItem type="pagebreak";
					writeOutput( "<p>two</p>" );
				}
				expect( pageText( file, 1 ) ).toInclude( "Page 5 of 6" );
				expect( pageText( file, 2 ) ).toInclude( "Page 6 of 6" );
			});

			it( title="the css page counter classes follow startPageNumber", body=function( currentSpec ) {
				var file = path & "css.pdf";
				document format="pdf" filename="#file#" overwrite=true startPageNumber=5 {
					documentItem type="footer" {
						writeOutput( 'pg <span class="pdf-page-number"></span>' );
					}
					writeOutput( "<p>one</p>" );
					documentItem type="pagebreak";
					writeOutput( "<p>two</p>" );
				}
				expect( pageText( file, 1 ) ).toInclude( "pg 5" );
				expect( pageText( file, 2 ) ).toInclude( "pg 6" );
			});

			it( title="evalAtPrint footer follows startPageNumber", body=function( currentSpec ) {
				var file = path & "evalatprint.pdf";
				document format="pdf" filename="#file#" overwrite=true startPageNumber=5 {
					documentItem type="footer" evalAtPrint=true {
						writeOutput( 'pg #cfdocument.currentpagenumber# of #cfdocument.totalpagecount#' );
					}
					writeOutput( "<p>one</p>" );
					documentItem type="pagebreak";
					writeOutput( "<p>two</p>" );
				}
				expect( pageText( file, 1 ) ).toInclude( "pg 5 of 6" );
				expect( pageText( file, 2 ) ).toInclude( "pg 6 of 6" );
			});

			it( title="sections: startPageNumber on cfdocument is continued through the sections", body=function( currentSpec ) {
				var file = path & "sections_continue.pdf";
				document format="pdf" filename="#file#" overwrite=true startPageNumber=3 {
					documentSection {
						documentItem type="footer" {
							writeOutput( 'pg #cfdocument.currentpagenumber#' );
						}
						writeOutput( "<p>s1 one</p>" );
						documentItem type="pagebreak";
						writeOutput( "<p>s1 two</p>" );
					}
					documentSection {
						documentItem type="footer" {
							writeOutput( 'pg #cfdocument.currentpagenumber#' );
						}
						writeOutput( "<p>s2 one</p>" );
					}
				}
				expect( pageText( file, 1 ) ).toInclude( "pg 3" );
				expect( pageText( file, 2 ) ).toInclude( "pg 4" );
				expect( pageText( file, 3 ) ).toInclude( "pg 5" );
			});

			it( title="sections: startPageNumber on cfdocumentsection restarts the numbering", body=function( currentSpec ) {
				var file = path & "sections_restart.pdf";
				document format="pdf" filename="#file#" overwrite=true {
					documentSection {
						documentItem type="footer" {
							writeOutput( 'pg #cfdocument.currentpagenumber#' );
						}
						writeOutput( "<p>s1 one</p>" );
						documentItem type="pagebreak";
						writeOutput( "<p>s1 two</p>" );
					}
					documentSection startPageNumber=20 {
						documentItem type="footer" {
							writeOutput( 'pg #cfdocument.currentpagenumber#' );
						}
						writeOutput( "<p>s2 one</p>" );
						documentItem type="pagebreak";
						writeOutput( "<p>s2 two</p>" );
					}
				}
				expect( pageText( file, 1 ) ).toInclude( "pg 1" );
				expect( pageText( file, 2 ) ).toInclude( "pg 2" );
				expect( pageText( file, 3 ) ).toInclude( "pg 20" );
				expect( pageText( file, 4 ) ).toInclude( "pg 21" );
			});

			it( title="sections: css page counter follows the section startPageNumber", body=function( currentSpec ) {
				var file = path & "sections_css.pdf";
				document format="pdf" filename="#file#" overwrite=true {
					documentSection startPageNumber=7 {
						documentItem type="footer" {
							writeOutput( 'pg <span class="pdf-page-number"></span>' );
						}
						writeOutput( "<p>s1 one</p>" );
						documentItem type="pagebreak";
						writeOutput( "<p>s1 two</p>" );
					}
					documentSection startPageNumber=50 {
						documentItem type="footer" {
							writeOutput( 'pg <span class="pdf-page-number"></span>' );
						}
						writeOutput( "<p>s2 one</p>" );
					}
				}
				expect( pageText( file, 1 ) ).toInclude( "pg 7" );
				expect( pageText( file, 2 ) ).toInclude( "pg 8" );
				expect( pageText( file, 3 ) ).toInclude( "pg 50" );
			});

			it( title="an invalid startPageNumber is rejected", body=function( currentSpec ) {
				expect( function() {
					document format="pdf" name="local.pdfData" startPageNumber=0 { writeOutput( "<p>x</p>" ); }
				}).toThrow( message="startPageNumber" );
				expect( function() {
					document format="pdf" name="local.pdfData" startPageNumber=2.5 { writeOutput( "<p>x</p>" ); }
				}).toThrow( message="startPageNumber" );
				expect( function() {
					document format="pdf" name="local.pdfData" {
						documentSection startPageNumber=-1 { writeOutput( "<p>x</p>" ); }
					}
				}).toThrow( message="startPageNumber" );
			});

		});
	}

}
