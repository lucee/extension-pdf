component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll(){
		variables.srcPdf = getTempFile(getTempDirectory(),"LDEV-4329-src", "pdf");
		variables.destPdf = getTempFile(getTempDirectory(),"LDEV-4329-dest", "pdf");
	}

	function afterAll(){
		if ( fileExists( variables.srcPdf ) )
			fileDelete( variables.srcPdf );
		if ( fileExists( variables.destPdf ) )
			fileDelete( variables.destPdf );
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4329", function() {
			it( title='cfpdf action="addHeader" with destination attribute throws incorrect error, overwrite=false',
					skip=true, body=function( currentSpec ) {
				afterAll();
				createPdf( srcPdf );
				createPdf( destPdf );
				pdf action="addheader" source=srcPdf destination=destPdf text="test text";

			});

			it( title='cfpdf action="addFooter" with destination attribute throws incorrect error , overwrite=false',
					skip=true, body=function( currentSpec ) {
				afterAll();
				createPdf( srcPdf );
				createPdf( destPdf );
				pdf action="addFooter" source=srcPdf destination=destPdf text="test text";

			});

			it( title='cfpdf action="addHeader" with destination attribute throws incorrect error, overwrite=true',
					body=function( currentSpec ) {
				afterAll();
				createPdf( srcPdf );
				createPdf( destPdf );
				pdf action="addheader" source=srcPdf destination=destPdf text="test text" overwrite="true";

			});

			it( title='cfpdf action="addFooter" with destination attribute throws incorrect error , overwrite=true',
					body=function( currentSpec ) {
				afterAll();
				createPdf( srcPdf );
				createPdf( destPdf );
				pdf action="addFooter" source=srcPdf destination=destPdf text="test text" overwrite="true";

			});

		});
	}

	private function createPdf( required string filename ) {
		if ( fileExists( arguments.filename ) )
			fileDelete( arguments.filename );
		document filename="#arguments.filename#" {
			echo('<html>
				<body>
					<h1>hello world</h1>
				</body>
			</html>');
		}
	}
}