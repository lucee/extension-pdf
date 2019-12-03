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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.lucee.extension.pdf.ApplicationSettings;
import org.lucee.extension.pdf.PDFDocument;
import org.lucee.extension.pdf.util.ClassUtil;
import org.lucee.extension.pdf.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import lucee.commons.io.res.ContentType;
import lucee.commons.io.res.Resource;
import lucee.commons.net.http.HTTPResponse;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.util.Cast;
import lucee.runtime.util.Excepton;
import lucee.runtime.util.IO;

public final class PD4MLPDFDocument extends PDFDocument {

	private int mimetype = MIMETYPE_TEXT_HTML;
	private String strMimetype = null;

	private Cast caster;
	private Excepton exp;
	private IO io;

	public PD4MLPDFDocument() {
		super();
		caster = engine.getCastUtil();
		exp = engine.getExceptionUtil();
		io = engine.getIOUtil();
	}

	@Override
	public byte[] render(Dimension dimension, double unitFactor, PageContext pc, boolean generateOutlines) throws PageException, IOException {
		ConfigWeb config = pc.getConfig();
		PDFByReflection pd4ml = new PDFByReflection(config);
		pd4ml.generateOutlines(generateOutlines);
		pd4ml.enableTableBreaks(true);
		pd4ml.interpolateImages(true);
		// MUSTMUST DO NOT ENABLE, why this was disabled
		pd4ml.adjustHtmlWidth();

		// check size
		int mTop = toPoint(margintop, unitFactor);
		int mLeft = toPoint(marginleft, unitFactor);
		int mBottom = toPoint(marginbottom, unitFactor);
		int mRight = toPoint(marginright, unitFactor);
		if ((mLeft + mRight) > dimension.getWidth())
			throw exp.createApplicationException("current document width (" + caster.toString(dimension.getWidth()) + " point) is smaller that specified horizontal margin  ("
					+ caster.toString(mLeft + mRight) + " point).", "1 in = " + Math.round(1 * UNIT_FACTOR_IN) + " point and 1 cm = " + Math.round(1 * UNIT_FACTOR_CM) + " point");
		if ((mTop + mBottom) > dimension.getHeight())
			throw exp.createApplicationException("current document height (" + caster.toString(dimension.getHeight()) + " point) is smaller that specified vertical margin  ("
					+ caster.toString(mTop + mBottom) + " point).", "1 in = " + Math.round(1 * UNIT_FACTOR_IN) + " point and 1 cm = " + Math.round(1 * UNIT_FACTOR_CM) + " point");

		// Size
		pd4ml.setPageInsets(new Insets(mTop, mLeft, mBottom, mRight));
		pd4ml.setPageSize(dimension);

		// header
		if (header != null) pd4ml.setPageHeader(header);
		// footer
		if (footer != null) pd4ml.setPageFooter(footer);

		// content
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			content(pd4ml, pc, baos);

		}
		finally {
			io.closeSilent(baos);
		}
		return baos.toByteArray();
	}

	private void content(PDFByReflection pd4ml, PageContext pc, OutputStream os) throws PageException, IOException {
		ConfigWeb config = pc.getConfig();

		if (fontDirectory != null) {
			prepare(fontDirectory);
			pd4ml.useTTF(fontDirectory.getAbsolutePath(), fontembed);
		}
		else pd4ml.useTTF("java:fonts", fontembed);

		// body
		if (!Util.isEmpty(body, true)) {
			// optimize html
			URL base = getBase(pc);

			try {
				body = beautifyHTML(new InputSource(new StringReader(body)), base);
			}
			catch (Exception e) {}
			pd4ml.render(body, os, base);

		}
		// srcfile
		else if (srcfile != null) {
			if (charset == null) charset = pc.getResourceCharset();

			// mimetype
			if (Util.isEmpty(strMimetype)) {
				String mt = engine.getResourceUtil().getMimeType(srcfile, null);
				if (mt != null) setMimetype(mt);
			}
			InputStream is = srcfile.getInputStream();
			try {

				URL base = new URL("file://" + srcfile);
				if (!localUrl) {
					// PageContext pc = Thread LocalPageContext.get();

					String abs = srcfile.getAbsolutePath();
					String contract = ClassUtil.ContractPath(pc, abs);
					if (!abs.equals(contract)) {
						base = engine.getHTTPUtil().toURL(getDomain(pc.getHttpServletRequest()) + contract);
					}

				}

				// URL base = localUrl?new URL("file://"+srcfile):getBase();
				render(pd4ml, is, os, base);
			}
			catch (Throwable t) {
				if (t instanceof ThreadDeath) throw (ThreadDeath) t;
			}
			finally {
				io.closeSilent(is);
			}
		}
		// src
		else if (src != null) {
			if (charset == null) charset = engine.getCastUtil().toCharset("iso-8859-1");
			URL url = engine.getHTTPUtil().toURL(src);

			// set Proxy
			if (Util.isEmpty(proxyserver) && config.isProxyEnableFor(url.getHost())) {
				ProxyData pd = config.getProxyData();
				proxyserver = pd == null ? null : pd.getServer();
				proxyport = pd == null ? 0 : pd.getPort();
				proxyuser = pd == null ? null : pd.getUsername();
				proxypassword = pd == null ? null : pd.getPassword();
			}

			HTTPResponse method = engine.getHTTPUtil().get(url, authUser, authPassword, -1, null, userAgent, proxyserver, proxyport, proxyuser, proxypassword, null);

			// mimetype
			if (Util.isEmpty(strMimetype)) {
				ContentType ct = method.getContentType();
				if (ct != null) setMimetype(ct.toString());

			}
			InputStream is = new ByteArrayInputStream(method.getContentAsByteArray());
			try {

				render(pd4ml, is, os, url);
			}
			finally {
				io.closeSilent(is);
			}
		}
		else {
			pd4ml.render("<html><body> </body></html>", os, null);
		}
	}

	private void prepare(File fontDirectory) {
		prepare(fontDirectory, "pd4fonts.properties");
	}

	/*
	 * #this is an autogenerated file. please remove manually any references to copyrighted fonts #Fri
	 * Apr 17 20:00:52 CEST 2009 Arial=arial.ttf Arial\ Bold=arialbd.ttf Arial\ Bold\ Italic=arialbi.ttf
	 * Arial\ Italic=ariali.ttf Courier\ New=cour.ttf Courier\ New\ Bold=courbd.ttf Courier\ New\ Bold\
	 * Italic=courbi.ttf Courier\ New\ Italic=couri.ttf Times\ New\ Roman=times.ttf Times\ New\ Roman\
	 * Bold=timesbd.ttf Times\ New\ Roman\ Bold\ Italic=timesbi.ttf Times\ New\ Roman\ Italic=timesi.ttf
	 *
	 * @param args
	 */

	private static String beautifyHTML(InputSource is, URL base) throws PageException, SAXException, IOException {
		Document xml = XMLUtil.parseHTML(is);
		patchPD4MLProblems(xml, base);
		if (base != null) URLResolver.getInstance().transform(xml, base);
		String html = toHTML(xml);
		return html;
	}

	private static void patchPD4MLProblems(Document xml, URL base) {
		Element b = XMLUtil.getChildWithName("body", xml.getDocumentElement());
		if (!b.hasChildNodes()) {
			b.appendChild(xml.createTextNode(" "));
		}
		inlineExternalImages(CFMLEngineFactory.getInstance(), xml.getDocumentElement(), base.getHost() + ":" + base.getPort());
	}

	private static void inlineExternalImages(CFMLEngine engine, Node n, String hostPort) {
		if (n.getNodeName().equalsIgnoreCase("img") && n instanceof Element) {
			Element e = (Element) n;
			String src = e.getAttribute("src");
			try {
				if (src.startsWith("http://") || src.startsWith("https://")) {
					URL url = new URL(src);
					if (!(url.getHost() + ":" + url.getPort()).equalsIgnoreCase(hostPort)) {
						e.setAttribute("src", toBase64(url, engine));
					}
				}
			}
			catch (MalformedURLException mue) {}
		}
		else {
			NodeList children = n.getChildNodes();
			int len = children.getLength();
			for (int i = 0; i < len; i++) {
				inlineExternalImages(engine, children.item(i), hostPort);
			}
		}
	}

	private static String toBase64(URL url, CFMLEngine engine) {
		try {
			if (!url.getFile().endsWith(".jpg")) return url.toExternalForm();

			HTTPResponse rsp = engine.getHTTPUtil().get(url, null, null, -1, null, null, null, -1, null, null, null);
			if (!"image/jpeg".equals(rsp.getContentType().toString())) return url.toExternalForm();
			String b64 = engine.getCastUtil().toBase64(rsp.getContentAsByteArray());
			return "data:image/jpeg;base64," + b64;
		}
		catch (Exception e) {
			return url.toExternalForm();
		}

	}

	private URL getBase(PageContext pc) throws MalformedURLException, PageException, RuntimeException {
		// PageContext pc = Thread LocalPageContext.get();
		if (pc == null) return null;

		String userAgent = pc.getHttpServletRequest().getHeader("User-Agent");
		// bug in pd4ml-> html badse definition create a call
		if (!Util.isEmpty(userAgent) && (userAgent.startsWith("Java"))) return null;

		String url = getRequestURL(pc.getHttpServletRequest(), false);
		return CFMLEngineFactory.getInstance().getHTTPUtil().toURL(url, -1, true);
	}

	private void render(PDFByReflection pd4ml, InputStream is, OutputStream os, URL base) throws IOException, PageException {
		try {

			// text/html
			if (mimetype == MIMETYPE_TEXT_HTML) {
				body = "";

				try {
					InputSource input = new InputSource(io.getReader(is, charset));
					body = beautifyHTML(input, base);
				}
				catch (Throwable t) {
					if (t instanceof ThreadDeath) throw (ThreadDeath) t;
				}
				// else if(body==null)body =IOUtil.toString(is,strCharset);
				pd4ml.render(body, os, base);
			}
			// text
			else if (mimetype == MIMETYPE_TEXT) {
				body = io.toString(is, charset);
				body = "<html><body><pre>" + engine.getHTMLUtil().escapeHTML(body) + "</pre></body></html>";
				pd4ml.render(body, os, null);
			}
			// image
			else if (mimetype == MIMETYPE_IMAGE) {
				Resource tmpDir = engine.getSystemUtil().getTempDirectory();
				Resource tmp = tmpDir.getRealResource(this + "-" + Math.random());
				io.copy(is, tmp, true);
				body = "<html><body><img src=\"file://" + tmp + "\"></body></html>";
				try {
					pd4ml.render(body, os, null);
				}
				finally {
					tmp.delete();
				}
			}
			// Application
			else if (mimetype == MIMETYPE_APPLICATION && "application/pdf".equals(strMimetype)) {
				io.copy(is, os, true, true);
			}
			else pd4ml.render(new InputStreamReader(is), os);
		}
		finally {
			io.closeSilent(is, os);
		}
	}

	@Override
	public void pageBreak(PageContext pc) throws IOException {
		pc.forceWrite("<pd4ml:page.break>");
	}

	@Override
	public String handlePageNumbers(String html) {
		html = Util.replace(html.trim(), "{currentsectionpagenumber}", "${page}", false);
		html = Util.replace(html, "{totalsectionpagecount}", "${total}", false);

		html = Util.replace(html.trim(), "{currentpagenumber}", "${page}", false);
		html = Util.replace(html, "{totalpagecount}", "${total}", false);
		return html;
	}

}
