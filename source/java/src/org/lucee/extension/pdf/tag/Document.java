/**
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package org.lucee.extension.pdf.tag;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.lucee.extension.pdf.ApplicationSettings;
import org.lucee.extension.pdf.PDFDocument;
import org.lucee.extension.pdf.PDFPageMark;
import org.lucee.extension.pdf.pd4ml.PD4MLPDFDocument;
import org.lucee.extension.pdf.util.ClassUtil;
import org.lucee.extension.pdf.util.PDFUtil;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSmartCopy;
import com.lowagie.text.pdf.SimpleBookmark;

import lucee.Info;
import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Cast;

public final class Document extends BodyTagImpl implements AbsDoc {

	// private static final String STYLE_BG_INVISIBLE = "background-color: transparent;
	// background-image: none;";

	private Resource filename = null;
	private boolean overwrite = false;
	private String name = null;
	private Dimension pagetype = PDFDocument.PAGETYPE_LETTER;
	private double pageheight = 0;
	private double pagewidth = 0;

	private double unitFactor = PDFDocument.UNIT_FACTOR_IN;
	private int encryption = PDFDocument.ENC_NONE;

	private String ownerpassword = null;
	private String userpassword = "";
	private int scale = -1;

	// TODO impl. tag Document backgroundvisible,fontembed,scale
	private boolean backgroundvisible;
	private int fontembed = PDFDocument.FONT_EMBED_YES;
	private File fontdir = null;

	private int permissions = 0;
	private PDFDocument _document;

	private ArrayList<PDFDocument> documents = new ArrayList<PDFDocument>();
	private ApplicationSettings applicationSettings = null;
	private byte[] pdf;
	private int sectionCounter = 0;

	public Document() {
		this._document = null;
	}

	@Override
	public void release() {
		super.release();
		filename = null;
		overwrite = false;
		name = null;
		pagetype = PDFDocument.PAGETYPE_LETTER;
		pageheight = 0;
		pagewidth = 0;
		unitFactor = PDFDocument.UNIT_FACTOR_IN;
		encryption = PDFDocument.ENC_NONE;
		ownerpassword = null;
		userpassword = "";
		permissions = 0;
		scale = -1;
		documents.clear();
		_document = null;
		backgroundvisible = false;
		fontembed = PDFDocument.FONT_EMBED_YES;
		fontdir = null;
		applicationSettings = null;
		this.pdf = null;
		sectionCounter = 0;
	}

	@Override
	public PDFDocument getPDFDocument() {
		if (_document == null) {
			_document = PDFDocument.newInstance(getApplicationSettings().getType());

			// Set default orientation for cfdocument. This happens here, instead of
			// in PDFDocument's property declarations because we only want to set it for
			// PDFDocuments that represent top-level cfdocuments, not cfdocumentsections.
			_document.setOrientationNoCheck(PDFDocument.ORIENTATION_PORTRAIT);
		}
		return _document;
	}

	public List<PDFDocument> getPDFDocuments() {
		return documents;
	}

	public PDFDocument getPDFDocument(int index) {
		try {
			PDFDocument existing = documents.get(index);
			if (existing != null) {
				return existing;
			}
		}
		catch (Exception e) {}
		return null;
	}

	//

	public byte[] getPDF() {
		return pdf;
	}

	public ApplicationSettings getApplicationSettings() {
		if (applicationSettings == null) applicationSettings = ApplicationSettings.getApplicationSettings(pageContext);
		return applicationSettings;
	}

	/**
	 * set the value proxyserver Host name or IP address of a proxy server.
	 *
	 * @param proxyserver value to set
	 **/
	public void setProxyserver(String proxyserver) {
		getPDFDocument().setProxyserver(proxyserver);
	}

	public void setProxyhost(String proxyserver) {
		getPDFDocument().setProxyserver(proxyserver);
	}

	/**
	 * set the value proxyport The port number on the proxy server from which the object is requested.
	 * Default is 80. When used with resolveURL, the URLs of retrieved documents that specify a port
	 * number are automatically resolved to preserve links in the retrieved document.
	 *
	 * @param proxyport value to set
	 **/
	public void setProxyport(double proxyport) {
		getPDFDocument().setProxyport((int) proxyport);
	}

	/**
	 * set the value username When required by a proxy server, a valid username.
	 *
	 * @param proxyuser value to set
	 **/
	public void setProxyuser(String proxyuser) {
		getPDFDocument().setProxyuser(proxyuser);
	}

	/**
	 * set the value password When required by a proxy server, a valid password.
	 *
	 * @param proxypassword value to set
	 **/
	public void setProxypassword(String proxypassword) {
		getPDFDocument().setProxypassword(proxypassword);
	}

	public void setSaveasname(String saveAsName) {
		// TODO impl
	}

	/**
	 * @param authUser the authUser to set
	 */
	public void setAuthuser(String authUser) {
		getPDFDocument().setAuthUser(authUser);
	}

	/**
	 * @param authPassword the authPassword to set
	 */
	public void setAuthpassword(String authPassword) {
		getPDFDocument().setAuthPassword(authPassword);
	}

	/**
	 * @param userAgent the userAgent to set
	 */
	public void setUseragent(String userAgent) {
		getPDFDocument().setUserAgent(userAgent);
	}

	/**
	 * @param format the format to set
	 * @throws PageException
	 */
	public void setFormat(String format) throws PageException {
		format = format.trim().toLowerCase();
		if (!"pdf".equals(format)) throw engine.getExceptionUtil().createApplicationException("invalid format [" + format + "], only the following format is supported [pdf]");
	}

	/**
	 * @param filename the filename to set
	 * @throws PageException
	 */
	public void setFilename(String filename) throws PageException {
		this.filename = engine.getResourceUtil().toResourceNotExisting(pageContext, filename);
		// pageContext.getConfig().getSecurityManager().checkFileLocation(this.filename);
	}

	/**
	 * @param overwrite the overwrite to set
	 */
	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param pagetype the pagetype to set
	 * @throws PageException
	 */
	public void setPagetype(String strPagetype) throws PageException {
		strPagetype = trimAndLower(strPagetype);
		if ("legal".equals(strPagetype)) pagetype = PDFDocument.PAGETYPE_LEGAL;
		else if ("letter".equals(strPagetype)) pagetype = PDFDocument.PAGETYPE_LETTER;
		else if ("a4".equals(strPagetype)) pagetype = PDFDocument.PAGETYPE_A4;
		else if ("a5".equals(strPagetype)) pagetype = PDFDocument.PAGETYPE_A5;
		else if ("b4".equals(strPagetype)) pagetype = PDFDocument.PAGETYPE_B4;
		else if ("b5".equals(strPagetype)) pagetype = PDFDocument.PAGETYPE_B5;
		else if ("b4-jis".equals(strPagetype)) pagetype = PDFDocument.PAGETYPE_B4_JIS;
		else if ("b4 jis".equals(strPagetype)) pagetype = PDFDocument.PAGETYPE_B4_JIS;
		else if ("b4_jis".equals(strPagetype)) pagetype = PDFDocument.PAGETYPE_B4_JIS;
		else if ("b4jis".equals(strPagetype)) pagetype = PDFDocument.PAGETYPE_B4_JIS;
		else if ("b5-jis".equals(strPagetype)) pagetype = PDFDocument.PAGETYPE_B5_JIS;
		else if ("b5 jis".equals(strPagetype)) pagetype = PDFDocument.PAGETYPE_B5_JIS;
		else if ("b5_jis".equals(strPagetype)) pagetype = PDFDocument.PAGETYPE_B5_JIS;
		else if ("b5jis".equals(strPagetype)) pagetype = PDFDocument.PAGETYPE_B5_JIS;
		else if ("custom".equals(strPagetype)) pagetype = PDFDocument.PAGETYPE_CUSTOM;
		else throw engine.getExceptionUtil()
				.createApplicationException("invalid page type [" + strPagetype + "], valid page types are [legal,letter,a4,a5,b4,b5,b4-jis,b5-jis,custom]");

	}

	/**
	 * @param pageheight the pageheight to set
	 * @throws PageException
	 */
	public void setPageheight(double pageheight) throws PageException {
		if (pageheight < 1) throw engine.getExceptionUtil().createApplicationException("pageheight must be a positive number");
		this.pageheight = pageheight;
	}

	/**
	 * @param pagewidth the pagewidth to set
	 * @throws PageException
	 */
	public void setPagewidth(double pagewidth) throws PageException {
		if (pagewidth < 1) throw engine.getExceptionUtil().createApplicationException("pagewidth must be a positive number");
		this.pagewidth = pagewidth;
	}

	public void setMargin(Object margin) throws PageException {
		if (engine.getDecisionUtil().isNumeric(margin)) {
			double nbr = engine.getCastUtil().toDoubleValue(margin);
			getPDFDocument().setMargintop(nbr);
			getPDFDocument().setMarginbottom(nbr);
			getPDFDocument().setMarginleft(nbr);
			getPDFDocument().setMarginright(nbr);
		}
		else {
			Cast cast = engine.getCastUtil();
			Struct sct = cast.toStruct(margin);

			Object o = sct.get("top", null);
			if (o != null) getPDFDocument().setMargintop(cast.toDoubleValue(o));
			o = sct.get("bottom", null);
			if (o != null) getPDFDocument().setMarginbottom(cast.toDoubleValue(o));
			o = sct.get("left", null);
			if (o != null) getPDFDocument().setMarginleft(cast.toDoubleValue(o));
			o = sct.get("right", null);
			if (o != null) getPDFDocument().setMarginright(cast.toDoubleValue(o));
		}
	}

	public void setPage(Object page) throws PageException {
		if (engine.getDecisionUtil().isNumeric(page)) {
			double nbr = engine.getCastUtil().toDoubleValue(page);
			setPagewidth(nbr);
			setPageheight(nbr);
		}
		else {
			Cast cast = engine.getCastUtil();
			Struct sct = cast.toStruct(page);

			Object o = sct.get("width", null);
			if (o != null) setPagewidth(cast.toDoubleValue(o));

			o = sct.get("height", null);
			if (o != null) setPageheight(cast.toDoubleValue(o));

			o = sct.get("type", null);
			if (o != null) setPagetype(cast.toString(o));
		}
	}

	public void setMarginbottom(double marginbottom) {
		getPDFDocument().setMarginbottom(marginbottom);
	}

	/**
	 * @param marginleft the marginleft to set
	 */
	public void setMarginleft(double marginleft) {
		getPDFDocument().setMarginleft(marginleft);
	}

	/**
	 * @param marginright the marginright to set
	 */
	public void setMarginright(double marginright) {
		getPDFDocument().setMarginright(marginright);
	}

	/**
	 * @param margintop the margintop to set
	 */
	public void setMargintop(double margintop) {
		getPDFDocument().setMargintop(margintop);
	}

	/**
	 * @param bookmark the bookmark to set
	 */
	public void setBookmark(boolean bookmark) {
		getPDFDocument().setBookmark(bookmark);
	}

	public void setHtmlbookmark(boolean bookmark) {
		getPDFDocument().setHtmlBookmark(bookmark);
	}

	/**
	 * @param localUrl the localUrl to set
	 */
	public void setLocalurl(boolean localUrl) {
		getPDFDocument().setLocalUrl(localUrl);
	}

	/**
	 * @param unitFactor the unit to set
	 * @throws PageException
	 */
	public void setUnit(String strUnit) throws PageException {
		strUnit = trimAndLower(strUnit);
		if ("in".equals(strUnit) || "inch".equals(strUnit)) unitFactor = PDFDocument.UNIT_FACTOR_IN;
		else if ("cm".equals(strUnit)) unitFactor = PDFDocument.UNIT_FACTOR_CM;
		else if ("point".equals(strUnit) || "pt".equals(strUnit)) unitFactor = PDFDocument.UNIT_FACTOR_POINT;
		else if ("pixel".equals(strUnit) || "px".equals(strUnit)) unitFactor = PDFDocument.UNIT_FACTOR_PIXEL;
		else throw engine.getExceptionUtil().createApplicationException("invalid unit [" + strUnit + "], valid units are [cm,in,pt,px]");
	}

	/**
	 * @param encryption the encryption to set
	 * @throws PageException
	 */
	public void setEncryption(String strEncryption) throws PageException {
		strEncryption = trimAndLower(strEncryption);
		if ("none".equals(strEncryption)) encryption = PDFDocument.ENC_NONE;
		else if ("40-bit".equals(strEncryption)) encryption = PDFDocument.ENC_40BIT;
		else if ("40bit".equals(strEncryption)) encryption = PDFDocument.ENC_40BIT;
		else if ("40 bit".equals(strEncryption)) encryption = PDFDocument.ENC_40BIT;
		else if ("40_bit".equals(strEncryption)) encryption = PDFDocument.ENC_40BIT;
		else if ("128-bit".equals(strEncryption)) encryption = PDFDocument.ENC_128BIT;
		else if ("128bit".equals(strEncryption)) encryption = PDFDocument.ENC_128BIT;
		else if ("128 bit".equals(strEncryption)) encryption = PDFDocument.ENC_128BIT;
		else if ("128_bit".equals(strEncryption)) encryption = PDFDocument.ENC_128BIT;
		else throw engine.getExceptionUtil().createApplicationException("invalid encryption [" + strEncryption + "], valid encryption values are [none, 40-bit, 128-bit]");
	}

	/**
	 * @param ownerpassword the ownerpassword to set
	 * @throws PageException
	 */
	public void setOwnerpassword(String ownerpassword) {
		this.ownerpassword = ownerpassword;
	}

	/**
	 * @param userpassword the userpassword to set
	 */
	public void setUserpassword(String userpassword) {
		this.userpassword = userpassword;
	}

	/**
	 * @param permissions the permissions to set
	 * @throws PageException
	 */
	public void setPermissions(String strPermissions) throws PageException {
		permissions = PDFUtil.toPermissions(strPermissions);
	}

	/**
	 * @param scale the scale to set
	 * @throws PageException
	 */
	public void setScale(double scale) throws PageException {
		if (scale < 0) throw engine.getExceptionUtil().createApplicationException("scale must be a positive number");
		if (scale > 100) throw engine.getExceptionUtil().createApplicationException("scale must be a number less or equal than 100");
		this.scale = (int) scale;
	}

	/**
	 * @param src the src to set
	 * @throws PageException
	 */
	public void setSrc(String src) throws PageException {
		getPDFDocument().setSrc(src);
	}

	/**
	 * @param srcfile the srcfile to set @throws PageException @throws
	 */
	public void setSrcfile(String strSrcfile) throws PageException {
		Resource srcfile = engine.getResourceUtil().toResourceExisting(pageContext, strSrcfile);
		pageContext.getConfig().getSecurityManager().checkFileLocation(srcfile);
		getPDFDocument().setSrcfile(srcfile);
	}

	/**
	 * @param mimetype the mimetype to set
	 * @throws PageException
	 */
	public void setMimetype(String strMimetype) throws PageException {
		getPDFDocument().setMimetype(strMimetype);
		strMimetype = strMimetype.toLowerCase().trim();
	}

	public void setHeader(PDFPageMark header) {
		getPDFDocument().setHeader(header);
	}

	public void setFooter(PDFPageMark footer) {
		getPDFDocument().setFooter(footer);
	}

	public void setBackgroundvisible(boolean backgroundvisible) {
		this.backgroundvisible = backgroundvisible;
	}

	public void setFontembed(String fontembed) throws PageException {
		if (engine.getStringUtil().isEmpty(fontembed, true)) return;

		fontembed = fontembed.trim();
		Boolean fe = engine.getCastUtil().toBoolean(fontembed, null);
		if (fe == null) {
			if ("selective".equalsIgnoreCase(fontembed)) this.fontembed = PDFDocument.FONT_EMBED_SELECCTIVE;
			else throw engine.getExceptionUtil().createAbortException("invalid value for fontembed [" + fontembed + "], valid values for fontembed are [yes,no,selective]");
		}
		else if (fe.booleanValue()) {
			this.fontembed = PDFDocument.FONT_EMBED_YES;
		}
		else {
			this.fontembed = PDFDocument.FONT_EMBED_NO;
		}
		getPDFDocument().setFontembed(this.fontembed);
	}

	public void setFontdirectory(String fontDirectory) throws PageException {
		if (engine.getStringUtil().isEmpty(fontDirectory, true)) return;
		fontDirectory = fontDirectory.trim();

		Resource tmp = engine.getResourceUtil().toResourceExisting(pageContext, fontDirectory);
		if (!(tmp instanceof File)) throw engine.getExceptionUtil().createAbortException("[" + tmp + "] need to be a local file.");
		if (!tmp.isDirectory()) throw engine.getExceptionUtil().createAbortException("[" + tmp + "] is not a directory.");
		fontdir = (File) tmp;
		getPDFDocument().setFontDirectory(fontdir);
	}

	public void addPDFDocument(PDFDocument document) {
		// set proxy settings
		if (documents.contains(document)) return; // should never happen, just an insurance

		if (getPDFDocument().hasProxy()) {
			document.setProxyserver(getPDFDocument().getProxyserver());
			document.setProxyport(getPDFDocument().getProxyport());
			document.setProxyuser(getPDFDocument().getProxyuser());
			document.setProxypassword(getPDFDocument().getProxypassword());
		}
		document.setBookmark(getPDFDocument().getBookmark());
		document.setLocalUrl(getPDFDocument().getLocalUrl());
		document.setFontDirectory(getPDFDocument().getFontDirectory());
		document.setFontembed(getPDFDocument().getFontembed() ? PDFDocument.FONT_EMBED_YES : PDFDocument.FONT_EMBED_NO);

		// Apply cfdocument's orientation to cfdocumentsections that don't have one
		// specified.
		if (document.getOrientation() == null) {
			document.setOrientationNoCheck(getPDFDocument().getOrientation());
		}

		documents.add(document);
	}

	@Override
	public int doStartTag() throws PageException {
		if (fontdir == null) getPDFDocument().setFontDirectory(applicationSettings.getFontDirectory());
		/*
		 * Struct cfdoc = engine.getCreationUtil().createStruct(); // TODO make a read only struct
		 * cfdoc.setEL("currentpagenumber", "{currentpagenumber}"); cfdoc.setEL("totalpagecount",
		 * "{totalpagecount}"); cfdoc.setEL("totalsectionpagecount", "{totalsectionpagecount}");
		 * cfdoc.setEL("currentsectionpagenumber", "{currentsectionpagenumber}"); //
		 * cfdoc.setReadOnly(true); TODO pageContext.variablesScope().setEL("cfdocument", cfdoc);
		 */
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public void doInitBody() {}

	@Override
	public int doAfterBody() throws PageException {
		sectionCounter = 0; // must be 0 for the next round
		if (pdf == null) { // first run of the tag
			getPDFDocument().setBody(bodyContent.getString());
		}
		try {
			bodyContent.clear();
		}
		catch (IOException e) {}
		try {
			return _doAfterBody();
		}
		catch (Exception e) {
			throw engine.getCastUtil().toPageException(e);
		}
	}

	@Override
	public int doEndTag() throws PageException {
		return EVAL_PAGE;
	}

	public int _doAfterBody() throws Exception {
		// set root header/footer to sections
		boolean second = pdf != null;
		boolean doBookmarks = false;
		boolean doHtmlBookmarks = false;
		if (_document != null) {
			if (!second) {
				PDFPageMark header = _document.getHeader();
				PDFPageMark footer = _document.getFooter();
				boolean hasHeader = header != null;
				boolean hasFooter = footer != null;
				if (hasFooter || hasHeader) {
					Iterator<PDFDocument> it = documents.iterator();
					PDFDocument doc;
					while (it.hasNext()) {
						doc = it.next();
						if (hasHeader && doc.getHeader() == null) doc.setHeader(header);
						if (hasFooter && doc.getFooter() == null) doc.setFooter(footer);
					}
				}

			}
			doBookmarks = _document.getBookmark();
			doHtmlBookmarks = _document.getHtmlBookmark();
		}
		// only if there is no documentsection, we are interested in the content from document
		if (documents.size() == 0) {
			documents.add(_document);
		}

		if (!second && hasEvalAtPrint(documents)) {
			this.pdf = renderInital(doBookmarks, doHtmlBookmarks);
			return EVAL_BODY_AGAIN;
		}

		// if (pdf == null)
		pdf = renderInital(doBookmarks, doHtmlBookmarks);

		if (filename != null) {
			if (filename.exists() && !overwrite) throw engine.getExceptionUtil().createApplicationException("file [" + filename + "] already exist",
					"to allow overwrite the resource, set attribute [overwrite] to [true]");

			OutputStream os = null;
			try {
				if (filename instanceof File) os = new FileOutputStream(filename.getAbsolutePath());
				else os = filename.getOutputStream();
				renderUpdate(pdf, os);
			}
			finally {
				Util.closeEL(os);
			}

		}
		else if (!Util.isEmpty(name)) {
			renderUpdate(pdf, null);
		}
		else {
			HttpServletResponse rsp = pageContext.getHttpServletResponse();
			if (rsp.isCommitted())
				throw engine.getExceptionUtil().createApplicationException("content is already flushed", "you can't rewrite head of response after part of the page is flushed");
			rsp.setContentType("application/pdf");

			OutputStream os = getOutputStream();
			try {
				renderUpdate(pdf, os);
			}
			finally {
				try {
					if (os != null) os.flush();
				}
				catch (Throwable t) {
					if (t instanceof ThreadDeath) throw (ThreadDeath) t;
				}
				Util.closeEL(os);
				ClassUtil.setClosed(ClassUtil.getRootOut(pageContext), true);
			}
			throw engine.getExceptionUtil().createAbort();
		}
		return SKIP_BODY;

	}

	private boolean hasEvalAtPrint(ArrayList<PDFDocument> documents2) {
		Iterator<PDFDocument> it = documents.iterator();
		PDFDocument doc;
		PDFPageMark h;
		PDFPageMark f;
		while (it.hasNext()) {
			doc = it.next();
			h = doc.getHeader();
			if (h != null && h.isEvalAtPrint()) return true;
			f = doc.getFooter();
			if (f != null && f.isEvalAtPrint()) return true;

		}
		return false;
	}

	private byte[] renderInital(boolean doBookmarks, boolean doHtmlBookmarks) throws Exception {
		PDFDocument[] pdfDocs = new PDFDocument[documents.size()];
		PdfReader[] pdfReaders = new PdfReader[pdfDocs.length];
		Iterator<PDFDocument> it = documents.iterator();
		int index = 0, pageOffset = 0, count = 0, pages;
		Dimension dimension = null;

		// generate pdf with pd4ml

		while (it.hasNext()) {
			count++;
			pdfDocs[index] = it.next();
			pdfDocs[index].setPageOffset(pageOffset);

			// Set the dimension based on the specified orientation of each PDF doc.
			// This allows for mixed-orientation PDFs.
			dimension = getDimension(pdfDocs[index].getOrientation());

			int multiCount = getMultipleHF(pdfDocs[index]);
			// multiple header/footer
			if (multiCount > 1) {
				PdfReader[] tmp = new PdfReader[multiCount];
				for (int i = 0; i < multiCount; i++) {
					pdfDocs[index].setHFIndex(i);
					tmp[i] = new PdfReader(pdfDocs[index].render(dimension, unitFactor, pageContext, doHtmlBookmarks));
				}
				try {
					pdfReaders[index] = merge(tmp);
				}
				catch (Exception e) {
					CFMLEngine eng = CFMLEngineFactory.getInstance();
					if (pdfDocs[index] instanceof PD4MLPDFDocument) {
						throw eng.getExceptionUtil()
								.createApplicationException("attribute evalAtPrint is not fully supported with the classic PDF Engine, please use the regular PDF Engine.");
					}
					throw eng.getExceptionUtil().createPageRuntimeException(eng.getCastUtil().toPageException(e));
				}
			}
			else {
				pdfReaders[index] = new PdfReader(pdfDocs[index].render(dimension, unitFactor, pageContext, doHtmlBookmarks));
			}
			pdfDocs[index].setHFIndex(0);

			pages = pdfReaders[index].getNumberOfPages();
			pdfDocs[index].setPages(pages);
			pageOffset += pages;
			index++;
		}

		// collect together
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		com.lowagie.text.Document document = new com.lowagie.text.Document(pdfReaders[0].getPageSizeWithRotation(1));
		PdfSmartCopy copy = new PdfSmartCopy(document, baos);
		document.open();
		String name;
		ArrayList bookmarks = doBookmarks ? new ArrayList() : null;
		try {
			int size, totalPage = 0, pageNo = 0;
			Map<String, String> parent;
			for (int doc = 0; doc < pdfReaders.length; doc++) {
				size = pdfReaders[doc].getNumberOfPages();
				PdfImportedPage ip;
				// bookmarks
				if (doBookmarks) {
					name = pdfDocs[doc].getName();
					if (!Util.isEmpty(name)) {
						bookmarks.add(parent = PDFUtil.generateGoToBookMark(name, totalPage + 1));
					}
					else parent = null;

					if (doHtmlBookmarks) {
						java.util.List pageBM = SimpleBookmark.getBookmark(pdfReaders[doc]);
						if (pageBM != null) {
							if (totalPage > 0) SimpleBookmark.shiftPageNumbers(pageBM, totalPage, null);
							if (parent != null) PDFUtil.setChildBookmarks(parent, pageBM);
							else bookmarks.addAll(pageBM);
						}
					}
				}

				totalPage++;
				for (int page = 1; page <= size; page++) {
					pageNo++;
					if (page > 1) totalPage++;
					ip = copy.getImportedPage(pdfReaders[doc], page);
					copy.addPage(ip);
				}
			}
			if (doBookmarks && !bookmarks.isEmpty()) copy.setOutlines(bookmarks);
		}
		finally {
			document.close();
		}
		return baos.toByteArray();

	}

	private PdfReader merge(PdfReader[] pdfReaders) throws DocumentException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		com.lowagie.text.Document document = new com.lowagie.text.Document(pdfReaders[0].getPageSizeWithRotation(1));
		PdfSmartCopy copy = new PdfSmartCopy(document, baos);
		document.open();
		try {
			for (int doc = 0; doc < pdfReaders.length; doc++) {
				PdfImportedPage ip;
				ip = copy.getImportedPage(pdfReaders[doc], doc + 1);
				copy.addPage(ip);
			}
		}
		finally {
			document.close();
		}
		return new PdfReader(baos.toByteArray());
	}

	private int getMultipleHF(PDFDocument doc) {
		int count;
		if (doc.getHeader() != null && (count = doc.getHeader().getHtmlTemplates().size()) > 1) return count;
		if (doc.getFooter() != null && (count = doc.getFooter().getHtmlTemplates().size()) > 1) return count;
		return 1;
	}

	private void renderUpdate(byte[] pdf, OutputStream os) throws Exception {

		// permission/encryption
		if (PDFDocument.ENC_NONE != encryption) {
			PdfReader reader = new PdfReader(pdf);
			com.lowagie.text.Document document = new com.lowagie.text.Document(reader.getPageSize(1));

			Info info = CFMLEngineFactory.getInstance().getInfo();
			document.addCreator("Lucee PDF Extension");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfCopy copy = new PdfCopy(document, baos);
			// PdfWriter writer = PdfWriter.getInstance(document, pdfOut);
			{
				boolean userEmpty = Util.isEmpty(userpassword);
				boolean ownerEmpty = Util.isEmpty(ownerpassword);
				// one is empty the other not
				if (userEmpty != ownerEmpty) {
					if (userEmpty) userpassword = ownerpassword;
					else ownerpassword = userpassword;
				}
			}

			copy.setEncryption(PDFDocument.ENC_128BIT == encryption, userpassword, ownerpassword, permissions);
			document.open();
			int size = reader.getNumberOfPages();
			for (int page = 1; page <= size; page++) {
				copy.addPage(copy.getImportedPage(reader, page));
			}
			document.close();
			pdf = baos.toByteArray();
		}

		// write out
		if (os != null) Util.copy(new ByteArrayInputStream(pdf), os, true, false);
		if (!Util.isEmpty(name)) {
			pageContext.setVariable(name, pdf);
		}
	}

	private OutputStream getOutputStream() throws PageException, IOException {
		try {
			return ClassUtil.getResponseStream(pageContext);
		}
		catch (IllegalStateException ise) {
			throw engine.getExceptionUtil().createTemplateException("content is already send to user, flush");
		}
	}

	private Dimension getDimension(String orientation) throws PageException {
		// page size custom
		Dimension dim = pagetype;
		if (isCustom(pagetype)) {
			if (pageheight == 0 || pagewidth == 0) throw engine.getExceptionUtil()
					.createApplicationException("when attribute pagetype has value [custom], the attributes [pageheight, pagewidth] must have a positive numeric value");
			dim = new Dimension(PDFDocument.toPoint(pagewidth, unitFactor), PDFDocument.toPoint(pageheight, unitFactor));
		}
		// page orientation
		if (orientation.equals(PDFDocument.ORIENTATION_LANDSCAPE)) {
			dim = new Dimension(dim.height, dim.width);
		}
		return dim;
	}

	private boolean isCustom(Dimension d) throws PageException {
		if (d.height <= 0 || d.width <= 0) throw engine.getExceptionUtil()
				.createApplicationException("if you define pagetype as custom, you have to define attribute pageheight and pagewith with a positive numeric value");

		return (d.width + d.height) == 2;
	}

	/**
	 * sets if has body or not
	 *
	 * @param hasBody
	 */
	public void hasBody(boolean hasBody) {

	}
	/**
	 * @param orientation the orientation to set @throws PageException
	 */
	public void setOrientation(String strOrientation) throws PageException {
		getPDFDocument().setOrientation(strOrientation);
	}

	public static String trimAndLower(String str) {
		if (str == null) return "";
		return str.trim().toLowerCase();
	}

	public int getIndex() {
		return sectionCounter++;
	}

}
