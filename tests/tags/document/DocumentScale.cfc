component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentScale/generated/";
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		directoryCreate( variables.path, true, true );
	}

	function run( testResults, testBox ) {

		describe( "cfdocument scale attribute", function() {

			it( title="scale=100 produces full size PDF", body=function( currentSpec ) {
				document format="pdf" scale="100" filename="#path#scale100.pdf" overwrite=true {
					writeOutput( "<h1 style='font-size:72px'>SCALE TEST</h1>" );
				}
				expect( isPDFFile( "#path#scale100.pdf" ) ).toBeTrue();
			});

			it( title="scale=50 produces a valid PDF", body=function( currentSpec ) {
				document format="pdf" scale="50" filename="#path#scale50.pdf" overwrite=true {
					writeOutput( "<h1 style='font-size:72px'>SCALE TEST</h1>" );
				}
				expect( isPDFFile( "#path#scale50.pdf" ) ).toBeTrue();
			});

			it( title="scale=25 produces a valid PDF", body=function( currentSpec ) {
				document format="pdf" scale="25" filename="#path#scale25.pdf" overwrite=true {
					writeOutput( "<h1 style='font-size:72px'>SCALE TEST</h1>" );
				}
				expect( isPDFFile( "#path#scale25.pdf" ) ).toBeTrue();
			});

			it( title="scale affects rendered content size", body=function( currentSpec ) {
				// render large content at scale 100 and scale 50
				// scale 50 should fit in fewer pages because content is half-size
				var bigContent = "<div style='font-size:36px; line-height:1.5'>";
				loop from=1 to=100 index="local.i" {
					bigContent &= "<p>Paragraph #local.i#: The quick brown fox jumps over the lazy dog. Lorem ipsum dolor sit amet.</p>";
				}
				bigContent &= "</div>";

				document format="pdf" scale="100" filename="#path#size_100.pdf" overwrite=true {
					writeOutput( bigContent );
				}
				document format="pdf" scale="50" filename="#path#size_50.pdf" overwrite=true {
					writeOutput( bigContent );
				}

				var pdf100 = {};
				var pdf50 = {};
				cfpdf( action="getinfo", source="#path#size_100.pdf", name="pdf100" );
				cfpdf( action="getinfo", source="#path#size_50.pdf", name="pdf50" );

				systemOutput( "scale 100 pages: #pdf100.totalPages#, scale 50 pages: #pdf50.totalPages#", true );

				// scale 50 must have strictly fewer pages than scale 100
				expect( pdf50.totalPages ).toBeLT( pdf100.totalPages );
			});

			it( title="scale rejects negative values", body=function( currentSpec ) {
				expect( function() {
					document format="pdf" scale="-1" filename="#path#scale_neg.pdf" overwrite=true {
						writeOutput( "test" );
					}
				}).toThrow();
			});

			it( title="scale rejects values over 100", body=function( currentSpec ) {
				expect( function() {
					document format="pdf" scale="101" filename="#path#scale_over.pdf" overwrite=true {
						writeOutput( "test" );
					}
				}).toThrow();
			});

		});
	}

	function afterAll() {
		// leave artifacts for inspection
	}
}
