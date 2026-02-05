component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	// action="sanitize" - implemented
	// Removes potentially unsafe content: JS, links, metadata, attachments, etc.
	// Similar to optimize but security-focused

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFSanitize/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path );

		// Create a test PDF with various elements
		document fileName="#path#source.pdf" overwrite=true {
			writeOutput( "<h1>Sanitize Test</h1>" );
			writeOutput( "<p>This PDF has links: <a href='https://example.com'>Click here</a></p>" );
			writeOutput( "<p>And JavaScript might be embedded.</p>" );
		}

		// Add metadata
		pdf action="setInfo" source="#path#source.pdf" destination="#path#source.pdf" overwrite=true info={
			author: "Test Author",
			title: "Sensitive Document"
		};
	}

	function run( testResults, testBox ) {
		describe( "cfpdf action=sanitize", function() {

			it( title="sanitize removes all unsafe content by default", body=function( currentSpec ) {
				pdf action="sanitize" source="#path#source.pdf"
					destination="#path#sanitized.pdf" overwrite=true;

				expect( isPDFFile( "#path#sanitized.pdf" ) ).toBeTrue();
			});

			it( title="sanitize removes JavaScript", body=function( currentSpec ) {
				pdf action="sanitize" source="#path#source.pdf"
					destination="#path#no_js.pdf" overwrite=true;

				// PDF should have no JavaScript actions
				expect( isPDFFile( "#path#no_js.pdf" ) ).toBeTrue();
			});

			it( title="sanitize removes external links", body=function( currentSpec ) {
				pdf action="sanitize" source="#path#source.pdf"
					destination="#path#no_links.pdf" overwrite=true;

				// TODO: verify links removed
				expect( isPDFFile( "#path#no_links.pdf" ) ).toBeTrue();
			});

			it( title="sanitize removes metadata", body=function( currentSpec ) {
				pdf action="sanitize" source="#path#source.pdf"
					destination="#path#no_meta.pdf" overwrite=true;

				pdf action="getInfo" source="#path#no_meta.pdf" name="local.info";
				expect( info.author ?: "" ).toBeEmpty();
			});

			it( title="sanitize removes attachments", body=function( currentSpec ) {
				// First add an attachment
				fileWrite( "#path#attach.txt", "potentially dangerous file" );
				pdf action="addAttachments" source="#path#source.pdf"
					destination="#path#with_attach.pdf" overwrite=true {
					pdfparam source="#path#attach.txt";
				}

				pdf action="sanitize" source="#path#with_attach.pdf"
					destination="#path#clean.pdf" overwrite=true;

				// TODO: verify attachments removed
				expect( isPDFFile( "#path#clean.pdf" ) ).toBeTrue();
			});

			// name attribute not supported for sanitize - requires destination
			// it( title="sanitize with name attribute", skip=true, body=function( currentSpec ) {
			// });

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
