component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	// action="populate" - implemented
	// PDFBox: PDAcroForm, PDField.setValue()

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFFormPopulate/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path );

		// Create test XML data
		variables.testXML = '<?xml version="1.0"?>
<fields>
	<firstName>John</firstName>
	<lastName>Doe</lastName>
	<email>john@example.com</email>
	<phone>555-1234</phone>
</fields>';
		fileWrite( "#path#formdata.xml", testXML );

		// Create a simple PDF for structure tests
		document fileName="#path#noform.pdf" overwrite=true {
			writeOutput( "<h1>Not a Form</h1>" );
		}
	}

	function run( testResults, testBox ) {

		describe( "cfpdfform action=populate basic", function() {

			it( title="populate on PDF without forms still produces valid PDF", body=function( currentSpec ) {
				// Even with no form fields, populate should work (just won't change anything)
				pdfform action="populate" source="#path#noform.pdf"
					destination="#path#populated_noform.pdf" overwrite=true {
					pdfformparam name="nonexistent" value="test";
				}

				expect( isPDFFile( "#path#populated_noform.pdf" ) ).toBeTrue();
			});

			it( title="populate returns PDF variable with name attribute", body=function( currentSpec ) {
				pdfform action="populate" source="#path#noform.pdf" name="local.filled" {
					pdfformparam name="test" value="value";
				}

				expect( isPDFObject( filled ) ).toBeTrue();
			});

		});

		// Tests below require actual PDF forms - skipped until form.pdf exists
		describe( "cfpdfform action=populate with cfpdfformparam", function() {

			it( title="populate single field with cfpdfformparam", skip=true, body=function( currentSpec ) {
				pdfform action="populate" source="#path#form.pdf"
					destination="#path#filled.pdf" overwrite=true {
					pdfformparam name="firstName" value="John";
				}

				expect( isPDFFile( "#path#filled.pdf" ) ).toBeTrue();
			});

			it( title="populate multiple fields with cfpdfformparam", skip=true, body=function( currentSpec ) {
				pdfform action="populate" source="#path#form.pdf"
					destination="#path#multi.pdf" overwrite=true {
					pdfformparam name="firstName" value="John";
					pdfformparam name="lastName" value="Doe";
					pdfformparam name="email" value="john@example.com";
				}

				expect( isPDFFile( "#path#multi.pdf" ) ).toBeTrue();

				// Verify values were set
				pdfform action="read" source="#path#multi.pdf" result="local.fields";
				expect( fields.firstName ).toBe( "John" );
				expect( fields.lastName ).toBe( "Doe" );
			});

		});

		describe( "cfpdfform action=populate with XMLdata", function() {

			it( title="populate from XML file", skip=true, body=function( currentSpec ) {
				pdfform action="populate" source="#path#form.pdf"
					XMLdata="#path#formdata.xml"
					destination="#path#from_xml.pdf" overwrite=true;

				expect( isPDFFile( "#path#from_xml.pdf" ) ).toBeTrue();
			});

			it( title="populate from XML string", skip=true, body=function( currentSpec ) {
				pdfform action="populate" source="#path#form.pdf"
					XMLdata="#testXML#"
					destination="#path#from_xml_str.pdf" overwrite=true;

				expect( isPDFFile( "#path#from_xml_str.pdf" ) ).toBeTrue();
			});

			it( title="populate from XML object", skip=true, body=function( currentSpec ) {
				var xmlObj = xmlParse( testXML );

				pdfform action="populate" source="#path#form.pdf"
					XMLdata="#xmlObj#"
					destination="#path#from_xml_obj.pdf" overwrite=true;

				expect( isPDFFile( "#path#from_xml_obj.pdf" ) ).toBeTrue();
			});

		});

		describe( "cfpdfform action=populate options", function() {

			it( title="populate with overwriteData=false preserves existing values", skip=true, body=function( currentSpec ) {
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
				expect( fields.firstName ).toBe( "Jane" ); // Original preserved
			});

			it( title="populate outputs to browser when no destination", skip=true, body=function( currentSpec ) {
				// When destination not specified, outputs to browser
				// Hard to test in unit test context
				skip( "Browser output test - manual verification needed" );
			});

			it( title="populate with name attribute returns PDF variable", skip=true, body=function( currentSpec ) {
				pdfform action="populate" source="#path#form.pdf" name="local.filled" {
					pdfformparam name="firstName" value="John";
				}

				expect( isPDFObject( filled ) ).toBeTrue();
			});

		});

		// FDF support not yet implemented - tests commented out
		// describe( "cfpdfform action=populate with FDF", function() {
		// 	it( title="populate from FDF file", skip=true, body=function( currentSpec ) {
		// 		pdfform action="read" source="#path#form.pdf" fdfdata="#path#export.fdf";
		// 		pdfform action="populate" source="#path#form.pdf"
		// 			fdfdata="#path#export.fdf"
		// 			destination="#path#from_fdf.pdf" overwrite=true;
		// 		expect( isPDFFile( "#path#from_fdf.pdf" ) ).toBeTrue();
		// 	});
		// });

	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
