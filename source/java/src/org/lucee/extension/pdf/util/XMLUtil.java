package org.lucee.extension.pdf.util;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;

public class XMLUtil {

	public static final short UNDEFINED_NODE = -1;
	private static TransformerFactory transformerFactory;

	public static Element getRootElement(Node node) {
		Document doc = null;
		if (node instanceof Document) doc = (Document) node;
		else doc = node.getOwnerDocument();
		return doc.getDocumentElement();
	}

	public static synchronized Element getChildWithName(String name, Element el) {
		Element[] children = getChildElementsAsArray(el);
		for (int i = 0; i < children.length; i++) {
			if (name.equalsIgnoreCase(children[i].getNodeName())) return children[i];
		}
		return null;
	}

	public static Element[] getChildElementsAsArray(Node node) {
		ArrayList<Node> nodeList = getChildNodes(node, Node.ELEMENT_NODE, null);
		return nodeList.toArray(new Element[nodeList.size()]);
	}

	public static synchronized ArrayList<Node> getChildNodes(Node node, short type, String filter) {
		ArrayList<Node> rtn = new ArrayList<Node>();
		NodeList nodes = node.getChildNodes();
		int len = nodes.getLength();
		Node n;
		for (int i = 0; i < len; i++) {
			try {
				n = nodes.item(i);
				if (n != null && (type == UNDEFINED_NODE || n.getNodeType() == type)) {
					if (filter == null || filter.equals(n.getLocalName())) rtn.add(n);
				}
			}
			catch (Exception t) {
			}
		}
		return rtn;
	}

	public static Document getDocument(Node node) {
		if (node instanceof Document) return (Document) node;
		return node.getOwnerDocument();
	}

	/**
	 * Parse HTML to a W3C DOM Document using jsoup.
	 */
	public static final Document parseHTML(InputSource xml) throws SAXException, IOException {
		try {
			// Read the input source content
			java.io.Reader reader = xml.getCharacterStream();
			String html;
			if (reader != null) {
				StringBuilder sb = new StringBuilder();
				char[] buffer = new char[4096];
				int n;
				while ((n = reader.read(buffer)) != -1) {
					sb.append(buffer, 0, n);
				}
				html = sb.toString();
			}
			else if (xml.getByteStream() != null) {
				html = new String(xml.getByteStream().readAllBytes(), "UTF-8");
			}
			else if (xml.getSystemId() != null) {
				html = Jsoup.connect(xml.getSystemId()).get().html();
			}
			else {
				throw new SAXException("No input source provided");
			}

			// Parse with jsoup and convert to W3C DOM
			Parser parser = Parser.htmlParser();
			parser.tagSet().onNewTag( tag -> {
				if ( !tag.isKnownTag() ) tag.set( Tag.SelfClose );
			});
			org.jsoup.nodes.Document jsoupDoc = Jsoup.parse( html, parser );
			jsoupDoc.outputSettings().syntax( org.jsoup.nodes.Document.OutputSettings.Syntax.xml );
			W3CDom w3cDom = new W3CDom();
			return w3cDom.fromJsoup(jsoupDoc);
		}
		catch (Exception e) {
			throw new SAXException(e);
		}
	}

	public static Transformer getTransformer() throws TransformerConfigurationException, TransformerFactoryConfigurationError {
		try {
			return getTransformerFactory().newTransformer();
		}
		catch (Exception e) {
			return TransformerFactory.newInstance().newTransformer();
		}
	}

	public static TransformerFactory getTransformerFactory() {
		if (transformerFactory == null) {
			try {
				Class<?> clazz = CFMLEngineFactory.getInstance().getClassUtil().loadClass("lucee.runtime.text.xml.XMLUtil");
				transformerFactory = (TransformerFactory) clazz.getMethod("getTransformerFactory", new Class[0]).invoke(null, new Object[0]);
			}
			catch (Exception e) {
				// fallback if Lucee's XMLUtil is not available
				transformerFactory = TransformerFactory.newInstance();
			}
		}
		return transformerFactory;
	}

	// toString(node, omitXMLDecl, indent, publicId, systemId, encoding);
	public static String toString(Node node, boolean omitXMLDecl, boolean indent, String publicId, String systemId, String encoding) throws PageException {
		// FUTURE use interface from loader
		try {
			Class<?> clazz = CFMLEngineFactory.getInstance().getClassUtil().loadClass("lucee.runtime.text.xml.XMLCaster");
			Method method = clazz.getMethod("toString", new Class[] { Node.class, boolean.class, boolean.class, String.class, String.class, String.class });
			return (String) method.invoke(null, new Object[] { node, omitXMLDecl, indent, publicId, systemId, encoding });
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	public static InputSource toInputSource(String str) {
		return new InputSource(new StringReader(str));
	}
}
