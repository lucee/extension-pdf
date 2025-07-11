component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-1500", function() {
			it( title="LDEV-1500, cfdocument with orientation and bookmark attribute", body=function( currentSpec ) {
				document format="pdf" orientation="portrait" bookmark="true" htmlbookmark="true" name="local.test1500" {
					documentSection name="section1" {
						writeoutput("test Pdf test");
					}
					documentSection name="section2" {
						writeoutput("test Pdf tes");
					}
				}

				pdf action="extractBookmarks" source="test1500" name="local.bookmarks";
				expect(bookmarks[1].get("Title")).toBe("section1");
				expect(bookmarks[2].get("Title")).toBe("section2");
			});
		});
	}
}
