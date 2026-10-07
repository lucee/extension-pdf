component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentSectionItem/generated/";
		afterAll();

		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );
	}

	function run( testResults, testBox ) {
		describe( "cfdocumentitem", function() {

			it( title="pagebreak creates multiple pages", body=function( currentSpec ) {
				document format="pdf" filename="#path#pagebreak.pdf" overwrite=true {
					writeOutput( "<h1>Page 1</h1>" );
					documentItem type="pagebreak";
					writeOutput( "<h1>Page 2</h1>" );
					documentItem type="pagebreak";
					writeOutput( "<h1>Page 3</h1>" );
				}

				pdf action="getInfo" source="#path#pagebreak.pdf" name="local.info";
				expect( info.totalPages ).toBe( 3 );
			});

		});

		describe( "cfdocumentsection", function() {

			it( title="section with different margins", body=function( currentSpec ) {
				document format="pdf" filename="#path#sections.pdf" overwrite=true {
					documentSection margintop="0.5" marginbottom="0.5" {
						writeOutput( "<h1>Section 1</h1><p>Small margins</p>" );
					}
					documentSection margintop="2" marginbottom="2" {
						writeOutput( "<h1>Section 2</h1><p>Large margins</p>" );
					}
				}

				expect( isPDFFile( "#path#sections.pdf" ) ).toBeTrue();
			});

			it( title="section with different page orientation", body=function( currentSpec ) {
				document format="pdf" filename="#path#section_orient.pdf" overwrite=true {
					documentSection orientation="portrait" {
						writeOutput( "<h1>Portrait Section</h1>" );
					}
					documentSection orientation="landscape" {
						writeOutput( "<h1>Landscape Section</h1>" );
					}
				}

				expect( isPDFFile( "#path#section_orient.pdf" ) ).toBeTrue();
				pdf action="getInfo" source="#path#section_orient.pdf" name="local.info";
				expect( info.totalPages ).toBe( 2 );
			});

			it( title="section with header and footer", body=function( currentSpec ) {
				document format="pdf" filename="#path#section_hf.pdf" overwrite=true {
					documentSection {
						documentItem type="header" {
							writeOutput( "<div>Section Header</div>" );
						}
						documentItem type="footer" {
							writeOutput( "<div>Section Footer</div>" );
						}
						writeOutput( "<h1>Content</h1><p>This section has its own header and footer.</p>" );
					}
				}

				expect( isPDFFile( "#path#section_hf.pdf" ) ).toBeTrue();
			});

			it( title="multiple sections with pagebreaks between", body=function( currentSpec ) {
				document format="pdf" filename="#path#multi_section.pdf" overwrite=true {
					documentSection {
						writeOutput( "<h1>Section 1 - Page 1</h1>" );
						documentItem type="pagebreak";
						writeOutput( "<h1>Section 1 - Page 2</h1>" );
					}
					documentSection {
						writeOutput( "<h1>Section 2 - Page 1</h1>" );
					}
				}

				expect( isPDFFile( "#path#multi_section.pdf" ) ).toBeTrue();
				pdf action="getInfo" source="#path#multi_section.pdf" name="local.info";
				expect( info.totalPages ).toBe( 3 );
			});

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
