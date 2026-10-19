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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextUserAgent;

/**
 * Flying Saucer user agent that sets a connect and read timeout on every connection it opens
 * (stylesheets, images, ...); the default user agent has none, so an unresponsive host blocks the
 * request forever
 */
public class TimeoutUserAgent extends ITextUserAgent {

	private final int timeoutMillis;

	public TimeoutUserAgent(ITextOutputDevice outputDevice, int dotsPerPixel, int timeoutMillis) {
		super(outputDevice, dotsPerPixel);
		this.timeoutMillis = timeoutMillis;
	}

	@Override
	protected URLConnection onHttpConnection(HttpURLConnection conn) throws IOException {
		// called by openConnection before the request is sent (getResponseCode), so the timeouts apply
		applyTimeout(conn);
		URLConnection result = super.onHttpConnection(conn);
		// a redirect returns a new, not yet connected connection
		applyTimeout(result);
		return result;
	}

	private void applyTimeout(URLConnection conn) {
		if (conn != null && timeoutMillis > 0) {
			conn.setConnectTimeout(timeoutMillis);
			conn.setReadTimeout(timeoutMillis);
		}
	}
}
