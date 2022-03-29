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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.lucee.extension.pdf.util.PDFUtil;
import org.lucee.extension.pdf.util.StructSupport;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

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

	public PdfReader getPdfReader() throws PageException {
		try {
			if (barr != null) {
				if (password != null) return new PdfReader(barr, password.getBytes());
				return new PdfReader(barr);
			}
			if (password != null) return new PdfReader(PDFUtil.toBytes(resource), password.getBytes());
			return new PdfReader(PDFUtil.toBytes(resource));
		}
		catch (IOException ioe) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().createApplicationException("can not load file" + password + " [" + resource + "]", ioe.getMessage());
		}
	}

	private String getFilePath() {
		if (resource == null) return "";
		return resource.getAbsolutePath();
	}

	public Struct getInfo() {

		PdfReader pr = null;
		try {
			pr = getPdfReader();
			// PdfDictionary catalog = pr.getCatalog();
			int permissions = pr.getPermissions();
			boolean encrypted = pr.isEncrypted();

			Struct info = CFMLEngineFactory.getInstance().getCreationUtil().createStruct();
			info.setEL("FilePath", getFilePath());

			// access
			info.setEL("ChangingDocument", allowed(encrypted, permissions, PdfWriter.ALLOW_MODIFY_CONTENTS));
			info.setEL("Commenting", allowed(encrypted, permissions, PdfWriter.ALLOW_MODIFY_ANNOTATIONS));
			info.setEL("ContentExtraction", allowed(encrypted, permissions, PdfWriter.ALLOW_SCREENREADERS));
			info.setEL("CopyContent", allowed(encrypted, permissions, PdfWriter.ALLOW_COPY));
			info.setEL("DocumentAssembly", allowed(encrypted, permissions, PdfWriter.ALLOW_ASSEMBLY + PdfWriter.ALLOW_MODIFY_CONTENTS));
			info.setEL("FillingForm", allowed(encrypted, permissions, PdfWriter.ALLOW_FILL_IN + PdfWriter.ALLOW_MODIFY_ANNOTATIONS));
			info.setEL("Printing", allowed(encrypted, permissions, PdfWriter.ALLOW_PRINTING));
			info.setEL("Secure", "");
			info.setEL("Signing", allowed(encrypted, permissions, PdfWriter.ALLOW_MODIFY_ANNOTATIONS + PdfWriter.ALLOW_MODIFY_CONTENTS + PdfWriter.ALLOW_FILL_IN));

			info.setEL("Encryption", encrypted ? "Password Security" : "No Security");// MUST
			info.setEL("TotalPages", CFMLEngineFactory.getInstance().getCastUtil().toDouble(pr.getNumberOfPages()));
			info.setEL("Version", "1." + pr.getPdfVersion());
			info.setEL("permissions", "" + permissions);
			info.setEL("permiss", "" + PdfWriter.ALLOW_FILL_IN);

			info.setEL("Application", "");
			info.setEL("Author", "");
			info.setEL("CenterWindowOnScreen", "");
			info.setEL("Created", "");
			info.setEL("FitToWindow", "");
			info.setEL("HideMenubar", "");
			info.setEL("HideToolbar", "");
			info.setEL("HideWindowUI", "");
			info.setEL("Keywords", "");
			info.setEL("Language", "");
			info.setEL("Modified", "");
			info.setEL("PageLayout", "");
			info.setEL("Producer", "");
			info.setEL("Properties", "");
			info.setEL("ShowDocumentsOption", "");
			info.setEL("ShowWindowsOption", "");
			info.setEL("Subject", "");
			info.setEL("Title", "");
			info.setEL("Trapped", "");

			int total = pr.getNumberOfPages() + 1;
			Struct pagesize = CFMLEngineFactory.getInstance().getCreationUtil().createStruct();
			Array rotation = CFMLEngineFactory.getInstance().getCreationUtil().createArray();
			Array pagesize1 = CFMLEngineFactory.getInstance().getCreationUtil().createArray();

			for (int i = 1; i < total; i++) {
				rotation.appendEL(pr.getPageRotation(i));
			}

			int count = pr.getNumberOfPages() + 1;

			for (int j = 1; j < count; j++) {
				pagesize.setEL("Height", pr.getPageSize(j).getHeight());
				pagesize.setEL("Width", pr.getPageSize(j).getWidth());
				pagesize1.appendEL(pagesize);
			}

			info.setEL("PageRotation", rotation);
			info.setEL("Pagesize", pagesize1);

			// info
			HashMap imap = pr.getInfo();
			Iterator it = imap.entrySet().iterator();
			Map.Entry entry;
			while (it.hasNext()) {
				entry = (Entry) it.next();
				info.setEL(CFMLEngineFactory.getInstance().getCastUtil().toString(entry.getKey(), null), entry.getValue());
			}
			return info;
		}
		catch (PageException pe) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().createPageRuntimeException(pe);
		}
		finally {
			if (pr != null) pr.close();
		}
	}

	private static Object allowed(boolean encrypted, int permissions, int permission) {
		return (!encrypted || (permissions & permission) > 0) ? "Allowed" : "Not Allowed";
	}

	public void setPages(String strPages) throws PageException {
		if (Util.isEmpty(strPages)) return;
		if (pages == null) pages = new HashSet<Integer>();
		PDFUtil.parsePageDefinition(pages, strPages, -1);
	}

	public Set<Integer> getPages() {
		// if(pages==null)pages=new HashSet();
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

	public PDDocument toPDDocument() throws IOException {
		PDDocument doc;
		if (barr != null) {
			if (password != null) doc = Loader.loadPDF(new ByteArrayInputStream(barr, 0, barr.length), password);
			else doc = Loader.loadPDF(new ByteArrayInputStream(barr, 0, barr.length));
		}
		else if (resource instanceof File) {
			if (password != null) doc = Loader.loadPDF((File) resource, password);
			else doc = Loader.loadPDF((File) resource);
		}
		else {
			barr = PDFUtil.toBytes(resource);
			if (password != null) doc = Loader.loadPDF(new ByteArrayInputStream(barr, 0, barr.length), password);
			else doc = Loader.loadPDF(new ByteArrayInputStream(barr, 0, barr.length));
		}
		return doc;

	}
}