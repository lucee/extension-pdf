/**
 * Copyright (c) 2015, Lucee Association Switzerland
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.lucee.extension.pdf;

import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.w3c.dom.Document;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.PageSizeUnits;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import org.lucee.extension.pdf.util.LuceeLogHandler;

import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

/**
 * PDFDocument - HTML to PDF renderer using OpenHTMLToPDF.
 * This replaces the old PD4ML and Flying Saucer implementations.
 */
public class PDFDocument {

	// Type constants (kept for backward compatibility, but now ignored)
	public static final int TYPE_NONE = 0;
	public static final int TYPE_PD4ML = 1;   // Legacy - treated same as TYPE_NONE
	public static final int TYPE_FS = 2;      // Legacy - treated same as TYPE_NONE

	// Page types (in points - 72 points per inch)
	public static final Dimension PAGETYPE_CUSTOM = new Dimension(1, 1);
	public static final Dimension PAGETYPE_LETTER = new Dimension(612, 792);       // 8.5 x 11 inches
	public static final Dimension PAGETYPE_LEGAL = new Dimension(612, 1008);       // 8.5 x 14 inches
	public static final Dimension PAGETYPE_A4 = new Dimension(595, 842);           // 210 x 297 mm
	public static final Dimension PAGETYPE_A5 = new Dimension(420, 595);           // 148 x 210 mm
	public static final Dimension PAGETYPE_B4 = new Dimension(709, 1001);          // 250 x 353 mm
	public static final Dimension PAGETYPE_B5 = new Dimension(499, 709);           // 176 x 250 mm
	public static final Dimension PAGETYPE_B4_JIS = new Dimension(729, 1032);      // JIS B4
	public static final Dimension PAGETYPE_B5_JIS = new Dimension(516, 729);       // JIS B5

	// Unit conversion factors (to points)
	public static final double UNIT_FACTOR_IN = 72.0;      // 1 inch = 72 points
	public static final double UNIT_FACTOR_CM = 28.3465;   // 1 cm = 28.3465 points
	public static final double UNIT_FACTOR_POINT = 1.0;    // 1 point = 1 point
	public static final double UNIT_FACTOR_PIXEL = 0.75;   // 1 pixel = 0.75 points (at 96 DPI)

	// Font embed options
	public static final int FONT_EMBED_YES = 1;
	public static final int FONT_EMBED_NO = 2;
	public static final int FONT_EMBED_SELECCTIVE = 3;

	// Encryption options
	public static final int ENC_NONE = 0;
	public static final int ENC_40BIT = 1;
	public static final int ENC_128BIT = 2;

	// Orientation constants
	public static final int ORIENTATION_UNDEFINED = -1;
	public static final int ORIENTATION_PORTRAIT = 0;
	public static final int ORIENTATION_LANDSCAPE = 1;

	// Margin constants (in points)
	public static final double MARGIN_INIT = 36;      // Default margin 0.5 inch
	public static final double MARGIN_WITH_HF = 72;   // Margin when header/footer present (1 inch)

	// Instance fields
	private String body;
	private String src;
	private Resource srcfile;
	private String authUser;
	private String authPassword;
	private String userAgent;
	private String proxyserver;
	private int proxyport = 80;
	private String proxyuser;
	private String proxypassword;
	private boolean bookmark;
	private boolean htmlBookmark;
	private boolean localUrl;
	private int fontembed = FONT_EMBED_YES;
	private File fontDirectory;
	private double margintop = -1;
	private double marginbottom = -1;
	private double marginleft = -1;
	private double marginright = -1;
	private String mimetype;
	private int orientation = ORIENTATION_UNDEFINED;
	private PDFPageMark header;
	private PDFPageMark footer;
	private String name;
	private int pageOffset = 0;
	private int pages = 0;
	private int hfIndex = 0;

	private static final CFMLEngine engine = CFMLEngineFactory.getInstance();

	public PDFDocument() {
	}

	/**
	 * Factory method for creating PDFDocument instances.
	 * The type parameter is now ignored - OpenHTMLToPDF is always used.
	 */
	public static PDFDocument newInstance(int type) {
		// Type is ignored - we always use OpenHTMLToPDF now
		return new PDFDocument();
	}

