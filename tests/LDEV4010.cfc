component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath(getCurrentTemplatePath()) & "LDEV4010\";
		afterAll();
		
		if (!directoryExists(variables.path)) directoryCreate(variables.path);

		loop list="one,two" item="name" {
			document fileName="#path#pdfFile#name#.pdf" name="pdfVar#name#" overwrite=true {
				writeoutput("test pdf file #name#");
			}
		}
	}


	function run( testResults , testBox ) {
		describe( "test case for LDEV-4010", function() {
			it( title="cfpdf merge with destination attribute and pdf file as source", body=function( currentSpec ) {
				pdf action="merge" source="#path#pdfFileone.pdf,#path#pdfFiletwo.pdf" destination="#path#merged.pdf" overwrite="yes";

				expect(isPDFFile("#path#merged.pdf")).toBeTrue();
			});
			it( title="cfpdf merge with destination attribute and cfpdfparam pdf file as source", body=function( currentSpec ) {
				pdf action="merge" destination="#path#paramMerged.pdf" overwrite="yes" {
					pdfparam source="#path#pdfFileone.pdf";
					pdfparam source="#path#pdfFiletwo.pdf";
				}

				expect(isPDFFile("#path#paramMerged.pdf")).toBeTrue();
			});
			it( title="cfpdf merge with name attribute and pdf file as source", body=function( currentSpec ) {
				pdf action="merge" source="#path#pdfFileone.pdf,#path#pdfFiletwo.pdf" name="local.mergedPdfVar";

				expect(isPDFObject(mergedPdfVar)).toBeTrue();
			});
			it( title="cfpdf merge with name attribute and two cfpdfparam pdf files as source", body=function( currentSpec ) {
				pdf action="merge" name="local.paramMergedPdfVar" {
					pdfparam source="#path#pdfFileone.pdf";
					pdfparam source="#path#pdfFiletwo.pdf";
				}

				expect(isPDFObject(paramMergedPdfVar)).toBeTrue();
			});

			it( title="cfpdf merge with name attribute and single cfpdfparam pdf file as source", body=function( currentSpec ) {
				pdf action="merge" name="local.paramMergedPdfVar" {
					pdfparam source="#path#pdfFileone.pdf";
				}

				expect(isPDFObject(paramMergedPdfVar)).toBeTrue();
			});

			it( title="cfpdf merge with destination attribute and pdf variable as source", body=function( currentSpec ) {
				pdf action="merge" source="pdfVarone,pdfVartwo" destination="#path#merged2.pdf" overwrite="yes";

				expect(isPDFFile("#path#merged2.pdf")).toBeTrue();
			});
			it( title="cfpdf merge with destination attribute and cfpdfparam pdf variable as source", body=function( currentSpec ) {
				pdf action="merge" destination="#path#paramMerged2.pdf" overwrite="yes" {
					pdfparam source="pdfVarone";
					pdfparam source="pdfVartwo";
				}

				expect(isPDFFile("#path#paramMerged2.pdf")).toBeTrue();
			});
			it( title="cfpdf merge with name attribute and pdf variable as source", body=function( currentSpec ) {
				pdf action="merge" source="pdfVarone,pdfVartwo" name="local.mergedPdfVar";

				expect(isPDFObject(mergedPdfVar)).toBeTrue();
			});
			it( title="cfpdf merge with name attribute and cfpdfparam pdf variable as source", body=function( currentSpec ) {
				pdf action="merge"  name="local.paramMergedPdfVar" {
					pdfparam source="pdfVarone";
					pdfparam source="pdfVartwo";
				}
				
				expect(isPDFObject(paramMergedPdfVar)).toBeTrue();
			});
		});
	}

	function afterAll() {
		if (directoryExists(variables.path)) directoryDelete(variables.path, true);
	}
}