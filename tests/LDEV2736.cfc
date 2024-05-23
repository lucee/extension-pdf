component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

    function beforeAll() {
        variables.dir = getTempDirectory() & "/LDEV2256";
        if (!directoryExists(variables.dir)) directoryCreate(variables.dir);

        document filename="#variables.dir#/main.pdf",overwrite="true",format="pdf" {
            writeoutput("test main pdf");
        }

        document filename="#variables.dir#/copyFrom.pdf" overwrite="true" format="pdf" name="copyFrom" {
            writeoutput("test watermark pdf <br>");
            writeoutput('<img src="https://raw.githubusercontent.com/lucee/Lucee/6.0/test/functions/images/lucee.png">');
        }
    }

    function run( testResults , testBox ) {
        describe( "Testcase for LDEV-2736", function() {
            it( title="cfpdf - removewatermark didn't work", body = function( currentSpec ) {
                pdf action="addwatermark" source="#variables.dir#/main.pdf" copyFrom="#variables.dir#/copyFrom.pdf" name="local.name";

                expect( function(){
                    pdf action="removewatermark" source="#variables.dir#/main.pdf";  //needs name or destination
                }).toThrow();

                pdf action="removewatermark" source="#variables.dir#/main.pdf" name="local.out";
                expect( isPDFObject( out ) ).toBeTrue();

                var dest = "#variables.dir#/removed.pdf";
                pdf action="removewatermark" source="#variables.dir#/main.pdf" destination="#dest#";

                expect( isPDFObject(dest)).toBeTrue();
            });
        });
    }

    function afterAll() {
        if (directoryExists(variables.dir)) directoryDelete(variables.dir, true);
    }
}