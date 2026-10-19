/**
 *
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
package org.lucee.extension.pdf;

import java.util.ArrayList;
import java.util.List;

public class PDFPageMark {

	private int areaHeight;
	private List<String> htmlTemplates = new ArrayList<>();
	private final boolean evalAtPrint;
	private int hfIndex;

	/**
	 * Constructor of the class
	 * 
	 * @param areaHeight
	 * @param htmlTemplate
	 */
	public PDFPageMark(int areaHeight, String htmlTemplate, boolean evalAtPrint) {
		this.areaHeight = areaHeight;
		htmlTemplates.add(htmlTemplate);
		this.evalAtPrint = evalAtPrint;
	}

	public boolean isEvalAtPrint() {
		return evalAtPrint;
	}

	/**
	 * @return the areaHeight
	 */
	public int getAreaHeight() {
		return areaHeight;
	}

	/**
	 * @param areaHeight the areaHeight to set
	 */
	public void setAreaHeight(int areaHeight) {
		this.areaHeight = areaHeight;
	}

	/**
	 * @return the htmlTemplate
	 */
	public List<String> getHtmlTemplates() {
		return htmlTemplates;
	}

	public String getHtmlTemplate() {
		int size = htmlTemplates.size();
		if (size == 0) return "";
		if (size == 1) return htmlTemplates.get(0);
		// the index comes from the render loop over the largest template count of header and footer, so
		// it can be past the end of this list; clamp instead of failing the whole document
		if (hfIndex < 0) return htmlTemplates.get(0);
		if (hfIndex >= size) return htmlTemplates.get(size - 1);
		return htmlTemplates.get(hfIndex);
	}

	public void addHtmlTemplate(String htmlTemplate) {
		htmlTemplates.add(htmlTemplate);
	}

	public PDFPageMark setIndex(int hfIndex) {
		this.hfIndex = hfIndex;
		return this;
	}
}