component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath(getCurrentTemplatePath()) & "LDEV4048";
		afterAll();
		if (!directoryExists(variables.path)) directoryCreate(variables.path);
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4041", function( currentSpec ) {
			beforeEach(function( currentSpec ){
				cfdocument( format="PDF", filename="#variables.path#/test.pdf", overwrite="true", name="variables.PDFvar"){
					writeoutput("test pdf");
				};
			});
			// pdf file as source
			it(title="cfpdf setinfo with pdf file as source without destination and name attribute", body=function( currentSpec )  {
				pdf action="setinfo" source="#variables.path#/test.pdf" info="#{"author":"lucee"}#";

				pdf action="getInfo" source="#variables.path#/test.pdf" name="local.pdfInfo";
				expect(pdfinfo.author).toBe("lucee");
			});
			it(title="cfpdf setinfo with destination and pdf file as source", body=function( currentSpec )  {
				pdf action="setinfo" source="#variables.path#/test.pdf" info="#{"author":"lucee"}#" destination="#path#/testRes.pdf";

				pdf action="getInfo" source="#variables.path#/testRes.pdf" name="local.pdfInfo";
				expect(pdfinfo.author).toBe("lucee");
			});
			it(title="cfpdf setinfo with name and pdf file as source", body=function( currentSpec )  {
				pdf action="setinfo" source="#variables.path#/test.pdf" info="#{"author":"lucee"}#" name="local.infoUpdatedPDF";

				pdf action="getInfo" source="infoUpdatedPDF" name="local.pdfInfo";
				expect(pdfinfo.author).toBe("lucee");
			});
			// pdf variable string value as source
			it(title="cfpdf setinfo with pdf variable string value as source without destination and name attribute", body=function( currentSpec )  {
				pdf action="setinfo" source="variables.PDFvar" info="#{"author":"lucee"}#";

				pdf action="getInfo" source="variables.PDFvar" name="local.pdfInfo";
				expect(pdfinfo.author).toBe("lucee");
			});
			it(title="cfpdf setinfo with destination and pdf variable string value as source", body=function( currentSpec )  {
				pdf action="setinfo" source="variables.PDFvar" info="#{"author":"lucee"}#" destination="#path#/testRes1.pdf" overwrite=true;

				pdf action="getInfo" source="#variables.path#/testRes1.pdf" name="local.pdfInfo";
				expect(pdfinfo.author).toBe("lucee");
			});
			it(title="cfpdf setinfo with name and pdf variable string value as source", body=function( currentSpec )  {
				pdf action="setinfo" source="variables.PDFvar" info="#{"author":"lucee"}#" name="local.infoUpdatedPDF";

				pdf action="getInfo" source="infoUpdatedPDF" name="local.pdfInfo";
				expect(pdfinfo.author).toBe("lucee");
			});
			// pdf binary value as source
			it(title="cfpdf setinfo with destination and pdf binary value as source", body=function( currentSpec )  {
				pdf action="setinfo" source="#variables.PDFvar#" info="#{"author":"lucee"}#" destination="#path#/testRes1.pdf" overwrite=true;
				
				pdf action="getInfo" source="#variables.path#/testRes1.pdf" name="local.pdfInfo";
				expect(pdfinfo.author).toBe("lucee");
			});
			it(title="cfpdf setinfo with name and pdf binary value as source", body=function( currentSpec )  {
				pdf action="setinfo" source="#variables.PDFvar#" info="#{"author":"lucee"}#" name="local.infoUpdatedPDF";
				
				pdf action="getInfo" source="infoUpdatedPDF" name="local.pdfInfo";
				expect(pdfinfo.author).toBe("lucee");
			});
			it(title="cfpdf setinfo with pdf binary value as source without destination and name attribute throws", body=function( currentSpec )  {
				expect( () => {	pdf action="setinfo" source="#variables.PDFvar#" info="#{"author":"lucee"}#";} ).toThrow();
			});
		});
	}

	function afterAll() {
		if (directoryExists(variables.path)) directoryDelete(variables.path, true);
	}
}