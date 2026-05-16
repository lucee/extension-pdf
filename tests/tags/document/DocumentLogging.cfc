component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentLogging/generated/";
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		directoryCreate( variables.path, true, true );

		configureLuceePdfLog( "trace" );
	}

	function run( testResults, testBox ) {

		describe( "OpenHTMLToPDF logging bridge", function() {

			it( "bridge routes engine output to the pdf log", function() {
				var marker = "MARKER_BRIDGE_ACTIVE_#createUUID()#";
				cflog( log="pdf", type="info", text=marker );

				document format="pdf" filename="#path#bridge.pdf" overwrite=true {
					writeOutput( "<p>render something so the engine logs</p>" );
				}

				var logContent = getLogAfterMarker( marker );
				// engine emits at least the stylesheet-parse INFO and the fast-mode INFO
				expect( logContent ).toInclude( "parse stylesheets" );
				expect( logContent ).toInclude( "fast-mode renderer" );
			});

			it( "source name is trimmed to the last dot segment", function() {
				var marker = "MARKER_SOURCE_TRIM_#createUUID()#";
				cflog( log="pdf", type="info", text=marker );

				document format="pdf" filename="#path#source-trim.pdf" overwrite=true {
					writeOutput( "<p>x</p>" );
				}

				var logContent = getLogAfterMarker( marker );
				// our LuceeXRLogger calls trimSource() — full FQNs must not appear
				expect( logContent ).notToInclude( "com.openhtmltopdf" );
				// short source names should be present in the log lines that did appear
				expect( logContent ).toMatch( ',"(general|load|match|init|render|cascade|css-parse|exception|layout|xml-entities)",' );
			});

			it( "SLF4J '{}' placeholders are substituted from message args", function() {
				var marker = "MARKER_SUBSTITUTION_#createUUID()#";
				cflog( log="pdf", type="info", text=marker );

				document format="pdf" filename="#path#substitution.pdf" overwrite=true {
					writeOutput( "<p>x</p>" );
				}

				var logContent = getLogAfterMarker( marker );
				// engine's LOAD message is "TIME: parse stylesheets {}ms" with a numeric arg —
				// substituted should look like "parse stylesheets 1ms", never literal "{}"
				expect( logContent ).toMatch( "parse stylesheets \d+\s*ms" );
				expect( logContent ).notToInclude( "{}" );
			});

			it( "clean render produces no ERROR-level entries", function() {
				var marker = "MARKER_CLEAN_#createUUID()#";
				cflog( log="pdf", type="info", text=marker );

				document format="pdf" filename="#path#clean.pdf" overwrite=true {
					writeOutput( "<h1>Hello</h1><p>nothing exotic here.</p>" );
				}

				var logContent = getLogAfterMarker( marker );
				// Lucee classic layout starts each line with the level inside quotes
				expect( logContent ).notToMatch( '(?m)^"ERROR"' );
			});

		});

	}

	private string function getLogAfterMarker( required string marker ) {
		var logFile = expandPath( "{lucee-config}/logs/pdf.log" );
		if ( !fileExists( logFile ) ) return "";
		var content = fileRead( logFile );
		var pos = content.findNoCase( arguments.marker );
		if ( pos == 0 ) return "";
		return content.mid( pos + len( arguments.marker ), len( content ) );
	}

	private void function configureLuceePdfLog( required string level ) {
		configImport( type: "server", password: request.SERVERADMINPASSWORD, data: {
			loggers: {
				pdf: {
					appender: "resource",
					appenderArguments: { path: "{lucee-config}/logs/pdf.log" },
					level: arguments.level,
					layout: "classic"
				}
			}
		});
	}

}
