component extends="org.lucee.cfml.test.LuceeTestCase" {
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-3836", function() {
            it( title="PDF modern Engine Chinese font test with font directory attribute", body=function( currentSpec ) {
                processingdirective pageEncoding="UTF-8";
                document type="modern" fontdirectory="LDEV3836_fonts" name="test" {
                    echo(
                        '<span>PDF modern Engine Chinese font test: </span>
                        <span style="font-family:Muyao-Softbrush;">, </span>
                        <span style="font-family:Muyao-Softbrush;"></span>'
                    );
                };

                pdf action="extractText" source="test" name="res" type="string";

                expect(trim(res)).toBe("PDF modern Engine Chinese font test: ,");
            });
        });
    }
}