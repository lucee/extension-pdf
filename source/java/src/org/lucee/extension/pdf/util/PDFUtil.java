/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
package org.lucee.extension.pdf.util;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.lucee.extension.pdf.PDFStruct;
import org.lucee.extension.pdf.tag.PDF;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.util.ListUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;

public class PDFUtil {

	// Encryption type constants
	public static final int ENCRYPT_RC4_40 = 1;
	public static final int ENCRYPT_RC4_128 = 2;
	public static final int ENCRYPT_RC4_128M = 3;
	public static final int ENCRYPT_AES_128 = 4;
	public static final int ENCRYPT_NONE = -1;

	// Permission constants (matching iText/PDF spec values for compatibility)
	public static final int ALLOW_PRINTING = 4;
	public static final int ALLOW_MODIFY_CONTENTS = 8;
	public static final int ALLOW_COPY = 16;
	public static final int ALLOW_MODIFY_ANNOTATIONS = 32;
	public static final int ALLOW_FILL_IN = 256;
	public static final int ALLOW_SCREENREADERS = 512;
	public static final int ALLOW_ASSEMBLY = 1024;
	public static final int ALLOW_DEGRADED_PRINTING = 2048;

	private static final int PERMISSION_ALL = ALLOW_ASSEMBLY + ALLOW_COPY + ALLOW_DEGRADED_PRINTING + ALLOW_FILL_IN
			+ ALLOW_MODIFY_ANNOTATIONS + ALLOW_MODIFY_CONTENTS + ALLOW_PRINTING + ALLOW_SCREENREADERS;

	/**
	 * convert a string list of permission
	 * 
	 * @param strPermissions
	 * @return
	 * @throws PageException
	 */
	public static int toPermissions(String strPermissions) throws PageException {
		if (strPermissions == null) return 0;
		int permissions = 0;
		strPermissions = strPermissions.trim();
		ListUtil util = CFMLEngineFactory.getInstance().getListUtil();
		String[] arr = util.toStringArray(util.toArrayRemoveEmpty(strPermissions, ","));
		for (int i = 0; i < arr.length; i++) {
			permissions = add(permissions, toPermission(arr[i]));
		}
		return permissions;
	}

	/**
	 * convert a string defintion of a permision in a integer Constant
	 *
	 * @param strPermission
	 * @return
	 * @throws PageException
	 */
	public static int toPermission(String strPermission) throws PageException {
		strPermission = strPermission.trim().toLowerCase();
		if ("allowassembly".equals(strPermission)) return ALLOW_ASSEMBLY;
		else if ("none".equals(strPermission)) return 0;
		else if ("all".equals(strPermission)) return PERMISSION_ALL;
		else if ("assembly".equals(strPermission)) return ALLOW_ASSEMBLY;
		else if ("documentassembly".equals(strPermission)) return ALLOW_ASSEMBLY;
		else if ("allowdegradedprinting".equals(strPermission)) return ALLOW_DEGRADED_PRINTING;
		else if ("degradedprinting".equals(strPermission)) return ALLOW_DEGRADED_PRINTING;
		else if ("printing".equals(strPermission)) return ALLOW_PRINTING;
		else if ("allowfillin".equals(strPermission)) return ALLOW_FILL_IN;
		else if ("fillin".equals(strPermission)) return ALLOW_FILL_IN;
		else if ("fillingform".equals(strPermission)) return ALLOW_FILL_IN;
		else if ("allowmodifyannotations".equals(strPermission)) return ALLOW_MODIFY_ANNOTATIONS;
		else if ("modifyannotations".equals(strPermission)) return ALLOW_MODIFY_ANNOTATIONS;
		else if ("allowmodifycontents".equals(strPermission)) return ALLOW_MODIFY_CONTENTS;
		else if ("modifycontents".equals(strPermission)) return ALLOW_MODIFY_CONTENTS;
		else if ("allowcopy".equals(strPermission)) return ALLOW_COPY;
		else if ("copy".equals(strPermission)) return ALLOW_COPY;
		else if ("copycontent".equals(strPermission)) return ALLOW_COPY;
		else if ("allowprinting".equals(strPermission)) return ALLOW_PRINTING;
		else if ("allowscreenreaders".equals(strPermission)) return ALLOW_SCREENREADERS;
		else if ("screenreaders".equals(strPermission)) return ALLOW_SCREENREADERS;

		else throw CFMLEngineFactory.getInstance().getExceptionUtil().createApplicationException("invalid permission [" + strPermission
				+ "], valid permission values are [AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations, AllowFillIn, AllowScreenReaders, AllowAssembly, AllowDegradedPrinting]");
	}

