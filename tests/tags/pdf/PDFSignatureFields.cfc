component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	// action="readSignatureFields" - implemented
	// action="validateSignature" - NOT YET implemented (tests skipped)
	// PDFBox: PDAcroForm.getFields(), PDSignatureField

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFSignatureFields/generated/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );

		// Note: Testing signature features requires PDFs with actual signature fields
		// These would typically be created in Acrobat or via PDFBox
		// For now, create a simple PDF - real tests need signed PDFs
		document fileName="#path#unsigned.pdf" overwrite=true {
			writeOutput( "<h1>Signature Test</h1><p>This PDF has no signature fields.</p>" );
		}
	}

	function run( testResults, testBox ) {

		describe( "cfpdf action=readSignatureFields", function() {

			it( title="read signature fields from PDF without signatures", body=function( currentSpec ) {
				pdf action="readSignatureFields" source="#path#unsigned.pdf" name="local.fields";

				// Should return empty query
				expect( isQuery( fields ) ).toBeTrue();
				expect( fields.recordCount ).toBe( 0 );
			});

			it( title="read signature fields returns query with expected columns", body=function( currentSpec ) {
				pdf action="readSignatureFields" source="#path#unsigned.pdf" name="local.fields";

				expect( isQuery( fields ) ).toBeTrue();
				// Adobe CF returns: name, certifiable, signable, isSigned
				expect( listFindNoCase( fields.columnList, "name" ) ).toBeGT( 0 );
				expect( listFindNoCase( fields.columnList, "signable" ) ).toBeGT( 0 );
				expect( listFindNoCase( fields.columnList, "isSigned" ) ).toBeGT( 0 );
			});

			// TODO: Add tests with actual signed PDFs
			// it( title="read signature fields from signed PDF", body=function( currentSpec ) {
			// 	pdf action="readSignatureFields" source="#path#signed.pdf" name="local.fields";
			//
			// 	expect( fields.recordCount ).toBeGT( 0 );
			// 	expect( fields.isSigned[1] ).toBeTrue();
			// });

		});

		describe( title="cfpdf action=validateSignature", body=function() {

			it( title="validate signature on unsigned PDF", body=function( currentSpec ) {
				pdf action="validateSignature" source="#path#unsigned.pdf" name="local.result";

				// Should indicate no signatures
				expect( isStruct( result ) ).toBeTrue();
				expect( result.hasSignatures ).toBeFalse();
				expect( result.signatureCount ).toBe( 0 );
				expect( isArray( result.signatures ) ).toBeTrue();
			});

			// TODO: Add tests with actual signed PDFs
			// it( title="validate valid signature", body=function( currentSpec ) {
			// 	pdf action="validateSignature" source="#path#validly_signed.pdf" name="local.result";
			//
			// 	expect( result.valid ).toBeTrue();
			// });
			//
			// it( title="validate tampered signature", body=function( currentSpec ) {
			// 	pdf action="validateSignature" source="#path#tampered.pdf" name="local.result";
			//
			// 	expect( result.valid ).toBeFalse();
			// });

		});

	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
