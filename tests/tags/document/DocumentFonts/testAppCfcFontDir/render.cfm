<cfscript>
	pdfPath = getDirectoryFromPath( getCurrentTemplatePath() ) & "../generated/app_cfc.pdf";
	document format="pdf" filename="#pdfPath#" overwrite=true {
		writeOutput( '<p style="font-size:5em;font-family:Liberation Sans;">From AppCfc</p>' );
	}
	writeOutput( "ok" );
</cfscript>
