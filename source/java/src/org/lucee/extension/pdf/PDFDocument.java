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
package org.lucee.extension.pdf;

import java.awt.Dimension;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;

import org.icepdf.core.exceptions.PDFException;
import org.lucee.extension.pdf.pd4ml.PD4MLPDFDocument;
import org.lucee.extension.pdf.util.Margin;
import org.lucee.extension.pdf.util.XMLUtil;
import org.lucee.extension.pdf.xhtmlrenderer.FSPDFDocument;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lucee.commons.io.res.ContentType;
import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

public abstract class PDFDocument {

	// PageType
	public static final Dimension PAGETYPE_ISOB5 = new Dimension(501, 709);
	public static final Dimension PAGETYPE_ISOB4 = new Dimension(709, 1002);
	public static final Dimension PAGETYPE_ISOB3 = new Dimension(1002, 1418);
	public static final Dimension PAGETYPE_ISOB2 = new Dimension(1418, 2004);
	public static final Dimension PAGETYPE_ISOB1 = new Dimension(2004, 2836);
	public static final Dimension PAGETYPE_ISOB0 = new Dimension(2836, 4008);
	public static final Dimension PAGETYPE_HALFLETTER = new Dimension(396, 612);
	public static final Dimension PAGETYPE_LETTER = new Dimension(612, 792);
	public static final Dimension PAGETYPE_TABLOID = new Dimension(792, 1224);
	public static final Dimension PAGETYPE_LEDGER = new Dimension(1224, 792);
	public static final Dimension PAGETYPE_NOTE = new Dimension(540, 720);
	public static final Dimension PAGETYPE_LEGAL = new Dimension(612, 1008);

	public static final Dimension PAGETYPE_A10 = new Dimension(74, 105);
	public static final Dimension PAGETYPE_A9 = new Dimension(105, 148);
	public static final Dimension PAGETYPE_A8 = new Dimension(148, 210);
	public static final Dimension PAGETYPE_A7 = new Dimension(210, 297);
	public static final Dimension PAGETYPE_A6 = new Dimension(297, 421);
	public static final Dimension PAGETYPE_A5 = new Dimension(421, 595);
	public static final Dimension PAGETYPE_A4 = new Dimension(595, 842);
	public static final Dimension PAGETYPE_A3 = new Dimension(842, 1190);
	public static final Dimension PAGETYPE_A2 = new Dimension(1190, 1684);
	public static final Dimension PAGETYPE_A1 = new Dimension(1684, 2384);
	public static final Dimension PAGETYPE_A0 = new Dimension(2384, 3370);

	public static final Dimension PAGETYPE_B4 = new Dimension(708, 1000);
	public static final Dimension PAGETYPE_B5 = new Dimension(499, 708);
	public static final Dimension PAGETYPE_B4_JIS = new Dimension(728, 1031);
	public static final Dimension PAGETYPE_B5_JIS = new Dimension(516, 728);
	public static final Dimension PAGETYPE_CUSTOM = new Dimension(1, 1);

	// orientation
	public static final String ORIENTATION_LANDSCAPE = "landscape";
	public static final String ORIENTATION_PORTRAIT = "portrait";

	// encryption
	public static final int ENC_NONE = 0;
	public static final int ENC_40BIT = 1;
	public static final int ENC_128BIT = 2;

	// fontembed
	public static final int FONT_EMBED_NO = 0;
	public static final int FONT_EMBED_YES = 1;
	public static final int FONT_EMBED_SELECCTIVE = FONT_EMBED_YES;

	// unit
	public static final double UNIT_FACTOR_CM = Margin.UNIT_FACTOR_CM;// 85d / 3d;// =28.333333333333333333333333333333333333333333;
	public static final double UNIT_FACTOR_IN = Margin.UNIT_FACTOR_IN;// UNIT_FACTOR_CM * 2.54;
	public static final double UNIT_FACTOR_POINT = Margin.UNIT_FACTOR_PT;// 1;
	public static final double UNIT_FACTOR_PIXEL = Margin.UNIT_FACTOR_PX;// 1d/12d/16d;

