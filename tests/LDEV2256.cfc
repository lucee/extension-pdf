component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

    function beforeAll() {
        variables.dir = getDirectoryFromPath(getCurrentTemplatePath()) & "LDEV2256";
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
        describe( "Testcase for LDEV-2256", function() {
            it( title="Checking the cfpdf copyFrom - pdf with an image on pdf content", body = function( currentSpec ) {
                pdf action="addwatermark" source="#variables.dir#/main.pdf" copyFrom="#variables.dir#/copyFrom.pdf" name="name";

                expect(isPDFObject(name)).toBeTrue();
            });
        });
    }

    function afterAll() {
        if (directoryExists(variables.dir)) directoryDelete(variables.dir, true);
    }
}