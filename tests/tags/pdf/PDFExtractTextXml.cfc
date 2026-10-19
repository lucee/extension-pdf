component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFExtractTextXml/generated/";
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		directoryCreate( variables.path, true, true );

		document format="pdf" filename="#path#special.pdf" overwrite=true {
			writeOutput( "<p>Tom &amp; Jerry &lt;tag&gt; ""quoted"" it's</p>" );
		}
		document format="pdf" filename="#path#twopages.pdf" overwrite=true {
			writeOutput( "<p>page one</p><div style='page-break-before:always'><p>page two</p></div>" );
		}
	}

	function run( testResults, testBox ) {

		describe( "cfpdf action=extractText type=xml", function() {

			it( title="returns well-formed XML when the text contains markup characters", body=function( currentSpec ) {
				pdf action="extractText" source="#path#special.pdf" type="xml" name="local.xmlText";
				expect( isXML( xmlText ) ).toBeTrue( "extractText type=xml must return valid XML: " & xmlText );
				var doc = xmlParse( xmlText );
				var pageText = doc.DocText.TextPerPage.page[ 1 ].xmlText;
				expect( pageText ).toInclude( "Tom & Jerry <tag>" );
				expect( pageText ).toInclude( """quoted"" it's" );
			});

			it( title="has one page element per page with its page number", body=function( currentSpec ) {
				pdf action="extractText" source="#path#twopages.pdf" type="xml" name="local.xmlText";
				var pages = xmlParse( xmlText ).DocText.TextPerPage.page;
				expect( arrayLen( pages ) ).toBe( 2 );
				expect( pages[ 1 ].xmlAttributes.pagenumber ).toBe( 1 );
				expect( pages[ 1 ].xmlText ).toInclude( "page one" );
				expect( pages[ 2 ].xmlAttributes.pagenumber ).toBe( 2 );
				expect( pages[ 2 ].xmlText ).toInclude( "page two" );
			});

			it( title="pages attribute still selects a single page", body=function( currentSpec ) {
				pdf action="extractText" source="#path#twopages.pdf" type="string" pages="2" name="local.txt";
				expect( txt ).toInclude( "page two" );
				expect( txt ).notToInclude( "page one" );
			});

		});

		describe( "cfpdf actions close the source PDF", function() {

			it( title="extractText, thumbnail, extractImage and the watermark image do not leak open files", skip=!directoryExists( "/proc/self/fd" ), body=function( currentSpec ) {
				var thumbDir = path & "thumbs/";
				directoryCreate( thumbDir, true, true );
				// warm up (class loading can open jars)
				pdf action="extractText" source="#path#special.pdf" name="local.t";
				pdf action="thumbnail" source="#path#special.pdf" destination=thumbDir overwrite=true;
				var before = arrayLen( directoryList( "/proc/self/fd" ) );
				loop from=1 to=30 index="local.i" {
					pdf action="extractText" source="#path#special.pdf" name="local.t";
					pdf action="thumbnail" source="#path#special.pdf" destination=thumbDir overwrite=true;
					pdf action="extractImage" source="#path#special.pdf" destination=thumbDir imagePrefix="img" overwrite=true;
				}
				var after = arrayLen( directoryList( "/proc/self/fd" ) );
				systemOutput( "open file descriptors before: #before#, after: #after#", true );
				// every leaked PDDocument keeps the PDF file open, 30 iterations x 3 actions would add ~90
				expect( after - before ).toBeLT( 10, "open file descriptors grew from #before# to #after#" );
			});

		});
	}

	function afterAll() {
		// leave artifacts for inspection
	}
}
