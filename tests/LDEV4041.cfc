component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" skip=true{
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4041", function( currentSpec ) {
			it(title="cfpdf action=read with the AES-256 encrypted pdf file", body=function( currentSpec )  {
				try {
					var hasError = false;
					pdf action = "read"
						source ="#getDirectoryFromPath(getCurrentTemplatePath())#LDEV4041_pdf/AES_256.pdf"
						name="local.res";
				}
				catch(any e) {
					var hasError = true;
					local.res = e;
				}
				expect(hasError).toBefalse();
				expect(isPDFObject(res)).toBeTrue();
			});
		});
	}
}