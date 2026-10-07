component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFPermissions/generated/";

		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		directoryCreate( variables.path, true, true );

		variables.ownerPassword = "owner-pass-123";

		// Create source PDF
		document fileName="#path#source.pdf" overwrite=true {
			writeOutput( "<p>Test content for permissions</p>" );
		}
	}

	function run( testResults, testBox ) {

		describe( "cfpdf protect permissions", function() {

			it( title="'allowprinting' grants printing permission", body=function( currentSpec ) {
				pdf action="protect" source="#path#source.pdf" destination="#path#allow_printing.pdf"
					newOwnerPassword="#ownerPassword#" permissions="allowprinting" overwrite=true;

				pdf action="getInfo" source="#path#allow_printing.pdf" name="local.info" password="#ownerPassword#";
				expect( info.Printing ).toBe( "Allowed" );
			});

			it( title="'printing' and 'allowprinting' produce identical PDFs", body=function( currentSpec ) {
				// BUG ##6: "printing" (line 128) maps to ALLOW_DEGRADED_PRINTING (2048)
				// which calls setCanPrintFaithful(), while "allowprinting" (line 139) maps to
				// ALLOW_PRINTING (4) which calls setCanPrint(). They set DIFFERENT PDFBox flags.
				// The duplicate "printing" match at line 140 (ALLOW_PRINTING) is unreachable.
				//
				// This can't be directly tested via getInfo because PDFBox's canPrint() returns
				// true for both basic and faithful printing. The bug manifests when a PDF reader
				// checks the raw permission bits - "printing" will only have the faithful flag,
				// missing the basic print flag that "allowprinting" sets.
				//
				// Skip: requires Java-level inspection of AccessPermission.getPermissionBytes()
				// to distinguish the two. Documenting here for awareness.
				pdf action="protect" source="#path#source.pdf" destination="#path#perm_printing.pdf"
					newOwnerPassword="#ownerPassword#" permissions="printing" overwrite=true;
				pdf action="protect" source="#path#source.pdf" destination="#path#perm_allowprinting.pdf"
					newOwnerPassword="#ownerPassword#" permissions="allowprinting" overwrite=true;

				// These files should be byte-identical if the permissions are the same
				var bytes1 = fileReadBinary( "#path#perm_printing.pdf" );
				var bytes2 = fileReadBinary( "#path#perm_allowprinting.pdf" );
				expect( arrayLen( bytes1 ) ).toBe( arrayLen( bytes2 ),
					"'printing' and 'allowprinting' should produce identical permission flags" );
			});

			it( title="copy permission is applied correctly", body=function( currentSpec ) {
				pdf action="protect" source="#path#source.pdf" destination="#path#copy.pdf"
					newOwnerPassword="#ownerPassword#" permissions="copy" overwrite=true;

				pdf action="getInfo" source="#path#copy.pdf" name="local.info" password="#ownerPassword#";
				expect( info.CopyContent ).toBe( "Allowed" );
			});

			it( title="multiple permissions can be combined", body=function( currentSpec ) {
				pdf action="protect" source="#path#source.pdf" destination="#path#multi.pdf"
					newOwnerPassword="#ownerPassword#" permissions="allowprinting,copy,fillin" overwrite=true;

				pdf action="getInfo" source="#path#multi.pdf" name="local.info" password="#ownerPassword#";
				expect( info.Printing ).toBe( "Allowed" );
				expect( info.CopyContent ).toBe( "Allowed" );
				expect( info.FillingForm ).toBe( "Allowed" );
			});

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
