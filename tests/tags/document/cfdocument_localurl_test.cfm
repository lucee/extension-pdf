<cfset pdfFile = expandPath("./cfdocument_localurl_test.pdf")>
<cfset imagePath = expandPath("./LDEV1519/image.jpg")>

<!--- Clean up any previous test output --->
<cfif fileExists(pdfFile)>
    <cffile action="delete" file="#pdfFile#">
</cfif>

<!--- Create PDF with cfdocument using localurl support --->
<cfdocument format="pdf" localurl="true" saveAsName="#pdfFile#">
    <html>
        <body>
            <h1>cfdocument localurl test</h1>
            <img src="LDEV1519/image.jpg" width="100" height="100">
        </body>
    </html>
</cfdocument>

<!--- Check if PDF was created --->
<cfif fileExists(pdfFile)>
    <cfoutput>PDF created successfully: #pdfFile#</cfoutput>
<cfelse>
    <cfoutput>PDF creation failed.</cfoutput>
</cfif>

<!--- Optionally, add more checks to validate image presence in PDF (binary scan, etc.) --->
