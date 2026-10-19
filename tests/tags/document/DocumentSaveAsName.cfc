component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.uri = contractPath( getDirectoryFromPath( getCurrentTemplatePath() ) ) & "/DocumentSaveAsName/render.cfm";
	}

	private struct function requestPdf( required string name ) {
		return _internalRequest( template: variables.uri, urls: { name: arguments.name } );
	}

	private string function getDisposition( required struct res ) {
		var h = res.headers ?: {};
		return h[ "Content-Disposition" ] ?: "";
	}

	function run( testResults, testBox ) {
		describe( "cfdocument saveAsName Content-Disposition", function() {

			it( title="plain name is used as is", body=function( currentSpec ) {
				var res = requestPdf( "report.pdf" );
				expect( getDisposition( res ) ).toBe( 'inline; filename="report.pdf"' );
			});

			it( title="CR/LF can not inject headers", body=function( currentSpec ) {
				var res = requestPdf( "evil.pdf" & chr( 13 ) & chr( 10 ) & "X-Injected: yes" );
				var cd = getDisposition( res );
				expect( cd ).notToInclude( chr( 10 ) );
				expect( cd ).notToInclude( chr( 13 ) );
				expect( structKeyExists( res.headers, "X-Injected" ) ).toBeFalse();
				expect( cd ).toBe( 'inline; filename="evil.pdfX-Injected: yes"' );
			});

			it( title="quotes can not break out of the filename parameter", body=function( currentSpec ) {
				var res = requestPdf( 'a"; filename="b.exe' );
				expect( getDisposition( res ) ).toBe( 'inline; filename="a_; filename=_b.exe"' );
			});

			it( title="non ascii names get an RFC 5987 filename*", body=function( currentSpec ) {
				var res = requestPdf( "Grüezi.pdf" );
				expect( getDisposition( res ) ).toBe( "inline; filename=""Gr_ezi.pdf""; filename*=UTF-8''Gr%C3%BCezi.pdf" );
			});

		});
	}
}
