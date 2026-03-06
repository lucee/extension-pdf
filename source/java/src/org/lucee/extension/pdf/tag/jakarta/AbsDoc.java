package org.lucee.extension.pdf.tag.jakarta;

import org.lucee.extension.pdf.PDFDocument;
import lucee.runtime.exp.PageException;

public interface AbsDoc {
	public PDFDocument getPDFDocument() throws PageException;
}
