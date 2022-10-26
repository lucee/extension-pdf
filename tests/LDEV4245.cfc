component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath(getCurrentTemplatePath()) & "LDEV4245\";
		afterAll();

		if (!directoryExists(variables.path)) directoryCreate(variables.path);
		document fileName="#path#pdfFile.pdf" name="pdfVar" overwrite=true {
			writeoutput("test pdf file");
		}
	}


	function run( testResults , testBox ) {
		describe( "testcase for LDEV-4245", function() {

			it( title="cfpdf extracttext with name attribute", body=function( currentSpec ) {
				pdf action="extracttext" source="#path#pdfFile.pdf" name="result";

				expect(trim(result)).toInclude("test pdf file");
			});

			it( title="cfpdf extracttext with destination attribute", body=function( currentSpec ) {
				pdf action="extracttext" source="#path#pdfFile.pdf" destination="#path#result.txt" type="string" overwrite="yes";
				var result = fileExists("#path#result.txt");
				expect(result).toBe("true");
				expect(trim(fileread("#path#result.txt"))).toBe("test pdf file");
			});
			
		});
	}

	function afterAll() {
		if (directoryExists(variables.path)) directoryDelete(variables.path, true);
	}
}