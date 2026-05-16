component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf"{

	function beforeAll() {
		document name="variables.testPDF" overwrite=true {
			writeoutput("test pdf file");
		} 
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4025", function( currentSpec ) {
			it(title="Checking error message for using empty string in cfpdf destination attribute", body=function( currentSpec )  {
				try {
					pdf action="addheader" source="#variables.testPDF#" destination="" text="test header";
				}
				catch(any e) {
					var errorMsg = e.message;
				}
				expect(errorMsg.find("it cannot be empty value")).toBeGT(0);
			});
			it(title="Checking error message for space in cfpdf destination attribute", body=function( currentSpec )  {
				try {
					pdf action="addheader" source="#variables.testPDF#" destination="  " text="test header";
				}
				catch(any e) {
					var errorMsg = e.message;
				}
				expect(errorMsg.find("it cannot be empty value")).toBeGT(0);
			});
			it(title="Checking error message for using the null value in cfpdf destination attribute", body=function( currentSpec )  {
				try {
					pdf action="addheader" source="#variables.testPDF#" destination="#nullValue()#" text="test header";

				}
				catch(any e) {
					var errorMsg = e.message;
				}
				expect(errorMsg.find("it cannot be empty value")).toBeGT(0);
			});
		});
	}
}