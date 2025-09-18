component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-1004 evalAtPrint=true", function() {
			xit( title="check placeholders are resolved (modern), evalAtPrint=true" , body=function( currentSpec ) {
				
				local.result = _internalRequest(
					template : "#createURI("LDEV1004")#/placeholders.cfm",
					url: {
						type: "modern",
						evalAtPrint: "true"
					}
				);
				local.pdf=local.result.filecontent_binary;
				expect( isPDFObject( pdf ) ).toBeTrue();

				pdf action="extractText" source="#pdf#" name="local.extractedText";

				expect( extractedText ).notToInclude("currentPageNumber", "placeholder [currentPageNumber] should be resolved");
				expect( extractedText ).notToInclude("totalPageCount", "placeholder [currentPageNumber]   should be resolved");
				expect( extractedText ).notToInclude("totalSectionPageCount", "placeholder [totalSectionPageCount]  should be resolved");
				expect( extractedText ).notToInclude("currentSectionPageNumber", "placeholder [currentSectionPageNumber] should be resolved");

			});

			xit( title="check placeholders are resolved (classic), evalAtPrint=true" , body=function( currentSpec ) {
				
				local.result = _internalRequest(
					template : "#createURI("LDEV1004")#/placeholders.cfm",
					url: {
						type: "classic",
						evalAtPrint: "true"
					}
				);
				local.pdf=local.result.filecontent_binary;
				expect( isPDFObject( pdf ) ).toBeTrue();

				pdf action="extractText" source="#pdf#" name="local.extractedText";

				expect( extractedText ).notToInclude("currentPageNumber", "placeholder [currentPageNumber] should be resolved");
				expect( extractedText ).notToInclude("totalPageCount", "placeholder [currentPageNumber]   should be resolved");
				expect( extractedText ).notToInclude("totalSectionPageCount", "placeholder [totalSectionPageCount]  should be resolved");
				expect( extractedText ).notToInclude("currentSectionPageNumber", "placeholder [currentSectionPageNumber] should be resolved");

			});
		});

		describe("Testcase for LDEV-1004 evalAtPrint=false", function() {

			it( title="check placeholders are resolved (modern), evalAtPrint=false" , body=function( currentSpec ) {
				
				local.result = _internalRequest(
					template : "#createURI("LDEV1004")#/placeholders.cfm",
					url: {
						type: "modern",
						evalAtPrint: "false"
					}
				);
				local.pdf=local.result.filecontent_binary;
				expect( isPDFObject( pdf ) ).toBeTrue();

				pdf action="extractText" source="#pdf#" name="local.extractedText";

				expect( extractedText ).notToInclude("currentPageNumber", "placeholder [currentPageNumber] should be resolved");
				expect( extractedText ).notToInclude("totalPageCount", "placeholder [currentPageNumber]   should be resolved");
				expect( extractedText ).notToInclude("totalSectionPageCount", "placeholder [totalSectionPageCount]  should be resolved");
				expect( extractedText ).notToInclude("currentSectionPageNumber", "placeholder [currentSectionPageNumber] should be resolved");

			});

			it( title="check placeholders are resolved (classic), evalAtPrint=false" , body=function( currentSpec ) {
				
				local.result = _internalRequest(
					template : "#createURI("LDEV1004")#/placeholders.cfm",
					url: {
						type: "classic",
						evalAtPrint: "false"
					}
				);
				local.pdf=local.result.filecontent_binary;
				expect( isPDFObject( pdf ) ).toBeTrue();

				pdf action="extractText" source="#pdf#" name="local.extractedText";

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
