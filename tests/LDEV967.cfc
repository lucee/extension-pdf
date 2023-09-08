component extends = "org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.dir = getDirectoryFromPath(getCurrentTemplatePath()) & "_LDEV967";
		variables.file = dir & "/test967.pdf";
		variables.thumbnaildir = dir & "/thumbDir";

		if(!directoryExists(variables.dir)) directoryCreate(variables.dir);

		document filename="#file#" name="res" overwrite=true {
			loop times="5" {
				documentsection { echo("the test page for thumbnail"); }
			}
		}

	}

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-967", function() {

			beforeEach(function( currentSpec ){
				if (directoryExists(variables.thumbnaildir)) directoryDelete(variables.thumbnaildir, true);
			});

			it(title="CFPDF action=thumbnail with file resource", body=function( currentSpec ) {
				pdf action="thumbnail" source="#variables.file#" overwrite="true" destination="#variables.thumbnaildir#";
				var imgFiles = directoryList( path="#variables.thumbnaildir#", listInfo="name");
				imgFiles.sort("text");
				expect(arrayLen(imgFiles)).toBe(5);
				expect(imgFiles[1]).toBe("test967_page_1.jpg");
				expect(arrayEvery(imgFiles, (e) => { return listLast(e,".") == "jpg"})).toBeTrue();
			});

			it(title="CFPDF action=thumbnail with http resource", body=function( currentSpec ) {
				pdf action="thumbnail" source="https://github.com/lucee/Lucee/raw/master/test/tickets/LDEV1774/test.pdf" overwrite="true" destination="#variables.thumbnaildir#";
				var imgFiles = directoryList( path="#variables.thumbnaildir#", listInfo="path");
				expect(arrayLen(imgFiles)).toBe(8);
				expect(arrayEvery(imgFiles, (e) => { return listLast(e,".") == "jpg"})).toBeTrue();
			});

			it(title="CFPDF action=thumbnail with pdf binary variable", body=function( currentSpec ) {
				pdf action="thumbnail" source="#res#" overwrite="true" destination="#variables.thumbnaildir#";
				var imgFiles = directoryList( path="#variables.thumbnaildir#", listInfo="path");
				expect(arrayLen(imgFiles)).toBe(5);
				expect(arrayEvery(imgFiles, (e) => { return listLast(e,".") == "jpg"})).toBeTrue();
			});

			it(title="CFPDF action=thumbnail - pages attribute", body=function( currentSpec ) {
				pdf action="thumbnail" source="#res#" overwrite="true" destination="#variables.thumbnaildir#" pages="1-3";
				var imgFiles = directoryList( path="#variables.thumbnaildir#", listInfo="name");
				expect(arrayLen(imgFiles)).toBe(3);
			});

			it(title="CFPDF action=thumbnail - imagePrefix attribute", body=function( currentSpec ) {
				pdf action="thumbnail" source="#res#" overwrite="true" destination="#variables.thumbnaildir#" imageprefix="thumbImage";
				var imgFiles = directoryList( path="#variables.thumbnaildir#", listInfo="name");
				imgFiles.sort("text");
				expect(imgFiles[1]).toBe("thumbImage_page_1.jpg");
				expect(arrayEvery(imgFiles, (e) => { return find("thumbImage", e)})).toBeTrue();
			});

			it(title="CFPDF action=thumbnail - format attribute", body=function( currentSpec ) {
				pdf action="thumbnail" source="#res#" overwrite="true" destination="#variables.thumbnaildir#" format="png";
				var imgFiles = directoryList( path="#variables.thumbnaildir#", listInfo="name");
				expect(arrayEvery(imgFiles, (e) => { return listLast(e,".") == "png"})).toBeTrue();
			});

			it(title="CFPDF action=thumbnail - overwrite false", body=function( currentSpec ) {
				pdf action="thumbnail" source="#res#" overwrite="true" destination="#variables.thumbnaildir#" imageprefix="thumbImage";
				var imgFiles = directoryList( path="#variables.thumbnaildir#", listInfo="name");
				imgFiles.sort("text");
				expect(imgFiles[1]).toBe("thumbImage_page_1.jpg");
				expect(arrayEvery(imgFiles, (e) => { return find("thumbImage", e)})).toBeTrue();

				expect(function(){
					pdf action="thumbnail" source="#res#" overwrite="false" destination="#variables.thumbnaildir#" imageprefix="thumbImage";
				}).toThrow(); // overwite is false and file exists
			});

		});
	}

	function afterAll() {
		if (directoryExists(variables.dir)) directoryDelete(variables.dir, true);
	}
}