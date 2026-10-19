component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentFontLoadError/generated/";
		variables.fontDir = path & "fonts/";
		if ( directoryExists( path ) ) directoryDelete( path, true );
		directoryCreate( fontDir, true, true );
		// a file with a font extension that is not a font, and a file that is no font at all
		fileWrite( fontDir & "Broken.ttf", "this is not a TrueType font" );
		fileWrite( fontDir & "readme.txt", "fonts for the test" );

		// route the "pdf" log to a file we can read (only if it is not configured already)
		variables.logFile = path & "pdf.log";
		variables.pw = request.ServerAdminPassword;
		admin action="getLogSettings" type="server" password="#pw#" returnVariable="local.settings";
		variables.createdLog = !listFindNoCase( valueList( settings.name ), "pdf" );
		if ( createdLog ) {
			admin action="updateLogSettings" type="server" password="#pw#"
				name="pdf" level="warn" appenderClass="resource" appenderArgs={ "path": logFile }
				layoutClass="classic" layoutArgs={};
		}
	}

	function afterAll() {
		if ( variables.createdLog ?: false ) {
			admin action="removeLogSetting" type="server" password="#variables.pw#" name="pdf";
		}
	}

	function run( testResults, testBox ) {
		describe( "cfdocument fontdirectory with a font that can't be loaded", function() {

			it( title="document still renders", body=function( currentSpec ) {
				document format="pdf" fontdirectory="#fontDir#" filename="#path#out.pdf" overwrite=true {
					writeOutput( "<p>still renders</p>" );
				}
				pdf action="extractText" source="#path#out.pdf" name="local.text" type="string";
				expect( text ).toInclude( "still renders" );
			});

			it( title="the broken font is logged as a warning, non font files are not", body=function( currentSpec ) {
				// a "pdf" log configured outside this test writes somewhere else
				if ( !variables.createdLog ) return;
				// log appenders can write asynchronously, give it a few seconds
				var waited = 0;
				while ( waited < 10000 && ( !fileExists( logFile ) || !len( fileRead( logFile ) ) ) ) {
					sleep( 250 );
					waited += 250;
				}
				expect( fileExists( logFile ) ).toBeTrue( "pdf log file should have been written" );
				var content = fileRead( logFile );
				expect( content ).toInclude( "Broken.ttf" );
				expect( content ).toInclude( "failed to load font" );
				expect( content ).notToInclude( "readme.txt" );
			});

		});
	}
}