	// margin init
	public static final int MARGIN_INIT = 36;
	public static final int MARGIN_WITH_HF = 73;

	// mimetype
	protected static final int MIMETYPE_TEXT_HTML = 0;
	protected static final int MIMETYPE_TEXT = 1;
	protected static final int MIMETYPE_IMAGE = 2;
	protected static final int MIMETYPE_APPLICATION = 3;
	protected static final int MIMETYPE_APPLICATION_PDF = 4;
	protected static final int MIMETYPE_OTHER = -1;

	protected double margintop = -1;
	protected double marginbottom = -1;
	protected double marginleft = -1;
	protected double marginright = -1;

	protected int mimeType = MIMETYPE_OTHER;
	protected Charset charset = null;

	// No default value applied here, because a PDFDocument may represent either
	// a cfdocument or a cfdocumentsection. We only want the former to have a
	// default, so we will set it in Document instead, because Document is aware
	// of the context.
	protected String orientation = null;

	protected boolean backgroundvisible;
	protected boolean fontembed = true;
	protected PDFPageMark header;
	protected PDFPageMark footer;

	protected String proxyserver;
	protected int proxyport = 80;
	protected String proxyuser = null;
	protected String proxypassword = "";

	protected String src = null;
	protected Resource srcfile = null;
	protected String body;
	// private boolean isEvaluation;
	protected String name;
	protected String authUser;
	protected String authPassword;
	protected String userAgent;
	protected boolean localUrl;
	protected boolean bookmark;
	protected boolean htmlBookmark;
	protected final CFMLEngine engine;
	protected File fontDirectory;
	private int pageOffset;
	private int pages;

	public static int PD4ML = 1;
	public static int FS = 2;

	public PDFDocument() {
		engine = CFMLEngineFactory.getInstance();
		userAgent = "Lucee PDF Extension";

	}

	public static PDFDocument newInstance(int type) {
		if (PD4ML == type) return new PD4MLPDFDocument();
		return new FSPDFDocument();
	}

	public final void setHeader(PDFPageMark header) {
		this.header = header;
	}

	public final void setFooter(PDFPageMark footer) {
		this.footer = footer;
	}

	/**
	 * @param marginbottom the marginbottom to set
	 */
	public final void setMarginbottom(double marginbottom) {
		this.marginbottom = marginbottom;
	}

	/**
	 * @param marginleft the marginleft to set
	 */
	public final void setMarginleft(double marginleft) {
		this.marginleft = marginleft;
	}

	/**
	 * @param marginright the marginright to set
	 */
	public final void setMarginright(double marginright) {
		this.marginright = marginright;
	}

	/**
	 * @param margintop the margintop to set
	 */
	public final void setMargintop(double margintop) {
		this.margintop = margintop;
	}

	/**
	 * @param ct the mimetype to set
	 * @throws PageException
	 */
	public final void setMimetype(ContentType ct) throws PageException {
		// mimetype
		if (ct.getMimeType().startsWith("text/html")) mimeType = MIMETYPE_TEXT_HTML;
		else if (ct.getMimeType().startsWith("text/")) mimeType = MIMETYPE_TEXT;
		else if (ct.getMimeType().startsWith("image/")) mimeType = MIMETYPE_IMAGE;
		else if (ct.getMimeType().startsWith("application/pdf")) mimeType = MIMETYPE_APPLICATION_PDF;
		else mimeType = MIMETYPE_OTHER;

		// charset
		String strCharset = ct.getCharset();
		if (!Util.isEmpty(strCharset, true)) {
			charset = engine.getCastUtil().toCharset(strCharset);
		}
	}

