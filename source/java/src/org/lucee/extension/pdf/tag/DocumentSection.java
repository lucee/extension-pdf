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


import javax.servlet.jsp.tagext.Tag;

import org.lucee.extension.pdf.PDFPageMark;
import org.lucee.extension.pdf.xhtmlrenderer.FSPDFDocument;

import lucee.commons.io.res.Resource;
import lucee.runtime.exp.PageException;

public final class DocumentSection extends BodyTagImpl {

	
	private FSPDFDocument _document; 
	


	public DocumentSection() {
		this._document=null;
	}
	
	private FSPDFDocument getPDFDocument() {
		//SerialNumber sn = pageContext.getConfig().getSerialNumber();
		if(_document==null)_document=new FSPDFDocument();
		return _document;
	}
	
	@Override
	public void release() {
		super.release();
		_document=null;
	}
	
	

	/** set the value proxyserver
	*  Host name or IP address of a proxy server.
	* @param proxyserver value to set
	**/
	public void setProxyserver(String proxyserver)	{
		getPDFDocument().setProxyserver(proxyserver);
	}
	
	/** set the value proxyport
	*  The port number on the proxy server from which the object is requested. Default is 80. When 
	* 	used with resolveURL, the URLs of retrieved documents that specify a port number are automatically 
	* 	resolved to preserve links in the retrieved document.
	* @param proxyport value to set
	**/
	public void setProxyport(double proxyport)	{
		getPDFDocument().setProxyport((int)proxyport);
	}

	/** set the value username
	*  When required by a proxy server, a valid username.
	* @param proxyuser value to set
	**/
	public void setProxyuser(String proxyuser)	{
		getPDFDocument().setProxyuser(proxyuser);
	}

	/** set the value password
	*  When required by a proxy server, a valid password.
	* @param proxypassword value to set
	**/
	public void setProxypassword(String proxypassword)	{
		getPDFDocument().setProxypassword(proxypassword);
	}

	/**
	 * @param marginbottom the marginbottom to set
	 */
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
	 * @param src the src to set
	 * @throws ApplicationException 
	 */
	public void setSrc(String src) throws PageException {
		getPDFDocument().setSrc(src);
	}

	/**
	 * @param srcfile the srcfile to set
	 * @throws PageException 
	 * @throws  
	 */
	public void setSrcfile(String strSrcfile) throws PageException {
		Resource srcfile = engine.getResourceUtil().toResourceExisting(pageContext, strSrcfile);
		pageContext.getConfig().getSecurityManager().checkFileLocation(srcfile);
		getPDFDocument().setSrcfile(srcfile);
	}

	/**
	 * @param mimetype the mimetype to set
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
	
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		getPDFDocument().setName(name);
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

    @Override
	public int doStartTag()	{
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public void doInitBody()	{
		
	}
	
	@Override
	public int doAfterBody()	{
		getPDFDocument().setBody(bodyContent.getString());
		return SKIP_BODY;
	}
	
	@Override
	public int doEndTag() {
		Document doc = getDocument();
		if(doc!=null)doc.addPDFDocument(getPDFDocument());
		return EVAL_PAGE;
	}

	private Document getDocument()	{
		// get Mail Tag
		Tag parent=getParent();
		while(parent!=null && !(parent instanceof Document)) {
			parent=parent.getParent();
		}
		
		if(parent instanceof Document) {
			return  (Document)parent;
		}
		return null;
	}
	/**
	 * sets if has body or not
	 * @param hasBody
	 */
	public void hasBody(boolean hasBody) {
	    
	}


}