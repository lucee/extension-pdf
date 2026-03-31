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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.lucee.extension.pdf.util.PDFUtil;
import org.lucee.extension.pdf.util.StructSupport;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;

public class PDFStruct extends StructSupport implements Struct {

	private static final long serialVersionUID = -5513632004089290888L;

	private byte[] barr;
	private final String password;
	private Resource resource;
	private Set<Integer> pages;
	private Struct cachedInfo;

	public PDFStruct(byte[] barr, String password) {
		this.barr = barr;
		this.password = password;
	}

	public PDFStruct(Resource resource, String password) {
		this.resource = resource;
		this.password = password;
	}

	public PDFStruct(byte[] barr, Resource resource, String password) {
		this.resource = resource;
		this.barr = barr;
		this.password = password;
	}

	@Override
	public void clear() {
		getInfo().clear();
	}

	@Override
	public boolean containsKey(Key key) {
		return getInfo().containsKey(key);
	}

	@Override
	public boolean containsKey(PageContext pc, Key key) {
		return getInfo().containsKey(pc, key);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		PDFStruct duplicate = new PDFStruct(barr, resource, password);
		return duplicate;
	}

	@Override
	public Object get(Key key) throws PageException {
		return getInfo().get(key);
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		return getInfo().get(key, defaultValue);
	}

	@Override
	public Key[] keys() {
		return getInfo().keys();
	}

	@Override
	public Object remove(Key key) throws PageException {
		return getInfo().remove(key);
	}

	@Override
	public Object removeEL(Key key) {
		return getInfo().removeEL(key);
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		return getInfo().set(key, value);
	}

	@Override
	public Object setEL(Key key, Object value) {
		return getInfo().setEL(key, value);
	}

