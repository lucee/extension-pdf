component {

	variables.fetchedUrls = [];

	function onResourceFetch( required string url ) {
		variables.fetchedUrls.append( arguments.url );

		if ( arguments.url contains "-image.png" ) {
			// Return a tiny 1x1 red PNG as binary
			var tmpDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "DocumentResourceHandler/generated/";
			if ( !directoryExists( tmpDir ) ) directoryCreate( tmpDir, true, true );
			var tmpFile = tmpDir & "cfc-handler-img.png";
			var img = imageNew( "", 1, 1, "rgb", "red" );
			imageWrite( img, tmpFile, true );
			return fileReadBinary( tmpFile );
		}

		return javacast( "null", "" );
	}

	function getFetchedUrls() {
		return variables.fetchedUrls;
	}
}
