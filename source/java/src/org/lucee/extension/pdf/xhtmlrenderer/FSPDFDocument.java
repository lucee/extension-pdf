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

import org.lucee.extension.pdf.PDFDocument;
import org.lucee.extension.pdf.PDFPageMark;
import org.lucee.extension.pdf.util.ClassUtil;
import org.lucee.xml.XMLUtility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

public final class FSPDFDocument extends PDFDocument {
	
	
	public FSPDFDocument(){
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

	private void createPDF(PageContext pc, ITextRenderer renderer, Document doc, OutputStream os, URL base) throws DocumentException, MalformedURLException {
		if(base==null)base=searchBaseURL(doc);
		if(base==null)base=getRequestURL(pc);
		//if(base!=null) System.out.println("base:"+base.toExternalForm());
		renderer.setDocument(doc, base==null?null:base.toExternalForm());
		renderer.layout();
		renderer.createPDF(os);
	}

	private void createPDF(ITextRenderer renderer, String xhtml, OutputStream os) throws DocumentException {
		renderer.setDocumentFromString(xhtml);
		renderer.layout();
		renderer.createPDF(os);
	}
}