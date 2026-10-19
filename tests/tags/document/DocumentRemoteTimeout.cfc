component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentRemoteTimeout/generated/";
		if ( !directoryExists( path ) ) directoryCreate( path, true, true );

		// a server socket that accepts connections (kernel backlog) but never answers
		variables.blackhole = createObject( "java", "java.net.ServerSocket" ).init(
			0, 50, createObject( "java", "java.net.InetAddress" ).getLoopbackAddress() );
		variables.base = "http://127.0.0.1:" & blackhole.getLocalPort() & "/";

		// lower the remote fetch timeout for this test (default 30s)
		variables.system = createObject( "java", "java.lang.System" );
		variables.oldTimeout = system.getProperty( "lucee.pdf.remote.timeout" );
		system.setProperty( "lucee.pdf.remote.timeout", "2000" );
		variables.maxMillis = 20000;
	}

	function afterAll() {
		if ( isNull( variables.oldTimeout ) ) variables.system.clearProperty( "lucee.pdf.remote.timeout" );
		else variables.system.setProperty( "lucee.pdf.remote.timeout", variables.oldTimeout );
		variables.blackhole.close();
	}

	function run( testResults, testBox ) {
		describe( "cfdocument remote fetches time out instead of hanging", function() {

			it( title="unresponsive <img> host", body=function( currentSpec ) {
				var start = getTickCount();
				document format="pdf" filename="#path#img.pdf" overwrite=true {
					writeOutput( '<p>before image</p><img src="#base#image.png" /><p>after image</p>' );
				}
				expect( getTickCount() - start ).toBeLT( maxMillis );
				pdf action="extractText" source="#path#img.pdf" name="local.text" type="string";
				expect( text ).toInclude( "after image" );
			});

			it( title="unresponsive stylesheet host", body=function( currentSpec ) {
				var start = getTickCount();
				document format="pdf" filename="#path#css.pdf" overwrite=true {
					writeOutput( '<html><head><link rel="stylesheet" type="text/css" href="#base#style.css" /></head><body><p>styled</p></body></html>' );
				}
				expect( getTickCount() - start ).toBeLT( maxMillis );
				pdf action="extractText" source="#path#css.pdf" name="local.text" type="string";
				expect( text ).toInclude( "styled" );
			});

			it( title="unresponsive src host fails with an error", body=function( currentSpec ) {
				var start = getTickCount();
				expect( function() {
					document format="pdf" src="#base#page.html" filename="#path#src.pdf" overwrite=true;
				}).toThrow();
				expect( getTickCount() - start ).toBeLT( maxMillis );
			});

		});
	}
}
