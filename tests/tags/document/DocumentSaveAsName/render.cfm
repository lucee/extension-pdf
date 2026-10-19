<cfscript>
	document format="pdf" saveAsName="#url.name#" {
		writeOutput( "<p>saveAsName test</p>" );
	}
</cfscript>
