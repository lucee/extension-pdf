component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFMissingAction/generated/";
		if ( !directoryExists( path ) ) directoryCreate( path, true, true );
		document format="pdf" filename="#path#src.pdf" overwrite=true {
			writeOutput( "<p>source</p>" );
		}
	}

	function run( testResults, testBox ) {
		describe( "cfpdf without action", function() {

			it( title="throws a clear error instead of silently doing nothing", body=function( currentSpec ) {
				var msg = "";
				try {
					pdf source="#path#src.pdf" destination="#path#out.pdf" overwrite=true;
				}
				catch ( e ) {
					msg = e.message;
				}
				expect( msg ).toInclude( "attribute [action] is required" );
				expect( fileExists( "#path#out.pdf" ) ).toBeFalse();
			});

			it( title="with an action it still works", body=function( currentSpec ) {
				pdf action="getInfo" source="#path#src.pdf" name="local.info";
				expect( info.totalPages ).toBe( 1 );
			});

		});
	}
}
