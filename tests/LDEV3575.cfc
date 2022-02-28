component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-3781", function() {
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

            it( title="LDEV-1500, cfdocument with orientation and bookmark attribute", body=function( currentSpec ) {
                document format="pdf" orientation="portrait" bookmark="true" htmlbookmark="true" name="local.test1500" {
                    documentSection name="section1" {
                        writeoutput("test Pdf test");
                    }
                    documentSection name="section2" {
                        writeoutput("test Pdf tes");
                    }
                }
                var reader = createObject("java", "com.lowagie.text.pdf.PdfReader").init(test1500);
                var bookmarks = createObject("java","com.lowagie.text.pdf.SimpleBookmark").getBookmark(reader);

                expect(bookmarks[1].get("Title")).toBe("section1");
                expect(bookmarks[2].get("Title")).toBe("section2");
            });
        });
    }
}