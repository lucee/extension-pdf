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

import java.io.IOException;

import javax.servlet.jsp.tagext.Tag;

import org.lucee.extension.pdf.PDFDocument;
import org.lucee.extension.pdf.PDFPageMark;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.exp.PageException;

public final class DocumentItem extends BodyTagImpl {

	private static final int TYPE_PAGE_BREAK = 0;
	private static final int TYPE_HEADER = 1;
	private static final int TYPE_FOOTER = 2;
	private static final int TYPE_BOOKMARK = 3;

	private int type;
	private String name;
	private PDFPageMark body;
	private boolean evalAtPrint;
	private PDFDocument _document;

	@Override
	public void release() {
		super.release();
		this.body = null;
		name = null;
	}
	
	/**
	 * @param type
	 *            the type to set
	 * @throws ApplicationException
	 */
	public void setType(String strType) throws PageException {
		strType = Document.trimAndLower(strType);
		if("pagebreak".equals(strType))
			type = TYPE_PAGE_BREAK;
		else if("header".equals(strType))
			type = TYPE_HEADER;
		else if("footer".equals(strType))
			type = TYPE_FOOTER;
		else if("bookmark".equals(strType))
			type = TYPE_BOOKMARK;
		else
			engine.getExceptionUtil().createApplicationException("invalid type [" + strType + "], valid types are [pagebreak,header,footer,bookmark]");
		// else throw new ApplicationException("invalid type ["+strType+"], valid types are [pagebreak,header,footer]");

	}

	public void setEvalatprint(boolean evalAtPrint) {
		this.evalAtPrint = evalAtPrint;
	}

	@Override
	public int doStartTag() {
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public void doInitBody() {
	}

	@Override
	public int doAfterBody() {
		if(TYPE_HEADER == type || TYPE_FOOTER == type) {
			body = new PDFPageMark(-1, translate(bodyContent.getString()));
		}

		return SKIP_BODY;
	}

	private String translate(String html) {
		return getPDFDocument().handlePageNumbers(html);
		/*html = Util.replace(html.trim(), "{currentsectionpagenumber}", "${page}", false);
		html = Util.replace(html, "{totalsectionpagecount}", "${total}", false);

		html = Util.replace(html.trim(), "{currentpagenumber}", "${page}", false);
		html = Util.replace(html, "{totalpagecount}", "${total}", false);
		
		return html;*/
	}

	@Override
	public int doEndTag() throws PageException {
		try {
			_doEndTag();
		}
		catch (IOException e) {
			throw engine.getCastUtil().toPageException(e);
		}
		return EVAL_PAGE;
	}

	private void _doEndTag() throws IOException, PageException {
		if(TYPE_PAGE_BREAK == type) {
			getPDFDocument().pageBreak(pageContext);
			return;
		}
		else if(TYPE_BOOKMARK == type) {
			if(Util.isEmpty(name))
				throw engine.getExceptionUtil().createApplicationException("attribute [name] is required when type is [bookmark]");
			pageContext.forceWrite("<pd4ml:bookmark>" + name + "</pd4ml:bookmark>");
		}
		else if(body != null) {
			provideDocumentItem();
		}

	}

	private void provideDocumentItem() {
		// get Document Tag
		Tag parent = getParent();
		while(parent != null && !(parent instanceof Document) && !(parent instanceof DocumentSection)) {
			parent = parent.getParent();
		}

		if(parent instanceof Document) {
			Document doc = (Document)parent;
			if(TYPE_HEADER == type)
				doc.setHeader(body);
			else if(TYPE_FOOTER == type)
				doc.setFooter(body);
			return;
		}
		else if(parent instanceof DocumentSection) {
			DocumentSection doc = (DocumentSection)parent;
			if(TYPE_HEADER == type)
				doc.setHeader(body);
			else if(TYPE_FOOTER == type)
				doc.setFooter(body);
			return;
		}
	}

	/**
	 * sets if has body or not
	 * 
	 * @param hasBody
	 */
	public void hasBody(boolean hasBody) {

	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	private PDFDocument getPDFDocument() { 
		if(_document == null) {
			_document = getAbsDoc().getPDFDocument();
		}
		return _document;
	}
	
	private AbsDoc getAbsDoc() {
		// get Mail Tag
		Tag parent = getParent();
		while(parent != null && !(parent instanceof AbsDoc)) {
			parent = parent.getParent();
		}

		if(parent instanceof AbsDoc) {
			return (AbsDoc)parent;
		}
		return null;
	}
}