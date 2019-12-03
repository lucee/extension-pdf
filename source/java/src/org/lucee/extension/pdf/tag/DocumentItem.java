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
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.tagext.Tag;

import org.lucee.extension.pdf.PDFDocument;
import org.lucee.extension.pdf.PDFPageMark;

import lucee.loader.util.Util;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

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
	private Document doc;
	private boolean second;
	private int count;

	@Override
	public void release() {
		super.release();
		this.body = null;
		name = null;
		_document = null;
		evalAtPrint = false;
		doc = null;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String strType) throws PageException {
		strType = Document.trimAndLower(strType);
		if ("pagebreak".equals(strType)) type = TYPE_PAGE_BREAK;
		else if ("header".equals(strType)) type = TYPE_HEADER;
		else if ("footer".equals(strType)) type = TYPE_FOOTER;
		else if ("bookmark".equals(strType)) type = TYPE_BOOKMARK;
		else engine.getExceptionUtil().createApplicationException("invalid type [" + strType + "], valid types are [pagebreak,header,footer,bookmark]");
		// else throw new ApplicationException("invalid type ["+strType+"], valid types are
		// [pagebreak,header,footer]");

	}

	public void setEvalatprint(boolean evalAtPrint) {
		this.evalAtPrint = evalAtPrint;
	}

	@Override
	public int doStartTag() {
		doc = getDocument();
		second = doc.getPDF() != null;
		if (second) {
			if (doc.getPDFDocuments().size() > 1) evalAtPrint = true; // workaround because it does not work
			// correctly with false when more than one section
		}

		count = 1;
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public void doInitBody() throws PageException {
		if ((TYPE_HEADER == type || TYPE_FOOTER == type)) setPageInfo();
	}

	private void setPageInfo() throws PageException {
		Struct cfdoc = engine.getCreationUtil().createStruct(); // TODO make a read only struct
		if (second && evalAtPrint) {
			PDFDocument currPD = getPDFDocument();
			List<PDFDocument> list = getDocument().getPDFDocuments();
			Iterator<PDFDocument> it = list.iterator();
			int totalPageCount = 0;
			while (it.hasNext()) {
				totalPageCount += it.next().getPages();
			}
			cfdoc.setEL("currentpagenumber", Double.valueOf(currPD.getPageOffset() + count));
			cfdoc.setEL("currentsectionpagenumber", Double.valueOf(count));
			cfdoc.setEL("totalpagecount", Double.valueOf(totalPageCount));
			cfdoc.setEL("totalsectionpagecount", currPD.getPages());
		}
		else {
			cfdoc.setEL("currentpagenumber", "{currentpagenumber}");
			cfdoc.setEL("totalpagecount", "{totalpagecount}");
			cfdoc.setEL("totalsectionpagecount", "{totalsectionpagecount}");
			cfdoc.setEL("currentsectionpagenumber", "{currentsectionpagenumber}");
		}
		pageContext.variablesScope().setEL("cfdocument", cfdoc);
	}

	@Override
	public int doAfterBody() throws PageException {
		if ((TYPE_HEADER == type || TYPE_FOOTER == type)) {
			String b = bodyContent.getString();
			/*
			 * try { bodyContent.clear(); } catch (IOException e) { throw
			 * CFMLEngineFactory.getInstance().getCastUtil().toPageException(e); }
			 */
			if (!evalAtPrint) b = getPDFDocument().handlePageNumbers(b);
			if (body != null && count > 1) body.addHtmlTemplate(b);
			else body = new PDFPageMark(-1, b, evalAtPrint);
		}
		if (evalAtPrint && count < getPDFDocument().getPages()) {
			count++;
			setPageInfo();
			return EVAL_BODY_AGAIN;
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws PageException {
		// in second round we only care about header and footer
		if (second && TYPE_HEADER != type && TYPE_FOOTER != type) return EVAL_PAGE;

		try {
			if (TYPE_PAGE_BREAK == type) {
				getPDFDocument().pageBreak(pageContext);
			}
			else if (TYPE_BOOKMARK == type) {
				if (Util.isEmpty(name)) throw engine.getExceptionUtil().createApplicationException("attribute [name] is required when type is [bookmark]");
				pageContext.forceWrite("<pd4ml:bookmark>" + name + "</pd4ml:bookmark>");
			}
			// header/footer
			else {
				provideDocumentItem();
			}
		}
		catch (IOException e) {
			throw engine.getCastUtil().toPageException(e);
		}
		return EVAL_PAGE;
	}

	private void provideDocumentItem() throws PageException {
		// get Document Tag
		Tag parent = getParent();
		while (parent != null && !(parent instanceof Document) && !(parent instanceof DocumentSection)) {
			parent = parent.getParent();
		}
		PDFDocument pdfdoc;
		if (parent instanceof Document) {
			pdfdoc = ((Document) parent).getPDFDocument();
		}
		else {
			pdfdoc = ((DocumentSection) parent).getPDFDocument();
		}
		if (TYPE_HEADER == type) pdfdoc.setHeader(body);
		else if (TYPE_FOOTER == type) pdfdoc.setFooter(body);
		return;
	}

	private Document getDocument() {
		// get Document Tag
		Tag parent = getParent();
		while (parent != null && !(parent instanceof Document)) {
			parent = parent.getParent();
		}
		if (parent instanceof Document) {
			return ((Document) parent);
		}
		return null;
	}

	/**
	 * sets if has body or not
	 * 
	 * @param hasBody
	 */
	public void hasBody(boolean hasBody) {

	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	private PDFDocument getPDFDocument() throws PageException {
		if (_document == null) {
			_document = getAbsDoc().getPDFDocument();
		}
		return _document;
	}

	private AbsDoc getAbsDoc() {
		Tag parent = getParent();
		while (parent != null && !(parent instanceof AbsDoc)) {
			parent = parent.getParent();
		}

		if (parent instanceof AbsDoc) {
			return (AbsDoc) parent;
		}
		return null;
	}
}