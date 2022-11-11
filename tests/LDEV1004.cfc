component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" skip="true"{
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-1004", function() {
			it( title="check placeholders are resolved", body=function( currentSpec ) {
				
				local.result = _internalRequest(
					template : "#createURI("LDEV1004")#/placeholders.cfm"
				);
				local.pdf=local.result.filecontent_binary;
				expect( isPDFObject( pdf ) ).toBeTrue();

				pdf action="extracttext" source="#pdf#" name="local.extractedText";

				expect( extractedText ).notToInclude("currentPageNumber", "placeholder [currentPageNumber] should be resolved");
				expect( extractedText ).notToInclude("totalPageCount", "placeholder [currentPageNumber]   should be resolved");
				expect( extractedText ).notToInclude("totalSectionPageCount", "placeholder [totalSectionPageCount]  should be resolved");
				expect( extractedText ).notToInclude("currentSectionPageNumber", "placeholder [currentSectionPageNumber] should be resolved");

			});
		});
	}

	private string function createURI(string calledName){

		var baseURI = getDirectoryFromPath( contractPath( getCurrentTemplatePath() ) );
		return baseURI&""&calledName;
	}
}
