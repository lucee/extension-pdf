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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.action.PDDocumentCatalogAdditionalActions;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSBase;
import org.lucee.extension.pdf.PDFStruct;
import org.lucee.extension.pdf.util.PDFUtil;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.loader.util.Util;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Cast;
import lucee.runtime.util.ClassUtil;
import lucee.runtime.util.Strings;

public class PDF extends BodyTagImpl {

	private static final int ACTION_ADD_WATERMARK = 0;
	private static final int ACTION_DELETE_PAGES = 1;
	private static final int ACTION_GET_INFO = 2;
	private static final int ACTION_MERGE = 3;
	private static final int ACTION_PROCESSDDX = 4;
	private static final int ACTION_PROTECT = 5;
	private static final int ACTION_READ = 6;
	private static final int ACTION_REMOVE_WATERMARK = 7;
	private static final int ACTION_SET_INFO = 8;
	private static final int ACTION_THUMBNAIL = 9;
	private static final int ACTION_WRITE = 10;
	private static final int ACTION_EXTRACT_TEXT = 11;

	private static final int ACTION_ADD_HEADER = 12;
	private static final int ACTION_ADD_FOOTER = 13;
	private static final int ACTION_OPEN = 14;
	private static final int ACTION_EXTRACT_IMAGES = 15;
	private static final int ACTION_EXTRACT_BOOKMARKS = 16;
	private static final int ACTION_TRANSFORM = 17;
	private static final int ACTION_ADD_ATTACHMENTS = 18;
	private static final int ACTION_EXTRACT_ATTACHMENTS = 19;
	private static final int ACTION_READ_SIGNATURE_FIELDS = 20;
	private static final int ACTION_REMOVE_ATTACHMENTS = 21;
	private static final int ACTION_VALIDATE_SIGNATURE = 22;
	private static final int ACTION_OPTIMIZE = 23;
	private static final int ACTION_SANITIZE = 24;
	private static final int ACTION_ADD_STAMP = 25;

	private static final String FORMAT_JPG = "jpg";
	private static final String FORMAT_TIFF = "tiff";
	private static final String FORMAT_PNG = "png";

	private static final int ORDER_TIME = 0;
	private static final int ORDER_NAME = 1;

	private static final int RESOLUTION_HIGH = 0;
	private static final int RESOLUTION_LOW = 1;

	private static final int SAVE_OPTION_FULL = 0;
	private static final int SAVE_OPTION_INCREMENTAL = 1;
	private static final int SAVE_OPTION_LINEAR = 2;

	public static final int TYPE_STRING = 1;
	public static final int TYPE_XML = 2;

	private static final int NUMBERFORMAT_LOWERCASEROMAN = 1;
	private static final int NUMBERFORMAT_NUMERIC = 2;
	private static final int NUMBERFORMAT_UPPERCASEROMAN = 3;

	private static final int UNDEFINED = Integer.MIN_VALUE;

	// Alignment constants (matching iText Element values for backward compatibility)
	private static final int ALIGN_LEFT = 0;
	private static final int ALIGN_CENTER = 1;
	private static final int ALIGN_RIGHT = 2;

	private int action = ACTION_PROCESSDDX;
	private boolean ascending = false;
	private Object copyFrom = null;
	private String ddxFile = null;
	private Resource destination = null;
	private Resource directory = null;
	private int encrypt = PDFUtil.ENCRYPT_RC4_128;
	private boolean flatten = false;
	private boolean foreground = false;
	private String format = FORMAT_JPG;
	private Object image = null;
	private Struct info = null;
	private Struct inputFiles = null;
	private Struct outputFiles = null;
	private boolean isBase64 = false;
	private boolean keepBookmark = false;
	private String name = null;
	private String newOwnerPassword = null;
	private String newUserPassword = null;
	private float opacity = 0.3F;
	private int order = ORDER_TIME;
	private boolean overwrite = false;
	private String pages = null;
	private String password = null;
	private int permissions = 0;
	private String position = null;
	private int resolution = RESOLUTION_HIGH;
	private float rotation = 0;
	private int saveOption = SAVE_OPTION_FULL;
	private int scale = 25;
	private boolean showOnPrint = false;
	private Object source = null;
	private boolean stopOnError = false;
	private boolean transparent = false;
	private float version = 0;
	private java.util.List<PDFParamBean> params;
	private ResourceFilter filter = null;
	private String imagePrefix = null;
	private int type = TYPE_XML;
	private String text;
	private int numberformat = NUMBERFORMAT_NUMERIC;
	private int align = ALIGN_CENTER;
	private float leftmargin = 1;
	private float rightmargin = 1;
	private float topmargin = 0.5f;
	private float bottommargin = 0.5f;
	private Struct fontStruct = null;
	private float hscale = 1.0f;
	private float vscale = 1.0f;
	// optimize/sanitize options
	private boolean noBookmarks = false;
	private boolean noLinks = false;
	private boolean noJavaScript = false;
	private boolean noAttachments = false;
	private boolean noMetadata = false;
	private boolean noThumbnails = false;
	private boolean noComments = false;
	private boolean noForms = false;

	@Override
	public void release() {
		super.release();
		action = ACTION_PROCESSDDX;
		ascending = false;
		copyFrom = null;
		ddxFile = null;
		destination = null;
		directory = null;
		encrypt = PDFUtil.ENCRYPT_RC4_128;
		flatten = false;
		foreground = false;
		format = FORMAT_JPG;
		image = null;
		info = null;
		inputFiles = null;
		outputFiles = null;
		isBase64 = false;
		keepBookmark = false;
		name = null;
		newOwnerPassword = null;
		newUserPassword = null;
		opacity = 0.3F;
		order = ORDER_TIME;
		overwrite = false;
		pages = null;
		password = null;
		permissions = 0;
		position = null;
		resolution = RESOLUTION_HIGH;
		rotation = 0;
		saveOption = SAVE_OPTION_FULL;
		scale = 25;
		showOnPrint = false;
		source = null;
		stopOnError = false;
		transparent = false;
		version = 0;
		params = null;
		filter = null;
		imagePrefix = null;
		type = TYPE_XML;
		text = null;
		numberformat = NUMBERFORMAT_NUMERIC;
		align = ALIGN_CENTER;
		leftmargin = 1;
		rightmargin = 1;
		topmargin = 0.5f;
		bottommargin = 0.5f;
		fontStruct = null;
		hscale = 1.0f;
		vscale = 1.0f;
		noBookmarks = false;
		noLinks = false;
		noJavaScript = false;
		noAttachments = false;
		noMetadata = false;
		noThumbnails = false;
		noComments = false;
		noForms = false;
	}