	private static int add(int permissions, int permission) {
		if (permission == 0 || (permissions & permission) > 0) return permissions;
		return permissions + permission;
	}

	/**
	 * @param docs
	 * @param os
	 * @param removePages if true, pages defined in PDFDocument will be removed, otherwise all other
	 *            pages will be removed
	 * @param version
	 * @throws PageException
	 * @throws IOException
	 */
	public static void concat(PDFStruct[] docs, OutputStream os, boolean keepBookmark, boolean removePages, boolean stopOnError, char version)
			throws PageException, IOException {

		PDDocument resultDoc = new PDDocument();
		List<PDDocument> srcDocs = new ArrayList<>();
		PDDocumentOutline resultOutline = keepBookmark ? new PDDocumentOutline() : null;
		boolean hasBookmarks = false;

		try {
			int pageOffset = 0;

			for (int i = 0; i < docs.length; i++) {
				Set<Integer> pages = docs[i].getPages();
				PDDocument srcDoc;
				try {
					srcDoc = docs[i].toPDDocument();
				}
				catch (Throwable t) {
					if (t instanceof ThreadDeath) throw (ThreadDeath) t;
					if (!stopOnError) continue;
					throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(t);
				}
				srcDocs.add(srcDoc);

				int n = srcDoc.getNumberOfPages();

				// Build a mapping from old 1-based page number to new 0-based page index in resultDoc
				Map<Integer, Integer> pageMap = new HashMap<>();
				int pagesAdded = 0;

				for (int y = 0; y < n; y++) {
					int pageNum = y + 1; // 1-based
					if (pages != null && removePages == pages.contains(Integer.valueOf(pageNum))) {
						continue;
					}
					PDPage page = srcDoc.getPage(y);
					resultDoc.importPage(page);
					pageMap.put(pageNum, pageOffset + pagesAdded);
					pagesAdded++;
				}

				// Copy bookmarks from this source document
				if (keepBookmark) {
					PDDocumentOutline srcOutline = srcDoc.getDocumentCatalog().getDocumentOutline();
					if (srcOutline != null) {
						PDOutlineItem item = srcOutline.getFirstChild();
						while (item != null) {
							PDOutlineItem copy = copyOutlineItem( srcDoc, resultDoc, item, pageMap );
							if (copy != null) {
								resultOutline.addLast( copy );
								hasBookmarks = true;
							}
							item = item.getNextSibling();
						}
					}
				}

				pageOffset += pagesAdded;
			}

			// Set the merged outline on the result document
			if (keepBookmark && hasBookmarks) {
				resultDoc.getDocumentCatalog().setDocumentOutline( resultOutline );
			}

			// Set PDF version if specified
			if (version != 0) {
				resultDoc.setVersion(Float.parseFloat("1." + version));
			}

			resultDoc.save(os);
		}
		finally {
			for (PDDocument srcDoc : srcDocs) {
				try { srcDoc.close(); } catch (IOException e) { /* ignore */ }
			}
			resultDoc.close();
		}
	}

	/**
	 * Recursively copy an outline item, remapping page destinations using the given page map.
	 * Returns null if the bookmark's page was filtered out and it has no surviving children.
	 */
	private static PDOutlineItem copyOutlineItem( PDDocument srcDoc, PDDocument resultDoc, PDOutlineItem src, Map<Integer, Integer> pageMap ) throws IOException {
		// Determine the source page number (1-based)
		int srcPageNum = -1;
		try {
			if (src.getDestination() instanceof PDPageDestination) {
				PDPageDestination dest = (PDPageDestination) src.getDestination();
				PDPage page = dest.getPage();
				if (page != null) {
					srcPageNum = srcDoc.getPages().indexOf( page ) + 1;
				}
				else {
					srcPageNum = dest.getPageNumber() + 1;
				}
			}
		}
		catch (Exception e) {
			// ignore
		}

		// Check if this bookmark's page survived filtering
		Integer newPageIndex = srcPageNum > 0 ? pageMap.get( srcPageNum ) : null;

		// Recursively copy children
		List<PDOutlineItem> childCopies = new ArrayList<>();
		if (src.hasChildren()) {
			PDOutlineItem child = src.getFirstChild();
			while (child != null) {
				PDOutlineItem childCopy = copyOutlineItem( srcDoc, resultDoc, child, pageMap );
				if (childCopy != null) {
					childCopies.add( childCopy );
				}
				child = child.getNextSibling();
			}
		}

		// If this bookmark's page was filtered out and no children survived, skip it
		if (newPageIndex == null && childCopies.isEmpty()) {
			return null;
		}

		PDOutlineItem copy = new PDOutlineItem();
		copy.setTitle( src.getTitle() );

		// Set destination using actual PDPage reference from the result document
		if (newPageIndex != null) {
			PDPageFitDestination dest = new PDPageFitDestination();
			dest.setPage( resultDoc.getPage( newPageIndex ) );
			copy.setDestination( dest );
		}
		else if (!childCopies.isEmpty()) {
			// Parent bookmark whose page was filtered — point to first surviving child's page
			copy.setDestination( childCopies.get( 0 ).getDestination() );
		}

		for (PDOutlineItem childCopy : childCopies) {
			copy.addLast( childCopy );
		}

		return copy;
	}

