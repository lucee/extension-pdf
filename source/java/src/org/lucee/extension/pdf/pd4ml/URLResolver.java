/**
*
* Copyright (c) 2016, Lucee Assosication Switzerland
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
package org.lucee.extension.pdf.pd4ml;

import java.net.MalformedURLException;
import java.net.URL;

import org.lucee.xml.XMLUtility;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Transform a HTML String, set all relative Pathes inside HTML File to absolute
 *
 */
public final class URLResolver {

	private Tag[] tags = new Tag[] { new Tag("a", "href"), new Tag("link", "href"), new Tag("form", "action"), new Tag("applet", "code"),
			new Tag("script", "src"), new Tag("body", "background"), new Tag("frame", "src"), new Tag("bgsound", "src"), new Tag("img", "src"),

			new Tag("embed", new String[] { "src", "pluginspace" }), new Tag("object", new String[] { "data", "classid", "codebase", "usemap" })

	};

	public void transform(Node node, URL url) throws MalformedURLException {
		Element el;
		if(node.getNodeType() == Node.DOCUMENT_NODE) {
			transform(XMLUtility.getRootElement(node), url);
		}
		else if(node.getNodeType() == Node.ELEMENT_NODE) {
			el = (Element)node;
			String[] attr;
			NamedNodeMap map;
			String attrName, value, value2, nodeName = el.getNodeName();
			int len;
			// translate attribute
			for (int i = 0; i < tags.length; i++) {
				if(tags[i].tag.equalsIgnoreCase(nodeName)) {

					attr = tags[i].attributes;
					map = el.getAttributes();
					len = map.getLength();
					for (int y = 0; y < attr.length; y++) {
						for (int z = 0; z < len; z++) {
							attrName = map.item(z).getNodeName();
							if(attrName.equalsIgnoreCase(attr[y])) {
								value = el.getAttribute(attrName);
								value2 = add(url, value);

								if(value != value2) {
									el.setAttribute(attrName, value2);
								}

								break;
							}
						}
					}
				}
			}

			// list children
			NodeList nodes = el.getChildNodes();
			len = nodes.getLength();
			for (int i = 0; i < len; i++) {
				transform(nodes.item(i), url);
			}
		}
	}

	private String add(URL url, String value) {
		value = value.trim();
		String lcValue = value.toLowerCase();
		if(lcValue.startsWith("http://") || lcValue.startsWith("file://") || lcValue.startsWith("news://") || lcValue.startsWith("goopher://")
				|| lcValue.startsWith("javascript:"))
			return (value);
		try {
			return new URL(url, value.toString()).toExternalForm();
		}
		catch (MalformedURLException e) {
			return value;
		}
	}

	private class Tag {
		private String tag;
		private String[] attributes;

		private Tag(String tag, String[] attributes) {
			this.tag = tag.toLowerCase();
			this.attributes = new String[attributes.length];
			for (int i = 0; i < attributes.length; i++) {
				this.attributes[i] = attributes[i].toLowerCase();
			}

		}

		private Tag(String tag, String attribute1) {
			this.tag = tag.toLowerCase();
			this.attributes = new String[] { attribute1.toLowerCase() };
		}

	}

	public static URLResolver getInstance() {
		return new URLResolver();
	}

}