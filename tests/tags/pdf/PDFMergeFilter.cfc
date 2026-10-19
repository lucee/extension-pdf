component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFMergeFilter/generated/";
		variables.src = path & "src/";
		if ( directoryExists( path ) ) directoryDelete( path, true );
		directoryCreate( src, true, true );

		// one page each, the page text is the file name
		for ( var name in [ "alpha.pdf", "beta.pdf", "gamma1.pdf", "x+(1).pdf", "xx1.pdf" ] ) {
			document fileName="#src##name#" overwrite=true {
				writeOutput( "<p>file #name#</p>" );
			}
		}
		fileWrite( src & "notes.txt", "not a pdf" );
	}

	private numeric function pageCount( required string file ) {
		pdf action="getInfo" source="#arguments.file#" name="local.info";
		return info.totalPages;
	}

	function run( testResults, testBox ) {
		describe( "cfpdf action=merge directory with filter", function() {

			it( title="*.pdf skips non pdf files", body=function( currentSpec ) {
				pdf action="merge" directory="#src#" filter="*.pdf" destination="#path#all.pdf" overwrite=true;
				expect( pageCount( "#path#all.pdf" ) ).toBe( 5 );
			});

			it( title="prefix wildcard selects matching files only", body=function( currentSpec ) {
				pdf action="merge" directory="#src#" filter="g*.pdf" destination="#path#g.pdf" overwrite=true;
				expect( pageCount( "#path#g.pdf" ) ).toBe( 1 );
				pdf action="extractText" source="#path#g.pdf" name="local.text" type="string";
				expect( text ).toInclude( "gamma1.pdf" );
			});

			it( title="? matches exactly one character", body=function( currentSpec ) {
				pdf action="merge" directory="#src#" filter="gamma?.pdf" destination="#path#q.pdf" overwrite=true;
				expect( pageCount( "#path#q.pdf" ) ).toBe( 1 );
			});

			it( title="multiple patterns separated by |", body=function( currentSpec ) {
				pdf action="merge" directory="#src#" filter="alpha.pdf|beta.pdf" destination="#path#ab.pdf" overwrite=true;
				expect( pageCount( "#path#ab.pdf" ) ).toBe( 2 );
			});

			it( title="matching is case insensitive", body=function( currentSpec ) {
				pdf action="merge" directory="#src#" filter="ALPHA.PDF" destination="#path#upper.pdf" overwrite=true;
				expect( pageCount( "#path#upper.pdf" ) ).toBe( 1 );
			});

			it( title="regex characters in the pattern are literal", body=function( currentSpec ) {
				// as a regex "x+(1).pdf" would match "xx1.pdf" and not "x+(1).pdf"
				pdf action="merge" directory="#src#" filter="x+(1).pdf" destination="#path#literal.pdf" overwrite=true;
				expect( pageCount( "#path#literal.pdf" ) ).toBe( 1 );
				pdf action="extractText" source="#path#literal.pdf" name="local.text" type="string";
				expect( text ).toInclude( "x+(1).pdf" );
			});

		});
	}
}
