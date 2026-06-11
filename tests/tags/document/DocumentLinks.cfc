component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll() {
		variables.path = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentLinks/generated/";
		if ( directoryExists( variables.path ) ) directoryDelete( variables.path, true );
		if ( !directoryExists( variables.path ) ) directoryCreate( variables.path, true, true );
	}

	function run( testResults, testBox ) {

		describe( "cfdocument anchor href produces PDF link annotations", function() {

			it( title="<a href='http://...'> becomes a Link annotation with URI action", body=function( currentSpec ) {
				document format="pdf" filename="#path#http_link.pdf" overwrite=true {
					writeOutput( '<p>Visit <a href="http://example.com/foo">example</a> today.</p>' );
				}

				var uris = getLinkURIs( "#path#http_link.pdf" );
				expect( uris ).toHaveLength( 1 );
				expect( uris[ 1 ] ).toBe( "http://example.com/foo" );
			});

			it( title="<a href='https://...'> preserves https scheme", body=function( currentSpec ) {
				document format="pdf" filename="#path#https_link.pdf" overwrite=true {
					writeOutput( '<p><a href="https://lucee.org/">Lucee</a></p>' );
				}

				var uris = getLinkURIs( "#path#https_link.pdf" );
				expect( uris ).toHaveLength( 1 );
				expect( uris[ 1 ] ).toBe( "https://lucee.org/" );
			});

			it( title="<a href='mailto:...'> becomes a Link annotation with mailto URI", body=function( currentSpec ) {
				document format="pdf" filename="#path#mailto_link.pdf" overwrite=true {
					writeOutput( '<p>Contact <a href="mailto:foo@example.com">us</a>.</p>' );
				}

				var uris = getLinkURIs( "#path#mailto_link.pdf" );
				expect( uris ).toHaveLength( 1 );
				expect( uris[ 1 ] ).toBe( "mailto:foo@example.com" );
			});

			it( title="multiple links on one page are all captured", body=function( currentSpec ) {
				document format="pdf" filename="#path#multi_links.pdf" overwrite=true {
					writeOutput( '
						<p><a href="http://one.example.com/">one</a></p>
						<p><a href="http://two.example.com/">two</a></p>
						<p><a href="mailto:hello@example.com">three</a></p>
					' );
				}

				var uris = getLinkURIs( "#path#multi_links.pdf" );
				expect( uris ).toHaveLength( 3 );
				expect( uris ).toInclude( "http://one.example.com/" );
				expect( uris ).toInclude( "http://two.example.com/" );
				expect( uris ).toInclude( "mailto:hello@example.com" );
			});

			it( title="links across multiple pages are all captured", body=function( currentSpec ) {
				document format="pdf" filename="#path#paged_links.pdf" overwrite=true {
					writeOutput( '<p><a href="http://page1.example.com/">page 1 link</a></p>' );
					documentItem type="pagebreak";
					writeOutput( '<p><a href="http://page2.example.com/">page 2 link</a></p>' );
				}

				var uris = getLinkURIs( "#path#paged_links.pdf" );
				expect( uris ).toHaveLength( 2 );
				expect( uris ).toInclude( "http://page1.example.com/" );
				expect( uris ).toInclude( "http://page2.example.com/" );
			});

			it( title="link text is preserved in extracted text", body=function( currentSpec ) {
				document format="pdf" filename="#path#link_text.pdf" overwrite=true {
					writeOutput( '<p>Click <a href="http://example.com/">here for details</a>.</p>' );
				}

				pdf action="extractText" source="#path#link_text.pdf" name="local.text";
				expect( text ).toInclude( "here for details" );
			});

			it( title="<a> without href produces no link annotation", body=function( currentSpec ) {
				document format="pdf" filename="#path#no_href.pdf" overwrite=true {
					writeOutput( '<p>This is <a>not a link</a> at all.</p>' );
				}

				var count = countLinkAnnotations( "#path#no_href.pdf" );
				expect( count ).toBe( 0 );

				// text still renders
				pdf action="extractText" source="#path#no_href.pdf" name="local.text";
				expect( text ).toInclude( "not a link" );
			});

		});

		describe( "cfdocument internal anchor links", function() {

			it( title="<a href='##anchor'> with matching id produces a Link annotation", body=function( currentSpec ) {
				document format="pdf" filename="#path#internal_anchor.pdf" overwrite=true {
					writeOutput( '<p><a href="##chapter2">jump to chapter 2</a></p>' );
					documentItem type="pagebreak";
					writeOutput( '<h1 id="chapter2">Chapter 2</h1><p>content</p>' );
				}

				// Internal anchors produce a Link annotation with GoTo (not URI) action.
				// Assert the annotation exists; destination introspection is brittle so we don't pin it.
				var count = countLinkAnnotations( "#path#internal_anchor.pdf" );
				expect( count ).toBeGTE( 1, "internal anchor should produce at least one Link annotation" );
			});

		});
	}

	// Returns an array of URI strings from all PDAnnotationLink annotations with URI actions.
	private array function getLinkURIs( required string pdfPath ) {
		var file = createObject( "java", "java.io.File" ).init( arguments.pdfPath );
		var doc = createObject( "java", "org.apache.pdfbox.Loader" ).loadPDF( file );
		try {
			var uris = [];
			var pageIter = doc.getPages().iterator();
			while ( pageIter.hasNext() ) {
				var annotIter = pageIter.next().getAnnotations().iterator();
				while ( annotIter.hasNext() ) {
					var annot = annotIter.next();
					if ( annot.getSubtype() != "Link" ) continue;
					var action = annot.getAction();
					if ( isNull( action ) ) continue;
					if ( action.getType() == "Action" && action.getSubType() == "URI" ) {
						uris.append( action.getURI() );
					}
				}
			}
			return uris;
		}
		finally {
			doc.close();
		}
	}

	// Returns total count of Link-subtype annotations across all pages.
	private numeric function countLinkAnnotations( required string pdfPath ) {
		var file = createObject( "java", "java.io.File" ).init( arguments.pdfPath );
		var doc = createObject( "java", "org.apache.pdfbox.Loader" ).loadPDF( file );
		try {
			var count = 0;
			var pageIter = doc.getPages().iterator();
			while ( pageIter.hasNext() ) {
				var annotIter = pageIter.next().getAnnotations().iterator();
				while ( annotIter.hasNext() ) {
					if ( annotIter.next().getSubtype() == "Link" ) count++;
				}
			}
			return count;
		}
		finally {
			doc.close();
		}
	}

}
