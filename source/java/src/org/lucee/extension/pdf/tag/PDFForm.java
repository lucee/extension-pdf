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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;
import org.lucee.extension.pdf.PDFStruct;
import org.lucee.extension.pdf.util.PDFUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lucee.commons.io.res.Resource;
import lucee.loader.util.Util;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Cast;

/**
 * Implementation of the cfpdfform tag
 * Supports reading and populating PDF form fields
 */
public class PDFForm extends BodyTagImpl {

	private static final int ACTION_READ = 1;
	private static final int ACTION_POPULATE = 2;

	private int action = ACTION_READ;
	private Object source = null;
	private String result = null;
	private Object xmldataObj = null;
	private String password = null;
	private Resource destination = null;
	private String name = null;
	private boolean overwrite = false;
	private boolean overwriteData = true;
	private List<PDFFormParamBean> params = null;

	@Override
	public void release() {
		super.release();
		action = ACTION_READ;
		source = null;
		result = null;
		xmldataObj = null;
		password = null;
		destination = null;
		name = null;
		overwrite = false;
		overwriteData = true;
		params = null;
	}

	public void setAction( String strAction ) throws PageException {
		strAction = org.lucee.extension.pdf.tag.Document.trimAndLower( strAction ).replace( "-", "" ).replace( "_", "" );
		if ( "read".equals( strAction ) )
			action = ACTION_READ;
		else if ( "populate".equals( strAction ) )
			action = ACTION_POPULATE;
		else
			throw engine.getExceptionUtil().createApplicationException(
					"Invalid pdfform action [" + strAction + "], supported actions are [read, populate]" );
	}

	public void setSource( Object source ) {
		this.source = source;
	}

	public void setResult( String result ) {
		this.result = result;
	}

	public void setXmldata( Object xmldata ) {
		this.xmldataObj = xmldata;
	}

	public void setPassword( String password ) {
		this.password = password;
	}

	public void setDestination( String destination ) throws PageException {
		this.destination = engine.getResourceUtil().toResourceNotExisting( pageContext, destination );
	}

	public void setName( String name ) {
		this.name = name;
	}

	public void setOverwrite( boolean overwrite ) {
		this.overwrite = overwrite;
	}

	public void setOverwritedata( boolean overwriteData ) {
		this.overwriteData = overwriteData;
	}

	public void addParam( PDFFormParamBean param ) {
		if ( params == null )
			params = new ArrayList<>();
		params.add( param );
	}

	@Override
	public int doStartTag() throws PageException {
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public int doEndTag() throws PageException {
		try {
			if ( ACTION_READ == action )
				doActionRead();
			else if ( ACTION_POPULATE == action )
				doActionPopulate();
		}
		catch ( Exception e ) {
			throw engine.getCastUtil().toPageException( e );
		}
		return EVAL_PAGE;
	}

	private void doActionRead() throws PageException, IOException {
		required( "pdfform", "read", "source", source );
		String xmldataVarName = xmldataObj != null ? engine.getCastUtil().toString( xmldataObj, null ) : null;
		if ( result == null && xmldataVarName == null ) {
			throw engine.getExceptionUtil().createApplicationException(
					"At least one of [result] or [xmldata] attributes is required for pdfform action=read" );
		}

		Cast caster = engine.getCastUtil();
		PDDocument doc = null;

		try {
			// Load the PDF
			doc = loadPDDocument( source, password );

			// Get the AcroForm
			PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
			Struct fields = engine.getCreationUtil().createStruct();

			if ( acroForm != null ) {
				// Iterate through all fields and collect values
				for ( PDField field : acroForm.getFieldTree() ) {
					readField( field, fields, "" );
				}
			}

			// Set result as struct
			if ( result != null ) {
				pageContext.setVariable( result, fields );
			}

			// Set xmldata as XML string
			if ( xmldataVarName != null ) {
				StringBuilder xml = new StringBuilder();
				xml.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );
				xml.append( "<fields>\n" );

				for ( Object keyObj : fields.keySet() ) {
					String key = keyObj.toString();
					Object value = fields.get( engine.getCreationUtil().createKey( key ), "" );
					xml.append( "\t<" ).append( escapeXmlName( key ) ).append( ">" );
					xml.append( escapeXmlValue( caster.toString( value, "" ) ) );
					xml.append( "</" ).append( escapeXmlName( key ) ).append( ">\n" );
				}

				xml.append( "</fields>" );
				pageContext.setVariable( xmldataVarName, xml.toString() );
			}
		}
		finally {
			if ( doc != null ) {
				try {
					doc.close();
				}
				catch ( IOException e ) {
				}
			}
		}
	}

