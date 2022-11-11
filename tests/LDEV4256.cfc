component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" skip=true {
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4256", function() {
			it( title="check cfdocument with the style float right in table", body=function( currentSpec ) {
				try {
					document name="local.test4256" {
						echo('<html>
							<body>
								<table>
									<tr>
										<td></td>
										<td style="float:right"></td>
									</tr>
								</table>
							</body>
						</html>');
					}
				}
				catch(any e) {
					local.test4256 = e.message;
				}
				expect( isPDFObject(test4256) ).toBeTrue();
			});
		});
	}
}
