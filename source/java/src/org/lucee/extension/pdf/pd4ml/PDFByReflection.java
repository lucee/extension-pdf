/**
*
* Copyright (c) 2016, Lucee Assosication Switzerland
* Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either 
* version 2.1 of the License, or (at your option) any later version.
* 
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public 
* License along with this library.  If not, see <http://www.gnu.org/licenses/>.
* 
**/
package org.lucee.extension.pdf.pd4ml;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;

import org.lucee.extension.pdf.PDFPageMark;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;
import lucee.runtime.util.Cast;
import lucee.runtime.util.ClassUtil;
import lucee.runtime.util.IO;
import lucee.runtime.util.ResourceUtil;

public class PDFByReflection {

	private final Object pd4ml;
	private CFMLEngine engine;
	private Cast caster;
	private static Class pd4mlClass;
	private static ClassLoader classLoader;
	private static Class pd4mlMarkClass;
	// private final boolean isEvaluation;

	public PDFByReflection(Config config) throws PageException {
		engine = CFMLEngineFactory.getInstance();
		caster = engine.getCastUtil();
		ClassUtil util = engine.getClassUtil();
		IO io = engine.getIOUtil();
		// this.isEvaluation=isEvaluation;
		try {
			// classLoader=new URLClassLoader(new URL[]{new
			// File("/Users/mic/Downloads/java/pd4ml/fullversion/pd4ml.volume.310/lib/pd4ml.jar").toURL()},this.getClass().getClassLoader());
			if (classLoader == null) {
				Resource temp = config.getConfigDir().getRealResource("temp");
				Resource file1 = temp.getRealResource("resource.lmdp");
				Resource file2 = temp.getRealResource("css.lmdp");
				Resource file3 = temp.getRealResource("fonts.lmdp");

				if (!file1.exists() || file1.length() == 0 || !file2.exists() || file2.length() == 0 || !file3.exists() || file3.length() == 0) {
					file1.delete();
					file2.delete();
					file3.delete();
					// ss_css2.jar

					ResourceUtil resUtil = engine.getResourceUtil();
					resUtil.removeChildrenSilent(temp, resUtil.getExtensionResourceFilter(".lmdp", false));

					file1.createFile(true);
					file2.createFile(true);
					file3.createFile(true);
					InputStream jar1 = getClass().getResourceAsStream("/org/lucee/extension/pdf/res/pd4ml.jar");
					io.copy(jar1, file1, true);
					InputStream jar2 = getClass().getResourceAsStream("/org/lucee/extension/pdf/res/ss_css2.jar");
					io.copy(jar2, file2, true);
					InputStream jar3 = getClass().getResourceAsStream("/org/lucee/extension/pdf/res/fonts.jar");
					io.copy(jar3, file3, true);

				}
				ClassLoader parent = CFMLEngineFactory.class.getClassLoader();

				// does the fonts.jar exists? if so we do not set any
				URL prop = parent.getResource("fonts/pd4fonts.properties");
				if (prop == null) prop = parent.getResource("/fonts/pd4fonts.properties");
				URL[] urls;
				if (prop == null) urls = new URL[] { caster.toFile(file1).toURL(), caster.toFile(file2).toURL(), caster.toFile(file3).toURL() };
				else urls = new URL[] { caster.toFile(file1).toURL(), caster.toFile(file2).toURL() };

				classLoader = new URLClassLoader(urls, parent);
			}
			if (pd4mlClass == null) pd4mlClass = util.loadClass(classLoader, "org.zefer.pd4ml.PD4ML");
			pd4ml = util.loadInstance(pd4mlClass);

		}
		catch (Exception e) {
			throw caster.toPageException(e);
		}
		pd4mlClass = pd4ml.getClass();
	}

	public void enableTableBreaks(boolean b) throws PageException {
		invoke(pd4ml, "enableTableBreaks", b);
	}

	public void interpolateImages(boolean b) throws PageException {
		invoke(pd4ml, "interpolateImages", b);
	}

	public void adjustHtmlWidth() throws PageException {
		invoke(pd4ml, "adjustHtmlWidth");
	}

	public void setPageInsets(Insets insets) throws PageException {
		invoke(pd4ml, "setPageInsets", insets);
	}

	public void setPageSize(Dimension dimension) throws PageException {
		invoke(pd4ml, "setPageSize", dimension);
	}

	public void setPageHeader(PDFPageMark header) throws PageException {
		invoke(pd4ml, "setPageHeader", toPD4PageMark(header));
	}

	public void generateOutlines(boolean flag) throws PageException {
		invoke(pd4ml, "generateOutlines", new Object[] { caster.toBoolean(flag) }, new Class[] { boolean.class });
	}