	public static Set<Integer> parsePageDefinition(String strPages, int lastPageNumber) throws PageException {
		if (Util.isEmpty(strPages)) return null;
		HashSet<Integer> set = new HashSet<Integer>();
		parsePageDefinition(set, strPages, lastPageNumber);
		return set;
	}

	public static void parsePageDefinition(Set<Integer> pages, String strPages, int lastPageNumber) throws PageException {
		if (Util.isEmpty(strPages)) return;
		CFMLEngine engine = CFMLEngineFactory.getInstance();
		String[] arr = CFMLEngineFactory.getInstance().getListUtil().toStringArrayTrim(CFMLEngineFactory.getInstance().getListUtil().toArrayRemoveEmpty(strPages, ","));
		int index, from, to;
		String strFrom, strTo;
		for (int i = 0; i < arr.length; i++) {
			index = arr[i].indexOf('-');
			if (index == -1) pages.add(engine.getCastUtil().toInteger(arr[i].trim()));
			else {
				strFrom = arr[i].substring(0, index).trim();
				strTo = arr[i].substring(index + 1).trim();
				if (i == 0 && Util.isEmpty(strFrom, true)) from = 1;
				else from = engine.getCastUtil().toIntValue(strFrom);
				if (i == (arr.length - 1) && Util.isEmpty(strTo, true)) to = lastPageNumber;
				else to = engine.getCastUtil().toIntValue(strTo);

				for (int y = from; y <= to; y++) {
					pages.add(Integer.valueOf(y));
				}
			}
		}
	}

	public static void encrypt(PDFStruct doc, OutputStream os, String newUserPassword, String newOwnerPassword, int permissions, int encryption)
			throws PageException, IOException {
		if (Util.isEmpty(newOwnerPassword)) newOwnerPassword = newUserPassword;

		PDDocument pdDoc = doc.toPDDocument();
		try {
			if (encryption != ENCRYPT_NONE) {
				AccessPermission ap = new AccessPermission();
				// Map permissions to PDFBox AccessPermission
				if ((permissions & ALLOW_PRINTING) > 0) ap.setCanPrint(true);
				if ((permissions & ALLOW_MODIFY_CONTENTS) > 0) ap.setCanModify(true);
				if ((permissions & ALLOW_COPY) > 0) ap.setCanExtractContent(true);
				if ((permissions & ALLOW_MODIFY_ANNOTATIONS) > 0) ap.setCanModifyAnnotations(true);
				if ((permissions & ALLOW_FILL_IN) > 0) ap.setCanFillInForm(true);
				if ((permissions & ALLOW_SCREENREADERS) > 0) ap.setCanExtractForAccessibility(true);
				if ((permissions & ALLOW_ASSEMBLY) > 0) ap.setCanAssembleDocument(true);
				if ((permissions & ALLOW_DEGRADED_PRINTING) > 0) ap.setCanPrintFaithful(true);

				StandardProtectionPolicy spp = new StandardProtectionPolicy(
					newOwnerPassword != null ? newOwnerPassword : "",
					newUserPassword != null ? newUserPassword : "",
					ap
				);
				// Map encryption type to key length and algorithm
				int keyLength;
				boolean preferAES = false;
				switch (encryption) {
					case ENCRYPT_RC4_40:
						keyLength = 40;
						break;
					case ENCRYPT_AES_128:
						keyLength = 128;
						preferAES = true;
						break;
					case ENCRYPT_RC4_128:
					case ENCRYPT_RC4_128M:
					default:
						keyLength = 128;
						break;
				}
				spp.setEncryptionKeyLength(keyLength);
				spp.setPreferAES(preferAES);
				pdDoc.protect(spp);
			}
			else {
				// Remove encryption
				pdDoc.setAllSecurityToBeRemoved(true);
			}

			pdDoc.save(os);
		}
		finally {
			pdDoc.close();
		}
	}

