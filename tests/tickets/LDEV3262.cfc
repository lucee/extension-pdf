component extends = "org.lucee.cfml.test.LuceeTestCase" labels="pdf" skip=true{
	function run( testResults, textbox ) {
		describe("testcase for LDEV-3262", function() {
			it(title="checking content-length in response header for cfdocument", body=function( currentSpec ) {
				local.result = _internalRequest(
					template : "#createURI("LDEV3262")#/LDEV3262.cfm"
				);
				expect(result["headers"]).tohavekey("content-length");
				expect(result["headers"]["content-length"]).toBeBetween(800, 1000);
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}