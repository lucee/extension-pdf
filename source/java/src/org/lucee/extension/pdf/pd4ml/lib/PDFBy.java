package org.lucee.extension.pdf.pd4ml.lib;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import org.lucee.extension.pdf.PDFPageMark;

import lucee.runtime.exp.PageException;

public interface PDFBy {
	public PDFBy newInstance() throws PageException;

	public void enableTableBreaks(boolean b) throws PageException;

	public void interpolateImages(boolean b) throws PageException;

	public void adjustHtmlWidth() throws PageException;

	public void setPageInsets(Insets insets) throws PageException;

	public void setPageSize(Dimension dimension) throws PageException;

	public void generateOutlines(boolean flag) throws PageException;

	public void useTTF(String pathToFontDirs, boolean embed) throws PageException;

	public boolean isPro() throws PageException;

	public void overrideDocumentEncoding(String encoding) throws PageException;

	public void setDefaultTTFs(String string, String string2, String string3) throws PageException;

	public void render(InputStreamReader reader, OutputStream os) throws PageException;

	public BufferedImage[] renderAsImages(URL url, int width, int height) throws PageException;

	public void render(String str, OutputStream os, URL base) throws PageException;

	public void setPageHeader(PDFPageMark header) throws PageException;

	public void setPageFooter(PDFPageMark footer) throws PageException;

}