	private void readField( PDField field, Struct fields, String prefix ) throws PageException {
		String fullName = prefix.isEmpty() ? field.getPartialName() : prefix + "." + field.getPartialName();

		if ( field instanceof PDNonTerminalField ) {
			// This is a container field, recurse into children
			for ( PDField child : ((PDNonTerminalField) field).getChildren() ) {
				readField( child, fields, fullName );
			}
		}
		else {
			// Terminal field - get its value
			String value = field.getValueAsString();
			if ( value == null )
				value = "";
			fields.set( engine.getCreationUtil().createKey( fullName ), value );
		}
	}

	private void doActionPopulate() throws PageException, IOException {
		required( "pdfform", "populate", "source", source );

		// Must have either destination or name
		if ( destination == null && name == null ) {
			throw engine.getExceptionUtil().createApplicationException(
					"Either [destination] or [name] attribute is required for pdfform action=populate" );
		}

		// Check destination exists
		if ( destination != null && destination.exists() && !overwrite ) {
			throw engine.getExceptionUtil().createApplicationException(
					"Destination file [" + destination + "] already exists. Use overwrite=true to replace." );
		}

		PDDocument doc = null;
		try {
			doc = loadPDDocument( source, password );
			PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();

			if ( acroForm != null ) {
				// Populate from params (cfpdfformparam tags)
				if ( params != null && !params.isEmpty() ) {
					for ( PDFFormParamBean param : params ) {
						String fieldName = param.getName();
						String fieldValue = param.getValue();
						if ( fieldName != null ) {
							PDField field = acroForm.getField( fieldName );
							if ( field != null ) {
								// Check overwriteData setting
								if ( overwriteData || Util.isEmpty( field.getValueAsString() ) ) {
									field.setValue( fieldValue != null ? fieldValue : "" );
								}
							}
						}
					}
				}

				// Populate from xmldata if provided (for populate action, xmldata is input)
				if ( xmldataObj != null && action == ACTION_POPULATE ) {
					populateFromXmlData( acroForm );
				}
			}

			// Output the result
			if ( name != null ) {
				// Return as PDF variable
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				doc.save( baos );
				byte[] pdfBytes = baos.toByteArray();
				PDFStruct pdfStruct = new PDFStruct( pdfBytes, password );
				pageContext.setVariable( name, pdfStruct );
			}

			if ( destination != null ) {
				// Save to file
				OutputStream os = destination.getOutputStream();
				try {
					doc.save( os );
				}
				finally {
					Util.closeEL( os );
				}
			}
		}
		finally {
			if ( doc != null ) {
				try {
					doc.close();
				}
				catch ( IOException e ) {
				}
			}
		}
	}

