component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {

		variables.testPdf = getTempFile( getTempDirectory(), "ldev4692", "pdf" );
		variables.testCode = createUniqueID();
		variables.password = createUniqueID();

		document fileName="#testPdf#" overwrite="true" {
			writeoutput(" test pdf file [#variables.testCode#]" );
		}
	}

	function run( testResults , testBox ) {
		describe( "testcase for LDEV-3048", function() {

			it( title="cfpdf check password routines, protect, removePassword", body=function( currentSpec ) {

				pdf action="extractText" source="#testPdf#" name="local.extractedText";
				expect( extractedText ).toInclude( testCode );

				pdf action="protect" source="#testPdf#" destination="#testPdf#" newUserPassword="#variables.password#" overwrite="true";
				pdf action="extractText" source="#testPdf#" name="local.extractedText" password="#variables.password#";
				expect( extractedText ).toInclude( testCode );
				expect(function(){
					pdf action="extractText" source="#testPdf#" name="local.extractedText";
				}).toThrow(); // no password

				pdf action="removePassword" source="#testPdf#" destination="#testPdf#" password="#variables.password#" overwrite="true";
				pdf action="extractText" source="#testPdf#" name="local.extractedText";
				expect( extractedText ).toInclude( testCode );

			});

		});
	}

	function afterAll(){
		if ( fileExists( variables.testPdf ) )
			fileDelete( variables.testPdf );
	};
}
