component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentErrorConditions/generated/";
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );
	}

	function run( testResults, testBox ) {

		describe( "cfdocument format attribute", function() {

			it( title="format='xls' throws (only pdf supported)", body=function( currentSpec ) {
				expect( function() {
					document format="xls" name="local.result" {
						writeOutput( "<p>x</p>" );
					}
				}).toThrow();
			});

			it( title="format='docx' throws", body=function( currentSpec ) {
				expect( function() {
					document format="docx" name="local.result" {
						writeOutput( "<p>x</p>" );
					}
				}).toThrow();
			});

			it( title="format='PDF' (uppercase) accepted (case-insensitive)", body=function( currentSpec ) {
				document format="PDF" name="local.result" {
					writeOutput( "<p>uppercase format</p>" );
				}
				expect( isPDFObject( result ) ).toBeTrue();
			});

		});

		describe( "cfdocument pagetype attribute", function() {

			it( title="pagetype='A6' throws (not in valid set)", body=function( currentSpec ) {
				expect( function() {
					document format="pdf" pagetype="A6" name="local.result" {
						writeOutput( "<p>x</p>" );
					}
				}).toThrow();
			});

			it( title="pagetype='nonsense' throws", body=function( currentSpec ) {
				expect( function() {
					document format="pdf" pagetype="nonsense" name="local.result" {
						writeOutput( "<p>x</p>" );
					}
				}).toThrow();
			});

			it( title="pagetype='A4' accepted", body=function( currentSpec ) {
				document format="pdf" pagetype="A4" name="local.result" {
					writeOutput( "<p>a4 page</p>" );
				}
				expect( isPDFObject( result ) ).toBeTrue();
			});

		});

		describe( "cfdocument page dimensions", function() {

			it( title="pagewidth=0 throws (must be positive)", body=function( currentSpec ) {
				expect( function() {
					document format="pdf" pagetype="custom" pagewidth=0 pageheight=10 name="local.result" {
						writeOutput( "<p>x</p>" );
					}
				}).toThrow();
			});

			it( title="pagewidth=-5 throws", body=function( currentSpec ) {
				expect( function() {
					document format="pdf" pagetype="custom" pagewidth=-5 pageheight=10 name="local.result" {
						writeOutput( "<p>x</p>" );
					}
				}).toThrow();
			});

			it( title="pageheight=0 throws", body=function( currentSpec ) {
				expect( function() {
					document format="pdf" pagetype="custom" pagewidth=10 pageheight=0 name="local.result" {
						writeOutput( "<p>x</p>" );
					}
				}).toThrow();
			});

			it( title="pageheight=-1 throws", body=function( currentSpec ) {
				expect( function() {
					document format="pdf" pagetype="custom" pagewidth=10 pageheight=-1 name="local.result" {
						writeOutput( "<p>x</p>" );
					}
				}).toThrow();
			});

		});

		describe( "cfdocument file output collisions", function() {

			it( title="writing to existing filename without overwrite throws", body=function( currentSpec ) {
				var target = path & "existing.pdf";

				document format="pdf" filename=target overwrite=true {
					writeOutput( "<p>first</p>" );
				}
				expect( fileExists( target ) ).toBeTrue();

				expect( function() {
					document format="pdf" filename=target {
						writeOutput( "<p>second</p>" );
					}
				}).toThrow();
			});

			it( title="writing to existing filename with overwrite=true succeeds", body=function( currentSpec ) {
				var target = path & "overwrite.pdf";

				document format="pdf" filename=target overwrite=true {
					writeOutput( "<p>first</p>" );
				}
				document format="pdf" filename=target overwrite=true {
					writeOutput( "<p>second</p>" );
				}

				pdf action="extractText" source=target name="local.text";
				expect( text ).toInclude( "second" );
				expect( text ).notToInclude( "first" );
			});

		});
	}

}