	private void populateFromXmlData( PDAcroForm acroForm ) throws PageException, IOException {
		String strXml = null;

		// xmldataObj could be a file path, XML string, or XML object
		if ( xmldataObj instanceof String ) {
			strXml = (String) xmldataObj;

			// Check if it's a file path
			try {
				Resource xmlFile = engine.getResourceUtil().toResourceExisting( pageContext, strXml );
				InputStream is = xmlFile.getInputStream();
				try {
					byte[] bytes = is.readAllBytes();
					strXml = new String( bytes, "UTF-8" );
				}
				finally {
					is.close();
				}
			}
			catch ( PageException e ) {
				// Not a file, treat as XML string
			}
		}
		else if ( xmldataObj instanceof Node ) {
			// Already a Node object, process it directly
			processXmlNode( (Node) xmldataObj, acroForm );
			return;
		}
		else {
			throw engine.getExceptionUtil().createApplicationException(
					"Invalid xmldata type. Expected file path, XML string, or XML object." );
		}

		// Parse the XML string
		if ( strXml != null ) {
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				org.w3c.dom.Document xmlDoc = builder.parse( new ByteArrayInputStream( strXml.getBytes( "UTF-8" ) ) );
				processXmlNode( xmlDoc.getDocumentElement(), acroForm );
			}
			catch ( Exception e ) {
				throw engine.getExceptionUtil().createApplicationException( "Invalid XML data: " + e.getMessage() );
			}
		}
	}

	private void processXmlNode( Node root, PDAcroForm acroForm ) throws IOException {
		// If it's a Document, get the document element
		if ( root instanceof org.w3c.dom.Document ) {
			root = ((org.w3c.dom.Document) root).getDocumentElement();
		}

		// Process child elements as field name/value pairs
		NodeList children = root.getChildNodes();
		for ( int i = 0; i < children.getLength(); i++ ) {
			Node child = children.item( i );
			if ( child instanceof Element ) {
				String fieldName = child.getNodeName();
				String fieldValue = child.getTextContent();

				PDField field = acroForm.getField( fieldName );
				if ( field != null ) {
					if ( overwriteData || Util.isEmpty( field.getValueAsString() ) ) {
						field.setValue( fieldValue != null ? fieldValue : "" );
					}
				}
			}
		}
	}

	private PDDocument loadPDDocument( Object source, String password ) throws PageException, IOException {
		Cast caster = engine.getCastUtil();

		// Source is a PDF variable name
		if ( source instanceof String ) {
			String strSource = (String) source;

			// Check if it's a variable name referring to a PDFStruct
			try {
				Object var = pageContext.getVariable( strSource );
				if ( var instanceof PDFStruct ) {
					byte[] barr = ((PDFStruct) var).getRaw();
					return Loader.loadPDF( new RandomAccessReadBuffer( new ByteArrayInputStream( barr ) ),
							password );
				}
			}
			catch ( PageException e ) {
				// Not a variable, treat as file path
			}

			// Treat as file path
			Resource res = engine.getResourceUtil().toResourceExisting( pageContext, strSource );
			return loadFromResource( res, password );
		}

		// Source is a PDFStruct
		if ( source instanceof PDFStruct ) {
			byte[] barr = ((PDFStruct) source).getRaw();
			if ( !Util.isEmpty( password ) ) {
				return Loader.loadPDF( new RandomAccessReadBuffer( new ByteArrayInputStream( barr ) ), password );
			}
			return Loader.loadPDF( new RandomAccessReadBuffer( new ByteArrayInputStream( barr ) ) );
		}

		// Source is a Resource (file)
		if ( source instanceof Resource ) {
			return loadFromResource( (Resource) source, password );
		}

		throw engine.getExceptionUtil().createApplicationException(
				"Invalid source type [" + source.getClass().getName() + "] for pdfform" );
	}

	private PDDocument loadFromResource( Resource res, String password ) throws IOException {
		InputStream is = res.getInputStream();
		try {
			if ( !Util.isEmpty( password ) ) {
				return Loader.loadPDF( new RandomAccessReadBuffer( is ), password );
			}
			return Loader.loadPDF( new RandomAccessReadBuffer( is ) );
		}
		catch (Throwable t) {
			Util.closeEL( is );
			throw t;
		}
	}

	private String escapeXmlName( String name ) {
		// XML element names can't start with numbers or contain certain chars
		// Replace invalid chars with underscores
		return name.replaceAll( "[^a-zA-Z0-9_.-]", "_" ).replaceFirst( "^([0-9])", "_$1" );
	}

	private String escapeXmlValue( String value ) {
		if ( value == null )
			return "";
		return value.replace( "&", "&amp;" ).replace( "<", "&lt;" ).replace( ">", "&gt;" )
				.replace( "\"", "&quot;" ).replace( "'", "&apos;" );
	}
}
