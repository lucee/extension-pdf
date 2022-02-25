component extends="org.lucee.cfml.test.LuceeTestCase"{

    function beforeAll() {
        variables.path = getDirectoryFromPath(getCurrentTemplatePath()) & "LDEV3587/";
        variables.file = path & "testPDF.pdf";

        if (!directoryExists(variables.path)) directorycreate(variables.path);

        document filename="#variables.file#" overwrite="true"{
            writeoutput("test PDF file");
        }
    }

    function run( testResults , testBox ) {
        describe( "test case for LDEV-3587", function() {
            it(title = "cfpdf addheader without desstination and name attribute, addheader to source file ", body = function( currentSpec ) {
                try {
                    res = "Added header to source file";
                    pdf action="addHeader" text="test context" source="#variables.file#";
                    writeDump(test);
                }
                catch(any e) {
                    res = e.message;
                }
                expect(res).toBe("Added header to source file");
            });
            it(title = "cfpdf addheader with name attribute, addheader to variable", body = function( currentSpec ) {
                try {
                    pdf action="addheader" source="#variables.file#" text="test content" name="local.pdfObj";
                }
                catch(any e) {
                    local.pdfObj = e.message;
                }
                expect(isPDFObject(pdfObj)).toBeTrue();
            });
            it(title = "cfpdf addheader with destination attribute, addheader to destination file", body = function( currentSpec ) {
                var destinationFile = path & "testdestination.pdf";
                pdf action="addHeader" source="#variables.file#" text="test content" destination="#destinationFile#" overwrite="yes";
                expect(fileExists("#destinationFile#")).toBeTrue();
            });
        });
    }

    function afterAll() {
        if (directoryExists(variables.path)) directoryDelete(variables.path,true);
    }
}