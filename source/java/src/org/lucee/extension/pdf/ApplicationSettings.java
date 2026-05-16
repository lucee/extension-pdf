package org.lucee.extension.pdf;

import java.io.File;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.Struct;

public class ApplicationSettings {
	private final File fontDirectory;

	public ApplicationSettings(File fontDirectory) {
		this.fontDirectory = fontDirectory;
	}

	public File getFontDirectory() {
		return fontDirectory;
	}

	public static ApplicationSettings getApplicationSettings(PageContext pc) {
		File fontDirectory = null;
		try {
			BIF bif = CFMLEngineFactory.getInstance().getClassUtil().loadBIF(pc, "lucee.runtime.functions.system.GetApplicationSettings");
			Struct sct = (Struct) bif.invoke(pc, new Object[] { Boolean.TRUE });
			Object o = sct.get("pdf", null);
			if (o instanceof Struct) {
				Struct pdf = (Struct) o;
				o = pdf.get("fontDirectory", null);
				if (o instanceof String) {
					Resource res = CFMLEngineFactory.getInstance().getResourceUtil().toResourceExisting(pc, (String) o);
					if (res.isDirectory() && res instanceof File) fontDirectory = (File) res;
				}
			}
		}
		catch (Exception e) {
			// silent - font/PDF config errors should not break the application
		}
		if (fontDirectory == null) {
			fontDirectory = getDefaultFontDirectory(pc.getConfig());
		}

		return new ApplicationSettings(fontDirectory);
	}

	public static File getDefaultFontDirectory(Config config) {
		Resource fonts = config.getConfigDir().getRealResource("fonts");
		fonts.mkdirs();
		if (fonts.isDirectory() && fonts instanceof File) return (File) fonts;
		return null;
	}
}
