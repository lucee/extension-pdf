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
package org.lucee.extension.pdf.tag.jakarta;

import org.lucee.extension.pdf.ApplicationSettings;
import org.lucee.extension.pdf.PDFDocument;
import org.lucee.extension.pdf.PDFPageMark;

import jakarta.servlet.jsp.tagext.Tag;
import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;

public final class DocumentSection extends BodyTagImpl implements AbsDoc {

	private PDFDocument _document;
	private int index;
	private boolean second;

	public DocumentSection() {
		this._document = null;
	}

	@Override
	public void release() {
		super.release();
		index = 0;
		_document = null;

	}

	@Override
	public PDFDocument getPDFDocument() throws PageException {
		if (_document == null) { // in the second round we already have this

			Document doc = getDocumentEL();
			this.index = doc.getIndex();
			_document = doc.getPDFDocument(index);
			second = true;
			if (_document == null) {
				second = false;
				ApplicationSettings settings = doc.getApplicationSettings();
				settings.validateEngine();
				_document = PDFDocument.newInstance(settings.getType());
			}

		}
		return _document;
	}

	private PDFDocument pdfDocumentEL() {
		try {
			return getPDFDocument();
		}
		catch (PageException pe) {
			throw engine.getExceptionUtil().createPageRuntimeException(pe);
		}
	}

	/**
	 * set the value proxyserver Host name or IP address of a proxy server.
	 *
	 * @param proxyserver value to set
	 **/
	public void setProxyserver(String proxyserver) {
		pdfDocumentEL().setProxyserver(proxyserver);
	}

	/**
	 * set the value proxyport The port number on the proxy server from which the object is requested.
	 * Default is 80. When used with resolveURL, the URLs of retrieved documents that specify a port
	 * number are automatically resolved to preserve links in the retrieved document.
	 *
	 * @param proxyport value to set
	 **/
	public void setProxyport(double proxyport) {
		pdfDocumentEL().setProxyport((int) proxyport);
	}

	/**
	 * set the value username When required by a proxy server, a valid username.
	 *
	 * @param proxyuser value to set
	 **/
	public void setProxyuser(String proxyuser) {
		pdfDocumentEL().setProxyuser(proxyuser);
	}

	/**
	 * set the value password When required by a proxy server, a valid password.
	 *
	 * @param proxypassword value to set
	 **/
	public void setProxypassword(String proxypassword) {
		pdfDocumentEL().setProxypassword(proxypassword);
	}

	/**
	 * @param marginbottom the marginbottom to set
	 */
	public void setMarginbottom(double marginbottom) {
		pdfDocumentEL().setMarginbottom(marginbottom);
	}

	/**
	 * @param marginleft the marginleft to set
	 */
	public void setMarginleft(double marginleft) {
		pdfDocumentEL().setMarginleft(marginleft);
	}

	/**
	 * @param marginright the marginright to set
	 */
	public void setMarginright(double marginright) {
		pdfDocumentEL().setMarginright(marginright);
	}

	/**
	 * @param margintop the margintop to set
	 */
	public void setMargintop(double margintop) {
		pdfDocumentEL().setMargintop(margintop);
	}

	/**
	 * @param orientation the orientation to set @throws PageException
	 */
	public void setOrientation(String strOrientation) throws PageException {
		pdfDocumentEL().setOrientation(strOrientation);
	}

	/**
	 * @param src the src to set
	 */
	public void setSrc(String src) throws PageException {
		pdfDocumentEL().setSrc(src);
	}

	/**
	 * @param srcfile the srcfile to set @throws PageException @throws
	 */
	public void setSrcfile(String strSrcfile) throws PageException {
		Resource srcfile = engine.getResourceUtil().toResourceExisting(pageContext, strSrcfile);
		pageContext.getConfig().getSecurityManager().checkFileLocation(srcfile);
		pdfDocumentEL().setSrcfile(srcfile);
	}

	/**
	 * @param mimetype the mimetype to set
	 */
	public void setMimetype(String strMimetype) throws PageException {
		pdfDocumentEL().setMimetype(strMimetype);
		strMimetype = strMimetype.toLowerCase().trim();
	}

	public void setHeader(PDFPageMark header) {
		pdfDocumentEL().setHeader(header);
	}

	public void setFooter(PDFPageMark footer) {
		pdfDocumentEL().setFooter(footer);
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		pdfDocumentEL().setName(name);
	}

	/**
	 * @param authUser the authUser to set
	 */
	public void setAuthuser(String authUser) {
		pdfDocumentEL().setAuthUser(authUser);
	}

	/**
	 * @param authPassword the authPassword to set
	 */
	public void setAuthpassword(String authPassword) {
		pdfDocumentEL().setAuthPassword(authPassword);
	}

	/**
	 * @param userAgent the userAgent to set
	 */
	public void setUseragent(String userAgent) {
		pdfDocumentEL().setUserAgent(userAgent);
	}

	@Override
	public int doStartTag() throws PageException {
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public void doInitBody() {

	}

	@Override
	public int doAfterBody() {
		pdfDocumentEL().setBody(bodyContent.getString());
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws PageException {
		if (!second) getDocument().addPDFDocument(getPDFDocument());
		return EVAL_PAGE;
	}

	public Document getDocumentEL() {
		try {
			return getDocument();
		}
		catch (PageException e) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().createPageRuntimeException(e);
		}
	}

	public Document getDocument() throws PageException {
		Tag parent = getParent();
		while (parent != null && !(parent instanceof Document)) {
			parent = parent.getParent();
		}

		if (parent instanceof Document) {
			return (Document) parent;
		}
		throw CFMLEngineFactory.getInstance().getExceptionUtil().createApplicationException("tag documentsection must be within tag document");
	}

	/**
	 * sets if has body or not
	 *
	 * @param hasBody
	 */
	public void hasBody(boolean hasBody) {

	}

	public int getIndex() {
		return index;
	}

}
