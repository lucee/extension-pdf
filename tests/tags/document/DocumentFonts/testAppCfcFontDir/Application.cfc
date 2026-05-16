component {
	this.name = "DocumentFontsAppCfc";
	// font dir is at tests/artifacts/fonts; this Application.cfc is at tests/tags/document/DocumentFonts/testAppCfcFontDir/
	this.pdf = {
		fontDirectory: getDirectoryFromPath( getCurrentTemplatePath() ) & "../../../../artifacts/fonts"
	};
}
