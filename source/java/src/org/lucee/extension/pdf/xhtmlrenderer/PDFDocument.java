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
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;

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

import org.lucee.extension.pdf.util.ClassUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

public final class PDFDocument {

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
	
	
	public static final Dimension PAGETYPE_B4=new Dimension(708,1000);
	public static final Dimension PAGETYPE_B5=new Dimension(499,708);
	public static final Dimension PAGETYPE_B4_JIS=new Dimension(728,1031);
	public static final Dimension PAGETYPE_B5_JIS=new Dimension(516,728);
	public static final Dimension PAGETYPE_CUSTOM=new Dimension(1,1);
			
	// encryption
	public static final int ENC_NONE=0;
	public static final int ENC_40BIT=1;
	public static final int ENC_128BIT=2;
	
	//	fontembed 
	public static final int FONT_EMBED_NO=0;
	public static final int FONT_EMBED_YES=1;
	public static final int FONT_EMBED_SELECCTIVE=FONT_EMBED_YES;

	// unit
	public static final double UNIT_FACTOR_CM=85d/3d;// =28.333333333333333333333333333333333333333333;
	public static final double UNIT_FACTOR_IN=UNIT_FACTOR_CM*2.54;
	public static final double UNIT_FACTOR_POINT=1;
		
	// margin init
	private static final int MARGIN_INIT=36;

	// mimetype
	private static final int MIMETYPE_TEXT_HTML = 0;
	private static final int MIMETYPE_TEXT = 1;
	private static final int MIMETYPE_IMAGE = 2;  
	//private static final int MIMETYPE_APPLICATION = 3;
	private static final int MIMETYPE_APPLICATION_PDF = 4;
	private static final int MIMETYPE_OTHER = -1;
		
	private double margintop=-1;
	private double marginbottom=-1;
	private double marginleft=-1;
	private double marginright=-1;

	private int mimeType=MIMETYPE_OTHER;
	private Charset charset=null;

	private boolean backgroundvisible;
	private boolean fontembed=true;
	private PDFPageMark header;
	private PDFPageMark footer;
	
	private String proxyserver;
	private int proxyport=80;
	private String proxyuser=null;
	private String proxypassword="";

	private String src=null;
	private Resource srcfile=null;
	private String body;
	//private boolean isEvaluation;
	private String name;
	private String authUser;
	private String authPassword;
	private String userAgent;
	private boolean localUrl;
	private boolean bookmark; 
	private boolean htmlBookmark;
	private CFMLEngine engine;
	
	
	
	public PDFDocument(){
		engine=CFMLEngineFactory.getInstance();
		userAgent= "Lucee "+engine.getInfo().getVersion();
		
	}
	
	public void setHeader(PDFPageMark header) {
		this.header=header;
	}

	public void setFooter(PDFPageMark footer) {
		this.footer=footer;
	}
	

	/**
	 * @param marginbottom the marginbottom to set
	 */
	public void setMarginbottom(double marginbottom) {
		this.marginbottom = marginbottom;
	}

	/**
	 * @param marginleft the marginleft to set
	 */
	public void setMarginleft(double marginleft) {
		this.marginleft = marginleft;
	}

	/**
	 * @param marginright the marginright to set
	 */
	public void setMarginright(double marginright) {
		this.marginright = marginright;
	}

	/**
	 * @param margintop the margintop to set
	 */
	public void setMargintop(double margintop) {
		this.margintop = margintop;
	}
	