	public final void setMimetype(String strMimetype) throws PageException {
		strMimetype = strMimetype.toLowerCase().trim();

		// mimetype
		if (strMimetype.startsWith("text/html")) mimeType = MIMETYPE_TEXT_HTML;
		else if (strMimetype.startsWith("text/")) mimeType = MIMETYPE_TEXT;
		else if (strMimetype.startsWith("image/")) mimeType = MIMETYPE_IMAGE;
		else if (strMimetype.startsWith("application/pdf")) mimeType = MIMETYPE_APPLICATION_PDF;
		else mimeType = MIMETYPE_OTHER;

		// charset
		String[] arr = engine.getListUtil().toStringArray(strMimetype, ";");
		if (arr.length >= 2) {
			strMimetype = arr[0].trim();
			for (int i = 1; i < arr.length; i++) {
				String[] item = engine.getListUtil().toStringArray(arr[i], "=");
				if (item.length == 1) {
					charset = engine.getCastUtil().toCharset(item[0].trim());
					break;
				}
				else if (item.length == 2 && item[0].trim().equals("charset")) {
					charset = engine.getCastUtil().toCharset(item[1].trim());
					break;
				}
			}
		}
	}

	public String getOrientation() {
		return this.orientation;
	}

	/**
	 * @param orientation the orientation to set
	 * @throws PageException
	 */
	public void setOrientation(String strOrientation) throws PageException {
		if (strOrientation != ORIENTATION_LANDSCAPE && strOrientation != ORIENTATION_PORTRAIT) {
			String err = String.format(
					"invalid orientation [%s], valid orientations are [%s,%s]",
					strOrientation,
					ORIENTATION_PORTRAIT,
					ORIENTATION_LANDSCAPE
				);
			throw engine.getExceptionUtil().createApplicationException(err);
		}
		setOrientationNoCheck(strOrientation);
	}

	/**
	 * Set the orientation, without any parameter checking. Use this when the
	 * calling method cannot throw exceptions, and be careful!
	 * @param orientation the orientation to set. ("portrait" or "landscape")
	 */
	public void setOrientationNoCheck(String strOrientation) {
		this.orientation = strOrientation;
	}

	/**
	 * set the value proxyserver Host name or IP address of a proxy server.
	 *
	 * @param proxyserver value to set
	 **/
	public final void setProxyserver(String proxyserver) {
		this.proxyserver = proxyserver;
	}

	/**
	 * set the value proxyport The port number on the proxy server from which the object is requested.
	 * Default is 80. When used with resolveURL, the URLs of retrieved documents that specify a port
	 * number are automatically resolved to preserve links in the retrieved document.
	 *
	 * @param proxyport value to set
	 **/
	public final void setProxyport(int proxyport) {
		this.proxyport = proxyport;
	}

	/**
	 * set the value username When required by a proxy server, a valid username.
	 *
	 * @param proxyuser value to set
	 **/
	public final void setProxyuser(String proxyuser) {
		this.proxyuser = proxyuser;
	}

	/**
	 * set the value password When required by a proxy server, a valid password.
	 *
	 * @param proxypassword value to set
	 **/
	public final void setProxypassword(String proxypassword) {
		this.proxypassword = proxypassword;
	}

	/**
	 * @param src
	 * @throws PDFException
	 */
	public final void setSrc(String src) throws PageException {
		if (srcfile != null) throw engine.getExceptionUtil().createApplicationException("You cannot specify both the src and srcfile attributes");
		this.src = src;
	}

	/**
	 * @param srcfile the srcfile to set
	 * @throws PDFException
	 */
	public final void setSrcfile(Resource srcfile) throws PageException {
		if (src != null) throw engine.getExceptionUtil().createApplicationException("You cannot specify both the src and srcfile attributes");
		this.srcfile = srcfile;
	}

	public final void setBody(String body) {
		this.body = body;
	}

	public final String getBody() {
		return body;
	}

	public abstract byte[] render(Dimension dimension, double unitFactor, PageContext pc, boolean generategenerateOutlines) throws Exception;

	protected final static URL getRequestURL(PageContext pc) {
		if (pc == null) return null;
		try {
			return CFMLEngineFactory.getInstance().getHTTPUtil().toURL(getDirectoryFromPath(getRequestURL(pc.getHttpServletRequest(), false)));
		}
		catch (Throwable t) {
			if (t instanceof ThreadDeath) throw (ThreadDeath) t;
			return null;
		}
	}

	public final PDFPageMark getHeader() {
		return header;
	}

	public final PDFPageMark getFooter() {
		return footer;
	}

	public final void setFontembed(int fontembed) {
		this.fontembed = fontembed != FONT_EMBED_NO;
	}

