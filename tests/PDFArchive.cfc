component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" skip=true {

	// SKIP: action="archive" not yet implemented
	// See: FEATURES.md - Medium effort
	// Converts PDF to PDF/A format for archival
	// PDFBox: preflight module, PDFAIdentificationSchema

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFArchive/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path );

		// Create a test PDF
		document fileName="#path#source.pdf" overwrite=true {
			writeOutput( "<h1>Archive Test</h1>" );
			writeOutput( "<p>This document will be converted to PDF/A format.</p>" );
		}
	}

	function run( testResults, testBox ) {
		describe( "cfpdf action=archive", function() {

			it( title="convert to PDF/A-2b by default", body=function( currentSpec ) {
				pdf action="archive" source="#path#source.pdf"
					destination="#path#archived.pdf" overwrite=true;

				expect( isPDFFile( "#path#archived.pdf" ) ).toBeTrue();
				// TODO: verify PDF/A compliance
			});

			it( title="convert to PDF/A-2b explicitly", body=function( currentSpec ) {
				pdf action="archive" source="#path#source.pdf"
					destination="#path#pdfa2b.pdf" overwrite=true
					standard="2b";

				expect( isPDFFile( "#path#pdfa2b.pdf" ) ).toBeTrue();
			});

			it( title="convert to PDF/A-3b", body=function( currentSpec ) {
				pdf action="archive" source="#path#source.pdf"
					destination="#path#pdfa3b.pdf" overwrite=true
					standard="3b";

				expect( isPDFFile( "#path#pdfa3b.pdf" ) ).toBeTrue();
			});

			it( title="archive with name attribute", body=function( currentSpec ) {
				pdf action="archive" source="#path#source.pdf" name="local.archived";

				expect( isPDFObject( archived ) ).toBeTrue();
			});

			it( title="archive preserves content", body=function( currentSpec ) {
				pdf action="archive" source="#path#source.pdf"
					destination="#path#content_check.pdf" overwrite=true;

				pdf action="extractText" source="#path#content_check.pdf"
					name="local.text" type="string";
				expect( text ).toInclude( "Archive Test" );
			});

		});
	}

	function afterAll() {
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
	}
}
