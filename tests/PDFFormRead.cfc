component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	// action="read" - implemented
	// action="populate" - NOT YET implemented
	// PDFBox: PDAcroForm, PDField

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFFormRead/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path );

		// Note: Testing form features requires PDFs with actual form fields
		// These would typically be created in Acrobat
		// Create a simple PDF for now - real tests need AcroForm PDFs
		document fileName="#path#noform.pdf" overwrite=true {
			writeOutput( "<h1>Not a Form</h1><p>This PDF has no form fields.</p>" );
		}
	}

	function run( testResults, testBox ) {
		describe( "cfpdfform action=read", function() {

			// Skip tests that require actual form PDFs (form.pdf doesn't exist)
			it( title="read form fields to struct via result attribute", skip=true, body=function( currentSpec ) {
				pdfform action="read" source="#path#form.pdf" result="local.fields";

				expect( isStruct( fields ) ).toBeTrue();
			});

			it( title="read form fields to XML via XMLdata attribute", skip=true, body=function( currentSpec ) {
				pdfform action="read" source="#path#form.pdf" XMLdata="local.xmlData";

				expect( isXML( xmlData ) ).toBeTrue();
			});

			it( title="read both result and XMLdata", skip=true, body=function( currentSpec ) {
				pdfform action="read" source="#path#form.pdf"
					result="local.fields" XMLdata="local.xmlData";

				expect( isStruct( fields ) ).toBeTrue();
				expect( isXML( xmlData ) ).toBeTrue();
			});

			it( title="read from PDF without forms returns empty struct", body=function( currentSpec ) {
				pdfform action="read" source="#path#noform.pdf" result="local.fields";

				expect( isStruct( fields ) ).toBeTrue();
				expect( structIsEmpty( fields ) ).toBeTrue();
			});

			it( title="read from PDF without forms returns XML", body=function( currentSpec ) {
				pdfform action="read" source="#path#noform.pdf" xmldata="local.xmlData";

				expect( isXML( xmlData ) ).toBeTrue();
			});

			it( title="read from PDF variable", skip=true, body=function( currentSpec ) {
				pdf action="read" source="#path#form.pdf" name="local.pdfVar";
				pdfform action="read" source="pdfVar" result="local.fields";

				expect( isStruct( fields ) ).toBeTrue();
			});

			// TODO: Add tests with actual form PDFs
			// These tests require PDFs with AcroForm fields
			// it( title="read text field value", body=function( currentSpec ) {
			// 	pdfform action="read" source="#path#text_form.pdf" result="local.fields";
			// 	expect( fields.firstName ).toBe( "John" );
			// });
			//
			// it( title="read checkbox value", body=function( currentSpec ) {
			// 	pdfform action="read" source="#path#checkbox_form.pdf" result="local.fields";
			// 	expect( fields.agreed ).toBeTrue();
			// });
			//
			// it( title="read dropdown/combo value", body=function( currentSpec ) {
			// 	pdfform action="read" source="#path#dropdown_form.pdf" result="local.fields";
			// 	expect( fields.country ).toBe( "Australia" );
			// });

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
