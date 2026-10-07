component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" skip="true"{
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-3781", function() {
            it( title="Checking cfdocument saveAsName attribute", body=function( currentSpec ) {
                var path = "http://"&cgi.SERVER_NAME & getDirectoryFromPath(cgi.SCRIPT_NAME);
                http url="#path#/LDEV3781/LDEV3781.cfm" result="local.res";
                expect(res.responseheader["Content-Disposition"]).toBe('inline; filename="test3781.pdf"');
            });
        });
    }
}
