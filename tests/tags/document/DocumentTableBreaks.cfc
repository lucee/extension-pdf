component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentTableBreaks/generated/";
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path );
	}

	private function generateLargeTable( numeric rowCount=100 ) {
		var html = "<table border='1' cellpadding='5' cellspacing='0' width='100%'>";
		html &= "<tr><th>Row</th><th>Data</th></tr>";
		for ( var i = 1; i <= rowCount; i++ ) {
			html &= "<tr><td>Row #i#</td><td>Content for row #i#</td></tr>";
		}
		html &= "</table>";
		return html;
	}

	private function generateMarkerTable( numeric rowCount=60 ) {
		var html = "<table border='1' cellpadding='5' cellspacing='0' width='100%'>";
		for ( var i = 1; i <= rowCount; i++ ) {
			html &= "<tr><td>MARKER_#i#</td></tr>";
		}
		html &= "</table>";
		return html;
	}

	private function generateTallRowTable( numeric rowCount=10 ) {
		var html = "<table border='1' width='100%'>";
		for ( var i = 1; i <= rowCount; i++ ) {
			html &= "<tr><td style='height:150px;'>Tall Row #i#</td></tr>";
		}
		html &= "</table>";
		return html;
	}

	private function generateRowspanTheadTable( numeric totalRows=60 ) {
		// rowspan cell crosses a page boundary while thead repeats on continuation pages.
		// Exercises openhtmltopdf 1.1.35 fix for thead/rowspan overlap (PR ##141).
		var html = "<table border='1' cellpadding='5' cellspacing='0' width='100%'>";
		html &= "<thead><tr><th>Group</th><th>Item</th><th>Detail</th></tr></thead>";
		html &= "<tbody>";
		html &= "<tr><td rowspan='#totalRows#' style='background:##ffeecc;'>GROUP_HEADER</td>";
		html &= "<td>Item 1</td><td>Detail 1</td></tr>";
		for ( var i = 2; i <= totalRows; i++ ) {
			html &= "<tr><td>Item #i#</td><td>Detail #i#</td></tr>";
		}
		html &= "</tbody></table>";
		return html;
	}

	private function generateForumReproTable( boolean malformed=false ) {
		var html = "<h1>Table Break Test</h1>";
		for ( var t = 1; t <= 2; t++ ) {
			html &= "<table>";
			if ( malformed ) {
				html &= "<thead><th><th width='200'>Column 1</th><th width='200'>Column 2</th><th width='200'>Column 3</th></th></thead>";
			} else {
				html &= "<thead><tr><th width='200'>Column 1</th><th width='200'>Column 2</th><th width='200'>Column 3</th></tr></thead>";
			}
			html &= "<tbody>";
			for ( var i = 1; i <= 20; i++ ) {
				html &= "<tr><td width='200'>#i#</td><td width='200'>again #i#</td><td width='200'>one more time #i#</td></tr>";
			}
			html &= "</tbody></table>";
		}
		return html;
	}

	function run( testResults, testBox ) {

		describe( "cfdocument table page breaks", function() {

			it( title="large table should span multiple pages", body=function( currentSpec ) {
				var file = "#path#table_breaks.pdf";

				document format="pdf" pagetype="A4" filename="#file#" overwrite=true {
					writeOutput( generateLargeTable() );
				}

				expect( isPDFFile( file ) ).toBeTrue();

				pdf action="getInfo" source="#file#" name="local.info";
				expect( info.totalPages ).toBeGT( 1, "a 100 row table should span multiple pages" );

				pdf action="extractText" source="#file#" name="local.text";
				expect( text ).toInclude( "Row 1" );
				expect( text ).toInclude( "Row 100" );
			});

			it( title="table content should not be lost at page boundaries", body=function( currentSpec ) {
				var file = "#path#table_no_loss.pdf";
				var rowCount = 60;

				document format="pdf" pagetype="A4" filename="#file#" overwrite=true {
					writeOutput( generateMarkerTable( rowCount ) );
				}

				expect( isPDFFile( file ) ).toBeTrue();

				pdf action="extractText" source="#file#" name="local.text";

				for ( var i = 1; i <= rowCount; i++ ) {
					expect( text ).toInclude( "MARKER_#i#", "row #i# should not be lost at a page boundary" );
				}
			});

			it( title="table with tall rows should span pages", body=function( currentSpec ) {
				var file = "#path#table_tall_rows.pdf";

				document format="pdf" pagetype="A4" filename="#file#" overwrite=true {
					writeOutput( generateTallRowTable() );
				}

				expect( isPDFFile( file ) ).toBeTrue();

				pdf action="getInfo" source="#file#" name="local.info";
				expect( info.totalPages ).toBeGT( 1, "10 rows at 150px height should span multiple pages" );

				pdf action="extractText" source="#file#" name="local.text";
				expect( text ).toInclude( "Tall Row 1" );
				expect( text ).toInclude( "Tall Row 10" );
			});

		});

		describe( "cfdocument table rendering - thead and rowspan", function() {

			it( title="rowspan cell with thead spans multiple pages", body=function( currentSpec ) {
				var file = "#path#rowspan_thead.pdf";
				var rowCount = 60;

				document format="pdf" pagetype="A4" filename="#file#" overwrite=true {
					writeOutput( generateRowspanTheadTable( rowCount ) );
				}

				expect( isPDFFile( file ) ).toBeTrue();

				pdf action="getInfo" source="#file#" name="local.info";
				expect( info.totalPages ).toBeGT( 1, "#rowCount# rows should span multiple pages" );

				pdf action="extractText" source="#file#" name="local.text";
				expect( text ).toInclude( "GROUP_HEADER", "rowspan cell content should survive page break" );
				expect( text ).toInclude( "Group", "thead should be present" );
				for ( var i = 1; i <= rowCount; i++ ) {
					expect( text ).toInclude( "Item #i#", "row #i# should not be lost at a page boundary" );
				}
			});

		});

		describe( "cfdocument table rendering - forum reproduction", function() {

			it( title="two consecutive tables with thead", body=function( currentSpec ) {
				var file = "#path#forum_repro.pdf";

				document format="pdf" pagetype="A4" filename="#file#" overwrite=true {
					writeOutput( generateForumReproTable( false ) );
				}

				expect( isPDFFile( file ) ).toBeTrue();

				pdf action="extractText" source="#file#" name="local.text";
				expect( text ).toInclude( "Column 1", "thead should be present" );
				expect( text ).toInclude( "one more time 1" );
				expect( text ).toInclude( "one more time 20" );
			});

			it( title="two consecutive tables with malformed thead", body=function( currentSpec ) {
				var file = "#path#forum_repro_malformed.pdf";

				document format="pdf" pagetype="A4" filename="#file#" overwrite=true {
					writeOutput( generateForumReproTable( true ) );
				}

				expect( isPDFFile( file ) ).toBeTrue();

				pdf action="extractText" source="#file#" name="local.text";
				expect( text ).toInclude( "one more time 1" );
				expect( text ).toInclude( "one more time 20" );
			});

		});
	}

}