	public final void setFontDirectory(File fontdirectory) {
		this.fontDirectory = fontdirectory;
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public final void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the authUser
	 */
	public final String getAuthUser() {
		return authUser;
	}

	/**
	 * @param authUser the authUser to set
	 */
	public final void setAuthUser(String authUser) {
		this.authUser = authUser;
	}

	/**
	 * @return the authPassword
	 */
	public final String getAuthPassword() {
		return authPassword;
	}

	/**
	 * @param authPassword the authPassword to set
	 */
	public final void setAuthPassword(String authPassword) {
		this.authPassword = authPassword;
	}

	/**
	 * @return the userAgent
	 */
	public final String getUserAgent() {
		return userAgent;
	}

	/**
	 * @param userAgent the userAgent to set
	 */
	public final void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * @return the proxyserver
	 */
	public final String getProxyserver() {
		return proxyserver;
	}

	/**
	 * @return the proxyport
	 */
	public final int getProxyport() {
		return proxyport;
	}

	/**
	 * @return the proxyuser
	 */
	public final String getProxyuser() {
		return proxyuser;
	}

	/**
	 * @return the proxypassword
	 */
	public final String getProxypassword() {
		return proxypassword;
	}

	public final boolean hasProxy() {
		return !Util.isEmpty(proxyserver);
	}

	/**
	 * @return the localUrl
	 */
	public final boolean getLocalUrl() {
		return localUrl;
	}

	/**
	 * @param localUrl the localUrl to set
	 */
	public final void setLocalUrl(boolean localUrl) {
		this.localUrl = localUrl;
	}

	/**
	 * @return the bookmark
	 */
	public final boolean getBookmark() {
		return bookmark;
	}

	/**
	 * @param bookmark the bookmark to set
	 */
	public final void setBookmark(boolean bookmark) {
		this.bookmark = bookmark;
	}

	/**
	 * @return the htmlBookmark
	 */
	public final boolean getHtmlBookmark() {
		return htmlBookmark;
	}

	/**
	 * @param htmlBookmark the htmlBookmark to set
	 */
	public final void setHtmlBookmark(boolean htmlBookmark) {
		this.htmlBookmark = htmlBookmark;
	}

	public File getFontDirectory() {
		return fontDirectory;
	}

	public boolean getFontembed() {
		return fontembed;
	}

	protected final static String getRequestURL(HttpServletRequest req, boolean includeQueryString) {
		StringBuffer sb = req.getRequestURL();
		int maxpos = sb.indexOf("/", 8);
		if (maxpos > -1) {
			if (req.isSecure()) {
				if (sb.substring(maxpos - 4, maxpos).equals(":443")) sb.delete(maxpos - 4, maxpos);
			}
			else {
				if (sb.substring(maxpos - 3, maxpos).equals(":80")) sb.delete(maxpos - 3, maxpos);
			}

			if (includeQueryString && !Util.isEmpty(req.getQueryString())) sb.append('?').append(req.getQueryString());
		}
		return sb.toString();
	}

	public final static String getDirectoryFromPath(String path) {
		int posOfLastDel = path.lastIndexOf('/');
		String parent = "";

		if (path.lastIndexOf('\\') > posOfLastDel) posOfLastDel = path.lastIndexOf("\\");
		if (posOfLastDel != -1) parent = path.substring(0, posOfLastDel + 1);
		else if (path.equals(".") || path.equals("..")) parent = String.valueOf(File.separatorChar);
		else if (path.startsWith(".")) parent = String.valueOf(File.separatorChar);
		else parent = String.valueOf(File.separatorChar);
		return parent;
	}

	public static String getDomain(HttpServletRequest req) {
		StringBuilder sb = new StringBuilder();
		sb.append(req.isSecure() ? "https://" : "http://");
		sb.append(req.getServerName());
		sb.append(':');
		sb.append(req.getServerPort());
		if (!Util.isEmpty(req.getContextPath())) sb.append(req.getContextPath());
		return sb.toString();
	}

	protected static URL searchBaseURL(Document doc) {
		Element html = doc.getDocumentElement();
		NodeList list = html.getChildNodes();
		Node n;
		for (int i = list.getLength() - 1; i >= 0; i--) {
			n = list.item(i);
			// head
			if (n instanceof Element && ((Element) n).getNodeName().equalsIgnoreCase("head")) {
				Element head = (Element) n;
				NodeList _list = html.getChildNodes();
				for (int _i = _list.getLength() - 1; _i >= 0; _i--) {
					n = list.item(i);
					// base
					if (n instanceof Element && ((Element) n).getNodeName().equalsIgnoreCase("base")) {
						Element base = (Element) n;
						String href = base.getAttribute("href");
						if (!Util.isEmpty(href)) {
							try {
								return CFMLEngineFactory.getInstance().getHTTPUtil().toURL(href);
							}
							catch (MalformedURLException e) {}
						}
					}

				}
			}
		}
		return null;
	}

	public static String toHTML(Node node) throws PageException {
		if (Node.DOCUMENT_NODE == node.getNodeType()) return toHTML(XMLUtil.getRootElement(node));

		StringBuilder sb = new StringBuilder();
		toHTML(node, sb);
		return sb.toString();
	}

	private static void toHTML(Node node, StringBuilder sb) throws PageException {
		short type = node.getNodeType();
		if (Node.ELEMENT_NODE == type) {
			Element el = (Element) node;
			String tagName = el.getTagName();
			sb.append('<');
			sb.append(tagName);

			NamedNodeMap attrs = el.getAttributes();
			Attr attr;
			int len = attrs.getLength();
			for (int i = 0; i < len; i++) {
				attr = (Attr) attrs.item(i);
				sb.append(' ');
				sb.append(attr.getName());
				sb.append("=\"");
				sb.append(attr.getValue());
				sb.append('"');
			}
			NodeList children = el.getChildNodes();
			len = children.getLength();

			boolean doEndTag = len != 0 || (tagName.length() == 4 && (tagName.equalsIgnoreCase("head") || tagName.equalsIgnoreCase("body")));

			if (!doEndTag) sb.append(" />");
			else sb.append('>');

			for (int i = 0; i < len; i++) {
				toHTML(children.item(i), sb);
			}

			if (doEndTag) {
				sb.append("</");
				sb.append(el.getTagName());
				sb.append('>');
			}
		}
		else if (node instanceof CharacterData) {
			sb.append(CFMLEngineFactory.getInstance().getHTMLUtil().escapeHTML(node.getNodeValue()));
		}
	}

	protected void prepare(File fontDirectory, String propertyName) {
		if (!fontDirectory.isDirectory()) return;
		File fontProps = new File(fontDirectory, propertyName);
		if (fontProps.isFile()) return;

		// create file content
		StringBuilder sb = new StringBuilder("# generated by the Lucee PDF Extension based on the files in this directory");
		File[] children = fontDirectory.listFiles();
		String name;
		for (File child: children) {
			if (!child.getName().toLowerCase().endsWith(".ttf")) continue;
			try {
				name = Font.createFont(Font.TRUETYPE_FONT, child).getName();
				sb.append(engine.getStringUtil().replace(name, " ", "\\ ", false, false)).append('=').append(child.getName()).append('\n');
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		// create the file
		try {
			engine.getIOUtil().copy(new ByteArrayInputStream(sb.toString().trim().getBytes("UTF-8")), new FileOutputStream(fontProps), true, true);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public final static int toPoint(double value, double unitFactor) {
		if (value < 0) return MARGIN_INIT;
		return (int) Math.round(value * unitFactor);
		// return r;
	}

	public abstract void pageBreak(PageContext pc) throws IOException;

	public abstract String handlePageNumbers(String html);

	public void setPageOffset(int pageOffset) {
		this.pageOffset = pageOffset;
	}

	public int getPageOffset() {
		return pageOffset;
	}

	public void setPages(int pages) {
		this.pages = pages;
	}

	public int getPages() {
		return pages;
	}

	public void setHFIndex(int i) {
		if (header != null) header.setIndex(i);
		if (footer != null) footer.setIndex(i);
	}

}
