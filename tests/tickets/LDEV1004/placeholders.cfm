<cfparam name="url.type">
<cfparam name="url.evalatprint">
<!--- https://dev.lucee.org/t/cfdocument-error-on-total-page-count/2102 --->
<cfdocument format="pdf" type="#url.type#">
    <cfdocumentitem type="header" evalatprint="#url.evalatprint#">
        <table width="100%" border="0" cellpadding="0" cellspacing="0">
            <strong><tr><td align="right"><cfoutput>#cfdocument.currentSectionPageNumber# of</strong>
                <strong>#cfdocument.totalSectionPageCount#</cfoutput></td></tr></strong>
        </table>
    </cfdocumentitem>

    <cfdocumentitem type="footer" evalatprint="#url.evalatprint#">
        <table width="100%" border="0" cellpadding="0" cellspacing="0">
            <strong><tr><td align="center"><cfoutput>#cfdocument.currentPageNumber# of</strong>
                <strong>#cfdocument.totalPageCount#</cfoutput></td></tr></strong>
        </table>
    </cfdocumentitem>

    <cfdocumentsection>
        <h1>Section 1</h1>
        <cfloop from=1 to=5 index="i">
            Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.<p>
        </cfloop>
    </cfdocumentsection>

    <cfdocumentsection>
        <h1>Section 2</h1>
        <cfloop from=1 to=5 index="i">
            Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.<p>
        </cfloop>
    </cfdocumentsection>

    <cfdocumentsection>
    <h1>Section 3</h1>
        <cfloop from=1 to=5 index="i">
            Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.<p>
        </cfloop>
    </cfdocumentsection>
</cfdocument>