	// Setters
	public void setBody(String body) {
		this.body = body;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public void setSrcfile(Resource srcfile) {
		this.srcfile = srcfile;
	}

	public void setAuthUser(String authUser) {
		this.authUser = authUser;
	}

	public void setAuthPassword(String authPassword) {
		this.authPassword = authPassword;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public void setProxyserver(String proxyserver) {
		this.proxyserver = proxyserver;
	}

	public void setProxyport(int proxyport) {
		this.proxyport = proxyport;
	}

	public void setProxyuser(String proxyuser) {
		this.proxyuser = proxyuser;
	}

	public void setProxypassword(String proxypassword) {
		this.proxypassword = proxypassword;
	}

	public void setBookmark(boolean bookmark) {
		this.bookmark = bookmark;
	}

	public void setHtmlBookmark(boolean htmlBookmark) {
		this.htmlBookmark = htmlBookmark;
	}

	public void setLocalUrl(boolean localUrl) {
		this.localUrl = localUrl;
	}

	public void setFontembed(int fontembed) {
		this.fontembed = fontembed;
	}

	public void setFontDirectory(File fontDirectory) {
		this.fontDirectory = fontDirectory;
	}

	public void setMargintop(double margintop) {
		this.margintop = margintop;
	}

	public void setMarginbottom(double marginbottom) {
		this.marginbottom = marginbottom;
	}

	public void setMarginleft(double marginleft) {
		this.marginleft = marginleft;
	}

	public void setMarginright(double marginright) {
		this.marginright = marginright;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	public void setOrientation(String strOrientation) throws PageException {
		if (Util.isEmpty(strOrientation, true)) return;
		strOrientation = strOrientation.trim().toLowerCase();
		if ("portrait".equals(strOrientation)) {
			this.orientation = ORIENTATION_PORTRAIT;
		}
		else if ("landscape".equals(strOrientation)) {
			this.orientation = ORIENTATION_LANDSCAPE;
		}
		else {
			throw engine.getExceptionUtil().createApplicationException(
				"Invalid orientation [" + strOrientation + "], valid orientations are [portrait, landscape]");
		}
	}

	public void setHeader(PDFPageMark header) {
		this.header = header;
	}

	public void setFooter(PDFPageMark footer) {
		this.footer = footer;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPageOffset(int pageOffset) {
		this.pageOffset = pageOffset;
	}

	public void setPages(int pages) {
		this.pages = pages;
	}

	public void setHFIndex(int hfIndex) {
		this.hfIndex = hfIndex;
	}

	// Getters
	public String getBody() {
		return body;
	}

	public String getSrc() {
		return src;
	}

	public Resource getSrcfile() {
		return srcfile;
	}

	public String getAuthUser() {
		return authUser;
	}

	public String getAuthPassword() {
		return authPassword;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public String getProxyserver() {
		return proxyserver;
	}

	public int getProxyport() {
		return proxyport;
	}

	public String getProxyuser() {
		return proxyuser;
	}

	public String getProxypassword() {
		return proxypassword;
	}

	public boolean getBookmark() {
		return bookmark;
	}

	public boolean getHtmlBookmark() {
		return htmlBookmark;
	}

	public boolean getLocalUrl() {
		return localUrl;
	}

	public boolean getFontembed() {
		return fontembed == FONT_EMBED_YES;
	}

	public File getFontDirectory() {
		return fontDirectory;
	}

	public double getMargintop() {
		return margintop;
	}

	public double getMarginbottom() {
		return marginbottom;
	}

	public double getMarginleft() {
		return marginleft;
	}

	public double getMarginright() {
		return marginright;
	}

	public String getMimetype() {
		return mimetype;
	}

	public int getOrientation() {
		return orientation;
	}

	public PDFPageMark getHeader() {
		return header;
	}

	public PDFPageMark getFooter() {
		return footer;
	}

	public String getName() {
		return name;
	}

	public int getPageOffset() {
		return pageOffset;
	}

	public int getPages() {
		return pages;
	}

	public int getHFIndex() {
		return hfIndex;
	}

	public boolean hasProxy() {
		return !Util.isEmpty(proxyserver);
	}

	/**
	 * Convert a value to points using the given unit factor.
	 */
	public static int toPoint(double value, double unitFactor) {
		return (int) Math.round(value * unitFactor);
	}

	/**
	 * Render the HTML content to PDF.
	 *
	 * @param dimension Page dimensions in points
	 * @param unitFactor Unit conversion factor for margins
	 * @param pc PageContext for resource resolution
	 * @param doHtmlBookmarks Whether to include HTML bookmarks
	 * @return PDF as byte array
	 */
	public byte[] render(Dimension dimension, double unitFactor, PageContext pc, boolean doHtmlBookmarks) throws PageException {
		try {
			String html = getHTMLContent(pc);
			String baseUrl = getBaseUrl(pc);

			// Parse HTML with JSoup and convert to W3C DOM
			org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);
			jsoupDoc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);

			// Convert local file paths to file:// URIs for OpenHTMLToPDF compatibility
			convertLocalPathsToURIs(jsoupDoc);

			W3CDom w3cDom = new W3CDom();
			Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

			// Inject CSS for page size and margins
			String pageCSS = buildPageCSS(dimension, unitFactor);
			injectPageCSS(jsoupDoc, pageCSS);

			// Re-convert after CSS injection
			w3cDoc = w3cDom.fromJsoup(jsoupDoc);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfRendererBuilder builder = new PdfRendererBuilder();

			// Set the W3C DOM
			builder.withW3cDocument(w3cDoc, baseUrl);

			// Configure fonts
			if (fontDirectory != null && fontDirectory.isDirectory()) {
				File[] fonts = fontDirectory.listFiles((dir, name) ->
					name.toLowerCase().endsWith(".ttf") || name.toLowerCase().endsWith(".otf"));
				if (fonts != null) {
					for (File font : fonts) {
						try {
							// Get the actual font family name from the font file
							java.awt.Font awtFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, font);
							String fontFamily = awtFont.getFamily();
							builder.useFont(font, fontFamily);
						}
						catch (Exception e) {
							// Fallback to filename if font can't be read
							try {
								String fontName = font.getName().replaceFirst("[.][^.]+$", "");
								builder.useFont(font, fontName);
							}
							catch (Exception e2) {
								// Skip fonts that can't be loaded
							}
						}
					}
				}
			}

			builder.toStream(baos);

			// Route OpenHTMLToPDF logging through Lucee (uncomment when done debugging)
			// Log pdfLog = pc.getConfig().getLog("pdf");
			// Logger ohtLogger = Logger.getLogger("com.openhtmltopdf");
			// ohtLogger.setUseParentHandlers(false);
			// ohtLogger.addHandler(new LuceeLogHandler(pdfLog));

			builder.run();

			return baos.toByteArray();
		}
		catch (Exception e) {
			throw engine.getCastUtil().toPageException(e);
		}
	}

	/**
	 * Build CSS @page rules for page size and margins.
	 */
	private String buildPageCSS(Dimension dimension, double unitFactor) {
		StringBuilder css = new StringBuilder();
		css.append("@page { ");

		// Page size
		double widthPt = dimension.width;
		double heightPt = dimension.height;
		css.append("size: ").append(widthPt).append("pt ").append(heightPt).append("pt; ");

		// Margins
		if (margintop >= 0) {
			css.append("margin-top: ").append(toPoint(margintop, unitFactor)).append("pt; ");
		}
		if (marginbottom >= 0) {
			css.append("margin-bottom: ").append(toPoint(marginbottom, unitFactor)).append("pt; ");
		}
		if (marginleft >= 0) {
			css.append("margin-left: ").append(toPoint(marginleft, unitFactor)).append("pt; ");
		}
		if (marginright >= 0) {
			css.append("margin-right: ").append(toPoint(marginright, unitFactor)).append("pt; ");
		}

		css.append("}");

		// Header/footer support via CSS running elements
		if (header != null) {
			css.append(" @page { @top-center { content: element(header); } }");
			css.append(" #pdf-header { position: running(header); }");
		}
		if (footer != null) {
			css.append(" @page { @bottom-center { content: element(footer); } }");
			css.append(" #pdf-footer { position: running(footer); }");
		}

		// CSS counters for page numbers - OpenHTMLToPDF supports these
		css.append(" .pdf-page-number::before { content: counter(page); }");
		css.append(" .pdf-page-count::before { content: counter(pages); }");

		return css.toString();
	}

	/**
	 * Inject CSS into the HTML document head.
	 */
	private void injectPageCSS(org.jsoup.nodes.Document doc, String css) {
		org.jsoup.nodes.Element head = doc.head();
		if (head == null) {
			head = doc.appendElement("head");
		}
		head.prependElement("style").attr("type", "text/css").text(css);

		// Inject header/footer content if present
		org.jsoup.nodes.Element body = doc.body();
		if (body != null) {
			if (header != null) {
				String headerHtml = header.getHtml(hfIndex);
				if (!Util.isEmpty(headerHtml)) {
					body.prependElement("div")
						.attr("id", "pdf-header")
						.html(processPageVariables(headerHtml));
				}
			}
			if (footer != null) {
				String footerHtml = footer.getHtml(hfIndex);
				if (!Util.isEmpty(footerHtml)) {
					body.appendElement("div")
						.attr("id", "pdf-footer")
						.html(processPageVariables(footerHtml));
				}
			}
		}
	}

	/**
	 * Convert local file paths in src attributes to file:// URIs.
	 * OpenHTMLToPDF requires proper URIs, not Windows paths like d:\path\file.png
	 */
	private void convertLocalPathsToURIs(org.jsoup.nodes.Document doc) {
		// Process img, link, script, and other elements with src/href attributes
		for (org.jsoup.nodes.Element el : doc.select("[src], [href]")) {
			String src = el.attr("src");
			String href = el.attr("href");

			if (!Util.isEmpty(src)) {
				String converted = pathToFileURI(src);
				if (converted != null) {
					el.attr("src", converted);
				}
			}
			if (!Util.isEmpty(href)) {
				String converted = pathToFileURI(href);
				if (converted != null) {
					el.attr("href", converted);
				}
			}
		}
	}

	/**
	 * Convert a local file path to a file:// URI if it looks like a Windows or Unix path.
	 * Returns null if the path is already a URL or doesn't need conversion.
	 */
	private String pathToFileURI(String path) {
		if (path == null || path.isEmpty()) return null;

		// Skip if already a URL
		String lower = path.toLowerCase();
		if (lower.startsWith("http://") || lower.startsWith("https://") ||
			lower.startsWith("file://") || lower.startsWith("data:")) {
			return null;
		}

		// Check if it looks like a Windows path (e.g., C:\ or d:\)
		if (path.length() > 2 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':') {
			try {
				File f = new File(path);
				return f.toURI().toString();
			}
			catch (Exception e) {
				return null;
			}
		}

		// Check if it looks like an absolute Unix path
		if (path.startsWith("/") && !path.startsWith("//")) {
			try {
				File f = new File(path);
				if (f.exists()) {
					return f.toURI().toString();
				}
			}
			catch (Exception e) {
				return null;
			}
		}

		return null;
	}

	/**
	 * Process page number variables in header/footer content.
	 * OpenHTMLToPDF uses CSS counters which are injected via buildPageCSS().
	 */
	private String processPageVariables(String html) {
		// Replace CFML placeholder patterns with spans that CSS will populate with counters
		html = html.replace("{currentpagenumber}", "<span class=\"pdf-page-number\"></span>");
		html = html.replace("{totalpagecount}", "<span class=\"pdf-page-count\"></span>");
		html = html.replace("{currentsectionpagenumber}", "<span class=\"pdf-page-number\"></span>");
		html = html.replace("{totalsectionpagecount}", "<span class=\"pdf-page-count\"></span>");
		return html;
	}

	/**
	 * Get the HTML content to render.
	 */
	private String getHTMLContent(PageContext pc) throws PageException, IOException {
		// Priority: body > srcfile > src
		// Note: body can be empty string for blank PDFs - that's valid
		if (body != null) {
			// Return body even if empty - creates blank PDF
			return body.isEmpty() ? "<html><body></body></html>" : body;
		}
		if (srcfile != null) {
			InputStream is = srcfile.getInputStream();
			try {
				return engine.getIOUtil().toString(is, StandardCharsets.UTF_8);
			}
			finally {
				Util.closeEL(is);
			}
		}
		if (!Util.isEmpty(src)) {
			return fetchURL(src, pc);
		}
		// No content at all - return blank page (backwards compatible behavior)
		return "<html><body></body></html>";
	}

	/**
	 * Fetch content from a URL.
	 */
	private String fetchURL(String urlStr, PageContext pc) throws PageException, IOException {
		// Handle local URLs
		if (localUrl && !urlStr.toLowerCase().startsWith("http://") && !urlStr.toLowerCase().startsWith("https://")) {
			Resource res = engine.getResourceUtil().toResourceExisting(pc, urlStr);
			InputStream is = res.getInputStream();
			try {
				return engine.getIOUtil().toString(is, StandardCharsets.UTF_8);
			}
			finally {
				Util.closeEL(is);
			}
		}

		// TODO: Implement proper URL fetching with proxy/auth support
		// For now, use JSoup for basic URL fetching
		try {
			org.jsoup.Connection conn = Jsoup.connect(urlStr);
			if (!Util.isEmpty(userAgent)) {
				conn.userAgent(userAgent);
			}
			if (!Util.isEmpty(authUser) && !Util.isEmpty(authPassword)) {
				String auth = java.util.Base64.getEncoder().encodeToString(
					(authUser + ":" + authPassword).getBytes(StandardCharsets.UTF_8));
				conn.header("Authorization", "Basic " + auth);
			}
			// TODO: Add proxy support when needed
			return conn.get().html();
		}
		catch (IOException e) {
			throw engine.getExceptionUtil().createApplicationException(
				"Failed to fetch URL [" + urlStr + "]: " + e.getMessage());
		}
	}

	/**
	 * Handle page number placeholders in content.
	 * Replaces placeholders with CSS counter elements that OpenHTMLToPDF can process.
	 */
	public String handlePageNumbers(String content) {
		if (content == null) return content;
		// These placeholders get replaced at runtime with CSS page counters
		content = content.replace("{currentpagenumber}", "<span class=\"page-number\"></span>");
		content = content.replace("{totalpagecount}", "<span class=\"page-count\"></span>");
		content = content.replace("{currentsectionpagenumber}", "<span class=\"page-number\"></span>");
		content = content.replace("{totalsectionpagecount}", "<span class=\"page-count\"></span>");
		return content;
	}

	/**
	 * Write a page break marker to the output.
	 * With OpenHTMLToPDF, page breaks are handled via CSS.
	 */
	public void pageBreak(PageContext pc) throws IOException {
		// Write a CSS page-break element
		pc.forceWrite("<div style=\"page-break-after: always;\"></div>");
	}

	/**
	 * Determine the base URL for resolving relative resources.
	 */
	private String getBaseUrl(PageContext pc) {
		try {
			if (srcfile != null) {
				if (srcfile instanceof File) {
					return ((File) srcfile).toURI().toString();
				}
				return srcfile.getAbsolutePath();
			}
			if (!Util.isEmpty(src)) {
				try {
					new URL(src);
					return src;
				}
				catch (MalformedURLException e) {
					// Not a valid URL, try as file path
					Resource res = engine.getResourceUtil().toResourceExisting(pc, src);
					if (res instanceof File) {
						return ((File) res).toURI().toString();
					}
				}
			}
			// Default to current template directory
			Resource curr = pc.getCurrentTemplatePageSource().getResource();
			if (curr != null) {
				Resource parent = curr.getParentResource();
				if (parent instanceof File) {
					return ((File) parent).toURI().toString();
				}
			}
		}
		catch (Exception e) {
			// Ignore and return null
		}
		return null;
	}
}
