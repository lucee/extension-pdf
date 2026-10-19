/**
 *
 * Copyright (c) 2026, Lucee Association Switzerland
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
package org.lucee.extension.pdf.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * builds a safe Content-Disposition header value from a user supplied file name (cfdocument
 * saveAsName)
 */
public final class ContentDisposition {

	private ContentDisposition() {
	}

	/**
	 * @return the header value, or null if nothing is left of the name after removing control characters
	 */
	public static String inline(String fileName) {
		if (fileName == null) return null;
		// control characters (CR/LF included) would allow header injection / response splitting
		String clean = fileName.replaceAll("\\p{Cntrl}", "").trim();
		if (clean.isEmpty()) return null;

		StringBuilder ascii = new StringBuilder(clean.length());
		boolean nonAscii = false;
		for (int i = 0; i < clean.length(); i++) {
			char c = clean.charAt(i);
			if (c == '"' || c == '\\') ascii.append('_');
			else if (c > 126) {
				ascii.append('_');
				nonAscii = true;
			}
			else ascii.append(c);
		}

		StringBuilder sb = new StringBuilder("inline; filename=\"").append(ascii).append('"');
		// RFC 6266 / RFC 5987: the full name for clients that understand it
		if (nonAscii) sb.append("; filename*=UTF-8''").append(URLEncoder.encode(clean, StandardCharsets.UTF_8).replace("+", "%20").replace("*", "%2A"));
		return sb.toString();
	}
}