	public void setImageprefix(String imagePrefix) {
		this.imagePrefix = imagePrefix;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setFont(Struct font) throws PageException {
		this.fontStruct = font;
	}

	public void setNumberformat(String numberformat) throws PageException {
		if (Util.isEmpty(numberformat, true)) return;
		numberformat = numberformat.trim().toLowerCase();

		if ("numeric".equals(numberformat)) this.numberformat = NUMBERFORMAT_NUMERIC;
		else if ("number".equals(numberformat)) this.numberformat = NUMBERFORMAT_NUMERIC;
		else if ("lowercase-roman".equals(numberformat)) this.numberformat = NUMBERFORMAT_LOWERCASEROMAN;
		else if ("lowercaseroman".equals(numberformat)) this.numberformat = NUMBERFORMAT_LOWERCASEROMAN;
		else if ("uppercase-roman".equals(numberformat)) this.numberformat = NUMBERFORMAT_UPPERCASEROMAN;
		else if ("uppercaseroman".equals(numberformat)) this.numberformat = NUMBERFORMAT_UPPERCASEROMAN;

		else throw engine.getExceptionUtil()
				.createApplicationException("Invalid numberformat [" + numberformat + "], supported numberformats are " + "[numeric, lowercaseroman, uppercaseroman] (aliases: number, lowercase-roman, uppercase-roman)");
	}

	public void setAlign(String align) throws PageException {
		if (Util.isEmpty(align, true)) return;
		align = align.trim().toLowerCase();

		if ("center".equals(align)) this.align = ALIGN_CENTER;
		else if ("left".equals(align)) this.align = ALIGN_LEFT;
		else if ("right".equals(align)) this.align = ALIGN_RIGHT;

		else throw engine.getExceptionUtil().createApplicationException("Invalid PDF [align] value [" + align + "], supported align values are [center, left, right]");
	}

	public void setLeftmargin(double leftmargin) throws PageException {
		this.leftmargin = (float) leftmargin;
	}

	public void setRightmargin(double rightmargin) throws PageException {
		this.rightmargin = (float) rightmargin;
	}

	public void setTopmargin(double topmargin) throws PageException {
		this.topmargin = (float) topmargin;
	}

	public void setBottommargin(double bottommargin) throws PageException {
		this.bottommargin = (float) bottommargin;
	}

	public void setAction(String strAction) throws PageException {
		strAction = Document.trimAndLower(strAction).replace("-", "").replace("_", "");
		if ("addwatermark".equals(strAction)) action = ACTION_ADD_WATERMARK;
		else if ("deletepages".equals(strAction)) action = ACTION_DELETE_PAGES;
		else if ("deletepage".equals(strAction)) action = ACTION_DELETE_PAGES;
		else if ("getinfo".equals(strAction)) action = ACTION_GET_INFO;
		else if ("merge".equals(strAction)) action = ACTION_MERGE;
		else if ("open".equals(strAction)) action = ACTION_OPEN;
		else if ("removepassword".equals(strAction)) action = ACTION_OPEN;
		else if ("protect".equals(strAction)) action = ACTION_PROTECT;
		else if ("read".equals(strAction)) action = ACTION_READ;
		else if ("removewatermark".equals(strAction)) action = ACTION_REMOVE_WATERMARK;
		else if ("setinfo".equals(strAction)) action = ACTION_SET_INFO;
		else if ("thumbnail".equals(strAction)) action = ACTION_THUMBNAIL;
		else if ("write".equals(strAction)) action = ACTION_WRITE;
		else if ("extracttext".equals(strAction)) action = ACTION_EXTRACT_TEXT;
		else if ("addheader".equals(strAction)) action = ACTION_ADD_HEADER;
		else if ("addfooter".equals(strAction)) action = ACTION_ADD_FOOTER;
		else if ("extractimage".equals(strAction)) action = ACTION_EXTRACT_IMAGES;
		else if ("extractimages".equals(strAction)) action = ACTION_EXTRACT_IMAGES;
		else if ("extractbookmarks".equals(strAction)) action = ACTION_EXTRACT_BOOKMARKS;
		else if ("transform".equals(strAction)) action = ACTION_TRANSFORM;
		else if ("addattachments".equals(strAction)) action = ACTION_ADD_ATTACHMENTS;
		else if ("extractattachments".equals(strAction)) action = ACTION_EXTRACT_ATTACHMENTS;
		else if ("removeattachments".equals(strAction)) action = ACTION_REMOVE_ATTACHMENTS;
		else if ("readsignaturefields".equals(strAction)) action = ACTION_READ_SIGNATURE_FIELDS;
		else if ("validatesignature".equals(strAction)) action = ACTION_VALIDATE_SIGNATURE;
		else if ("optimize".equals(strAction)) action = ACTION_OPTIMIZE;
		else if ("sanitize".equals(strAction)) action = ACTION_SANITIZE;
		else if ("addstamp".equals(strAction)) action = ACTION_ADD_STAMP;

		else throw engine.getExceptionUtil().createApplicationException(
				"Invalid PDF action [" + strAction + "], supported actions are " + "[addAttachments, addHeader, addFooter, addStamp, addWatermark, deletePages, extractAttachments, extractBookmarks, extractImage, extractText, getInfo, merge, open, optimize, "
						+ "readSignatureFields, removeAttachments, removePassword, protect, read, removeWatermark, sanitize, setInfo, thumbnail, transform, validateSignature, write]");
	}

	public void setType(String strType) throws PageException {
		strType = Document.trimAndLower(strType);
		if ("string".equals(strType)) type = TYPE_STRING;
		else if ("text".equals(strType)) type = TYPE_STRING;
		else if ("plain".equals(strType)) type = TYPE_STRING;
		else if ("xml".equals(strType)) type = TYPE_XML;

		else throw engine.getExceptionUtil().createApplicationException("Invalid type [" + strType + "], supported types are " + "[string, text, plain, xml]");
	}

	public void setFilter(String pattern) throws PageException {
		if (Util.isEmpty(pattern)) return;
		// Convert glob to regex safely: split on * and ?, quote everything else
		StringBuilder regex = new StringBuilder("(?i)");
		for (int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			if (c == '*') regex.append(".*");
			else if (c == '?') regex.append(".");
			else regex.append(java.util.regex.Pattern.quote(String.valueOf(c)));
		}
		final String re = regex.toString();
		filter = res -> res.getName().matches(re);
	}

	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

	public void setCopyfrom(Object copyFrom) throws PageException {
		this.copyFrom = copyFrom;
	}

	public void setDdxfile(String ddxFile) {
		this.ddxFile = ddxFile;
	}

	public void setDestination(String destination) throws PageException {
		if (engine.getStringUtil().isEmpty(destination, true))
			throw engine.getExceptionUtil().createApplicationException("Attribute [destination] has an invalid value [" + destination + "], it cannot be empty value");
		this.destination = engine.getResourceUtil().toResourceNotExisting(pageContext, destination);
	}

	public void setDirectory(String directory) throws PageException {
		this.directory = engine.getResourceUtil().toResourceExisting(pageContext, directory);
	}

	public void setEncrypt(String strEncrypt) throws PageException {
		strEncrypt = Document.trimAndLower(strEncrypt);
		if ("aes128".equals(strEncrypt)) encrypt = PDFUtil.ENCRYPT_AES_128;
		else if ("aes-128".equals(strEncrypt)) encrypt = PDFUtil.ENCRYPT_AES_128;
		else if ("aes_128".equals(strEncrypt)) encrypt = PDFUtil.ENCRYPT_AES_128;
		else if ("none".equals(strEncrypt)) encrypt = PDFUtil.ENCRYPT_NONE;
		else if ("".equals(strEncrypt)) encrypt = PDFUtil.ENCRYPT_NONE;
		else if ("rc4128".equals(strEncrypt)) encrypt = PDFUtil.ENCRYPT_RC4_128;
		else if ("rc4-128".equals(strEncrypt)) encrypt = PDFUtil.ENCRYPT_RC4_128;
		else if ("rc4_128".equals(strEncrypt)) encrypt = PDFUtil.ENCRYPT_RC4_128;
		else if ("rc4128m".equals(strEncrypt)) encrypt = PDFUtil.ENCRYPT_RC4_128M;
		else if ("rc4-128m".equals(strEncrypt)) encrypt = PDFUtil.ENCRYPT_RC4_128M;
		else if ("rc4_128m".equals(strEncrypt)) encrypt = PDFUtil.ENCRYPT_RC4_128M;
		else if ("rc440".equals(strEncrypt)) encrypt = PDFUtil.ENCRYPT_RC4_40;
		else if ("rc4-40".equals(strEncrypt)) encrypt = PDFUtil.ENCRYPT_RC4_40;
		else if ("rc4_40".equals(strEncrypt)) encrypt = PDFUtil.ENCRYPT_RC4_40;

		else throw engine.getExceptionUtil()
				.createApplicationException("Invalid PDF encrypt [" + strEncrypt + "], supported [encrypt] types are " + "[aes_128, none, rc4_128, rc4_128m, rc4_40]");
	}

	public void setFlatten(boolean flatten) {
		this.flatten = flatten;
	}

	public void setForeground(boolean foreground) {
		this.foreground = foreground;
	}

	public void setFormat(String strFormat) throws PageException {
		strFormat = Document.trimAndLower(strFormat);
		if ("jpg".equals(strFormat)) format = FORMAT_JPG;
		else if ("jpeg".equals(strFormat)) format = FORMAT_JPG;
		else if ("jpe".equals(strFormat)) format = FORMAT_JPG;
		else if ("tiff".equals(strFormat)) format = FORMAT_TIFF;
		else if ("tif".equals(strFormat)) format = FORMAT_TIFF;
		else if ("png".equals(strFormat)) format = FORMAT_PNG;

		else throw engine.getExceptionUtil().createApplicationException("Invalid format [" + strFormat + "], supported formats " + "[jpg, tiff, png]");
	}

	public void setImage(Object image) {
		this.image = image;
	}

	public void setPrefix(String prefix) {
		this.imagePrefix = prefix;
	}

	public void setInfo(Struct info) {
		this.info = info;
	}

	public void setInputfiles(Struct inputFiles) {
		this.inputFiles = inputFiles;
	}

	public void setOutputfiles(Struct outputFiles) {
		this.outputFiles = outputFiles;
	}

	public void setIsbase64(boolean isBase64) {
		this.isBase64 = isBase64;
	}

	public void setKeepbookmark(boolean keepBookmark) {
		this.keepBookmark = keepBookmark;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNewownerpassword(String newOwnerPassword) {
		this.newOwnerPassword = newOwnerPassword;
	}

	public void setNewuserpassword(String newUserPassword) {
		this.newUserPassword = newUserPassword;
	}

	public void setOpacity(double opacity) throws PageException {
		if (opacity < 0 || opacity > 10)
			throw engine.getExceptionUtil()
					.createApplicationException("Invalid PDF opacity definition [" + engine.getCastUtil().toString(opacity) + "], value should be in range from 0 to 10");
		this.opacity = (float) (opacity / 10);
	}

	public void setOrder(String strOrder) throws PageException {
		strOrder = Document.trimAndLower(strOrder);
		if ("name".equals(strOrder)) order = ORDER_NAME;
		else if ("time".equals(strOrder)) order = ORDER_TIME;

		else throw engine.getExceptionUtil().createApplicationException("Invalid order [" + strOrder + "], supported order definitions are " + "[name, time]");
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public void setPages(String pages) {
		this.pages = pages;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPermissions(String strPermissions) throws PageException {
		permissions = PDFUtil.toPermissions(strPermissions);
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public void setResolution(String strResolution) throws PageException {
		strResolution = Document.trimAndLower(strResolution);
		if ("low".equals(strResolution)) resolution = RESOLUTION_LOW;
		else if ("high".equals(strResolution)) resolution = RESOLUTION_HIGH;

		else throw engine.getExceptionUtil().createApplicationException("Invalid PDF resolution [" + strResolution + "], supported resolutions are " + "[low, high]");
	}

	public void setRotation(double rotation) {
		rotation = rotation % 360D;
		this.rotation = (float) rotation;
	}

	public void setSaveoption(String strSaveOption) throws PageException {
		strSaveOption = Document.trimAndLower(strSaveOption);
		if ("full".equals(strSaveOption)) saveOption = SAVE_OPTION_FULL;
		else if ("incremental".equals(strSaveOption)) saveOption = SAVE_OPTION_INCREMENTAL;
		else if ("linear".equals(strSaveOption)) saveOption = SAVE_OPTION_LINEAR;

		else throw engine.getExceptionUtil()
				.createApplicationException("Invalid PDF saveOption [" + strSaveOption + "], supported saveOptions are " + "[full, linear, incremental]");
	}

	public void setScale(double scale) throws PageException {
		this.scale = (int) scale;
	}

	public void setShowonprint(boolean showOnPrint) {
		this.showOnPrint = showOnPrint;
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public void setStoponerror(boolean stopOnError) {
		this.stopOnError = stopOnError;
	}

	public void setTransparent(boolean transparent) {
		this.transparent = transparent;
	}

	public void setVersion(double version) throws PageException {
		if (version >= 1.0 && version <= 1.7) {
			this.version = (float) version;
		}
		else {
			throw engine.getExceptionUtil().createApplicationException(
					"Invalid PDF [version] specified [" + engine.getCastUtil().toString(version) + "], supported versions are " + "[1.0 to 1.7]");
		}
	}

	public void setHscale(double hscale) {
		this.hscale = (float) hscale;
	}

	public void setVscale(double vscale) {
		this.vscale = (float) vscale;
	}

	// Optimize/sanitize option setters
	public void setNobookmarks(boolean noBookmarks) {
		this.noBookmarks = noBookmarks;
	}

	public void setNolinks(boolean noLinks) {
		this.noLinks = noLinks;
	}

	public void setNojavascript(boolean noJavaScript) {
		this.noJavaScript = noJavaScript;
	}

	public void setNoattachments(boolean noAttachments) {
		this.noAttachments = noAttachments;
	}

	public void setNometadata(boolean noMetadata) {
		this.noMetadata = noMetadata;
	}

	public void setNothumbnails(boolean noThumbnails) {
		this.noThumbnails = noThumbnails;
	}

	public void setNocomments(boolean noComments) {
		this.noComments = noComments;
	}

	public void setNoforms(boolean noForms) {
		this.noForms = noForms;
	}

	@Override
	public int doStartTag() throws PageException {
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public void doInitBody() {
	}

	@Override
	public int doAfterBody() {
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws PageException {
		try {
			if (ACTION_ADD_WATERMARK == action) doActionAddWatermark();
			else if (ACTION_ADD_HEADER == action) doActionAddHeaderFooter(true);
			else if (ACTION_ADD_FOOTER == action) doActionAddHeaderFooter(false);
			else if (ACTION_REMOVE_WATERMARK == action) doActionRemoveWatermark();
			else if (ACTION_READ == action) doActionRead();
			else if (ACTION_WRITE == action) doActionWrite();
			else if (ACTION_GET_INFO == action) doActionGetInfo();
			else if (ACTION_SET_INFO == action) doActionSetInfo();
			else if (ACTION_MERGE == action) doActionMerge();
			else if (ACTION_DELETE_PAGES == action) doActionDeletePages();
			else if (ACTION_PROTECT == action) doActionProtect(true);
			else if (ACTION_OPEN == action) doActionProtect(false);
			else if (ACTION_THUMBNAIL == action) doActionThumbnail();
			else if (ACTION_EXTRACT_IMAGES == action) doActionExtractImages();
			else if (ACTION_EXTRACT_TEXT == action) doActionExtractText();
			else if (ACTION_EXTRACT_BOOKMARKS == action) doActionExtractBookmarks();
			else if (ACTION_TRANSFORM == action) doActionTransform();
			else if (ACTION_ADD_ATTACHMENTS == action) doActionAddAttachments();
			else if (ACTION_EXTRACT_ATTACHMENTS == action) doActionExtractAttachments();
			else if (ACTION_REMOVE_ATTACHMENTS == action) doActionRemoveAttachments();
			else if (ACTION_READ_SIGNATURE_FIELDS == action) doActionReadSignatureFields();
			else if (ACTION_VALIDATE_SIGNATURE == action) doActionValidateSignature();
			else if (ACTION_OPTIMIZE == action) doActionOptimize();
			else if (ACTION_SANITIZE == action) doActionSanitize();
			else if (ACTION_ADD_STAMP == action) doActionAddStamp();
			else throw engine.getExceptionUtil().createApplicationException(
				"attribute [action] is required for tag [cfpdf]");
		}
		catch (Exception e) {
			throw engine.getCastUtil().toPageException(e);
		}
		return EVAL_PAGE;
	}

	// Helper to create output stream based on destination/source configuration
	private OutputStream createOutputStream( PDFStruct doc, boolean needsBytes ) throws PageException, IOException {
		boolean destIsSource = destination != null && doc.getResource() != null && destination.equals( doc.getResource() );
		if ( needsBytes || destIsSource || destination == null ) {
			return new ByteArrayOutputStream();
		}
		return destination.getOutputStream();
	}

	// Helper to finalize output - handles copy-back and sets variable as PDFStruct if varName provided
	private byte[] finalizeOutput( OutputStream os, PDFStruct doc, String varName ) throws PageException, IOException {
		Util.closeEL( os );
		if ( os instanceof ByteArrayOutputStream ) {
			byte[] bytes = ( (ByteArrayOutputStream) os ).toByteArray();
			if ( destination != null )
				engine.getIOUtil().copy( new ByteArrayInputStream( bytes ), destination, true );
			else if ( doc.getResource() != null )
				engine.getIOUtil().copy( new ByteArrayInputStream( bytes ), doc.getResource(), true );
			if ( !Util.isEmpty( varName ) )
				pageContext.setVariable( varName, new PDFStruct( bytes, password ) );
			return bytes;
		}
		return null;
	}

	private void doActionWrite() throws PageException, IOException {
		required("pdf", "write", "source", source);
		required("pdf", "write", "destination", destination);

		if (destination != null && destination.exists() && !overwrite)
			throw engine.getExceptionUtil().createApplicationException("Destination PDF file [" + destination + "] already exists");

		PDFStruct doc = toPDFDocument(source, password, null);
		boolean destIsSource = doc.getResource() != null && destination.equals(doc.getResource());

		OutputStream os = null;
		if (destIsSource) {
			os = new ByteArrayOutputStream();
		}
		else if (destination != null) {
			os = destination.getOutputStream();
		}

		try {
			PDFUtil.concat(new PDFStruct[] { doc }, os, true, true, true, version != 0 ? (char) ('0' + (int) ((version - 1) * 10)) : (char) 0);
		}
		finally {
			Util.closeEL(os);
			if (os instanceof ByteArrayOutputStream) {
				if (destination != null)
					engine.getIOUtil().copy(new ByteArrayInputStream(((ByteArrayOutputStream) os).toByteArray()), destination, true);
			}
		}
		this.overwrite = true;
		this.info = doc.getInfo();
		doActionSetInfo();
	}

	private void doActionAddHeaderFooter(boolean isHeader) throws PageException, IOException {
		required("pdf", "write", "source", source);
		if (text == null)
			throw engine.getExceptionUtil().createApplicationException("when PDF action is [addHeader or addFooter], It requires a attribute [text]");

		if (destination != null && destination.exists() && !overwrite)
			throw engine.getExceptionUtil().createApplicationException("Destination PDF file [" + destination + "] already exists");

		PDFStruct doc = toPDFDocument(source, password, null);
		BIF bif = null;
		if (NUMBERFORMAT_NUMERIC != numberformat) {
			ClassUtil classUtil = engine.getClassUtil();
			try {
				bif = classUtil.loadBIF(pageContext, "lucee.runtime.functions.displayFormatting.NumberFormat");
			}
			catch (Exception e) {
				throw engine.getCastUtil().toPageException(e);
			}
		}

		OutputStream os = createOutputStream( doc, !Util.isEmpty( name ) );
		try (PDDocument pdDoc = doc.toPDDocument()) {
			int len = pdDoc.getNumberOfPages();
			Set<Integer> pageSet = PDFUtil.parsePageDefinition(pages, len);

			// Get font settings
			float fontSize = 10f;
			if (fontStruct != null) {
				Object sizeObj = fontStruct.get("size", null);
				if (sizeObj != null) {
					fontSize = engine.getCastUtil().toFloatValue(sizeObj, 10f);
				}
			}
			PDFont font = new PDType1Font(Standard14Fonts.FontName.COURIER);

			for (int p = 0; p < len; p++) {
				if (pageSet != null && !pageSet.contains(p + 1)) continue;

				PDPage page = pdDoc.getPage(p);
				PDRectangle pageSize = page.getMediaBox();
				String textContent = processText(text, p + 1, len, numberformat, bif);

				try (PDPageContentStream cs = new PDPageContentStream(pdDoc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
					cs.beginText();
					cs.setFont(font, fontSize);

					float y;
					if (isHeader) {
						y = pageSize.getHeight() - topmargin * 72 - fontSize;
					}
					else {
						y = bottommargin * 72;
					}

					float textWidth = font.getStringWidth(textContent) / 1000 * fontSize;
					float x;
					if (align == ALIGN_LEFT) {
						x = leftmargin * 72;
					}
					else if (align == ALIGN_RIGHT) {
						x = pageSize.getWidth() - rightmargin * 72 - textWidth;
					}
					else {
						x = (pageSize.getWidth() - textWidth) / 2;
					}

					cs.newLineAtOffset(x, y);
					cs.showText(textContent);
					cs.endText();
				}
			}

			pdDoc.save(os);
		}
		finally {
			finalizeOutput( os, doc, name );
		}
	}

	private String processText(String text, int page, int lastPage, int numberformat, BIF bif) throws PageException {
		String strPage;
		String strLastPage;

		if (NUMBERFORMAT_NUMERIC == numberformat) {
			strPage = page + "";
			strLastPage = lastPage + "";
		}
		else {
			strPage = (String) bif.invoke(pageContext, new Object[] { page + "", "roman" });
			strLastPage = (String) bif.invoke(pageContext, new Object[] { lastPage + "", "roman" });
			if (NUMBERFORMAT_LOWERCASEROMAN == numberformat) {
				strPage = strPage.toLowerCase();
				strLastPage = strLastPage.toLowerCase();
			}
		}

		Strings util = engine.getStringUtil();
		text = util.replace(text, "_PAGENUMBER", strPage, false, true);
		text = util.replace(text, "_LASTPAGENUMBER", strLastPage, false, true);
		return suppressWhiteSpace(text);
	}

	private static String suppressWhiteSpace(String str) {
		int len = str.length();
		StringBuilder sb = new StringBuilder(len);
		char c;
		char buffer = 0;
		for (int i = 0; i < len; i++) {
			c = str.charAt(i);
			if (c == '\n' || c == '\r') buffer = '\n';
			else if (Character.isWhitespace(c)) {
				if (buffer == 0) buffer = c;
			}
			else {
				if (buffer != 0) {
					sb.append(buffer);
					buffer = 0;
				}
				sb.append(c);
			}
		}
		if (buffer != 0) sb.append(buffer);
		return sb.toString();
	}

	private void doActionThumbnail() throws PageException, IOException {
		required("pdf", "thumbnail", "source", source);

		if (scale < 1 || scale > 100)
			throw engine.getExceptionUtil().createApplicationException("Attribute [scale] the value [" + scale + "] must between the range 1 to 100");

		if (destination == null) destination = engine.getResourceUtil().toResourceNotExisting(pageContext, "thumbnails");

		if (destination.isFile())
			throw engine.getExceptionUtil().createApplicationException("The attribute [destination] for the tag [cfpdf] is not a directory");

		if (destination.exists()) {
			if (!overwrite)
				throw engine.getExceptionUtil().createApplicationException("Destination directory [" + destination + "] already exists");
		}
		else destination.mkdirs();

		PDFStruct doc = toPDFDocument(source, password, null);
		int len = doc.getNumberOfPages();

		if (pages == null) pages = "1-" + len + "";
		Set<Integer> pageSet = PDFUtil.parsePageDefinition(pages, len);

		Resource resource;
		if (imagePrefix == null) imagePrefix = (resource = doc.getResource()) != null ? getName(resource.getName()) : "thumbnail";

		PDFUtil.thumbnail(pageContext, doc, destination.toString(), pageSet, format, imagePrefix, scale, overwrite);
	}

	private void doActionAddWatermark() throws PageException, IOException {
		required("pdf", "addWatermark", "source", source);
		if (copyFrom == null && image == null)
			throw engine.getExceptionUtil().createApplicationException("PDF action [addWaterMark] requires one of the following attributes " + "[copyFrom, image]");

		if (destination != null && destination.exists() && !overwrite)
			throw engine.getExceptionUtil().createApplicationException("Destination PDF file [" + destination + "] already exists");

		// Load watermark image
		BufferedImage watermarkImage = null;
		if (image != null) {
			if (image instanceof String) {
				Resource res = engine.getResourceUtil().toResourceExisting(pageContext, (String) image);
				watermarkImage = ImageIO.read(new File(res.getAbsolutePath()));
			}
			else {
				byte[] barr = engine.getCastUtil().toBinary(image);
				watermarkImage = ImageIO.read(new ByteArrayInputStream(barr));
			}
		}
		else {
			byte[] barr;
			try {
				Resource res = copyFrom instanceof String ? engine.getResourceUtil().toResourceExisting(pageContext, (String) copyFrom) : engine.getCastUtil().toResource(copyFrom);
				barr = PDFUtil.toBytes(res);
			}
			catch (PageException ee) {
				barr = engine.getCastUtil().toBinary(copyFrom);
			}
			watermarkImage = PDFUtil.toImage(new PDFStruct(barr, password));
		}

		// Position
		float x = UNDEFINED, y = UNDEFINED;
		if (!Util.isEmpty(position)) {
			int index = position.indexOf(',');
			if (index == -1)
				throw engine.getExceptionUtil().createApplicationException(
						"Attribute [position] has an invalid value [" + position + "]," + "value should follow one of the following pattern [40,50], [40,] or [,50]");
			String strX = position.substring(0, index).trim();
			String strY = position.substring(index + 1).trim();
			if (!Util.isEmpty(strX)) x = engine.getCastUtil().toIntValue(strX);
			if (!Util.isEmpty(strY)) y = engine.getCastUtil().toIntValue(strY);
		}

		PDFStruct doc = toPDFDocument(source, password, null);
		doc.setPages(pages);

		OutputStream os = createOutputStream( doc, !Util.isEmpty( name ) );
		try (PDDocument pdDoc = doc.toPDDocument()) {
			PDImageXObject pdImage = LosslessFactory.createFromImage(pdDoc, watermarkImage);
			Set<Integer> _pages = doc.getPages();
			int len = pdDoc.getNumberOfPages();

			for (int i = 0; i < len; i++) {
				if (_pages != null && !_pages.contains(Integer.valueOf(i + 1))) continue;

				PDPage page = pdDoc.getPage(i);
				PDRectangle pageSize = page.getMediaBox();

				float imgX = x != UNDEFINED ? x : (pageSize.getWidth() - pdImage.getWidth()) / 2;
				float imgY = y != UNDEFINED ? y : (pageSize.getHeight() - pdImage.getHeight()) / 2;

				try (PDPageContentStream cs = new PDPageContentStream(pdDoc, page,
						foreground ? PDPageContentStream.AppendMode.APPEND : PDPageContentStream.AppendMode.PREPEND, true, true)) {

					// Set opacity
					PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
					gs.setNonStrokingAlphaConstant(opacity);
					cs.setGraphicsStateParameters(gs);

					// Apply rotation if needed
					if (rotation != 0) {
						float centerX = imgX + pdImage.getWidth() / 2;
						float centerY = imgY + pdImage.getHeight() / 2;
						cs.transform(org.apache.pdfbox.util.Matrix.getTranslateInstance(centerX, centerY));
						cs.transform(org.apache.pdfbox.util.Matrix.getRotateInstance(Math.toRadians(rotation), 0, 0));
						cs.transform(org.apache.pdfbox.util.Matrix.getTranslateInstance(-pdImage.getWidth() / 2, -pdImage.getHeight() / 2));
						cs.drawImage(pdImage, 0, 0);
					}
					else {
						cs.drawImage(pdImage, imgX, imgY);
					}
				}
			}

			pdDoc.save(os);
		}
		finally {
			finalizeOutput( os, doc, name );
		}
	}

	private void doActionRemoveWatermark() throws PageException, IOException {
		required("pdf", "removeWatermark", "source", source);

		if (destination != null && destination.exists() && !overwrite)
			throw engine.getExceptionUtil().createApplicationException("Destination PDF file [" + destination + "] already exists");

		// Note: True watermark removal is complex - this is a simplified implementation
		// that essentially creates a copy. Full watermark removal would require
		// analyzing and modifying the PDF content streams.

		PDFStruct doc = toPDFDocument(source, password, null);
		doc.setPages(pages);

		if (destination == null) {
			destination = doc.getResource();
			if (destination == null)
				throw engine.getExceptionUtil().createApplicationException("PDF Source is not based on a resource, attribute [destination] file is required");
		}

		OutputStream os = createOutputStream( doc, !Util.isEmpty( name ) );
		try {
			PDFUtil.concat(new PDFStruct[] { doc }, os, true, true, true, (char) 0);
		}
		finally {
			finalizeOutput( os, doc, name );
		}
	}

	private void doActionDeletePages() throws PageException, IOException {
		required("pdf", "deletePage", "pages", pages, true);
		required("pdf", "deletePage", "source", source);

		PDFStruct doc = toPDFDocument(source, password, null);
		doc.setPages(pages);

		if (destination == null && Util.isEmpty(name)) {
			if (doc.getResource() == null)
				throw engine.getExceptionUtil().createApplicationException("Source is not based on a resource, attribute [destination] is required");
			destination = doc.getResource();
		}
		else if (destination != null && destination.exists() && !overwrite)
			throw engine.getExceptionUtil().createApplicationException("Destination PDF file [" + destination + "] already exists");

		OutputStream os = createOutputStream( doc, !Util.isEmpty( name ) );
		try {
			PDFUtil.concat(new PDFStruct[] { doc }, os, true, true, true, (char) 0);
		}
		finally {
			finalizeOutput( os, doc, name );
		}
	}

	private void doActionMerge() throws PageException, IOException {
		if (source == null && params == null && directory == null)
			throw engine.getExceptionUtil()
					.createApplicationException("At least one of the following combinations is required, attribute [source], attribute [directory] or [cfpdfparam] child tags");
		if (source != null && directory != null)
			throw engine.getExceptionUtil().createApplicationException("You cannot use both attributes [source, directory] at the same time, only specify one");
		if (destination == null && Util.isEmpty(name, true))
			throw engine.getExceptionUtil().createApplicationException("At least one of the following attributes is required [destination, name]");
		if (destination != null && destination.exists() && !overwrite)
			throw engine.getExceptionUtil().createApplicationException("Destination PDF file [" + destination + "] already exists");

		ArrayList<PDFStruct> docs = new ArrayList<PDFStruct>();
		PDFStruct doc;
		boolean isListing = false;
		boolean destIsSource = false;

		// source
		if (source != null) {
			if (engine.getDecisionUtil().isArray(source)) {
				Array arr = engine.getCastUtil().toArray(source);
				int len = arr.size();
				for (int i = 1; i <= len; i++) {
					docs.add(doc = toPDFDocument(arr.getE(i), password, null));
					if (doc.getResource() != null && destination.equals(doc.getResource()) && !destIsSource) destIsSource = true;
					doc.setPages(pages);
				}
			}
			else if (source instanceof String) {
				String[] sources = engine.getListUtil().toStringArrayTrim(engine.getListUtil().toArrayRemoveEmpty((String) source, ","));
				for (int i = 0; i < sources.length; i++) {
					docs.add(doc = toPDFDocument(sources[i], password, null));
					if (doc.getResource() != null && destination != null && destination.equals(doc.getResource()) && !destIsSource) destIsSource = true;
					doc.setPages(pages);
				}
			}
			else docs.add(toPDFDocument(source, password, null));
		}

		// directory
		if (directory != null && !directory.isDirectory()) {
			if (!directory.exists()) throw engine.getExceptionUtil().createApplicationException("Attribute [directory] does not exist");
			throw engine.getExceptionUtil().createApplicationException("Attribute [directory] is not a directory");
		}

		// params
		if (params != null) {
			Iterator<PDFParamBean> it = params.iterator();
			PDFParamBean param;
			while (it.hasNext()) {
				param = it.next();
				docs.add(doc = toPDFDocument(param.getSource(), param.getPassword(), directory));
				if (doc.getResource() != null && destination != null && destination.equals(doc.getResource()) && !destIsSource) destIsSource = true;
				doc.setPages(param.getPages());
			}
		}
		else if (directory != null) {
			isListing = true;
			Resource[] children = filter != null ? directory.listResources(filter) : directory.listResources();

			if (order == ORDER_NAME) {
				Arrays.sort(children, new Comparator<Resource>() {
					@Override
					public int compare(Resource o1, Resource o2) {
						int c = o1.getName().compareTo(o2.getName());
						return ascending ? c : -c;
					}
				});
			}
			else if (order == ORDER_TIME) {
				Arrays.sort(children, new Comparator<Resource>() {
					@Override
					public int compare(Resource o1, Resource o2) {
						int c = Long.compare(o1.lastModified(), o2.lastModified());
						return ascending ? c : -c;
					}
				});
			}

			for (int i = 0; i < children.length; i++) {
				if (destination != null && children[i].equals(destination)) destIsSource = true;
				docs.add(doc = toPDFDocument(children[i], password, null));
				doc.setPages(pages);
			}
		}

		int doclen = docs.size();
		if (doclen == 0)
			throw engine.getExceptionUtil().createApplicationException("PDF action [merge] requires at least 1 PDF file when merging");

		OutputStream os = null;
		if (!Util.isEmpty(name) || destIsSource) {
			os = new ByteArrayOutputStream();
		}
		else if (destination != null) {
			os = destination.getOutputStream();
		}

		try {
			if (!isListing) stopOnError = true;
			PDFUtil.concat(docs.toArray(new PDFStruct[docs.size()]), os, keepBookmark, false, stopOnError, (char) 0);
		}
		finally {
			Util.closeEL(os);
			if (os instanceof ByteArrayOutputStream) {
				if (destination != null)
					engine.getIOUtil().copy(new ByteArrayInputStream(((ByteArrayOutputStream) os).toByteArray()), destination, true);
				if (!Util.isEmpty(name)) pageContext.setVariable(name, ((ByteArrayOutputStream) os).toByteArray());
			}
		}
	}

	private void doActionRead() throws PageException {
		required("pdf", "read", "name", name, true);
		required("pdf", "read", "source", source);

		pageContext.setVariable(name, toPDFDocument(source, password, null));
	}

	private void doActionProtect(boolean protect) throws PageException, IOException {
		required("pdf", protect ? "protect" : "open", "source", source);

		if (protect && Util.isEmpty(newUserPassword) && Util.isEmpty(newOwnerPassword))
			throw engine.getExceptionUtil().createApplicationException("At least one of the following attributes is required [newUserPassword, newOwnerPassword]");
		if (!protect) required("pdf", "open", "password", password);

		PDFStruct doc = toPDFDocument(source, password, null);

		if (destination == null) {
			destination = doc.getResource();
			if (destination == null)
				throw engine.getExceptionUtil().createApplicationException("Source is not based on a resource, destination file is required");
		}
		else if (destination.exists() && !overwrite)
			throw engine.getExceptionUtil().createApplicationException("Destination file [" + destination + "] already exists");

		boolean destIsSource = doc.getResource() != null && destination.equals(doc.getResource());

		OutputStream os = null;
		if (destIsSource) {
			os = new ByteArrayOutputStream();
		}
		else {
			os = destination.getOutputStream();
		}

		try {
			if (protect) PDFUtil.encrypt(doc, os, newUserPassword, newOwnerPassword, permissions, encrypt);
			else PDFUtil.encrypt(doc, os, null, null, 0, PDFUtil.ENCRYPT_NONE);
		}
		finally {
			Util.closeEL(os);
			if (os instanceof ByteArrayOutputStream) {
				engine.getIOUtil().copy(new ByteArrayInputStream(((ByteArrayOutputStream) os).toByteArray()), destination, true);
			}
		}
	}

	private void doActionSetInfo() throws PageException, IOException {
		required("pdf", "setInfo", "info", info);
		required("pdf", "getInfo", "source", source);

		PDFStruct doc = toPDFDocument(source, password, null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		if (destination != null && name != null)
			throw engine.getExceptionUtil().createApplicationException("You cannot use both attributes [destination, name] at the same time, only specify one");

		// Determine destination and check if it's same as source
		boolean destIsSource = false;
		if (destination == null) {
			if (Util.isEmpty(name)) {
				if (doc.getResource() != null) {
					destination = doc.getResource();
					destIsSource = true;
				}
				else if (source instanceof String && doc.getResource() == null) name = (String) source;
				else throw engine.getExceptionUtil().createApplicationException(
						"PDF attribute [source] is not a resource (file) or variable, you must specify one of the following attributes [destination, name]");
			}
		}
		else {
			destIsSource = doc.getResource() != null && destination.equals(doc.getResource());
			if (destination.exists() && !overwrite && !destIsSource)
				throw engine.getExceptionUtil().createApplicationException("Destination file [" + destination + "] already exists");
		}

		// If destination is source, always write to ByteArrayOutputStream first
		OutputStream os = destIsSource ? baos : (destination != null ? destination.getOutputStream() : baos);

		try (PDDocument pdDoc = doc.toPDDocument()) {
			PDDocumentInformation docInfo = pdDoc.getDocumentInformation();

			// Set info values
			Iterator<Entry<Key, Object>> it = info.entryIterator();
			Entry<Key, Object> e;
			while (it.hasNext()) {
				e = it.next();
				String key = e.getKey().getLowerString();
				String value = engine.getCastUtil().toString(e.getValue(), "");

				if ("author".equals(key)) docInfo.setAuthor(value);
				else if ("keywords".equals(key)) docInfo.setKeywords(value);
				else if ("title".equals(key)) docInfo.setTitle(value);
				else if ("subject".equals(key)) docInfo.setSubject(value);
				else if ("creator".equals(key)) docInfo.setCreator(value);
				else if ("producer".equals(key)) docInfo.setProducer(value);
				else if ("trapped".equals(key)) {
					// Only set trapped if value is valid (True, False, or Unknown)
					if ("True".equalsIgnoreCase(value) || "False".equalsIgnoreCase(value) || "Unknown".equalsIgnoreCase(value)) {
						docInfo.setTrapped(engine.getStringUtil().ucFirst(value.toLowerCase()));
					}
					// Skip invalid or empty trapped values
				}
				else if (!"pagerotation".equals(key) && !"pagesize".equals(key)) {
					docInfo.setCustomMetadataValue(engine.getStringUtil().ucFirst(key), value);
				}
			}

			pdDoc.save(os);
		}
		finally {
			Util.closeEL(os);
		}

		// If destination was source, copy bytes to file now that source is closed
		if (destIsSource && destination != null) {
			engine.getIOUtil().copy(new ByteArrayInputStream(baos.toByteArray()), destination, true);
		}

		if (!Util.isEmpty(name)) {
			pageContext.setVariable(name, new PDFStruct(baos.toByteArray(), password));
		}
	}

	private void doActionGetInfo() throws PageException {
		required("pdf", "getInfo", "name", name, true);
		required("pdf", "getInfo", "source", source);

		PDFStruct doc = toPDFDocument(source, password, null);
		pageContext.setVariable(name, doc.getInfo());
	}

	private void doActionExtractText() throws PageException, IOException {
		required("pdf", "extractText", "source", source);
		PDFStruct doc = toPDFDocument(source, password, null);
		int len = doc.getNumberOfPages();

		if (pages == null) pages = "1-" + len + "";
		Set<Integer> pageSet = PDFUtil.parsePageDefinition(pages, len);

		if (destination == null && Util.isEmpty(name, true))
			throw engine.getExceptionUtil().createApplicationException("At least one of the following attributes is required [destination, name]");
		if (destination != null && destination.exists() && !overwrite)
			throw engine.getExceptionUtil().createApplicationException("Destination file [" + destination + "] already exists");

		if (!Util.isEmpty(name, true))
			pageContext.setVariable(name, PDFUtil.extractText(doc, pageSet, type, destination));
		else
			PDFUtil.extractText(doc, pageSet, type, destination);
	}

	private void doActionExtractBookmarks() throws PageException, IOException {
		required("pdf", "extractBookmarks", "source", source);
		PDFStruct doc = toPDFDocument(source, password, null);

		if (Util.isEmpty(name, true))
			throw engine.getExceptionUtil().createApplicationException("The [name] attribute is required");
		pageContext.setVariable(name, PDFUtil.extractBookmarks(pageContext, doc));
	}

	private void doActionExtractImages() throws PageException, IOException, InvalidPasswordException {
		required("pdf", "extractImages", "source", source);
		required("pdf", "extractImages", "destination", destination);
		required("pdf", "extractImages", "imagePrefix", imagePrefix);
		required("pdf", "extractImages", "format", format);
		PDFStruct doc = toPDFDocument(source, password, null);
		int len = doc.getNumberOfPages();
		if (pages == null || pages.equals("*")) pages = "1-" + len + "";
		Set<Integer> pageSet = PDFUtil.parsePageDefinition(pages, len);

		PDFUtil.extractImages(pageContext, doc, pageSet, destination, imagePrefix, format, overwrite);
	}

	private PDFStruct toPDFDocument(Object source, String password, Resource directory) throws PageException {
		return toPDFDocument(source, password, directory, true);
	}

	private PDFStruct toPDFDocument(Object source, String password, Resource directory, boolean eval) throws PageException {
		if (source instanceof PDFStruct) return (PDFStruct) source;
		if (engine.getDecisionUtil().isBinary(source)) {
			return new PDFStruct(engine.getCastUtil().toBinary(source), password);
		}
		if (source instanceof Resource) {
			return new PDFStruct((Resource) source, password);
		}
		if (source instanceof String) {
			String str = (String) source;

			// could be a variable name
			Object obj = null;
			try {
				obj = pageContext.getVariable(str);
			}
			catch (PageException pe) {
			}
			if (obj != null) return toPDFDocument(obj, password, directory, false);

			if (directory != null) {
				Resource res = directory.getRealResource(str);
				if (!res.isFile()) {
					Resource res2 = engine.getResourceUtil().toResourceNotExisting(pageContext, str);
					if (res2.isFile()) res = res2;
					else throw engine.getExceptionUtil().createApplicationException("variable, file or directory [" + res + "] does not exist");
				}
				return new PDFStruct(res, password);
			}
			return new PDFStruct(engine.getResourceUtil().toResourceExisting(pageContext, (String) source), password);
		}

		throw engine.getExceptionUtil().createCasterException(source, PDFStruct.class);
	}

	protected void setParam(PDFParamBean param) {
		if (params == null) params = new ArrayList<PDFParamBean>();
		params.add(param);
	}

	private static String getName(String strFileName) {
		int pos = strFileName.lastIndexOf('.');
		if (pos == -1) return strFileName;
		return strFileName.substring(0, pos);
	}

	private void doActionTransform() throws PageException, IOException {
		required("pdf", "transform", "source", source);

		if (destination != null && destination.exists() && !overwrite)
			throw engine.getExceptionUtil().createApplicationException("Destination PDF file [" + destination + "] already exists");

		PDFStruct doc = toPDFDocument(source, password, null);
		int len = doc.getNumberOfPages();

		if (pages == null) pages = "1-" + len;
		Set<Integer> pageSet = PDFUtil.parsePageDefinition(pages, len);

		OutputStream os = createOutputStream( doc, !Util.isEmpty( name ) );
		try (PDDocument pdDoc = doc.toPDDocument()) {
			for (int i = 0; i < len; i++) {
				if (pageSet != null && !pageSet.contains(i + 1)) continue;

				PDPage page = pdDoc.getPage(i);

				// Apply rotation (must be 0, 90, 180, or 270)
				if (rotation != 0) {
					int currentRotation = page.getRotation();
					int newRotation = (currentRotation + (int) rotation) % 360;
					if (newRotation < 0) newRotation += 360;
					page.setRotation(newRotation);
				}

				// Apply scaling via media box transformation
				if (hscale != 1.0f || vscale != 1.0f) {
					PDRectangle mediaBox = page.getMediaBox();
					float newWidth = mediaBox.getWidth() * hscale;
					float newHeight = mediaBox.getHeight() * vscale;
					page.setMediaBox(new PDRectangle(newWidth, newHeight));

					// Also adjust crop box if present
					PDRectangle cropBox = page.getCropBox();
					if (cropBox != null) {
						page.setCropBox(new PDRectangle(cropBox.getWidth() * hscale, cropBox.getHeight() * vscale));
					}
				}
			}

			pdDoc.save(os);
		}
		finally {
			finalizeOutput( os, doc, name );
		}
	}

	private void doActionAddAttachments() throws PageException, IOException {
		required("pdf", "addAttachments", "source", source);
		if (params == null || params.isEmpty())
			throw engine.getExceptionUtil().createApplicationException("PDF action [addAttachments] requires at least one cfpdfparam child tag");

		if (destination != null && destination.exists() && !overwrite)
			throw engine.getExceptionUtil().createApplicationException("Destination PDF file [" + destination + "] already exists");

		PDFStruct doc = toPDFDocument(source, password, null);

		OutputStream os = createOutputStream( doc, !Util.isEmpty( name ) );
		try (PDDocument pdDoc = doc.toPDDocument()) {
			PDDocumentNameDictionary names = pdDoc.getDocumentCatalog().getNames();
			if (names == null) {
				names = new PDDocumentNameDictionary(pdDoc.getDocumentCatalog());
				pdDoc.getDocumentCatalog().setNames(names);
			}

			PDEmbeddedFilesNameTreeNode efTree = names.getEmbeddedFiles();
			Map<String, PDComplexFileSpecification> existingFiles = new java.util.HashMap<>();
			if (efTree != null) {
				Map<String, PDComplexFileSpecification> existing = efTree.getNames();
				if (existing != null) existingFiles.putAll(existing);
			}
			else {
				efTree = new PDEmbeddedFilesNameTreeNode();
			}

			for (PDFParamBean param : params) {
				Object paramSource = param.getSource();
				Resource attachRes = engine.getResourceUtil().toResourceExisting(pageContext, paramSource.toString());

				String filename = param.getFilename();
				if (Util.isEmpty(filename)) filename = attachRes.getName();

				byte[] fileBytes = PDFUtil.toBytes(attachRes);

				PDEmbeddedFile embeddedFile = new PDEmbeddedFile(pdDoc, new ByteArrayInputStream(fileBytes));
				embeddedFile.setSize(fileBytes.length);

				String mimeType = param.getMimetype();
				if (!Util.isEmpty(mimeType)) {
					embeddedFile.setSubtype(mimeType);
				}

				PDComplexFileSpecification fileSpec = new PDComplexFileSpecification();
				fileSpec.setFile(filename);
				fileSpec.setEmbeddedFile(embeddedFile);

				String desc = param.getDescription();
				if (!Util.isEmpty(desc)) {
					fileSpec.setFileDescription(desc);
				}

				existingFiles.put(filename, fileSpec);
			}

			efTree.setNames(existingFiles);
			names.setEmbeddedFiles(efTree);

			pdDoc.save(os);
		}
		finally {
			finalizeOutput( os, doc, name );
		}
	}

	private void doActionExtractAttachments() throws PageException, IOException {
		required("pdf", "extractAttachments", "source", source);
		required("pdf", "extractAttachments", "destination", destination);

		if (!destination.exists()) destination.mkdirs();
		if (!destination.isDirectory())
			throw engine.getExceptionUtil().createApplicationException("Destination must be a directory for extractAttachments");

		PDFStruct doc = toPDFDocument(source, password, null);
		Array result = engine.getCreationUtil().createArray();

		try (PDDocument pdDoc = doc.toPDDocument()) {
			PDDocumentNameDictionary names = pdDoc.getDocumentCatalog().getNames();
			if (names != null) {
				PDEmbeddedFilesNameTreeNode efTree = names.getEmbeddedFiles();
				if (efTree != null) {
					Map<String, PDComplexFileSpecification> files = efTree.getNames();
					if (files != null) {
						for (Map.Entry<String, PDComplexFileSpecification> entry : files.entrySet()) {
							String filename = entry.getKey();
							PDComplexFileSpecification fileSpec = entry.getValue();
							PDEmbeddedFile embeddedFile = fileSpec.getEmbeddedFile();

							if (embeddedFile != null) {
								Resource outFile = destination.getRealResource(filename);
								if (outFile.exists() && !overwrite) {
									if (stopOnError)
										throw engine.getExceptionUtil().createApplicationException("File [" + outFile + "] already exists");
									continue;
								}

								engine.getIOUtil().copy(embeddedFile.createInputStream(), outFile, true);

								Struct info = engine.getCreationUtil().createStruct();
								info.set("filename", filename);
								info.set("path", outFile.getAbsolutePath());
								info.set("size", embeddedFile.getSize());
								if (fileSpec.getFileDescription() != null) {
									info.set("description", fileSpec.getFileDescription());
								}
								result.append(info);
							}
						}
					}
				}
			}
		}

		if (!Util.isEmpty(name)) {
			pageContext.setVariable(name, result);
		}
	}

	private void doActionRemoveAttachments() throws PageException, IOException {
		required("pdf", "removeAttachments", "source", source);

		if (destination != null && destination.exists() && !overwrite)
			throw engine.getExceptionUtil().createApplicationException("Destination PDF file [" + destination + "] already exists");

		PDFStruct doc = toPDFDocument(source, password, null);

		OutputStream os = createOutputStream( doc, !Util.isEmpty( name ) );
		try (PDDocument pdDoc = doc.toPDDocument()) {
			PDDocumentNameDictionary names = pdDoc.getDocumentCatalog().getNames();
			if (names != null) {
				names.setEmbeddedFiles(null);
			}

			pdDoc.save(os);
		}
		finally {
			finalizeOutput( os, doc, name );
		}
	}

	private void doActionReadSignatureFields() throws PageException, IOException {
		required("pdf", "readSignatureFields", "source", source);
		required("pdf", "readSignatureFields", "name", name, true);

		PDFStruct doc = toPDFDocument(source, password, null);

		// Create query with columns: name, signable, isSigned, certifiable
		Query query = engine.getCreationUtil().createQuery(new String[] { "name", "signable", "isSigned", "certifiable" }, 0, "signatureFields");

		try (PDDocument pdDoc = doc.toPDDocument()) {
			PDAcroForm acroForm = pdDoc.getDocumentCatalog().getAcroForm();
			if (acroForm != null) {
				for (PDField field : acroForm.getFieldTree()) {
					if (field instanceof PDSignatureField) {
						PDSignatureField sigField = (PDSignatureField) field;
						int row = query.addRow();
						query.setAt("name", row, sigField.getFullyQualifiedName());
						query.setAt("signable", row, true);
						query.setAt("isSigned", row, sigField.getSignature() != null);
						query.setAt("certifiable", row, false); // Would need deeper inspection
					}
				}
			}
		}

		pageContext.setVariable(name, query);
	}

	private void doActionValidateSignature() throws PageException, IOException {
		required("pdf", "validateSignature", "source", source);
		required("pdf", "validateSignature", "name", name, true);

		PDFStruct doc = toPDFDocument(source, password, null);

		// Create result struct with signature validation info
		Struct result = engine.getCreationUtil().createStruct();
		Array signatures = engine.getCreationUtil().createArray();

		try (PDDocument pdDoc = doc.toPDDocument()) {
			List<PDSignature> signatureList = pdDoc.getSignatureDictionaries();

			boolean hasSignatures = !signatureList.isEmpty();
			boolean allValid = true;

			for (PDSignature sig : signatureList) {
				Struct sigInfo = engine.getCreationUtil().createStruct();

				// Basic signature information
				sigInfo.set(engine.getCreationUtil().createKey("name"), sig.getName() != null ? sig.getName() : "");
				sigInfo.set(engine.getCreationUtil().createKey("reason"), sig.getReason() != null ? sig.getReason() : "");
				sigInfo.set(engine.getCreationUtil().createKey("location"), sig.getLocation() != null ? sig.getLocation() : "");
				sigInfo.set(engine.getCreationUtil().createKey("contactInfo"), sig.getContactInfo() != null ? sig.getContactInfo() : "");
				sigInfo.set(engine.getCreationUtil().createKey("signDate"), sig.getSignDate() != null ? sig.getSignDate().getTime() : null);

				// Filter and SubFilter
				String filter = sig.getFilter();
				String subFilter = sig.getSubFilter();
				sigInfo.set(engine.getCreationUtil().createKey("filter"), filter != null ? filter : "");
				sigInfo.set(engine.getCreationUtil().createKey("subFilter"), subFilter != null ? subFilter : "");

				// Check if signature has byte range (indicates it covers document content)
				int[] byteRange = sig.getByteRange();
				boolean hasValidByteRange = byteRange != null && byteRange.length == 4;
				sigInfo.set(engine.getCreationUtil().createKey("hasValidByteRange"), hasValidByteRange);

				// Check if signature has content (is actually signed)
				boolean isSigned = sig.getContents() != null && sig.getContents().length > 0;
				sigInfo.set(engine.getCreationUtil().createKey("isSigned"), isSigned);

				// Basic validation status - without full cryptographic validation
				// Full validation would require certificate chain verification
				boolean valid = isSigned && hasValidByteRange;
				sigInfo.set(engine.getCreationUtil().createKey("valid"), valid);

				if (!valid) allValid = false;

				signatures.append(sigInfo);
			}

			result.set(engine.getCreationUtil().createKey("hasSignatures"), hasSignatures);
			result.set(engine.getCreationUtil().createKey("signatureCount"), signatureList.size());
			result.set(engine.getCreationUtil().createKey("allValid"), hasSignatures && allValid);
			result.set(engine.getCreationUtil().createKey("signatures"), signatures);
		}

		pageContext.setVariable(name, result);
	}

	/**
	 * Optimize a PDF by removing specified elements.
	 * Reduces file size by stripping bookmarks, links, JavaScript, attachments, metadata, etc.
	 */
	private void doActionOptimize() throws PageException, IOException {
		required("pdf", "optimize", "source", source);
		required("pdf", "optimize", "destination", destination);

		if (destination != null && destination.exists() && !overwrite)
			throw engine.getExceptionUtil().createApplicationException("Destination PDF file [" + destination + "] already exists");

		PDFStruct doc = toPDFDocument(source, password, null);

		try (PDDocument pdDoc = doc.toPDDocument()) {
			PDDocumentCatalog catalog = pdDoc.getDocumentCatalog();

			// Remove bookmarks/outline
			if (noBookmarks) {
				catalog.setDocumentOutline(null);
			}

			// Remove JavaScript
			if (noJavaScript) {
				// Remove document-level JavaScript
				COSDictionary names = (COSDictionary) catalog.getCOSObject().getDictionaryObject(COSName.NAMES);
				if (names != null) {
					names.removeItem(COSName.getPDFName("JavaScript"));
				}
				// Remove open action if it's JavaScript
				catalog.setOpenAction(null);
			}

			// Remove attachments/embedded files
			if (noAttachments) {
				catalog.setNames(null); // Removes EmbeddedFiles name tree
				PDDocumentNameDictionary nameDictionary = catalog.getNames();
				if (nameDictionary != null) {
					nameDictionary.setEmbeddedFiles(null);
				}
			}

			// Remove metadata
			if (noMetadata) {
				pdDoc.setDocumentInformation(new PDDocumentInformation());
				catalog.setMetadata(null);
			}

			// Remove links and annotations
			if (noLinks || noComments) {
				for (PDPage page : pdDoc.getPages()) {
					List<PDAnnotation> annotations = page.getAnnotations();
					if (annotations != null) {
						List<PDAnnotation> toRemove = new ArrayList<>();
						for (PDAnnotation annot : annotations) {
							if (noLinks && annot instanceof PDAnnotationLink) {
								toRemove.add(annot);
							}
							else if (noComments && !(annot instanceof PDAnnotationLink)) {
								// Remove non-link annotations (comments, highlights, etc.)
								toRemove.add(annot);
							}
						}
						annotations.removeAll(toRemove);
					}
				}
			}

			// Remove forms/AcroForm
			if (noForms) {
				catalog.setAcroForm(null);
			}

			// Remove page thumbnails
			if (noThumbnails) {
				for (PDPage page : pdDoc.getPages()) {
					page.getCOSObject().removeItem(COSName.THUMB);
				}
			}

			// Save optimized PDF
			OutputStream os = destination.getOutputStream();
			try {
				pdDoc.save(os);
			}
			finally {
				Util.closeEL(os);
			}
		}
	}

	/**
	 * Sanitize a PDF for security by removing potentially dangerous elements.
	 * More aggressive than optimize - removes JavaScript, links, actions, attachments, and metadata.
	 */
	private void doActionSanitize() throws PageException, IOException {
		required("pdf", "sanitize", "source", source);
		required("pdf", "sanitize", "destination", destination);

		if (destination != null && destination.exists() && !overwrite)
			throw engine.getExceptionUtil().createApplicationException("Destination PDF file [" + destination + "] already exists");

		PDFStruct doc = toPDFDocument(source, password, null);

		try (PDDocument pdDoc = doc.toPDDocument()) {
			PDDocumentCatalog catalog = pdDoc.getDocumentCatalog();

			// Always remove JavaScript for sanitize
			COSDictionary names = (COSDictionary) catalog.getCOSObject().getDictionaryObject(COSName.NAMES);
			if (names != null) {
				names.removeItem(COSName.getPDFName("JavaScript"));
			}
			catalog.setOpenAction(null);

			// Always remove attachments for sanitize
			PDDocumentNameDictionary nameDictionary = catalog.getNames();
			if (nameDictionary != null) {
				nameDictionary.setEmbeddedFiles(null);
			}

			// Always remove metadata for sanitize
			pdDoc.setDocumentInformation(new PDDocumentInformation());
			catalog.setMetadata(null);

			// Remove all links and form actions
			for (PDPage page : pdDoc.getPages()) {
				List<PDAnnotation> annotations = page.getAnnotations();
				if (annotations != null) {
					List<PDAnnotation> toRemove = new ArrayList<>();
					for (PDAnnotation annot : annotations) {
						// Remove link annotations
						if (annot instanceof PDAnnotationLink) {
							toRemove.add(annot);
						}
						// Remove any annotation with an action
						else if (annot.getCOSObject().containsKey(COSName.A) ||
								 annot.getCOSObject().containsKey(COSName.AA)) {
							toRemove.add(annot);
						}
					}
					annotations.removeAll(toRemove);
				}
			}

			// Remove form actions but keep form fields (optional based on noForms)
			PDAcroForm acroForm = catalog.getAcroForm();
			if (acroForm != null) {
				if (noForms) {
					catalog.setAcroForm(null);
				}
				else {
					// Just remove actions from form
					for (PDField field : acroForm.getFieldTree()) {
						field.getCOSObject().removeItem(COSName.A);
						field.getCOSObject().removeItem(COSName.AA);
					}
				}
			}

			// Save sanitized PDF
			OutputStream os = destination.getOutputStream();
			try {
				pdDoc.save(os);
			}
			finally {
				Util.closeEL(os);
			}
		}
	}

	/**
	 * Add a stamp image to PDF pages (similar to watermark but as annotation).
	 */
	private void doActionAddStamp() throws PageException, IOException {
		required("pdf", "addStamp", "source", source);
		required("pdf", "addStamp", "image", image);

		if (destination == null && name == null)
			throw engine.getExceptionUtil().createApplicationException("Either [destination] or [name] is required for action addStamp");

		if (destination != null && destination.exists() && !overwrite)
			throw engine.getExceptionUtil().createApplicationException("Destination PDF file [" + destination + "] already exists");

		// Use same approach as watermark but with stamp annotation
		// For now, delegate to watermark implementation since stamp is essentially a positioned image
		doActionAddWatermark();
	}
}
