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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.lucee.extension.pdf.PDFDocument;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextUserAgent;

import lucee.commons.io.log.Log;
import lucee.runtime.PageContext;

/**
 * Flying Saucer user agent that asks the cfdocument resourceHandler for every resource (CSS,
 * images, ...) before fetching it the normal way. The handler returning null means "fetch as
 * usual".
 */
public final class ResourceHandlerUserAgent extends ITextUserAgent {

	private final PDFDocument document;
	private final PageContext pc;

	public ResourceHandlerUserAgent(ITextOutputDevice outputDevice, int dotsPerPixel, PDFDocument document, PageContext pc) {
		super(outputDevice, dotsPerPixel);
		this.document = document;
		this.pc = pc;
	}

	@Override
	protected InputStream openStream(String uri) throws IOException {
		// only remote resources go to the handler; Flying Saucer also loads its own default CSS
		// (jar:) and local files through this method
		if (!isRemote(uri)) return super.openStream(uri);
		byte[] data;
		try {
			data = document.fetchFromResourceHandler(pc, uri);
		}
		catch (Exception e) {
			// an error in the handler must not be swallowed silently, but it should not kill the render
			// either: log it and fall back to the default fetch, the same as a handler returning null
			Log log = pc.getConfig().getLog("pdf");
			if (log != null) log.log(Log.LEVEL_ERROR, "pdf", "resourceHandler failed for [" + uri + "], falling back to the default fetch", e);
			data = null;
		}
		if (data != null) return new ByteArrayInputStream(data);
		return super.openStream(uri);
	}

	private static boolean isRemote(String uri) {
		if (uri == null) return false;
		String s = uri.trim().toLowerCase();
		return s.startsWith("http://") || s.startsWith("https://");
	}
}
