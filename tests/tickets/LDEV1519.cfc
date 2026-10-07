component extends="org.lucee.cfml.test.LuceeTestCase"  labels="pdf"{
    function testPDFWaterMark (){
        cfdocument (format="PDF", name="local.test") {
            echo("<H1>I am a watermark test</H1>");
        }
       
        pdf action = "addWaterMark"
            source ="test"
            image="#getDirectoryFromPath(getCurrentTemplatePath())#LDEV1519/image.jpg"
            pages="1"
            name="watermarkedPDF"
            overwrite="true"
            position="0,0" 
            rotation="45";
            
        expect( isPdfObject( watermarkedPDF ) ).toBeTrue();
    }
}