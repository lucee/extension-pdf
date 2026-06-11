component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentFonts/generated/";
		variables.fontDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "../../artifacts/fonts";

		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		directoryCreate( variables.path, true, true );
	}

	function run( testResults, testBox ) {

		describe( "cfdocument fontdirectory - happy path", function() {

			it( title="Liberation Sans renders, family name appears in PDF", body=function( currentSpec ) {
				document format="pdf" fontdirectory="#fontDir#" filename="#path#liberation.pdf" overwrite=true {
					writeOutput( '<p style="font-size:5em;font-family:Liberation Sans;">Hello Liberation</p>' );
				}

				expect( isPDFFile( "#path#liberation.pdf" ) ).toBeTrue();

				pdf action="extractText" source="#path#liberation.pdf" name="local.txt" type="string";
				expect( trim( local.txt ) ).toBe( "Hello Liberation" );

				// font name should be embedded in the PDF (typically with a subset prefix like "ABCDEF+LiberationSans")
				expect( getPDFFontNamesList( "#path#liberation.pdf" ) ).toInclude( "LiberationSans" );
			});

			it( title="Arabic font renders RTL text (LDEV-3836 family regression)", body=function( currentSpec ) {
				processingdirective pageEncoding="UTF-8";
				document format="pdf" fontdirectory="#fontDir#" filename="#path#arabic.pdf" overwrite=true {
					writeOutput( '<p style="font-size:5em;font-family:Noto Naskh Arabic;">' & chr( 1605 ) & chr( 1585 ) & chr( 1581 ) & chr( 1576 ) & chr( 1575 ) & '</p>' );
				}

				expect( isPDFFile( "#path#arabic.pdf" ) ).toBeTrue();

				pdf action="extractText" source="#path#arabic.pdf" name="local.txt" type="string";
				// RTL extraction may reorder; assert at least one Arabic char survives the round-trip
				expect( local.txt ).toInclude( chr( 1605 ) );
			});

			it( title="Chinese font renders CJK characters (LDEV-3836)", body=function( currentSpec ) {
				processingdirective pageEncoding="UTF-8";
				// chr(20320) + chr(22909) = "ni hao" - characters known to be in the Muyao-Softbrush font
				var greeting = chr( 20320 ) & chr( 22909 );
				document format="pdf" fontdirectory="#fontDir#" filename="#path#chinese.pdf" overwrite=true {
					writeOutput( '<p style="font-size:5em;font-family:Muyao-Softbrush;">' & greeting & '</p>' );
				}

				expect( isPDFFile( "#path#chinese.pdf" ) ).toBeTrue();

				pdf action="extractText" source="#path#chinese.pdf" name="local.txt" type="string";
				expect( trim( local.txt ) ).toBe( greeting );
			});

		});

		describe( "cfdocument fontdirectory - case sensitivity (LDEV-2599)", function() {

			it( title="wrong-case family name falls back silently, no exception", body=function( currentSpec ) {
				// font is registered as "Liberation Sans"; CSS asks for "liberation sans" - case mismatch
				document format="pdf" fontdirectory="#fontDir#" filename="#path#wrong_case.pdf" overwrite=true {
					writeOutput( '<p style="font-size:5em;font-family:liberation sans;">Lowercase lookup</p>' );
				}

				expect( isPDFFile( "#path#wrong_case.pdf" ) ).toBeTrue();
				// text still extracts (fell back to default font, didn't crash)
				pdf action="extractText" source="#path#wrong_case.pdf" name="local.txt" type="string";
				expect( trim( local.txt ) ).toBe( "Lowercase lookup" );
			});

		});

		describe( "cfdocument fontdirectory - attribute order (LDEV-3575)", function() {

			it( title="orientation before fontdirectory does not NPE", body=function( currentSpec ) {
				document format="pdf" orientation="landscape" fontdirectory="#fontDir#" filename="#path#order.pdf" overwrite=true {
					writeOutput( '<p style="font-size:5em;font-family:Liberation Sans;">Order test</p>' );
				}

				expect( isPDFFile( "#path#order.pdf" ) ).toBeTrue();
			});

		});

		describe( "cfdocument fontdirectory - error contract", function() {

			it( title="non-existent fontdirectory throws", body=function( currentSpec ) {
				expect( function() {
					document format="pdf" fontdirectory="/nonexistent/path/to/fonts" name="local.r" {
						writeOutput( "<p>x</p>" );
					}
				}).toThrow();
			});

			it( title="fontdirectory pointing at a file throws", body=function( currentSpec ) {
				expect( function() {
					document format="pdf" fontdirectory="#fontDir#/OFL.txt" name="local.r" {
						writeOutput( "<p>x</p>" );
					}
				}).toThrow();
			});

			it( title="empty fontdirectory (no .ttf/.otf) renders with defaults", body=function( currentSpec ) {
				var empty = variables.path & "empty_fonts/";
				directoryCreate( empty );

				document format="pdf" fontdirectory="#empty#" filename="#path#empty.pdf" overwrite=true {
					writeOutput( '<p style="font-size:5em;">defaults only</p>' );
				}

				expect( isPDFFile( "#path#empty.pdf" ) ).toBeTrue();
			});

		});

		describe( "cfdocument without fontdirectory - default behaviour (LDEV-3639)", function() {

			it( title="no fontdirectory: renders, falls back to PDFBox built-ins", body=function( currentSpec ) {
				document format="pdf" filename="#path#no_dir.pdf" overwrite=true {
					writeOutput( '<p style="font-size:5em;font-family:Arial;">No font dir</p>' );
				}

				expect( isPDFFile( "#path#no_dir.pdf" ) ).toBeTrue();
				pdf action="extractText" source="#path#no_dir.pdf" name="local.txt" type="string";
				// Arial requested but not in any registered font dir; PDFBox subs Helvetica - text still renders
				expect( trim( local.txt ).reReplace( "\s+", " ", "all" ) ).toBe( "No font dir" );
			});

		});

		describe( "cfdocument fontembed attribute", function() {

			it( title="fontembed=true accepted, document renders", body=function( currentSpec ) {
				document format="pdf" fontembed="true" fontdirectory="#fontDir#" filename="#path#fe_yes.pdf" overwrite=true {
					writeOutput( '<p style="font-size:5em;font-family:Liberation Sans;">embed yes</p>' );
				}
				expect( isPDFFile( "#path#fe_yes.pdf" ) ).toBeTrue();
			});

			it( title="fontembed=false accepted, document renders", body=function( currentSpec ) {
				document format="pdf" fontembed="false" fontdirectory="#fontDir#" filename="#path#fe_no.pdf" overwrite=true {
					writeOutput( '<p style="font-size:5em;font-family:Liberation Sans;">embed no</p>' );
				}
				expect( isPDFFile( "#path#fe_no.pdf" ) ).toBeTrue();
			});

			it( title="fontembed=selective accepted, document renders", body=function( currentSpec ) {
				document format="pdf" fontembed="selective" fontdirectory="#fontDir#" filename="#path#fe_sel.pdf" overwrite=true {
					writeOutput( '<p style="font-size:5em;font-family:Liberation Sans;">embed selective</p>' );
				}
				expect( isPDFFile( "#path#fe_sel.pdf" ) ).toBeTrue();
			});

			it( title="invalid fontembed value throws", body=function( currentSpec ) {
				expect( function() {
					document format="pdf" fontembed="bogus" name="local.r" {
						writeOutput( "<p>x</p>" );
					}
				}).toThrow();
			});

			it( title="fontembed yes and no produce same extracted text (OHTPDF always embeds)", body=function( currentSpec ) {
				// document the current behaviour: fontembed attribute is parsed but not wired into OHTPDF
				// (which embeds by default). When that changes, this test should be updated.
				pdf action="extractText" source="#path#fe_yes.pdf" name="local.tyes";
				pdf action="extractText" source="#path#fe_no.pdf" name="local.tno";
				expect( trim( local.tyes ).reReplace( "yes|no", "", "all" ) ).toBe(
					trim( local.tno ).reReplace( "yes|no", "", "all" )
				);
			});

		});

		describe( "cfdocument this.pdf.fontDirectory (Application.cfc)", function() {

			it( title="picks up font dir from this.pdf.fontDirectory when no attribute set", body=function( currentSpec ) {
				// fresh request context so the sub-Application.cfc gets evaluated
				var subUri = contractPath( getDirectoryFromPath( getCurrentTemplatePath() ) ) & "/DocumentFonts/testAppCfcFontDir/render.cfm";
				var res = _internalRequest( template: subUri );

				expect( trim( res.filecontent ) ).toBe( "ok" );

				var pdfPath = variables.path & "app_cfc.pdf";
				expect( isPDFFile( pdfPath ) ).toBeTrue();

				pdf action="extractText" source="#pdfPath#" name="local.txt" type="string";
				expect( trim( local.txt ) ).toBe( "From AppCfc" );

				// proves the Application.cfc-supplied font dir was actually used
				expect( getPDFFontNamesList( pdfPath ) ).toInclude( "LiberationSans" );
			});

		});

	}

	function afterAll() {
		// drop a PNG thumbnail next to each generated PDF for visual inspection
		var pdfs = directoryList( variables.path, false, "name", "*.pdf" );
		for ( var pdfName in pdfs ) {
			var prefix = listFirst( pdfName, "." );
			pdf action="thumbnail"
				source="#variables.path##pdfName#"
				destination="#variables.path#"
				format="png"
				imagePrefix="#prefix#"
				scale=100
				overwrite=true;
		}
	}

	private string function getPDFFontNamesList( required string pdfPath ) {
		var file = createObject( "java", "java.io.File" ).init( arguments.pdfPath );
		var doc = createObject( "java", "org.apache.pdfbox.Loader" ).loadPDF( file );
		try {
			var names = [];
			var pageIter = doc.getPages().iterator();
			while ( pageIter.hasNext() ) {
				var resources = pageIter.next().getResources();
				var fontIter = resources.getFontNames().iterator();
				while ( fontIter.hasNext() ) {
					names.append( resources.getFont( fontIter.next() ).getName() );
				}
			}
			return arrayToList( names, "," );
		}
		finally {
			doc.close();
		}
	}

}
