package org.lucee.extension.pdf.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;

public class FontsJarExtractor {

	public static void extract(File target) throws URISyntaxException, IOException {
		// we first grap the classloader from lucee.jar
		ClassLoader cl = CFMLEngineFactory.class.getClassLoader();
		if (cl == null) cl = ClassLoader.getSystemClassLoader();

		// now we look for pd4fonts.properties that is within the fonts.jar
		URL res = cl.getResource("fonts/pd4fonts.properties");
		if (res == null) res = cl.getResource("/fonts/pd4fonts.properties");
		if (res == null) res = cl.getResource("pd4fonts.properties");
		if (res == null) res = cl.getResource("/pd4fonts.properties");
		if (res == null) return;

		final JarURLConnection connection = (JarURLConnection) res.openConnection();
		final URL url = connection.getJarFileURL();

		File file = new File(url.getPath());
		if (!file.isFile()) return;
		unzip(file, target);

	}

	public static void unzip(File src, File target) throws IOException {
		File trg;
		ZipInputStream zis = null;
		String fullname, name;
		int index;
		try {
			zis = new ZipInputStream(new FileInputStream(src));
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				fullname = entry.getName();
				index = fullname.lastIndexOf('/');
				if (index == -1) index = fullname.lastIndexOf('\\');
				name = index == -1 ? fullname : fullname.substring(index + 1);
				trg = new File(target, name);
				if (!trg.exists() && (fullname.endsWith(".ttf") || fullname.endsWith(".otf"))) {
					if (!trg.exists()) Util.copy(zis, new FileOutputStream(trg), false, true);
					trg.setLastModified(entry.getTime());
				}
				zis.closeEntry();
			}
		}
		finally {
			Util.closeEL(zis);
		}
	}

	public static void main(String[] args) throws Exception {
		extract(new File("/Users/mic/Tmp3/target"));
	}
}