	/**
	 * @param ct the mimetype to set
	 * @throws PageException 
	 */
	public void setMimetype(ContentType ct) throws PageException {
		// mimetype
		if(ct.getMimeType().startsWith("text/html"))		mimeType=MIMETYPE_TEXT_HTML;
		else if(ct.getMimeType().startsWith("text/"))		mimeType=MIMETYPE_TEXT;
		else if(ct.getMimeType().startsWith("image/"))		mimeType=MIMETYPE_IMAGE;
		else if(ct.getMimeType().startsWith("application/pdf"))mimeType=MIMETYPE_APPLICATION_PDF;
		else mimeType=MIMETYPE_OTHER;
		
		// charset
		String strCharset=ct.getCharset();
		if(!Util.isEmpty(strCharset, true)) {
			charset=engine.getCastUtil().toCharset(strCharset);
		}
	}
	public void setMimetype(String strMimetype) throws PageException {
		strMimetype = strMimetype.toLowerCase().trim();

		// mimetype
		if(strMimetype.startsWith("text/html"))			mimeType=MIMETYPE_TEXT_HTML;
		else if(strMimetype.startsWith("text/"))		mimeType=MIMETYPE_TEXT;
		else if(strMimetype.startsWith("image/"))		mimeType=MIMETYPE_IMAGE;
		else if(strMimetype.startsWith("application/pdf"))	mimeType=MIMETYPE_APPLICATION_PDF;
		else mimeType=MIMETYPE_OTHER;
		
		// charset
		String[] arr = engine.getListUtil().toStringArray(strMimetype, ";");
		if(arr.length>=2) {
			strMimetype=arr[0].trim();
			for(int i=1;i<arr.length;i++) {
				String[] item = engine.getListUtil().toStringArray(arr[i], "=");
				if(item.length==1) {
					charset=engine.getCastUtil().toCharset(item[0].trim());
					break;
				}
				else if(item.length==2 && item[0].trim().equals("charset")) {
					charset=engine.getCastUtil().toCharset(item[1].trim());
					break;
				}
			}
		}
	}
	
	/** set the value proxyserver
	*  Host name or IP address of a proxy server.
	* @param proxyserver value to set
	**/
	public void setProxyserver(String proxyserver)	{
		this.proxyserver=proxyserver;
	}
	
	/** set the value proxyport
	*  The port number on the proxy server from which the object is requested. Default is 80. When 
	* 	used with resolveURL, the URLs of retrieved documents that specify a port number are automatically 
	* 	resolved to preserve links in the retrieved document.
	* @param proxyport value to set
	**/
	public void setProxyport(int proxyport)	{
		this.proxyport=proxyport;
	}

	/** set the value username
	*  When required by a proxy server, a valid username.
	* @param proxyuser value to set
	**/
	public void setProxyuser(String proxyuser)	{
		this.proxyuser=proxyuser;
	}

	/** set the value password
	*  When required by a proxy server, a valid password.
	* @param proxypassword value to set
	**/
	public void setProxypassword(String proxypassword)	{
		this.proxypassword=proxypassword;
	}

	/**
	 * @param src
	 * @throws PDFException
	 */
	public void setSrc(String src) throws PageException {
		if(srcfile!=null) throw engine.getExceptionUtil().createApplicationException("You cannot specify both the src and srcfile attributes");
		this.src = src;
	}
	

	/**
	 * @param srcfile the srcfile to set
	 * @throws PDFException 
	 */
	public void setSrcfile(Resource srcfile) throws PageException {
		if(src!=null) throw engine.getExceptionUtil().createApplicationException("You cannot specify both the src and srcfile attributes");
		this.srcfile=srcfile;
	}

	public void setBody(String body) {
		this.body=body;
	}

	public byte[] render(Dimension dimension,double unitFactor, PageContext pc,boolean generategenerateOutlines) throws PageException, IOException {
		
		ITextRenderer renderer = new ITextRenderer();
		//renderer.getRootBox().setListener(new PDFCreationListenerImpl());
		// TODO do html bookmarks pd4ml.generateOutlines(generateOutlines);
		//pd4ml.enableTableBreaks(true);
		//pd4ml.interpolateImages(true);
		//pd4ml.adjustHtmlWidth();
		
		//check size
		int mTop = 	toPoint(margintop,unitFactor);
		int mLeft = toPoint(marginleft,unitFactor);
		int mBottom=toPoint(marginbottom,unitFactor);
		int mRight=toPoint(marginright,unitFactor);
		if((mLeft+mRight)>dimension.getWidth())
			throw engine.getExceptionUtil().createApplicationException("current document width ("+engine.getCastUtil().toString(dimension.getWidth())+" point) is smaller that specified horizontal margin  ("+engine.getCastUtil().toString(mLeft+mRight)+" point).",
					"1 in = "+Math.round(1*UNIT_FACTOR_IN)+" point and 1 cm = "+Math.round(1*UNIT_FACTOR_CM)+" point");
		if((mTop+mBottom)>dimension.getHeight())
			throw engine.getExceptionUtil().createApplicationException("current document height ("+engine.getCastUtil().toString(dimension.getHeight())+" point) is smaller that specified vertical margin  ("+engine.getCastUtil().toString(mTop+mBottom)+" point).",
					"1 in = "+Math.round(1*UNIT_FACTOR_IN)+" point and 1 cm = "+Math.round(1*UNIT_FACTOR_CM)+" point");
		
		// Size
		// TODO pd4ml.setPageInsets(new Insets(mTop,mLeft,mBottom,mRight));
		// TODO pd4ml.setPageSize(dimension);
		
		// header
		//if(header!=null) pd4ml.setPageHeader(header);
		// footer
		//if(footer!=null) pd4ml.setPageFooter(footer);
		
		// content
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try {
			content(renderer,pc,baos);
			
		}
		catch(Exception e){
			throw engine.getCastUtil().toPageException(e);
		}
		finally {
			Util.closeEL(baos);
		}
		return baos.toByteArray();
	}

