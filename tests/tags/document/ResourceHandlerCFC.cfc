component {

	variables.fetchedUrls = [];
	variables.parsedUrls = [];

	function onResourceFetch( required string url, struct parsedUrl ) {
		variables.fetchedUrls.append( arguments.url );
		if ( !isNull( arguments.parsedUrl ) ) variables.parsedUrls.append( arguments.parsedUrl );

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

	function getParsedUrls() {
		return variables.parsedUrls;
	}
}
