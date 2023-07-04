package org.lucee.extension.pdf.pd4ml.lib;

import lucee.commons.io.log.Log;
import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;

public class PDFByFactory {
	private static Object token = new Object();
	private static PDFBy instance;

	public static PDFBy getInstance(Config config) throws PageException {
		if (instance == null) {
			synchronized (token) {
				if (instance == null) {
					Log log = config.getLog("application");
					try {
						instance = new PDFByReflection(config);
						log.info("PDF", "using PD4ML via reflection from system Classloader");
					}
					catch (NoClassDefFoundError err) {
						log.error("PDF", err);
						instance = new PDFByInnerReflection(config);
						log.info("PDF", "using PD4ML via reflection from bundled version");
					}

				}
			}
		}
		return instance.newInstance();
	}
}
