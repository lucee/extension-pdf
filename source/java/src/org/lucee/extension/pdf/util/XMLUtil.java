package org.lucee.extension.pdf.util;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class XMLUtil {
	
	public static final short UNDEFINED_NODE=-1;
	
	public static Element getRootElement(Node node) {
	    Document doc=null;
		if(node instanceof Document) doc=(Document) node;
		else doc=node.getOwnerDocument();
		return doc.getDocumentElement();
	}

    public static synchronized Element getChildWithName(String name, Element el) {
		Element[] children = getChildElementsAsArray(el);
		for(int i=0;i<children.length;i++) {
			if(name.equalsIgnoreCase(children[i].getNodeName()))
				return children[i];
		}
		return null;
	}
    
    public static Element[] getChildElementsAsArray(Node node) {
    	ArrayList<Node> nodeList=getChildNodes(node,Node.ELEMENT_NODE,null);
        return  nodeList.toArray(new Element[nodeList.size()]);
    }
   
    public static synchronized ArrayList<Node> getChildNodes(Node node, short type, String filter) {
		ArrayList<Node> rtn=new ArrayList<Node>();
		NodeList nodes=node.getChildNodes();
		int len=nodes.getLength();
		Node n;
		for(int i=0;i<len;i++) {
			try {
				n=nodes.item(i);
				if(n!=null && (type==UNDEFINED_NODE || n.getNodeType()==type)){
					if(filter==null || filter.equals(n.getLocalName()))
					rtn.add(n);
				}
			}
			catch (Exception t){}
		}
		return rtn;
	}

    public static Document getDocument(Node node) {
		if(node instanceof Document) return (Document)node;
		return node.getOwnerDocument();
	}
    
    public static final Document parseHTML(InputSource xml) throws SAXException, IOException {
            XMLReader reader = new Parser();
            reader.setFeature(Parser.namespacesFeature, true);
            reader.setFeature(Parser.namespacePrefixesFeature, true);
        
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            
            DOMResult result = new DOMResult();
            transformer.transform(new SAXSource(reader, xml), result);
            return getDocument(result.getNode());
        } 
        catch (Exception e) {
            throw new SAXException(e);
        }
    }
}
