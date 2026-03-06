package org.lucee.extension.pdf.tag;

public interface Constants {
	public static final int ACTION_ADD_WATERMARK = 0;
	public static final int ACTION_DELETE_PAGES = 1;
	public static final int ACTION_GET_INFO = 2;
	public static final int ACTION_MERGE = 3;
	public static final int ACTION_PROCESSDDX = 5;
	public static final int ACTION_PROTECT = 5;
	public static final int ACTION_READ = 6;
	public static final int ACTION_REMOVE_WATERMARK = 7;
	public static final int ACTION_SET_INFO = 8;
	public static final int ACTION_THUMBNAIL = 9;
	public static final int ACTION_WRITE = 10;
	public static final int ACTION_EXTRACT_TEXT = 11;

	public static final int ACTION_ADD_HEADER = 12;
	public static final int ACTION_ADD_FOOTER = 13;
	public static final int ACTION_OPEN = 14;
	public static final int ACTION_EXTRACT_IMAGES = 15;
	public static final int ACTION_EXTRACT_BOOKMARKS = 16;

	public static final String FORMAT_JPG = "jpg";
	public static final String FORMAT_TIFF = "tiff";
	public static final String FORMAT_PNG = "png";

	public static final int ORDER_TIME = 0;
	public static final int ORDER_NAME = 1;

	public static final int RESOLUTION_HIGH = 0;
	public static final int RESOLUTION_LOW = 1;

	public static final int SAVE_OPTION_FULL = 0;
	public static final int SAVE_OPTION_INCREMENTAL = 1;
	public static final int SAVE_OPTION_LINEAR = 2;

	public static final int TYPE_STRING = 1;
	public static final int TYPE_XML = 2;

	public static final int NUMBERFORMAT_LOWERCASEROMAN = 1;
	public static final int NUMBERFORMAT_NUMERIC = 2;
	public static final int NUMBERFORMAT_UPPERCASEROMAN = 3;

	// private static final PDF_FILTER =
	// CFMLEngineFactory.getInstance().getResourceUtil().getExtensionResourceFilter("pdf", false);
	public static final int UNDEFINED = Integer.MIN_VALUE;
}
