component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFFormRead/generated/";
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );

		// AcroForm fixture built with PDFBox (firstName=John, lastName=Doe, email=john@example.com); Flying Saucer does not
		// turn HTML <input> elements into form fields, so the form can't come from cfdocument
		fileCopy( getDirectoryFromPath( getCurrentTemplatePath() ) & "fixtures/form-prefilled.pdf", "#path#form.pdf" );

		// Also generate a no-form PDF for negative tests
		document fileName="#path#noform.pdf" overwrite=true {
			writeOutput( "<h1>Not a Form</h1><p>No form fields here.</p>" );
		}
	}

	function run( testResults, testBox ) {
		describe( "cfpdfform action=read", function() {

			it( title="read form fields to struct via result attribute", body=function( currentSpec ) {
				pdfform action="read" source="#path#form.pdf" result="local.fields";

				expect( isStruct( fields ) ).toBeTrue();
				systemOutput( "Form fields: " & structKeyList( fields ), true );
				expect( structCount( fields ) ).toBe( 3 );
				expect( fields ).toHaveKey( "firstName" );
				expect( fields.firstName ).toBe( "John" );
				expect( fields.lastName ).toBe( "Doe" );
				expect( fields.email ).toBe( "john@example.com" );
			});

			it( title="read form fields to XML via XMLdata attribute", body=function( currentSpec ) {
				pdfform action="read" source="#path#form.pdf" XMLdata="local.xmlData";

				expect( isXML( xmlData ) ).toBeTrue();
				systemOutput( "XML: " & xmlData, true );
				expect( xmlData ).toInclude( "firstName" );
				expect( xmlData ).toInclude( "John" );
			});

			it( title="read both result and XMLdata", body=function( currentSpec ) {
				pdfform action="read" source="#path#form.pdf"
					result="local.fields" XMLdata="local.xmlData";

				expect( isStruct( fields ) ).toBeTrue();
				expect( isXML( xmlData ) ).toBeTrue();
				expect( fields ).toHaveKey( "firstName" );
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

			it( title="read from PDF variable", body=function( currentSpec ) {
				pdf action="read" source="#path#form.pdf" name="local.pdfVar";
				pdfform action="read" source="#pdfVar#" result="local.fields";

				expect( isStruct( fields ) ).toBeTrue();
				expect( fields ).toHaveKey( "firstName" );
			});

		});
	}

	function afterAll() {
		// leave artifacts for inspection
	}
}
