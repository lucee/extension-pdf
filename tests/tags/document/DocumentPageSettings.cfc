component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentPageSettings/generated/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );
	}

	function run( testResults, testBox ) {
		describe( "cfdocument page types", function() {

			it( title="create A4 PDF", body=function( currentSpec ) {
				document format="pdf" pagetype="A4" filename="#path#a4.pdf" overwrite=true {
					writeOutput( "<h1>A4 Document</h1>" );
				}
				expect( isPDFFile( "#path#a4.pdf" ) ).toBeTrue();
			});

			it( title="create Letter PDF", body=function( currentSpec ) {
				document format="pdf" pagetype="letter" filename="#path#letter.pdf" overwrite=true {
					writeOutput( "<h1>Letter Document</h1>" );
				}
				expect( isPDFFile( "#path#letter.pdf" ) ).toBeTrue();
			});

			it( title="create Legal PDF", body=function( currentSpec ) {
				document format="pdf" pagetype="legal" filename="#path#legal.pdf" overwrite=true {
					writeOutput( "<h1>Legal Document</h1>" );
				}
				expect( isPDFFile( "#path#legal.pdf" ) ).toBeTrue();
			});

			it( title="create custom size PDF", body=function( currentSpec ) {
				document format="pdf" pagetype="custom" pagewidth="5" pageheight="7" filename="#path#custom.pdf" overwrite=true {
					writeOutput( "<h1>Custom Size Document</h1>" );
				}
				expect( isPDFFile( "#path#custom.pdf" ) ).toBeTrue();
			});

		});

		describe( "cfdocument orientation", function() {

			it( title="create portrait PDF", body=function( currentSpec ) {
				document format="pdf" orientation="portrait" filename="#path#portrait.pdf" overwrite=true {
					writeOutput( "<h1>Portrait Document</h1>" );
				}
				expect( isPDFFile( "#path#portrait.pdf" ) ).toBeTrue();
			});

			it( title="create landscape PDF", body=function( currentSpec ) {
				document format="pdf" orientation="landscape" filename="#path#landscape.pdf" overwrite=true {
					writeOutput( "<h1>Landscape Document</h1>" );
				}
				expect( isPDFFile( "#path#landscape.pdf" ) ).toBeTrue();
			});

		});

		describe( "cfdocument margins", function() {

			it( title="create PDF with inch margins", body=function( currentSpec ) {
				document format="pdf" unit="in" margintop="1" marginbottom="1" marginleft="1" marginright="1" filename="#path#margins_inch.pdf" overwrite=true {
					writeOutput( "<h1>1 Inch Margins</h1>" );
				}
				expect( isPDFFile( "#path#margins_inch.pdf" ) ).toBeTrue();
			});

			it( title="create PDF with cm margins", body=function( currentSpec ) {
				document format="pdf" unit="cm" margintop="2" marginbottom="2" marginleft="2" marginright="2" filename="#path#margins_cm.pdf" overwrite=true {
					writeOutput( "<h1>2cm Margins</h1>" );
				}
				expect( isPDFFile( "#path#margins_cm.pdf" ) ).toBeTrue();
			});

			it( title="create PDF with zero margins", body=function( currentSpec ) {
				document format="pdf" margintop="0" marginbottom="0" marginleft="0" marginright="0" filename="#path#margins_zero.pdf" overwrite=true {
					writeOutput( "<h1>Zero Margins</h1>" );
				}
				expect( isPDFFile( "#path#margins_zero.pdf" ) ).toBeTrue();
			});

		});

		describe( "cfdocument combined settings", function() {

			it( title="create A4 landscape with custom margins", body=function( currentSpec ) {
				document format="pdf" pagetype="A4" orientation="landscape" unit="cm" margintop="1.5" marginbottom="1.5" marginleft="2" marginright="2" filename="#path#combined.pdf" overwrite=true {
					writeOutput( "<h1>Combined Settings</h1><p>A4 Landscape with custom margins</p>" );
				}
				expect( isPDFFile( "#path#combined.pdf" ) ).toBeTrue();
			});

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
