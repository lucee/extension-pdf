component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFFormPopulate/generated/";
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );

		// Generate a PDF with real AcroFields from HTML form elements
		document fileName="#path#form.pdf" overwrite=true {
			writeOutput( '
				<html><body>
				<h1>Test Form</h1>
				<form>
					<p>First: <input type="text" name="firstName" value="" /></p>
					<p>Last: <input type="text" name="lastName" value="" /></p>
					<p>Email: <input type="text" name="email" value="" /></p>
					<p>Phone: <input type="text" name="phone" value="" /></p>
				</form>
				</body></html>
			' );
		}

		// Create test XML data
		variables.testXML = '<?xml version="1.0"?>
<fields>
	<firstName>Jane</firstName>
	<lastName>Smith</lastName>
	<email>jane@example.com</email>
	<phone>555-1234</phone>
</fields>';
		fileWrite( "#path#formdata.xml", testXML );

		// Also generate a no-form PDF
		document fileName="#path#noform.pdf" overwrite=true {
			writeOutput( "<h1>Not a Form</h1>" );
		}
	}

	function run( testResults, testBox ) {

		describe( "cfpdfform action=populate with cfpdfformparam", function() {

			it( title="populate single field", body=function( currentSpec ) {
				pdfform action="populate" source="#path#form.pdf"
					destination="#path#single.pdf" overwrite=true {
					pdfformparam name="firstName" value="John";
				}

				expect( isPDFFile( "#path#single.pdf" ) ).toBeTrue();

				pdfform action="read" source="#path#single.pdf" result="local.fields";
				expect( fields.firstName ).toBe( "John" );
			});

			it( title="populate multiple fields", body=function( currentSpec ) {
				pdfform action="populate" source="#path#form.pdf"
					destination="#path#multi.pdf" overwrite=true {
					pdfformparam name="firstName" value="John";
					pdfformparam name="lastName" value="Doe";
					pdfformparam name="email" value="john@example.com";
				}

				expect( isPDFFile( "#path#multi.pdf" ) ).toBeTrue();

				pdfform action="read" source="#path#multi.pdf" result="local.fields";
				expect( fields.firstName ).toBe( "John" );
				expect( fields.lastName ).toBe( "Doe" );
				expect( fields.email ).toBe( "john@example.com" );
			});

			it( title="populate on PDF without forms still produces valid PDF", body=function( currentSpec ) {
				pdfform action="populate" source="#path#noform.pdf"
					destination="#path#populated_noform.pdf" overwrite=true {
					pdfformparam name="nonexistent" value="test";
				}

				expect( isPDFFile( "#path#populated_noform.pdf" ) ).toBeTrue();
			});

		});

		describe( "cfpdfform action=populate with XMLdata", function() {

			it( title="populate from XML file", body=function( currentSpec ) {
				pdfform action="populate" source="#path#form.pdf"
					XMLdata="#path#formdata.xml"
					destination="#path#from_xml_file.pdf" overwrite=true;

				expect( isPDFFile( "#path#from_xml_file.pdf" ) ).toBeTrue();

				pdfform action="read" source="#path#from_xml_file.pdf" result="local.fields";
				expect( fields.firstName ).toBe( "Jane" );
				expect( fields.lastName ).toBe( "Smith" );
			});

			it( title="populate from XML string", body=function( currentSpec ) {
				pdfform action="populate" source="#path#form.pdf"
					XMLdata="#testXML#"
					destination="#path#from_xml_str.pdf" overwrite=true;

				expect( isPDFFile( "#path#from_xml_str.pdf" ) ).toBeTrue();

				pdfform action="read" source="#path#from_xml_str.pdf" result="local.fields";
				expect( fields.email ).toBe( "jane@example.com" );
			});

			it( title="populate from XML object", body=function( currentSpec ) {
				var xmlObj = xmlParse( testXML );

				pdfform action="populate" source="#path#form.pdf"
					XMLdata="#xmlObj#"
					destination="#path#from_xml_obj.pdf" overwrite=true;

				expect( isPDFFile( "#path#from_xml_obj.pdf" ) ).toBeTrue();

				pdfform action="read" source="#path#from_xml_obj.pdf" result="local.fields";
				expect( fields.phone ).toBe( "555-1234" );
			});

		});

		describe( "cfpdfform action=populate options", function() {

			it( title="populate with name attribute returns PDF variable", body=function( currentSpec ) {
				pdfform action="populate" source="#path#form.pdf" name="local.filled" {
					pdfformparam name="firstName" value="John";
				}

				expect( isPDFObject( filled ) ).toBeTrue();
			});

			it( title="populate with overwriteData=false preserves existing values", body=function( currentSpec ) {
				// First populate
				pdfform action="populate" source="#path#form.pdf"
					destination="#path#initial.pdf" overwrite=true {
					pdfformparam name="firstName" value="Jane";
				}

				// Try to overwrite with overwriteData=false
				pdfform action="populate" source="#path#initial.pdf"
					destination="#path#no_overwrite.pdf" overwrite=true overwriteData=false {
					pdfformparam name="firstName" value="John";
				}

				pdfform action="read" source="#path#no_overwrite.pdf" result="local.fields";
				expect( fields.firstName ).toBe( "Jane" );
			});

		});

	}

	function afterAll() {
		// leave artifacts for inspection
	}
}
