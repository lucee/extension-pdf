component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	// action="addAttachments", "extractAttachments", "removeAttachments"
	// PDFBox: PDEmbeddedFile, PDComplexFileSpecification, PDEmbeddedFilesNameTreeNode

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFAttachments/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path );

		// Create a test PDF
		document fileName="#path#source.pdf" overwrite=true {
			writeOutput( "<h1>Attachments Test</h1><p>This PDF will have files attached.</p>" );
		}

		// Create test files to attach
		fileWrite( "#path#attachment1.txt", "This is attachment 1 content" );
		fileWrite( "#path#attachment2.txt", "This is attachment 2 content" );
		fileWrite( "#path#data.json", '{"key": "value", "number": 42}' );
	}

	function run( testResults, testBox ) {

		describe( "cfpdf action=addAttachments", function() {

			it( title="add single attachment with cfpdfparam", body=function( currentSpec ) {
				pdf action="addAttachments" source="#path#source.pdf"
					destination="#path#with_attachment.pdf" overwrite=true {
					pdfparam source="#path#attachment1.txt";
				}

				expect( isPDFFile( "#path#with_attachment.pdf" ) ).toBeTrue();
			});

			it( title="add multiple attachments", body=function( currentSpec ) {
				pdf action="addAttachments" source="#path#source.pdf"
					destination="#path#multi_attach.pdf" overwrite=true {
					pdfparam source="#path#attachment1.txt";
					pdfparam source="#path#attachment2.txt";
					pdfparam source="#path#data.json";
				}

				expect( isPDFFile( "#path#multi_attach.pdf" ) ).toBeTrue();
			});

			it( title="add attachment with custom filename", body=function( currentSpec ) {
				pdf action="addAttachments" source="#path#source.pdf"
					destination="#path#custom_name.pdf" overwrite=true {
					pdfparam source="#path#attachment1.txt" filename="readme.txt";
				}

				expect( isPDFFile( "#path#custom_name.pdf" ) ).toBeTrue();
			});

			it( title="add attachment with description", body=function( currentSpec ) {
				pdf action="addAttachments" source="#path#source.pdf"
					destination="#path#with_desc.pdf" overwrite=true {
					pdfparam source="#path#attachment1.txt" description="Important readme file";
				}

				expect( isPDFFile( "#path#with_desc.pdf" ) ).toBeTrue();
			});

			it( title="add attachment with mimetype", body=function( currentSpec ) {
				pdf action="addAttachments" source="#path#source.pdf"
					destination="#path#with_mime.pdf" overwrite=true {
					pdfparam source="#path#data.json" mimetype="application/json";
				}

				expect( isPDFFile( "#path#with_mime.pdf" ) ).toBeTrue();
			});

		});

		describe( "cfpdf action=extractAttachments (Lucee bonus)", function() {

			beforeEach( function( currentSpec ) {
				// Create a PDF with attachments first
				pdf action="addAttachments" source="#path#source.pdf"
					destination="#path#has_attachments.pdf" overwrite=true {
					pdfparam source="#path#attachment1.txt" filename="file1.txt";
					pdfparam source="#path#attachment2.txt" filename="file2.txt";
				}

				variables.extractPath = "#path#extracted/";
				if ( directoryExists( extractPath ) ) directoryDelete( extractPath, true );
				directoryCreate( extractPath );
			});

			it( title="extract all attachments to directory", body=function( currentSpec ) {
				pdf action="extractAttachments" source="#path#has_attachments.pdf"
					destination="#extractPath#";

				expect( fileExists( "#extractPath#file1.txt" ) ).toBeTrue();
				expect( fileExists( "#extractPath#file2.txt" ) ).toBeTrue();
			});

			it( title="extract attachments returns info array via name attribute", body=function( currentSpec ) {
				pdf action="extractAttachments" source="#path#has_attachments.pdf"
					destination="#extractPath#" name="local.attachments";

				expect( isArray( attachments ) ).toBeTrue();
				expect( arrayLen( attachments ) ).toBe( 2 );
				expect( attachments[1] ).toHaveKey( "filename" );
				expect( attachments[1] ).toHaveKey( "path" );
				expect( attachments[1] ).toHaveKey( "size" );
			});

			it( title="extract from PDF with no attachments returns empty array", body=function( currentSpec ) {
				pdf action="extractAttachments" source="#path#source.pdf"
					destination="#extractPath#" name="local.attachments";

				expect( isArray( attachments ) ).toBeTrue();
				expect( arrayLen( attachments ) ).toBe( 0 );
			});

			it( title="extract with overwrite=false errors on existing files", body=function( currentSpec ) {
				// Extract once
				pdf action="extractAttachments" source="#path#has_attachments.pdf"
					destination="#extractPath#";

				// Try again without overwrite
				expect( function() {
					pdf action="extractAttachments" source="#path#has_attachments.pdf"
						destination="#extractPath#" overwrite=false stopOnError=true;
				}).toThrow();
			});

		});

		describe( "cfpdf action=removeAttachments", function() {

			it( title="remove all attachments from PDF", body=function( currentSpec ) {
				// First add attachments
				pdf action="addAttachments" source="#path#source.pdf"
					destination="#path#to_strip.pdf" overwrite=true {
					pdfparam source="#path#attachment1.txt";
					pdfparam source="#path#attachment2.txt";
				}

				// Then remove them
				pdf action="removeAttachments" source="#path#to_strip.pdf"
					destination="#path#stripped.pdf" overwrite=true;

				expect( isPDFFile( "#path#stripped.pdf" ) ).toBeTrue();

				// Verify no attachments
				var stripPath = "#path#strip_test/";
				if ( directoryExists( stripPath ) ) directoryDelete( stripPath, true );
				directoryCreate( stripPath );

				pdf action="extractAttachments" source="#path#stripped.pdf"
					destination="#stripPath#" name="local.attachments";

				expect( arrayLen( attachments ) ).toBe( 0 );
			});

			it( title="remove attachments with name attribute returns PDF variable", body=function( currentSpec ) {
				pdf action="addAttachments" source="#path#source.pdf"
					destination="#path#for_name.pdf" overwrite=true {
					pdfparam source="#path#attachment1.txt";
				}

				pdf action="removeAttachments" source="#path#for_name.pdf" name="local.result";

				expect( isPDFObject( result ) ).toBeTrue();
			});

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