	@Override
	public int size() {
		return getInfo().size();
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {

		DumpData dd = getInfo().toDumpData(pageContext, maxlevel, properties);
		if (dd instanceof DumpTable) ((DumpTable) dd).setTitle("Struct (PDFDocument)");
		return dd;
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return getInfo().keyIterator();
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return getInfo().keysAsStringIterator();
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return getInfo().entryIterator();
	}

	@Override
	public Iterator<Object> valueIterator() {
		return getInfo().valueIterator();
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return getInfo().castToBooleanValue();
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return getInfo().castToBoolean(defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return getInfo().castToDateTime();
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return getInfo().castToDateTime(defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return getInfo().castToDoubleValue();
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return getInfo().castToDoubleValue(defaultValue);
	}

	@Override
	public String castToString() throws PageException {
		return getInfo().castToString();
	}

	@Override
	public String castToString(String defaultValue) {
		return getInfo().castToString(defaultValue);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return getInfo().compareTo(str);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return getInfo().compareTo(b);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return getInfo().compareTo(d);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return getInfo().compareTo(dt);
	}

	///////////////////////////////////////////////

	private String getFilePath() {
		if (resource == null) return "";
		return resource.getAbsolutePath();
	}

	public Struct getInfo() {
		if (cachedInfo != null) return cachedInfo;
		PDDocument pdDoc = null;
		try {
			pdDoc = toPDDocument();
			PDDocumentInformation docInfo = pdDoc.getDocumentInformation();
			AccessPermission ap = pdDoc.getCurrentAccessPermission();
			boolean encrypted = pdDoc.isEncrypted();

			Struct info = CFMLEngineFactory.getInstance().getCreationUtil().createStruct();
			info.setEL("FilePath", getFilePath());

			// Access permissions
			info.setEL("ChangingDocument", allowed(encrypted, ap != null ? ap.canModify() : true));
			info.setEL("Commenting", allowed(encrypted, ap != null ? ap.canModifyAnnotations() : true));
			info.setEL("ContentExtraction", allowed(encrypted, ap != null ? ap.canExtractForAccessibility() : true));
			info.setEL("CopyContent", allowed(encrypted, ap != null ? ap.canExtractContent() : true));
			info.setEL("DocumentAssembly", allowed(encrypted, ap != null ? ap.canAssembleDocument() : true));
			info.setEL("FillingForm", allowed(encrypted, ap != null ? ap.canFillInForm() : true));
			info.setEL("Printing", allowed(encrypted, ap != null ? ap.canPrint() : true));
			info.setEL("Secure", "");
			info.setEL("Signing", allowed(encrypted, ap != null ? (ap.canModifyAnnotations() && ap.canFillInForm()) : true));

			info.setEL("Encryption", encrypted ? "Password Security" : "No Security");
			info.setEL("TotalPages", CFMLEngineFactory.getInstance().getCastUtil().toDouble(pdDoc.getNumberOfPages()));
			info.setEL("Version", String.valueOf(pdDoc.getVersion()));

			// Document info from metadata
			info.setEL("Application", docInfo.getCreator() != null ? docInfo.getCreator() : "");
			info.setEL("Author", docInfo.getAuthor() != null ? docInfo.getAuthor() : "");
			info.setEL("CenterWindowOnScreen", "");
			info.setEL("Created", formatDate(docInfo.getCreationDate()));
			info.setEL("FitToWindow", "");
			info.setEL("HideMenubar", "");
			info.setEL("HideToolbar", "");
			info.setEL("HideWindowUI", "");
			info.setEL("Keywords", docInfo.getKeywords() != null ? docInfo.getKeywords() : "");
			info.setEL("Language", "");
			info.setEL("Modified", formatDate(docInfo.getModificationDate()));
			info.setEL("PageLayout", "");
			info.setEL("Producer", docInfo.getProducer() != null ? docInfo.getProducer() : "");
			info.setEL("Properties", "");
			info.setEL("ShowDocumentsOption", "");
			info.setEL("ShowWindowsOption", "");
			info.setEL("Subject", docInfo.getSubject() != null ? docInfo.getSubject() : "");
			info.setEL("Title", docInfo.getTitle() != null ? docInfo.getTitle() : "");
			info.setEL("Trapped", docInfo.getTrapped() != null ? docInfo.getTrapped() : "");

			// Page info
			int total = pdDoc.getNumberOfPages();
			Array rotation = CFMLEngineFactory.getInstance().getCreationUtil().createArray();
			Array pagesizeArr = CFMLEngineFactory.getInstance().getCreationUtil().createArray();

			for (int i = 0; i < total; i++) {
				PDPage page = pdDoc.getPage(i);
				rotation.appendEL(page.getRotation());

				Struct pagesize = CFMLEngineFactory.getInstance().getCreationUtil().createStruct();
				PDRectangle mediaBox = page.getMediaBox();
				pagesize.setEL("Height", mediaBox.getHeight());
				pagesize.setEL("Width", mediaBox.getWidth());
				pagesizeArr.appendEL(pagesize);
			}

			info.setEL("PageRotation", rotation);
			info.setEL("Pagesize", pagesizeArr);

			// Custom metadata properties
			for (String key : docInfo.getMetadataKeys()) {
				if (!isStandardKey(key)) {
					info.setEL(key, docInfo.getCustomMetadataValue(key));
				}
			}

			cachedInfo = info;
			return info;
		}
		catch (IOException ioe) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().createPageRuntimeException(
				CFMLEngineFactory.getInstance().getExceptionUtil()
					.createApplicationException("Error reading PDF info: " + ioe.getMessage()));
		}
		finally {
			if (pdDoc != null) {
				try {
					pdDoc.close();
				}
				catch (IOException e) {
					// ignore
				}
			}
		}
	}

	private static boolean isStandardKey(String key) {
		return "Title".equals(key) || "Author".equals(key) || "Subject".equals(key) ||
			   "Keywords".equals(key) || "Creator".equals(key) || "Producer".equals(key) ||
			   "CreationDate".equals(key) || "ModDate".equals(key) || "Trapped".equals(key);
	}

	private static String formatDate(Calendar cal) {
		if (cal == null) return "";
		return cal.getTime().toString();
	}

	private static Object allowed(boolean encrypted, boolean permission) {
		return (!encrypted || permission) ? "Allowed" : "Not Allowed";
	}

	public void setPages(String strPages) throws PageException {
		if (Util.isEmpty(strPages)) return;
		if (pages == null) pages = new HashSet<Integer>();
		int lastPage;
		try {
			lastPage = getNumberOfPages();
		}
		catch (IOException e) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().createApplicationException(
				"could not determine page count: " + e.getMessage());
		}
		PDFUtil.parsePageDefinition(pages, strPages, lastPage);
	}

	public Set<Integer> getPages() {
		return pages;
	}

	public Resource getResource() {
		return resource;
	}

	public byte[] getRaw() throws IOException {
		if (barr != null) return barr;
		return PDFUtil.toBytes(resource);
	}

	@Override
	public boolean containsValue(Object value) {
		return getInfo().containsValue(value);
	}

	@Override
	public java.util.Collection values() {
		return getInfo().values();
	}

	/**
	 * Load this PDF as a PDFBox PDDocument.
	 * Caller is responsible for closing the returned document.
	 */
	public PDDocument toPDDocument() throws IOException {
		PDDocument doc;
		if (barr != null) {
			if (password != null) {
				doc = Loader.loadPDF(new RandomAccessReadBuffer(barr), password);
			}
			else {
				doc = Loader.loadPDF(new RandomAccessReadBuffer(barr));
			}
		}
		else if (resource instanceof File) {
			if (password != null) {
				doc = Loader.loadPDF((File) resource, password);
			}
			else {
				doc = Loader.loadPDF((File) resource);
			}
		}
		else {
			barr = PDFUtil.toBytes(resource);
			if (password != null) {
				doc = Loader.loadPDF(new RandomAccessReadBuffer(barr), password);
			}
			else {
				doc = Loader.loadPDF(new RandomAccessReadBuffer(barr));
			}
		}
		return doc;
	}

	/**
	 * Get the number of pages in this PDF.
	 */
	public int getNumberOfPages() throws IOException {
		try (PDDocument doc = toPDDocument()) {
			return doc.getNumberOfPages();
		}
	}
}
