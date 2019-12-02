/**
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package org.lucee.extension.pdf.xhtmlrenderer;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.lucee.extension.pdf.PDFDocument;
import org.lucee.extension.pdf.PDFPageMark;
import org.lucee.extension.pdf.util.ClassUtil;
import org.lucee.extension.pdf.util.Margin;
import org.lucee.extension.pdf.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.lowagie.text.DocumentException;

import lucee.commons.io.res.ContentType;
import lucee.commons.net.http.HTTPResponse;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.util.Strings;

public final class FSPDFDocument extends PDFDocument {

	public FSPDFDocument() {}

	@Override
	public byte[] render(Dimension dimension, double unitFactor, PageContext pc, boolean generategenerateOutlines) throws PageException, IOException, DocumentException {
		ITextRenderer renderer = new ITextRenderer();

		// prepare(fontDirectory, "fs.properties");

		// fonts
		ITextFontResolver resolver = renderer.getFontResolver();
		resolver.addFontDirectory(fontDirectory.getCanonicalPath(), fontembed);

		// margin
		double mt = margintop;
		if (mt <= 0D) mt = getHeader() != null ? MARGIN_WITH_HF : MARGIN_INIT;
		double mb = marginbottom;
		if (mb <= 0D) mb = getFooter() != null ? MARGIN_WITH_HF : MARGIN_INIT;

		Margin margin = new Margin(this, unitFactor, margintop, marginbottom, marginleft, marginright);

		if ((margin.getLeftAsPoint() + margin.getRightAsPoint()) > dimension.getWidth()) throw engine.getExceptionUtil().createApplicationException(
				"current document width (" + engine.getCastUtil().toString(dimension.getWidth()) + " point) is smaller that specified horizontal margin  ("
						+ engine.getCastUtil().toString(margin.getLeftAsPoint() + margin.getRightAsPoint()) + " point).",
				"1 in = " + Math.round(1 * UNIT_FACTOR_IN) + " point and 1 cm = " + Math.round(1 * UNIT_FACTOR_CM) + " point");
		if ((margin.getTopAsPoint() + margin.getBottomAsPoint()) > dimension.getHeight()) throw engine.getExceptionUtil().createApplicationException(
				"current document height (" + engine.getCastUtil().toString(dimension.getHeight()) + " point) is smaller that specified vertical margin  ("
						+ engine.getCastUtil().toString(margin.getTopAsPoint() + margin.getBottomAsPoint()) + " point).",
				"1 in = " + Math.round(1 * UNIT_FACTOR_IN) + " point and 1 cm = " + Math.round(1 * UNIT_FACTOR_CM) + " point");

		// Size
		// TODO pd4ml.setPageInsets(new Insets(mTop,mLeft,mBottom,mRight));
		// TODO pd4ml.setPageSize(dimension);

		// content
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			content(renderer, pc, baos, margin, dimension, getPageOffset());

		}
		catch (Exception e) {
			throw engine.getCastUtil().toPageException(e);
		}
		finally {
			engine.getIOUtil().closeSilent(baos);
		}
		return baos.toByteArray();
	}

	private void content(ITextRenderer renderer, PageContext pc, OutputStream os, Margin margin, Dimension dimension, int pageOffset)
			throws PageException, IOException, SAXException, DocumentException {
		ConfigWeb config = pc.getConfig();
		// body
		Document doc;
		if (!Util.isEmpty(body, true)) {
			doc = parseHTML(XMLUtil.toInputSource(body), margin, dimension, pageOffset, true);
			String raw = XMLUtil.toString(doc, false, true, null, null, null);
			createPDF(pc, renderer, doc, os, null);
		}
		// srcfile
		else if (srcfile != null) {
			if (charset == null) charset = pc.getResourceCharset();

			// mimetype
			if (mimeType == MIMETYPE_OTHER) {
				ContentType ct = CFMLEngineFactory.getInstance().getResourceUtil().getContentType(srcfile);
				setMimetype(CFMLEngineFactory.getInstance().getResourceUtil().getContentType(srcfile));
			}
			InputStream is = srcfile.getInputStream();
			try {

				URL base = new URL("file://" + srcfile);
				if (!localUrl) {
					String abs = srcfile.getAbsolutePath();
					String contract = ClassUtil.ContractPath(pc, abs);
					if (!abs.equals(contract)) {
						base = engine.getHTTPUtil().toURL(getDomain(pc.getHttpServletRequest()) + contract);
					}
				}

				// URL base = localUrl?new URL("file://"+srcfile):getBase();
				render(pc, renderer, is, os, base, margin, dimension, pageOffset);
			}
			catch (Throwable t) {
				if (t instanceof ThreadDeath) throw (ThreadDeath) t;
			}
			finally {
				Util.closeEL(is);
			}
		}
		// src
		else if (src != null) {
			if (charset == null) charset = pc.getResourceCharset();
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
			if (mimeType == MIMETYPE_OTHER) {
				ContentType ct = method.getContentType();
				if (ct != null) setMimetype(ct);
			}

			InputStream is = new ByteArrayInputStream(method.getContentAsByteArray());
			try {
				render(pc, renderer, is, os, url, margin, dimension, pageOffset);
			}
			finally {
				engine.getIOUtil().closeSilent(is);
			}
		}
		else {
			createPDF(renderer, "<html><body> </body></html>", os);
		}
	}

	private void render(PageContext pc, ITextRenderer renderer, InputStream is, OutputStream os, URL base, Margin margin, Dimension dim, int pageOffset)
			throws PageException, IOException, SAXException, DocumentException {
		try {
			// text/html
			if (mimeType == MIMETYPE_TEXT_HTML || mimeType == MIMETYPE_OTHER) {
				InputSource input = new InputSource(engine.getIOUtil().getReader(is, charset));
				Document doc = parseHTML(input, margin, dim, pageOffset, true);
				String raw = XMLUtil.toString(doc, false, true, null, null, null);
				createPDF(pc, renderer, doc, os, base);
			}
			// text
			else if (mimeType == MIMETYPE_TEXT) {
				body = engine.getIOUtil().toString(is, charset);
				body = "<html><body><pre>" + engine.getHTMLUtil().escapeHTML(body) + "</pre></body></html>";
				createPDF(renderer, body, os);
			}
			// image
			else if (mimeType == MIMETYPE_IMAGE) {
				File tmpDir = CFMLEngineFactory.getTempDirectory();
				File tmp = new File(tmpDir, this + "-" + Math.random());
				engine.getIOUtil().copy(is, new FileOutputStream(tmp), true, true);
				body = "<html><body><img src=\"file://" + tmp + "\"></body></html>"; // TODO test this
				try {
					createPDF(renderer, body, os);
				}
				finally {
					tmp.delete();
				}
			}
			// Application
			else if (mimeType == MIMETYPE_APPLICATION_PDF) {
				engine.getIOUtil().copy(is, os, true, true);
			}
			else {
				throw engine.getExceptionUtil().createApplicationException("this mime type is not supported!");
			}
		}
		finally {
			Util.closeEL(is, os);
		}
	}

	private void createPDF(PageContext pc, ITextRenderer renderer, Document doc, OutputStream os, URL base) throws DocumentException, MalformedURLException {
		if (base == null) base = searchBaseURL(doc);
		if (base == null) base = getRequestURL(pc);
		renderer.setDocument(doc, base == null ? null : base.toExternalForm());
		renderer.layout();
		renderer.createPDF(os);
	}

	private void createPDF(ITextRenderer renderer, String xhtml, OutputStream os) throws DocumentException {
		renderer.setDocumentFromString(xhtml);
		renderer.layout();
		renderer.createPDF(os);
	}

	@Override
	public void pageBreak(PageContext pc) throws IOException {
		pc.forceWrite("<p class=\"pagebreak\"/>");
	}

	private Document parseHTML(InputSource is, Margin margin, Dimension dimension, int pageOffset, boolean addHF) throws IOException, SAXException, PageException {
		Document doc = XMLUtil.parseHTML(is);

		Element head = tag(doc, "head");
		Element body = tag(doc, "body");

		// no body
		if (body == null) {
			body = doc.createElement("body");
			doc.getDocumentElement().appendChild(body);
		}
		// no head
		if (head == null) {
			head = doc.createElement("head");
			doc.getDocumentElement().insertBefore(head, body);
		}

		PDFPageMark h = getHeader();
		PDFPageMark f = getFooter();

		// we have header or body
		if (addHF) {
			if (h != null || f != null) {
				if (h != null) add(doc, body, "luceefsheader");

				if (f != null) add(doc, body, "luceefsfooter");

				String raw = XMLUtil.toString(doc, false, true, null, null, null);
				Strings util = CFMLEngineFactory.getInstance().getStringUtil();
				if (h != null) raw = util.replace(raw, "{{{luceefsheader}}}", h.getHtmlTemplate(), true, false);
				if (f != null) raw = util.replace(raw, "{{{luceefsfooter}}}", f.getHtmlTemplate(), true, false);
				return parseHTML(XMLUtil.toInputSource(raw), margin, dimension, pageOffset, false);
			}
		}

		// body
		// add css for page break, header and footer
		StringBuilder sb = new StringBuilder();
		sb.append("@media print {").append('\n');
		sb.append(".pagebreak { page-break-after:always }").append('\n');
		if (h != null) sb.append("div.luceefsheader {display: block; position: running(header);}").append('\n');
		if (f != null) sb.append("div.luceefsfooter {display: block; position: running(footer);}").append('\n');
		sb.append("}").append('\n');

		if (h != null) sb.append("@page { @top-center    { content: element(header) }}").append('\n');
		if (f != null) sb.append("@page { @bottom-center { content: element(footer) }}").append('\n');

		sb.append(".luceefspagenumber:before {content: counter(page);}").append('\n');
		sb.append(".luceefspagecount:before {content: counter(pages);}").append('\n');
		sb.append(".luceefssecpagenumber:before {content: counter(page);}").append('\n');
		sb.append(".luceefssecpagecount:before {content: counter(pages);}").append('\n');

		sb.append("@page { margin: " + margin.getTop() + " " + margin.getRight() + " " + margin.getBottom() + " " + margin.getLeft() + "}").append('\n');
		sb.append("@page { size: " + asString(dimension.getWidth()) + " " + asString(dimension.getHeight()) + ";}").append('\n');
		sb.append("body {font-size: 21px;}").append('\n');

		Element style = doc.createElement("style");
		style.appendChild(doc.createTextNode(sb.toString()));
		head.appendChild(style);

		// margin
		/*
		 * String tmp = body.getAttribute("style"); if(Util.isEmpty(tmp)) sb=new StringBuilder(); else
		 * if(!tmp.endsWith(";"))sb=new StringBuilder(tmp).append(';'); else sb=new StringBuilder(tmp);
		 * sb.append("padding:0px;margin: ") .append(margin.top).append("pt ")
		 * .append(margin.right).append("pt ") .append(margin.bottom).append("pt ")
		 * .append(margin.left).append("pt;"); body.setAttribute("style", sb.toString());
		 */

		moveStyleScript(head, body);
		return doc;
	}

	private String asString(double d) throws PageException {
		return engine.getCastUtil().toString(d) + "pt";
	}

	private void add(Document doc, Element body, String name) throws SAXException, IOException {
		Element div = doc.createElement("div");
		div.setAttribute("class", name);
		div.appendChild(doc.createTextNode("{{{" + name + "}}}"));
		Node first = body.getFirstChild();
		if (first != null) body.insertBefore(div, first);
		else body.appendChild(div);

	}

	private static void moveStyleScript(Element head, Node p) {
		NodeList nl = p.getChildNodes();
		Node n;
		Element e;
		int len = nl.getLength();
		for (int i = 0; i < len; i++) {
			n = nl.item(i);
			// we only care about tags
			if (n instanceof Element) {
				e = (Element) n;
				if ("style".equalsIgnoreCase(e.getNodeName()) || "script".equalsIgnoreCase(e.getNodeName())) {
					// move to header
					p.removeChild(e);
					head.appendChild(e);
				}
				else moveStyleScript(head, e);
			}
		}
	}

	private Element tag(Document doc, String name) {
		Element root = doc.getDocumentElement();
		NodeList nl = root.getChildNodes();
		Node n;
		int len = nl.getLength();
		for (int i = 0; i < len; i++) {
			n = nl.item(i);
			if (n instanceof Element && name.equalsIgnoreCase(n.getNodeName())) {
				return (Element) n;
			}
		}
		return null;
	}

	@Override
	public String handlePageNumbers(String html) {
		html = Util.replace(html.trim(), "{currentsectionpagenumber}", "<span class=\"luceefssecpagenumber\"/>", false);
		html = Util.replace(html, "{totalsectionpagecount}", "<span class=\"luceefssecpagecount\"/>", false);

		html = Util.replace(html, "{currentpagenumber}", "<span class=\"luceefspagenumber\"/>", false);
		html = Util.replace(html, "{totalpagecount}", "<span class=\"luceefspagecount\"/>", false);
		return html;
	}
}