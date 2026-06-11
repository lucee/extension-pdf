component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-3575", function() {
            it( title="LDEV-3575, cfdocument with orientation and fontdirectory attribute", body=function( currentSpec ) {
                try {
                    document format="pdf" orientation="landscape" fontdirectory="#getDirectoryFromPath(getCurrentTemplatePath())#" name="local.test3575" {
                        writeoutput("test Pdf");
                    }
                }
                catch(any e) {
                    local.test3575 = e.message;
                }
                expect( isPDFObject(test3575) ).toBeTrue();
            });
        });
    }
}
