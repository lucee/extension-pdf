component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "PDFEncryptionTypes/generated/";

		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		directoryCreate( variables.path, true, true );

		variables.testCode = createUniqueID();
		variables.password = "test-password-123";

		// Create source PDF
		document fileName="#path#source.pdf" overwrite=true {
			writeOutput( "<p>Test content #variables.testCode#</p>" );
		}
	}

	function run( testResults, testBox ) {

		describe( "cfpdf protect encryption types", function() {

			it( title="RC4-40 encryption produces a valid encrypted PDF", body=function( currentSpec ) {
				pdf action="protect" source="#path#source.pdf" destination="#path#rc4_40.pdf"
					newUserPassword="#password#" encrypt="rc4_40" overwrite=true;

				// Should require password to read
				expect( function() {
					pdf action="extractText" source="#path#rc4_40.pdf" name="local.text";
				}).toThrow();

				// Should work with password
				pdf action="extractText" source="#path#rc4_40.pdf" name="local.text" password="#password#";
				expect( text ).toInclude( testCode );
			});

			it( title="RC4-128 encryption produces a valid encrypted PDF", body=function( currentSpec ) {
				pdf action="protect" source="#path#source.pdf" destination="#path#rc4_128.pdf"
					newUserPassword="#password#" encrypt="rc4_128" overwrite=true;

				expect( function() {
					pdf action="extractText" source="#path#rc4_128.pdf" name="local.text";
				}).toThrow();

				pdf action="extractText" source="#path#rc4_128.pdf" name="local.text" password="#password#";
				expect( text ).toInclude( testCode );
			});

			it( title="AES-128 encryption produces a valid encrypted PDF", body=function( currentSpec ) {
				// BUG ##4: ENCRYPT_AES_128 has the same value (128) as ENCRYPT_RC4_128,
				// so AES-128 and RC4-128 are indistinguishable - both produce RC4-128
				pdf action="protect" source="#path#source.pdf" destination="#path#aes_128.pdf"
					newUserPassword="#password#" encrypt="aes_128" overwrite=true;

				expect( function() {
					pdf action="extractText" source="#path#aes_128.pdf" name="local.text";
				}).toThrow();

				pdf action="extractText" source="#path#aes_128.pdf" name="local.text" password="#password#";
				expect( text ).toInclude( testCode );
			});

			it( title="AES-128 and RC4-128 produce different encryption", body=function( currentSpec ) {
				// BUG ##4: These currently produce identical output because both constants = 128
				pdf action="protect" source="#path#source.pdf" destination="#path#cmp_rc4.pdf"
					newUserPassword="#password#" encrypt="rc4_128" overwrite=true;
				pdf action="protect" source="#path#source.pdf" destination="#path#cmp_aes.pdf"
					newUserPassword="#password#" encrypt="aes_128" overwrite=true;

				// The raw bytes should differ because the encryption algorithms are different
				var rc4Bytes = fileReadBinary( "#path#cmp_rc4.pdf" );
				var aesBytes = fileReadBinary( "#path#cmp_aes.pdf" );

				// At minimum, file sizes should differ (AES has different block structure)
				// This is a weak check but catches the case where both are identical RC4
				expect( arrayLen( rc4Bytes ) ).notToBe( arrayLen( aesBytes ),
					"AES-128 and RC4-128 produced identical file sizes - likely both using RC4-128" );
			});

		});
	}

	function afterAll() {
		// Cleanup before run, not after - leave artifacts for inspection
	}
}
