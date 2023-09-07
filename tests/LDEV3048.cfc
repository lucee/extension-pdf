component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.outputDir = getDirectoryFromPath(getCurrentTemplatePath()) & "LDEV3048_images\";
		if (!directoryExists( variables.outputDir ) )
			directoryCreate( variables.outputDir );

		var img1file = getTempFile( variables.outputDir, "ldev3048-1", "png" );
		var img2file = getTempFile( variables.outputDir, "ldev3048-2", "png" );
		var img3file = getTempFile( variables.outputDir, "ldev3048-3", "png" );

		var img1  = ImageNew("", 111, 111, "rgb", "red");
		var img2  = ImageNew("", 222, 222, "rgb", "yellow");
		var img3  = ImageNew("", 333, 333, "rgb", "green");

		imageWrite(img1, img1file, true);
		imageWrite(img2, img2file, true);
		imageWrite(img3, img3file, true);

		if (!directoryExists(variables.outputDir)) directoryCreate(variables.outputDir);
		document fileName="#variables.outputDir#noImages.pdf" name="pdfVar" overwrite=true {
			writeoutput("test pdf file");
		}

		document fileName="#variables.outputDir#withImages.pdf" name="pdfVar" overwrite=true {
			```
			<cfoutput>
			<!DOCTYPE html>
			<html lang="en">
			<head>
				<meta charset="utf-8" />
				<title>LDEV-3048</title>
				<style type="text/css">
				.start-on-new-page {
					page-break-before: always;
				}
				</style>
			</head>
			<body>
				<div id="pageOne">
					<img src="#img1File#">
				</div>
				<div id="pageTwo" class="start-on-new-page">
					<img src="#img2File#">
					<img src="#img3File#">
				</div>
			</body>
			</html>
			</cfoutput>
			```
		}
		fileDelete(img1File);
		fileDelete(img2File);
		fileDelete(img3File);
	}



	function run( testResults , testBox ) {
		describe( "testcase for LDEV-3048", function() {

			it( title="cfpdf extractImages, pdf with no images", body=function( currentSpec ) {
				pdf action="extractImages" source="#outputDir#noImages.pdf"
					overwrite="true" format="png" imageprefix="no-image" password=""
					destination="#outputDir#";

				var imageFiles = directoryList( path=outputDir, filter="no-image*.png" );

				expect( len( imageFiles ) ).toBe( 0 );
			});

			it( title="cfpdf extractImages, pdf with 2 images, 1 per page", body=function( currentSpec ) {
				pdf action="extractImages" source="#outputDir#withImages.pdf" pages="*"
					overwrite="true" format="png" imageprefix="two-image" password=""
					destination="#outputDir#";

				var imageFiles = directoryList( path=outputDir, filter="two-image*.png" );

				expect( len( imageFiles ) ).toBe( 3 );
				var imgInfo = ImageInfo( imageFiles[ 1 ] );
				expect( imgInfo.height ).toBe( 111 );
				expect( imgInfo.width ).toBe( 111 );

			});

			it( title="cfpdf extractImages, pdf with 2 images, 1 per page, only from page 2", body=function( currentSpec ) {
				pdf action="extractImages" source="#outputDir#withImages.pdf" pages="2"
					overwrite="true" format="png" imageprefix="page-image" password=""
					destination="#outputDir#";

				var imageFiles = directoryList( path=outputDir, filter="page-image*.png" );

				expect( len( imageFiles ) ).toBe( 2 );
				var imgInfo = ImageInfo( imageFiles[ 1 ] );
				expect( imgInfo.height ).toBe( 222 );
				expect( imgInfo.width ).toBe( 222 );

				expect(function(){
					pdf action="extractImages" source="#outputDir#withImages.pdf" pages="2"
						overwrite="false" format="png" imageprefix="page-image" password=""
						destination="#outputDir#";
				}).toThrow(); // overwrite="false" and images already exist
			});

			it( title="cfpdf extractImages, invalid image format", body=function( currentSpec ) {
				expect(function(){
					pdf action="extractImages" source="#outputDir#withImages.pdf" pages="2"
						overwrite="true" format="monkey" imageprefix="invalid-image" password=""
						destination="#outputDir#";
				}).toThrow();
			});

		});
	}

	function afterAll() {
		if ( directoryExists( variables.outputDir ) )
			directoryDelete(variables.outputDir, true);
	}
}
