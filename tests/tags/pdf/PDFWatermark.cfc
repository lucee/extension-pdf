component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFWatermark/generated/";
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );

		// Create source PDF (no embedded images — text only)
		document fileName="#path#source.pdf" overwrite=true {
			writeOutput( "<h1>Page 1</h1><p>Content for watermark testing</p>" );
			documentItem type="pagebreak";
			writeOutput( "<h1>Page 2</h1><p>More content</p>" );
		}

		// Create a watermark image
		var img = imageNew( "", 200, 50, "argb", "white" );
		imageSetDrawingColor( img, "red" );
		imageDrawText( img, "WATERMARK", 10, 30, { size: 24, style: "bold" } );
		imageWrite( img, "#path#watermark.png", true );
	}

	// Counts images embedded in a PDF — used to verify watermark presence
	private numeric function countEmbeddedImages( required string pdfPath, required string outDir ) {
		if ( directoryExists( outDir ) ) directoryDelete( outDir, true );
		directoryCreate( outDir, true, true );
		pdf action="extractImage" source=pdfPath destination=outDir imagePrefix="w" overwrite=true;
		return arrayLen( directoryList( outDir, false, "name" ) );
	}

	function run( testResults, testBox ) {

		describe( "cfpdf action=addWatermark", function() {

			it( title="add watermark embeds image and keeps page count", body=function( currentSpec ) {
				pdf action="addWatermark" source="#path#source.pdf" image="#path#watermark.png"
					destination="#path#watermarked.pdf" overwrite=true;

				expect( isPDFFile( "#path#watermarked.pdf" ) ).toBeTrue();

				pdf action="getInfo" source="#path#watermarked.pdf" name="local.info";
				expect( info.totalPages ).toBe( 2 );

				// Source has no images; watermarked must have at least 1
				expect( countEmbeddedImages( "#path#source.pdf", "#path#count_source/" ) ).toBe( 0, "source pdf should have no images" );
				expect( countEmbeddedImages( "#path#watermarked.pdf", "#path#count_watermarked/" ) ).toBeGT( 0, "watermarked pdf must contain the watermark image" );
			});

			it( title="add watermark with position attribute embeds image", body=function( currentSpec ) {
				pdf action="addWatermark" source="#path#source.pdf" image="#path#watermark.png"
					position="50,100" destination="#path#positioned.pdf" overwrite=true;

				expect( isPDFFile( "#path#positioned.pdf" ) ).toBeTrue();
				expect( countEmbeddedImages( "#path#positioned.pdf", "#path#count_positioned/" ) ).toBeGT( 0 );
			});

			it( title="add watermark with opacity embeds image", body=function( currentSpec ) {
				pdf action="addWatermark" source="#path#source.pdf" image="#path#watermark.png"
					opacity=0.3 destination="#path#transparent.pdf" overwrite=true;

				expect( isPDFFile( "#path#transparent.pdf" ) ).toBeTrue();
				expect( countEmbeddedImages( "#path#transparent.pdf", "#path#count_transparent/" ) ).toBeGT( 0 );
			});

			it( title="add watermark to specific pages embeds image", body=function( currentSpec ) {
				pdf action="addWatermark" source="#path#source.pdf" image="#path#watermark.png"
					pages="1" destination="#path#page1_only.pdf" overwrite=true;

				expect( isPDFFile( "#path#page1_only.pdf" ) ).toBeTrue();
				expect( countEmbeddedImages( "#path#page1_only.pdf", "#path#count_page1/" ) ).toBeGT( 0 );
			});

			it( title="add watermark with foreground=true embeds image", body=function( currentSpec ) {
				pdf action="addWatermark" source="#path#source.pdf" image="#path#watermark.png"
					foreground=true destination="#path#foreground.pdf" overwrite=true;

				expect( isPDFFile( "#path#foreground.pdf" ) ).toBeTrue();
				expect( countEmbeddedImages( "#path#foreground.pdf", "#path#count_fg/" ) ).toBeGT( 0 );
			});

			it( title="add watermark with name attribute returns PDF variable", body=function( currentSpec ) {
				pdf action="addWatermark" source="#path#source.pdf" image="#path#watermark.png"
					name="local.result";

				expect( isPDFObject( result ) ).toBeTrue();
			});

			it( title="add watermark requires image or copyFrom", body=function( currentSpec ) {
				expect( function() {
					pdf action="addWatermark" source="#path#source.pdf"
						destination="#path#fail.pdf" overwrite=true;
				}).toThrow();
			});

		});

		describe( "cfpdf action=removeWatermark", function() {

			it( title="remove watermark preserves text and produces valid PDF", body=function( currentSpec ) {
				// First add a watermark
				pdf action="addWatermark" source="#path#source.pdf" image="#path#watermark.png"
					destination="#path#to_clean.pdf" overwrite=true;

				// Then "remove" it
				pdf action="removeWatermark" source="#path#to_clean.pdf"
					destination="#path#cleaned.pdf" overwrite=true;

				expect( isPDFFile( "#path#cleaned.pdf" ) ).toBeTrue();

				// Original text must survive
				pdf action="extractText" source="#path#cleaned.pdf" name="local.text";
				expect( text ).toInclude( "Page 1" );
				expect( text ).toInclude( "Page 2" );
			});

			it( title="remove watermark actually strips embedded image", body=function( currentSpec ) {
				pdf action="addWatermark" source="#path#source.pdf" image="#path#watermark.png"
					destination="#path#to_clean2.pdf" overwrite=true;

				var beforeCount = countEmbeddedImages( "#path#to_clean2.pdf", "#path#count_before/" );
				expect( beforeCount ).toBeGT( 0 );

				pdf action="removeWatermark" source="#path#to_clean2.pdf"
					destination="#path#cleaned2.pdf" overwrite=true;

				var afterCount = countEmbeddedImages( "#path#cleaned2.pdf", "#path#count_after/" );
				expect( afterCount ).toBeLT( beforeCount, "removeWatermark should drop the embedded image" );
			});

		});

	}

	function afterAll() {
		// leave artifacts for inspection
	}
}
