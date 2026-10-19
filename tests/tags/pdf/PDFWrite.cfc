component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFWrite/generated/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );

		// Create a PDF in memory
		document name="variables.pdfVar" {
			writeOutput( "<h1>Test PDF Content</h1><p>This is test content for write action.</p>" );
		}

		// AcroForm fixture built with PDFBox (Flying Saucer does not turn HTML <input> into form fields)
		var fixture = getDirectoryFromPath( getCurrentTemplatePath() ) & "fixtures/form.pdf";
		pdfform action="populate" source="#fixture#"
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

			it( title="write with flatten=true removes form fields, value remains visible", body=function( currentSpec ) {
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

				// no form left in the document at all
				pdfform action="read" source="#path#form_flattened.pdf" XMLdata="local.xml";
				expect( xml ).notToInclude( "Sherlock" );
			});

			it( title="write without flatten keeps the form fields", body=function( currentSpec ) {
				pdf action="write" source="#path#form_filled.pdf"
					destination="#path#form_kept.pdf" overwrite=true;

				pdfform action="read" source="#path#form_kept.pdf" result="local.after";
				expect( after.firstName ).toBe( "Sherlock" );
				expect( structCount( after ) ).toBe( 4 );
			});

			it( title="write flatten=true with destination equal to source", body=function( currentSpec ) {
				fileCopy( "#path#form_filled.pdf", "#path#form_inplace.pdf" );
				pdf action="write" source="#path#form_inplace.pdf"
					destination="#path#form_inplace.pdf" overwrite=true flatten=true;

				pdfform action="read" source="#path#form_inplace.pdf" result="local.after";
				expect( structCount( after ) ).toBe( 0 );
				pdf action="extractText" source="#path#form_inplace.pdf" name="local.text" type="string";
				expect( text ).toInclude( "Sherlock" );
			});

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
