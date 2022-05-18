component extends = "org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath(getCurrentTemplatePath()) & "LDEV3015";
		if (!directoryExists(variables.path)) directoryCreate("#variables.path#");
	}

	function run( testResults , testbox ) {
		describe( "Testcase for LDEV-3015" ,function () {
			it( title = "Checking action = extracttext with pdf without style", body = function ( currentSpec ) {
				cfdocument(format="pdf", filename="#variables.path#/withoutStyle.pdf", overwrite="true") {
					writeOutput("<p>This is PDF example document for the test without font styles.<p>");
				}
				cfpdf(action = "extracttext", type="string", name="local.extractResult", source="#variables.path#/withoutStyle.pdf");
				expect(trim(extractResult)).toBe("This is PDF example document for the test without font styles.");
			});
			it( title = "Checking action = extracttext with pdf with style", body=function ( currentSpec ) {	
				cfdocument(format="pdf", filename="#variables.path#/withStyle.pdf", overwrite="true") {
					writeOutput("<p>This is <strong>PDF example</strong> document for <small>the test</small> with font styles.</p>");
				}
				cfpdf(action = "extracttext", type="string", name="local.extractResult", source="#variables.path#/withStyle.pdf");
				expect(trim(local.extractResult)).toBe("This is PDF example document for the test with font styles.");
			});
		});
	}

	function afterAll() {
		if (directoryExists(variables.path)) directoryDelete(variables.path,true);
	}
}