	private static String escapeXml(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	public static Map<String, String> generateGoToBookMark(String title, int page) {
		return generateGoToBookMark(title, page, 0, 731);
	}

	public static Map<String, String> generateGoToBookMark(String title, int page, int x, int y) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("Title", title);
		map.put("Action", "GoTo");
		map.put("Page", page + " XYZ " + x + " " + y + " null");

		return map;
	}

	public static void setChildBookmarks(Map parent, List children) {
		Object kids = parent.get("Kids");
		if (kids instanceof List) {
			((List) kids).addAll(children);
		}
		else parent.put("Kids", children);
	}

	/**
	 * Convert a value to a PDFStruct.
	 */
	public static PDFStruct toPDFStruct(PageContext pc, Object value, String password) throws IOException, PageException {
		if (value instanceof PDFStruct) return (PDFStruct) value;
		CFMLEngine engine = CFMLEngineFactory.getInstance();
		if (engine.getDecisionUtil().isBinary(value)) {
			return new PDFStruct(engine.getCastUtil().toBinary(value), password);
		}
		if (value instanceof Resource) {
			return new PDFStruct((Resource) value, password);
		}
		if (value instanceof String) {
			Resource res = engine.getResourceUtil().toResourceExisting(pc, (String) value);
			return new PDFStruct(res, password);
		}
		throw engine.getExceptionUtil().createCasterException(value, PDFStruct.class);
	}

