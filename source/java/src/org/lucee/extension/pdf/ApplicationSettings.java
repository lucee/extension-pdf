package org.lucee.extension.pdf;

import java.io.File;

import org.lucee.extension.pdf.util.FontsJarExtractor;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.Struct;

public class ApplicationSettings {
	private final int type;
	private final File fontDirectory;
	private final String unsupportedEngine;

	private static boolean init = false;

	public ApplicationSettings(int type, File fontDirectory, String unsupportedEngine) {
		this.type = type;
		this.fontDirectory = fontDirectory;
		this.unsupportedEngine = unsupportedEngine;
	}

	public int getType() {
		return type;
	}

	public File getFontDirectory() {
		return fontDirectory;
	}

	public void validateEngine() throws PageException {
		if (unsupportedEngine != null) {
			PDFDocument.validateEngine(unsupportedEngine);
		}
	}

	public static ApplicationSettings getApplicationSettings(PageContext pc) {

		if (!init && pc != null) {
			initDefaultFontDirectory(pc.getConfig());
			init = true;
		}

		int type = PDFDocument.TYPE_FS;
		File fontDirectory = null;
		String unsupportedEngine = null;
		try {
			BIF bif = CFMLEngineFactory.getInstance().getClassUtil().loadBIF(pc, "lucee.runtime.functions.system.GetApplicationSettings");
			Struct sct = (Struct) bif.invoke(pc, new Object[] { Boolean.TRUE });
			Object o = sct.get("pdf", null);
			if (o instanceof Struct) {
				Struct pdf = (Struct) o;
				// type
				o = pdf.get("type", null);
				if (o == null) o = pdf.get("engine", null);
				if (o == null) o = pdf.get("renderer", null);

				if (o instanceof String) {
					String str = (String) o;
					if (str.equalsIgnoreCase("pd4ml") || str.equalsIgnoreCase("classic")) {
						unsupportedEngine = str;
					}
					else if (str.equalsIgnoreCase("fs") || str.equalsIgnoreCase("modern")) type = PDFDocument.TYPE_FS;
				}

				// fontDirectory
				o = pdf.get("fontDirectory", null);
				if (o instanceof String) {
					String str = (String) o;
					Resource res = CFMLEngineFactory.getInstance().getResourceUtil().toResourceExisting(pc, str);
					if (res.isDirectory() && res instanceof File) fontDirectory = (File) res;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		if (fontDirectory == null) {
			fontDirectory = getDefaultFontDirectory(pc.getConfig());
		}

		return new ApplicationSettings(type, fontDirectory, unsupportedEngine);
	}

	public static File getDefaultFontDirectory(Config config) {
		Resource fonts = config.getConfigDir().getRealResource("fonts");
		fonts.mkdirs();
		if (fonts.isDirectory() && fonts instanceof File) return (File) fonts;
		return null;
	}

	public synchronized static void initDefaultFontDirectory(Config config) {
		File dir = getDefaultFontDirectory(config);
		if (dir == null || dir.list().length > 1) return;

		try {
			FontsJarExtractor.extract(dir);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