	private void content(ITextRenderer renderer, PageContext pc, OutputStream os) throws PageException, IOException, SAXException, DocumentException {
		ConfigWeb config = pc.getConfig();
		// TODO pd4ml.useTTF("java:fonts", fontembed);
		/*
		css 
		@font-face {
    font-family: DejaVu Serif;
    src: url(fonts/DejaVuSerif.ttf);
    -fs-pdf-font-embed: embed;
    -fs-pdf-font-encoding: Identity-H;
}
		 
		 
		 
		 
		 
		 FontResolver resolver = renderer.getFontResolver();
resolver.addFont (
    "C:\\WINNT\\Fonts\\ARIALUNI.TTF",
    BaseFont.IDENTITY_H,
    BaseFont.NOT_EMBEDDED
);
		 * 
		 */
		
		// body
		Document doc;
    	if(!Util.isEmpty(body,true)) {
    		// optimize html
    		//URL req = getRequestURL(pc);
    		doc=toXML(new InputSource(new StringReader(body)));    		
    		createPDF(pc,renderer,doc,os,null);
			
    	}
    	// srcfile
    	else if(srcfile!=null) {
    		if(charset==null)charset=pc.getResourceCharset();
    		
			// mimetype
			if(mimeType==MIMETYPE_OTHER) {
				ContentType ct = CFMLEngineFactory.getInstance().getResourceUtil().getContentType(srcfile);
				setMimetype(CFMLEngineFactory.getInstance().getResourceUtil().getContentType(srcfile));
			}
			InputStream is = srcfile.getInputStream();
    		try {
    			
    			URL base = new URL("file://"+srcfile);
    			if(!localUrl){
    				String abs = srcfile.getAbsolutePath();
	    			String contract = ClassUtil.ContractPath(pc, abs);
	    			if(!abs.equals(contract)) {
	    				base=engine.getHTTPUtil().toURL(getDomain(pc.getHttpServletRequest())+contract);
	    			}
    			}
    			
    			//URL base = localUrl?new URL("file://"+srcfile):getBase();
    			render(pc,renderer, is,os,base);
			} 
    		catch (Throwable t) {}
    		finally {
    			Util.closeEL(is);
    		}
    	}
    	// src
    	else if(src!=null) {
    		if(charset==null)charset=pc.getResourceCharset();
    		URL url = engine.getHTTPUtil().toURL(src);
			
			// set Proxy
			if(Util.isEmpty(proxyserver) && config.isProxyEnableFor(url.getHost())) {
				ProxyData pd = config.getProxyData();
				proxyserver=pd==null?null:pd.getServer();
				proxyport=pd==null?0:pd.getPort();
				proxyuser=pd==null?null:pd.getUsername();
				proxypassword=pd==null?null:pd.getPassword();
			}
			HTTPResponse method = engine.getHTTPUtil().get(url, authUser, authPassword, -1,null, userAgent,
				proxyserver, proxyport, proxyuser, proxypassword,null);
			
			// mimetype
			if(mimeType==MIMETYPE_OTHER) {
				ContentType ct = method.getContentType();
				if(ct!=null)
					setMimetype(ct);
			}
			
			InputStream is = new ByteArrayInputStream(method.getContentAsByteArray());
			try {
				render(pc,renderer, is, os,url);
			}
			finally {
				engine.getIOUtil().closeSilent(is);
			}
    	}
    	else {
    		createPDF(renderer,"<html><body> </body></html>",os);
    	}
	}


