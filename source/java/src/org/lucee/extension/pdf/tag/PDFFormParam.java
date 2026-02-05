/**
 *
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 **/
package org.lucee.extension.pdf.tag;

import jakarta.servlet.jsp.tagext.Tag;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;

/**
 * Child tag for cfpdfform to specify form field name/value pairs
 */
public final class PDFFormParam extends TagImpl {

	PDFFormParamBean param = new PDFFormParamBean();

	public void setName( String name ) {
		param.setName( name );
	}

	public void setValue( String value ) {
		param.setValue( value );
	}

	@Override
	public int doStartTag() throws PageException {
		// Find parent PDFForm tag
		Tag parent = getParent();
		while ( parent != null && !(parent instanceof PDFForm) ) {
			parent = parent.getParent();
		}

		if ( parent instanceof PDFForm ) {
			PDFForm pdfForm = (PDFForm) parent;
			pdfForm.addParam( param );
		}
		else {
			throw CFMLEngineFactory.getInstance().getExceptionUtil()
					.createApplicationException( "Wrong Context, tag PDFFormParam must be inside a PDFForm tag" );
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

	@Override
	public void release() {
		super.release();
		param = new PDFFormParamBean();
	}
}