	public static byte[] toBytes(Resource res) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CFMLEngineFactory.getInstance().getIOUtil().copy(res.getInputStream(), baos, true, true);
		return baos.toByteArray();
	}

	public static BufferedImage toImage(PDFStruct doc) throws PageException, IOException {
		try (PDDocument pdDoc = doc.toPDDocument()) {
			return new PDFRenderer(pdDoc).renderImage(0, 1);
		}
	}

	public static Object extractText(PDFStruct doc, Set<Integer> pageNumbers, int type, Resource destination) throws IOException, InvalidPasswordException {
		try (PDDocument pdDoc = doc.toPDDocument()) {
			CFMLEngine engine = CFMLEngineFactory.getInstance();
			int n = pdDoc.getNumberOfPages();
			Iterator<Integer> it = pageNumbers.iterator();
			int p;
			StringBuilder sb = new StringBuilder();
			PDFTextStripper stripper = new PDFTextStripper();
			if (destination != null) stripper.setLineSeparator(" ");

			if (type == PDF.TYPE_XML) {
				sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				sb.append("<DocText>");
				sb.append("<TextPerPage>");
			}

			while (it.hasNext()) {
				try (PDDocument document = new PDDocument()) {
					p = it.next();
					if (type == PDF.TYPE_XML) sb.append("<page pagenumber=" + "\"" + p + "\" " + ">");
					if (p > n) throw new RuntimeException("pdf page size [" + p + "] out of range, maximum page size is [" + n + "]");
					document.addPage(pdDoc.getDocumentCatalog().getPages().get(p - 1));
					stripper.setSortByPosition(true);
					String text = stripper.getText(document);
					if (type == PDF.TYPE_XML) text = escapeXml(text);
					sb.append(text);
					if (type == PDF.TYPE_XML) sb.append("</page>");
				}
			}
			if (type == PDF.TYPE_XML) {
				sb.append("</TextPerPage>");
				sb.append("</DocText>");
			}

			if (destination != null) engine.getIOUtil().copy(new ByteArrayInputStream(sb.toString().getBytes("UTF-8")), destination, true);
			return sb.toString();
		}
	}

	public static void thumbnail(PageContext pc, PDFStruct doc, String destination, Set<Integer> pageNumbers, String format, String imagePrefix, int scale, boolean overwrite) throws IOException {
		CFMLEngine engine = CFMLEngineFactory.getInstance();

		try (PDDocument pdDoc = doc.toPDDocument()) {
			int n = pdDoc.getNumberOfPages();
			Iterator<Integer> it = pageNumbers.iterator();
			int p;

			PDFRenderer pdfRender = new PDFRenderer(pdDoc);

			while (it.hasNext()) {
				p = it.next();

				if (p > n) throw new RuntimeException("pdf page size [" + p + "] out of range, maximum page size is [" + n + "]");

				// thumbnail image file destination
				String imageDestination = destination + "/" + imagePrefix + "_page_" + p + "." + format;

				// Map scale (1-100) to DPI: scale 100 = 300 DPI, scale 25 = 75 DPI
				int dpi = Math.max(1, scale * 3);
				BufferedImage thumbnailImage = pdfRender.renderImageWithDPI(p - 1, dpi);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(thumbnailImage, format, baos); // this one not support .tiff format
				Resource res = engine.getResourceUtil().toResourceNotExisting(pc, imageDestination);
				if (res.exists() && !overwrite) throw new RuntimeException("Thumbnail image file already exists [" + imageDestination + "] and overwrite was false");
				engine.getIOUtil().copy(new ByteArrayInputStream(baos.toByteArray()), res, true);
			}
		}
	}

	public static void extractImages(PageContext pc, PDFStruct doc, Set<Integer> pageNumbers, Resource destination, String imagePrefix, String format, boolean overwrite) throws IOException, InvalidPasswordException, PageException {
		try (PDDocument pdDoc = doc.toPDDocument()) {
			int n = pdDoc.getNumberOfPages();
			Iterator<Integer> it = pageNumbers.iterator();
			int p;
			PDPageTree pages = pdDoc.getPages();
			int[] counter = {1}; // Use array to allow modification in recursive calls
			while (it.hasNext()) {
				p = it.next();
				if (p > n) throw new RuntimeException("pdf page size [" + p + "] out of range, maximum page size is [" + n + "]");
				PDResources pdResources = pages.get(p - 1).getResources();
				extractImagesFromResources(pc, pdResources, destination, imagePrefix, format, overwrite, counter);
			}
		}
	}

	private static void extractImagesFromResources(PageContext pc, PDResources pdResources, Resource destination, String imagePrefix, String format, boolean overwrite, int[] counter) throws IOException, PageException {
		if (pdResources == null) return;

		// Iterate through XObjects in document order
		for (COSName name : pdResources.getXObjectNames()) {
			PDXObject o = pdResources.getXObject(name);

			if (o instanceof PDImageXObject) {
				PDImageXObject image = (PDImageXObject) o;
				String filename = destination + "/" + imagePrefix + "-" + counter[0] + "." + format;
				CFMLEngine engine = CFMLEngineFactory.getInstance();
				Resource res = engine.getResourceUtil().toResourceNotExisting(pc, filename);
				if (res.exists() && !overwrite) throw new RuntimeException("image file already exists [" + filename + "] and overwrite was false");
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(image.getImage(), format, baos);
				CFMLEngineFactory.getInstance().getIOUtil().copy(new ByteArrayInputStream(baos.toByteArray()), res.getOutputStream(), true, true);
				counter[0]++;
			}
			else if (o instanceof PDFormXObject) {
				// Recursively extract images from FormXObjects (which may contain nested images)
				PDFormXObject formXObject = (PDFormXObject) o;
				extractImagesFromResources(pc, formXObject.getResources(), destination, imagePrefix, format, overwrite, counter);
			}
		}
	}

	/**
	 * Extract bookmarks from a PDF document using PDFBox.
	 */
	public static Object extractBookmarks(PageContext pc, PDFStruct doc) throws IOException, PageException {
		Array bookmarks = CFMLEngineFactory.getInstance().getCreationUtil().createArray();
		try (PDDocument pdDoc = doc.toPDDocument()) {
			PDDocumentOutline outline = pdDoc.getDocumentCatalog().getDocumentOutline();
			if (outline != null) {
				PDOutlineItem item = outline.getFirstChild();
				extractBookmarksRecursive(pdDoc, item, bookmarks);
			}
		}
		return bookmarks;
	}

	private static void extractBookmarksRecursive(PDDocument pdDoc, PDOutlineItem item, Array bookmarks) throws PageException {
		while (item != null) {
			Struct sct = CFMLEngineFactory.getInstance().getCreationUtil().createStruct();
			sct.set("Title", item.getTitle() != null ? item.getTitle() : "");

			// Try to get page number from destination
			int pageNum = -1;
			try {
				if (item.getDestination() instanceof PDPageDestination) {
					PDPageDestination dest = (PDPageDestination) item.getDestination();
					PDPage page = dest.getPage();
					if (page != null) {
						pageNum = pdDoc.getPages().indexOf(page) + 1;
					}
					else {
						pageNum = dest.getPageNumber() + 1;
					}
				}
			}
			catch (Exception e) {
				// ignore
			}

			sct.setEL("PageNumber", pageNum > 0 ? pageNum : 1);
			sct.setEL("Page", pageNum + " FitH 0");
			sct.setEL("Action", "GoTo");
			bookmarks.appendEL(sct);

			// Process children
			if (item.hasChildren()) {
				extractBookmarksRecursive(pdDoc, item.getFirstChild(), bookmarks);
			}

			item = item.getNextSibling();
		}
	}

}