	private static Document toXML(InputSource is) throws SAXException, IOException {
		Document xml = CFMLEngineFactory.getInstance().getXMLUtil().parse(is,null,true);
		// TODO if(base!=null)URLResolver.getInstance().transform(xml, base);
		return xml;
	}

	/*private static void patchPD4MLProblems(Document xml) {
		Element b = XMLUtil.getChildWithName("body", xml.getDocumentElement());
		if(!b.hasChildNodes()){
			b.appendChild(xml.createTextNode(" "));
		}
	}*/


	private URL getRequestURL(PageContext pc) {
		if(pc==null)return null;
		try {
			return engine.getHTTPUtil().toURL(getDirectoryFromPath(getRequestURL(pc.getHttpServletRequest(), false)));
		}
		catch(Throwable t){
			return null;
		}
	}


	private void render(PageContext pc, ITextRenderer renderer, InputStream is,OutputStream os, URL base) throws PageException, IOException, SAXException, DocumentException {
		try {
			
			// text/html
			if(mimeType==MIMETYPE_TEXT_HTML || mimeType==MIMETYPE_OTHER) {
				InputSource input = new InputSource(engine.getIOUtil().getReader(is,charset));
				Document doc = toXML(input);
				createPDF(pc,renderer,doc,os,base);
			}
			// text
			else if(mimeType==MIMETYPE_TEXT) {
				body =engine.getIOUtil().toString(is,charset); 
				body="<html><body><pre>"+engine.getHTMLUtil().escapeHTML(body)+"</pre></body></html>";
				createPDF(renderer,body,os);
			}
			// image
			else if(mimeType==MIMETYPE_IMAGE) {
				File tmpDir = CFMLEngineFactory.getTempDirectory();
				File tmp = new File(tmpDir,this+"-"+Math.random());
				engine.getIOUtil().copy(is, new FileOutputStream(tmp), true, true);
				body="<html><body><img src=\"file://"+tmp+"\"></body></html>"; // TODO test this
				try {
					createPDF(renderer,body,os);
				}
				finally {
					tmp.delete();
				}	
			}
			// Application
			else if(mimeType==MIMETYPE_APPLICATION_PDF) {
				engine.getIOUtil().copy(is, os,true,true);
			}
			else {
				throw engine.getExceptionUtil().createApplicationException("this mime type is not supported!");
			}
		}
		finally {
			Util.closeEL(is,os);
		}
	}

	public static int toPoint(double value,double unitFactor) {
		if(value<0) return MARGIN_INIT;
		return (int)Math.round(value*unitFactor);
		//return r;
	}

	public PDFPageMark getHeader() {
		return header;
	}
	public PDFPageMark getFooter() {
		return footer;
	}

