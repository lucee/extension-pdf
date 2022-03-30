component extends = "org.lucee.cfml.test.LuceeTestCase" labels="pdf" {
	function run( testResults , testBox ) {
		describe( "Test case for LDEV-3928", function() {
			it(title="CFPDF action=extracttext with https resource", body=function( currentSpec ) {
				pdf action="extracttext" source="https://github.com/lucee/Lucee/raw/master/test/functions/assets/simple.pdf" name="res" type="string";
				expect(trim(reReplaceNoCase(res,"[\r\n]+"," ", "all"))).toBe("Title This is the link");
			});
		});
	}
}