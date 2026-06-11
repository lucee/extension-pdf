component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFWrite/generated/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );

		// Create a PDF in memory
		document name="variables.pdfVar" {
			writeOutput( "<h1>Test PDF Content</h1><p>This is test content for write action.</p>" );
		}

		// Create a PDF with form fields for flatten test
		document fileName="#path#form_src.pdf" overwrite=true {
			writeOutput( '<form><input type="text" name="firstName" value="" /></form>' );
		}
		pdfform action="populate" source="#path#form_src.pdf"
			destination="#path#form_filled.pdf" overwrite=true {
			pdfformparam name="firstName" value="Sherlock";
		}
	}

	function run( testResults, testBox ) {
		describe( "cfpdf action=write", function() {

			it( title="write PDF variable to file", body=function( currentSpec ) {
				pdf action="write" source="pdfVar" destination="#path#written.pdf" overwrite=true;

				expect( isPDFFile( "#path#written.pdf" ) ).toBeTrue();
			});

			it( title="write PDF with overwrite=false should error on existing file", body=function( currentSpec ) {
				pdf action="write" source="pdfVar" destination="#path#nooverwrite.pdf" overwrite=true;

				expect( function() {
					pdf action="write" source="pdfVar" destination="#path#nooverwrite.pdf" overwrite=false;
				}).toThrow();
			});

			it( title="write preserves PDF content", body=function( currentSpec ) {
				pdf action="write" source="pdfVar" destination="#path#content.pdf" overwrite=true;

				pdf action="extractText" source="#path#content.pdf" name="local.text" type="string";
				expect( text ).toInclude( "Test PDF Content" );
			});

			it( title="write after modification preserves changes", body=function( currentSpec ) {
				// Add a header then write
				pdf action="addHeader" source="pdfVar" text="Added Header" name="local.modified";
				pdf action="write" source="modified" destination="#path#modified.pdf" overwrite=true;

				expect( isPDFFile( "#path#modified.pdf" ) ).toBeTrue();
			});

			it( title="write with flatten=true removes form fields, value remains visible", skip=true, body=function( currentSpec ) {
				// PDFBOX-5962 (3.0.4+): flatten() empties getFields() in memory but the underlying
				// COS field array survives save, so the form is resurrected on reload. The documented
				// getAcroForm(null) workaround doesn't help when the source PDF was previously populated
				// in a separate session. Re-enable once PDFBox lands a real fix.
				// Sanity check the source has form fields
				pdfform action="read" source="#path#form_filled.pdf" result="local.before";
				expect( before.firstName ).toBe( "Sherlock" );

				pdf action="write" source="#path#form_filled.pdf"
					destination="#path#form_flattened.pdf" overwrite=true flatten=true;

				expect( isPDFFile( "#path#form_flattened.pdf" ) ).toBeTrue();

				// Form fields should be gone
				pdfform action="read" source="#path#form_flattened.pdf" result="local.after";
				expect( structCount( after ) ).toBe( 0 );

				// Value still visible in page text
				pdf action="extractText" source="#path#form_flattened.pdf" name="local.text" type="string";
				expect( text ).toInclude( "Sherlock" );
			});

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