	public void setFontembed(int fontembed) {
		this.fontembed=fontembed!=FONT_EMBED_NO;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @return the authUser
	 */
	public String getAuthUser() {
		return authUser;
	}


	/**
	 * @param authUser the authUser to set
	 */
	public void setAuthUser(String authUser) {
		this.authUser = authUser;
	}


	/**
	 * @return the authPassword
	 */
	public String getAuthPassword() {
		return authPassword;
	}


	/**
	 * @param authPassword the authPassword to set
	 */
	public void setAuthPassword(String authPassword) {
		this.authPassword = authPassword;
	}


	/**
	 * @return the userAgent
	 */
	public String getUserAgent() {
		return userAgent;
	}


	/**
	 * @param userAgent the userAgent to set
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}


	/**
	 * @return the proxyserver
	 */
	public String getProxyserver() {
		return proxyserver;
	}


	/**
	 * @return the proxyport
	 */
	public int getProxyport() {
		return proxyport;
	}


	/**
	 * @return the proxyuser
	 */
	public String getProxyuser() {
		return proxyuser;
	}


	/**
	 * @return the proxypassword
	 */
	public String getProxypassword() {
		return proxypassword;
	}


	public boolean hasProxy() {
		return !Util.isEmpty(proxyserver);
	}


	/**
	 * @return the localUrl
	 */
	public boolean getLocalUrl() {
		return localUrl;
	}


	/**
	 * @param localUrl the localUrl to set
	 */
	public void setLocalUrl(boolean localUrl) {
		this.localUrl = localUrl;
	}


	/**
	 * @return the bookmark
	 */
	public boolean getBookmark() {
		return bookmark;
	}


	/**
	 * @param bookmark the bookmark to set
	 */
	public void setBookmark(boolean bookmark) {
		this.bookmark = bookmark;
	}


	/**
	 * @return the htmlBookmark
	 */
	public boolean getHtmlBookmark() {
		return htmlBookmark;
	}


	/**
	 * @param htmlBookmark the htmlBookmark to set
	 */
	public void setHtmlBookmark(boolean htmlBookmark) {
		this.htmlBookmark = htmlBookmark;
	}
	

	private void createPDF(PageContext pc, ITextRenderer renderer, Document doc, OutputStream os, URL base) throws DocumentException, MalformedURLException {
		if(base==null)base=searchBaseURL(doc);
		if(base==null)base=getRequestURL(pc);
		if(base!=null) System.out.println("base:"+base.toExternalForm());
		renderer.setDocument(doc, base==null?null:base.toExternalForm());
		renderer.layout();
		renderer.createPDF(os);
	}

	private void createPDF(ITextRenderer renderer, String xhtml, OutputStream os) throws DocumentException {
		renderer.setDocumentFromString(xhtml);
		renderer.layout();
		renderer.createPDF(os);
	}
	
	private URL searchBaseURL(Document doc) {
		Element html = doc.getDocumentElement();
		System.out.println("html:"+html.getNodeName());
		NodeList list = html.getChildNodes();
		Node n;
		for(int i=list.getLength()-1;i>=0;i--) {
			n=list.item(i);
			// head
			if(n instanceof Element && ((Element)n).getNodeName().equalsIgnoreCase("head")) {
				Element head=(Element) n;
				System.out.println("head:"+head.getNodeName());
				NodeList _list = html.getChildNodes();
				for(int _i=_list.getLength()-1;_i>=0;_i--) {
					n=list.item(i);
					// base
					if(n instanceof Element && ((Element)n).getNodeName().equalsIgnoreCase("base")) {
						Element base=(Element) n;
						String href = base.getAttribute("href");
						if(!Util.isEmpty(href)) {
							try {
								System.out.println("base:"+href);
								return engine.getHTTPUtil().toURL(href);
							}
							catch (MalformedURLException e) {}
						}
					}
					
				}
			} 
		}
		// TODO Auto-generated method stub
		return null;
	}
	
	private static String getRequestURL( HttpServletRequest req, boolean includeQueryString ) {
        StringBuffer sb = req.getRequestURL();
        int maxpos = sb.indexOf( "/", 8 );
        if ( maxpos > -1 ) {
            if ( req.isSecure() ) {
                if ( sb.substring( maxpos - 4, maxpos ).equals( ":443" ) )
                    sb.delete( maxpos - 4, maxpos );
            }
            else {
                if ( sb.substring( maxpos - 3, maxpos ).equals( ":80" ) )
                    sb.delete( maxpos - 3, maxpos );
            }

            if ( includeQueryString && !Util.isEmpty( req.getQueryString() ) )
                sb.append( '?' ).append( req.getQueryString() );
        }
        return sb.toString();
    }
	
	public static String getDirectoryFromPath(String path) {
		int posOfLastDel = path.lastIndexOf('/');
		String parent = "";
		
		if(path.lastIndexOf('\\') > posOfLastDel)
			posOfLastDel = path.lastIndexOf("\\");
		if(posOfLastDel != -1)
			parent = path.substring(0, posOfLastDel + 1);
		else
		if(path.equals(".") || path.equals(".."))
			parent = String.valueOf(File.separatorChar);
		else if(path.startsWith("."))
			parent = String.valueOf(File.separatorChar);
		else
			parent = String.valueOf(File.separatorChar);
		return parent;
	}
	

	private static String getDomain(HttpServletRequest req) { // DIFF 23
		StringBuilder sb=new StringBuilder();
		sb.append(req.isSecure()?"https://":"http://");
		sb.append(req.getServerName());
		sb.append(':');
		sb.append(req.getServerPort());
		if(!Util.isEmpty(req.getContextPath()))sb.append(req.getContextPath());
		return sb.toString();
	}

}