component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentPageMarkIndex/generated/";
		if ( !directoryExists( path ) ) directoryCreate( path, true, true );
	}

	// PDFPageMark lives in the extension bundle, load it through the extension's maven coordinate
	private function newPageMark( required string template ) {
		var version = "";
		var q = extensionList();
		loop query=q {
			if ( q.name contains "PDF Extension" ) version = q.version;
		}
		return createObject( "java", "org.lucee.extension.pdf.PDFPageMark", { maven: [ "org.lucee:pdf:" & version ] } )
			.init( javaCast( "int", -1 ), arguments.template, javaCast( "boolean", true ) );
	}

	function run( testResults, testBox ) {
		describe( "cfdocument header/footer template index", function() {

			it( title="index past the last template uses the last template", body=function( currentSpec ) {
				var mark = newPageMark( "first" );
				mark.addHtmlTemplate( "second" );
				mark.setIndex( javaCast( "int", 5 ) );
				expect( mark.getHtmlTemplate() ).toBe( "second" );
			});

			it( title="negative index uses the first template", body=function( currentSpec ) {
				var mark = newPageMark( "first" );
				mark.addHtmlTemplate( "second" );
				mark.setIndex( javaCast( "int", -1 ) );
				expect( mark.getHtmlTemplate() ).toBe( "first" );
			});

			it( title="index in range picks that template", body=function( currentSpec ) {
				var mark = newPageMark( "first" );
				mark.addHtmlTemplate( "second" );
				mark.addHtmlTemplate( "third" );
				mark.setIndex( javaCast( "int", 1 ) );
				expect( mark.getHtmlTemplate() ).toBe( "second" );
			});

			it( title="evalAtPrint header and footer render on every page", body=function( currentSpec ) {
				document format="pdf" filename="#path#hf.pdf" overwrite=true {
					documentitem type="header" evalAtPrint=true {
						writeOutput( "HEAD #cfdocument.currentpagenumber# of #cfdocument.totalpagecount#" );
					}
					documentitem type="footer" evalAtPrint=true {
						writeOutput( "FOOT #cfdocument.currentpagenumber#" );
					}
					for ( var i = 1; i <= 3; i++ ) {
						writeOutput( "<p>page #i#</p>" );
						if ( i < 3 ) documentitem type="pagebreak";
					}
				}
				pdf action="getInfo" source="#path#hf.pdf" name="local.info";
				expect( info.totalPages ).toBe( 3 );
				for ( var p = 1; p <= 3; p++ ) {
					pdf action="extractText" source="#path#hf.pdf" pages="#p#" name="local.t" type="string";
					expect( t ).toInclude( "HEAD #p# of 3" );
					expect( t ).toInclude( "FOOT #p#" );
				}
			});

		});
	}
}
