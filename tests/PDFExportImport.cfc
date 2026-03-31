component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" skip=true {

	// SKIP: action="export" and "import" not yet implemented
	// See: FEATURES.md - Low priority
	// Export/import comments (XFDF) and metadata (XMP)

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFExportImport/generated/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );

		// Create a test PDF with metadata
		document fileName="#path#source.pdf" overwrite=true {
			writeOutput( "<h1>Export/Import Test</h1>" );
			writeOutput( "<p>Testing comment and metadata export/import.</p>" );
		}

		pdf action="setInfo" source="#path#source.pdf" info={
			author: "Test Author",
			title: "Test Document",
			subject: "Export Import Testing",
			keywords: "test, export, import, xfdf, xmp"
		};
	}

	function run( testResults, testBox ) {

		describe( "cfpdf action=export type=comment", function() {

			it( title="export comments to XFDF file", body=function( currentSpec ) {
				pdf action="export" type="comment" source="#path#source.pdf"
					exportTo="#path#comments.xfdf";

				expect( fileExists( "#path#comments.xfdf" ) ).toBeTrue();
			});

			it( title="exported XFDF is valid XML", body=function( currentSpec ) {
				pdf action="export" type="comment" source="#path#source.pdf"
					exportTo="#path#valid.xfdf";

				var content = fileRead( "#path#valid.xfdf" );
				expect( isXML( content ) ).toBeTrue();
			});

		});

		describe( "cfpdf action=import type=comment", function() {

			it( title="import comments from XFDF file", body=function( currentSpec ) {
				// First export
				pdf action="export" type="comment" source="#path#source.pdf"
					exportTo="#path#to_import.xfdf";

				// Create a fresh PDF
				document fileName="#path#target.pdf" overwrite=true {
					writeOutput( "<h1>Target for import</h1>" );
				}

				// Import
				pdf action="import" type="comment" source="#path#target.pdf"
					importFrom="#path#to_import.xfdf"
					destination="#path#with_comments.pdf" overwrite=true;

				expect( isPDFFile( "#path#with_comments.pdf" ) ).toBeTrue();
			});

		});

		describe( "cfpdf action=export type=metadata", function() {

			it( title="export metadata to XMP file", body=function( currentSpec ) {
				pdf action="export" type="metadata" source="#path#source.pdf"
					exportTo="#path#metadata.xmp";

				expect( fileExists( "#path#metadata.xmp" ) ).toBeTrue();
			});

			it( title="exported XMP contains document info", body=function( currentSpec ) {
				pdf action="export" type="metadata" source="#path#source.pdf"
					exportTo="#path#check_meta.xmp";

				var content = fileRead( "#path#check_meta.xmp" );
				expect( content ).toInclude( "Test Author" );
			});

		});

		describe( "cfpdf action=import type=metadata", function() {

			it( title="import metadata from XMP file", body=function( currentSpec ) {
				// First export
				pdf action="export" type="metadata" source="#path#source.pdf"
					exportTo="#path#meta_import.xmp";

				// Create a fresh PDF
				document fileName="#path#blank.pdf" overwrite=true {
					writeOutput( "<h1>Blank PDF</h1>" );
				}

				// Import
				pdf action="import" type="metadata" source="#path#blank.pdf"
					importFrom="#path#meta_import.xmp"
					destination="#path#with_meta.pdf" overwrite=true;

				pdf action="getInfo" source="#path#with_meta.pdf" name="local.info";
				expect( info.author ?: "" ).toBe( "Test Author" );
			});

		});

	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
