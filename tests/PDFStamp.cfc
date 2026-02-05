component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" skip=true {

	// SKIP: action="addStamp" not yet implemented
	// See: FEATURES.md - Medium effort
	// PDFBox: PDAnnotationRubberStamp

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFStamp/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path );

		// Create a multi-page test PDF
		document fileName="#path#source.pdf" overwrite=true {
			writeOutput( "<h1>Page 1</h1><p>Content for stamping</p>" );
			documentItem type="pagebreak";
			writeOutput( "<h1>Page 2</h1><p>More content</p>" );
			documentItem type="pagebreak";
			writeOutput( "<h1>Page 3</h1><p>Even more content</p>" );
		}
	}

	function run( testResults, testBox ) {
		describe( "cfpdf action=addStamp", function() {

			it( title="add stamp to all pages", body=function( currentSpec ) {
				pdf action="addStamp" source="#path#source.pdf"
					destination="#path#stamped.pdf" overwrite=true {
					pdfparam pages="1-3" coordinates="100,100,300,150"
						iconName="Approved" note="Document approved";
				}

				expect( isPDFFile( "#path#stamped.pdf" ) ).toBeTrue();
			});

			it( title="add stamp to specific page", body=function( currentSpec ) {
				pdf action="addStamp" source="#path#source.pdf"
					destination="#path#stamp_page1.pdf" overwrite=true {
					pdfparam pages="1" coordinates="50,700,250,750"
						iconName="Draft" note="Draft version";
				}

				expect( isPDFFile( "#path#stamp_page1.pdf" ) ).toBeTrue();
			});

			it( title="add multiple stamps", body=function( currentSpec ) {
				pdf action="addStamp" source="#path#source.pdf"
					destination="#path#multi_stamp.pdf" overwrite=true {
					pdfparam pages="1" coordinates="50,700,150,750" iconName="Approved";
					pdfparam pages="2" coordinates="50,700,150,750" iconName="Draft";
					pdfparam pages="3" coordinates="50,700,150,750" iconName="Final";
				}

				expect( isPDFFile( "#path#multi_stamp.pdf" ) ).toBeTrue();
			});

			it( title="add stamp with different icon names", body=function( currentSpec ) {
				// Standard stamp icons: Approved, Experimental, NotApproved, AsIs,
				// Expired, NotForPublicRelease, Confidential, Final, Sold,
				// Departmental, ForComment, TopSecret, Draft, ForPublicRelease

				pdf action="addStamp" source="#path#source.pdf"
					destination="#path#confidential.pdf" overwrite=true {
					pdfparam pages="1" coordinates="400,700,550,750"
						iconName="Confidential" note="Restricted access";
				}

				expect( isPDFFile( "#path#confidential.pdf" ) ).toBeTrue();
			});

			it( title="add stamp with name attribute", body=function( currentSpec ) {
				pdf action="addStamp" source="#path#source.pdf" name="local.stamped" {
					pdfparam pages="1" coordinates="100,100,200,150" iconName="Approved";
				}

				expect( isPDFObject( stamped ) ).toBeTrue();
			});

		});
	}

	function afterAll() {
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
	}
}
