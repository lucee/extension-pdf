component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" skip=true {

	// SKIP: action="redact" not yet implemented
	// See: FEATURES.md - Low priority
	// Black out content at specified coordinates
	// PDFBox: PDAnnotation for redaction

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFRedact/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path );

		// Create a test PDF with sensitive content
		document fileName="#path#source.pdf" overwrite=true {
			writeOutput( "<h1>Confidential Document</h1>" );
			writeOutput( "<p>Employee Name: John Smith</p>" );
			writeOutput( "<p>SSN: 123-45-6789</p>" );
			writeOutput( "<p>Salary: $75,000</p>" );
		}
	}

	function run( testResults, testBox ) {
		describe( "cfpdf action=redact", function() {

			it( title="redact area on single page", body=function( currentSpec ) {
				pdf action="redact" source="#path#source.pdf"
					destination="#path#redacted.pdf" overwrite=true {
					// Coordinates: llx, lly, urx, ury (lower-left x,y to upper-right x,y)
					pdfparam pages="1" coordinates="100,500,300,520"; // Redact SSN area
				}

				expect( isPDFFile( "#path#redacted.pdf" ) ).toBeTrue();

				// Verify redacted content is not extractable
				pdf action="extractText" source="#path#redacted.pdf"
					name="local.text" type="string";
				expect( text ).notToInclude( "123-45-6789" );
			});

			it( title="redact multiple areas", body=function( currentSpec ) {
				pdf action="redact" source="#path#source.pdf"
					destination="#path#multi_redact.pdf" overwrite=true {
					pdfparam pages="1" coordinates="100,500,300,520"; // SSN
					pdfparam pages="1" coordinates="100,450,250,470"; // Salary
				}

				expect( isPDFFile( "#path#multi_redact.pdf" ) ).toBeTrue();
			});

			it( title="redact with name attribute", body=function( currentSpec ) {
				pdf action="redact" source="#path#source.pdf" name="local.redacted" {
					pdfparam pages="1" coordinates="100,500,300,520";
				}

				expect( isPDFObject( redacted ) ).toBeTrue();
			});

		});
	}

	function afterAll() {
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
	}
}
