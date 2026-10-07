component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFRemoveAttachments/generated/";
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );

		// Create a source PDF
		document fileName="#path#source.pdf" overwrite=true {
			writeOutput( "<h1>Attachment Test</h1>" );
		}

		// Create a text file to attach
		fileWrite( "#path#attachment.txt", "This is an attachment" );

		// Create a PDF with attachments
		pdf action="addAttachments" source="#path#source.pdf"
			destination="#path#with_attachments.pdf" overwrite=true {
			pdfparam source="#path#attachment.txt";
		}
	}

	function run( testResults, testBox ) {

		describe( "cfpdf action=removeAttachments", function() {

			it( title="remove all attachments from PDF", body=function( currentSpec ) {
				pdf action="removeAttachments" source="#path#with_attachments.pdf"
					destination="#path#cleaned.pdf" overwrite=true;

				expect( isPDFFile( "#path#cleaned.pdf" ) ).toBeTrue();

				// Verify attachments were removed by trying to extract
				var extractDir = "#path#verify_empty/";
				if ( directoryExists( extractDir ) ) directoryDelete( extractDir, true );
				directoryCreate( extractDir, true, true );

				pdf action="extractAttachments" source="#path#cleaned.pdf"
					destination="#extractDir#" overwrite=true;

				var files = directoryList( extractDir, false, "name" );
				expect( arrayLen( files ) ).toBe( 0 );
			});

			it( title="remove attachments with name attribute", body=function( currentSpec ) {
				pdf action="removeAttachments" source="#path#with_attachments.pdf"
					name="local.result";

				expect( isPDFObject( result ) ).toBeTrue();
			});

			it( title="remove attachments from PDF without attachments", body=function( currentSpec ) {
				pdf action="removeAttachments" source="#path#source.pdf"
					destination="#path#still_clean.pdf" overwrite=true;

				expect( isPDFFile( "#path#still_clean.pdf" ) ).toBeTrue();
			});

		});
	}

	function afterAll() {
		// leave artifacts for inspection
	}
}
