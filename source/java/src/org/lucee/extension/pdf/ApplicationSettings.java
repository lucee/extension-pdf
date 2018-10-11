package org.lucee.extension.pdf;

import java.io.File;

public class ApplicationSettings {
	private final int type;
	private final File fontDirectory;
	
	public ApplicationSettings(int type, File fontDirectory) {
		this.type = type;
		this.fontDirectory = fontDirectory;
	}

	public int getType() {
		return type;
	}

	public File getFontDirectory() {
		return fontDirectory;
	}
}