	public void useTTF(String pathToFontDirs, boolean embed) throws PageException {
		invoke(pd4ml, "useTTF", new Object[] { pathToFontDirs, caster.toBoolean(embed) }, new Class[] { String.class, boolean.class });
	}

	public boolean isPro() throws PageException {
		return CFMLEngineFactory.getInstance().getCastUtil().toBooleanValue(invoke(pd4ml, "isPro", new Object[] {}, new Class[] {}));
	}

	public void overrideDocumentEncoding(String encoding) throws PageException {
		invoke(pd4ml, "overrideDocumentEncoding", new Object[] { encoding }, new Class[] { String.class });
	}

	public void setDefaultTTFs(String string, String string2, String string3) throws PageException {
		invoke(pd4ml, "setDefaultTTFs", new Object[] { string, string2, string3 }, new Class[] { String.class, String.class, String.class });
	}

	public void setPageFooter(PDFPageMark footer) throws PageException {
		// if(isEvaluation) return;
		invoke(pd4ml, "setPageFooter", toPD4PageMark(footer));
	}

	public void render(InputStreamReader reader, OutputStream os) throws PageException {
		invoke(pd4ml, "render", new Object[] { reader, os }, new Class[] { reader.getClass(), OutputStream.class });
	}

	public BufferedImage[] renderAsImages(URL url, int width, int height) throws PageException {
		return (BufferedImage[]) invoke(pd4ml, "renderAsImages", new Object[] { url, width, height }, new Class[] { URL.class, int.class, int.class });
	}

	public void render(String str, OutputStream os, URL base) throws PageException {
		// setEvaluationFooter();

		StringReader sr = new StringReader(str);
		if (base == null) {
			invoke(pd4ml, "render", new Object[] { sr, os }, new Class[] { sr.getClass(), OutputStream.class });
		}
		else {
			invoke(pd4ml, "render", new Object[] { sr, os, base }, new Class[] { sr.getClass(), OutputStream.class, URL.class });
		}
		// invoke(pd4ml,"render",new StringReader(str),os,OutputStream.class);
	}

	/*
	 * private void setEvaluationFooterX() throws PageException { if(isEvaluation)
	 * invoke(pd4ml,"setPageFooter",toPD4PageMark(new PDFPageMark(-1,EVAL_TEXT))); }
	 */

	private Object toPD4PageMark(PDFPageMark mark) throws PageException {
		Object pd4mlMark = null;
		try {
			ClassUtil util = engine.getClassUtil();
			if (pd4mlMarkClass == null) pd4mlMarkClass = util.loadClass(classLoader, "org.zefer.pd4ml.PD4PageMark");
			pd4mlMark = util.loadInstance(pd4mlMarkClass);

		}
		catch (Exception e) {
			e.printStackTrace();
		}

		invoke(pd4mlMark, "setAreaHeight", mark.getAreaHeight());
		invoke(pd4mlMark, "setHtmlTemplate", mark.getHtmlTemplate());
		return pd4mlMark;
	}

	private Object invoke(Object o, String methodName, Object[] args, Class[] argClasses) throws PageException {
		try {
			return o.getClass().getMethod(methodName, argClasses).invoke(o, args);
		}
		catch (Exception e) {
			throw caster.toPageException(e);
		}
	}

	/*
	 * private void invoke(Object o,String methodName, Object argument1, Object argument2,Class clazz)
	 * throws PageException { try { o.getClass().getMethod(methodName, new
	 * Class[]{argument1.getClass(),clazz}).invoke(o, new Object[]{argument1,argument2}); } catch
	 * (Exception e) { throw Caster.toPageException(e); } }
	 */
	private void invoke(Object o, String methodName, Object argument) throws PageException {
		try {
			o.getClass().getMethod(methodName, new Class[] { argument.getClass() }).invoke(o, new Object[] { argument });
		}
		catch (Exception e) {
			throw caster.toPageException(e);
		}
	}

	private void invoke(Object o, String methodName, boolean argument) throws PageException {
		try {
			o.getClass().getMethod(methodName, new Class[] { boolean.class }).invoke(o, new Object[] { caster.toRef(argument) });
		}
		catch (Exception e) {
			throw caster.toPageException(e);
		}
	}

	private void invoke(Object o, String methodName, int argument) throws PageException {
		try {
			o.getClass().getMethod(methodName, new Class[] { int.class }).invoke(o, new Object[] { caster.toRef(argument) });
		}
		catch (Exception e) {
			throw caster.toPageException(e);
		}
	}

	private void invoke(Object o, String methodName) throws PageException {
		try {
			o.getClass().getMethod(methodName, new Class[] {}).invoke(o, new Object[] {});
		}
		catch (Exception e) {
			throw caster.toPageException(e);
		}
	}
}