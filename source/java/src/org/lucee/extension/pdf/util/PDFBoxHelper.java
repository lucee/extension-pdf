package org.lucee.extension.pdf.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;

/**
 * Test and CFML-facing helpers for PDFBox operations on extension classpath.
 * Used by integration tests because dependency JAR classes are not visible to createObject("java", ...).
 */
public final class PDFBoxHelper {

	private PDFBoxHelper() {}

	public static List<String> getLinkURIs(String pdfPath) throws IOException {
		File file = new File(pdfPath);
		try (PDDocument doc = Loader.loadPDF(file)) {
			List<String> uris = new ArrayList<>();
			Iterator<org.apache.pdfbox.pdmodel.PDPage> pageIter = doc.getPages().iterator();
			while (pageIter.hasNext()) {
				for (PDAnnotation annot : pageIter.next().getAnnotations()) {
					if (!"Link".equals(annot.getSubtype())) continue;
					PDAction action = ((PDAnnotationLink) annot).getAction();
					if (action == null) continue;
					if ("Action".equals(action.getType()) && "URI".equals(action.getSubType()) && action instanceof PDActionURI) {
						uris.add(((PDActionURI) action).getURI());
					}
				}
			}
			return uris;
		}
	}

	public static int countLinkAnnotations(String pdfPath) throws IOException {
		File file = new File(pdfPath);
		try (PDDocument doc = Loader.loadPDF(file)) {
			int count = 0;
			Iterator<org.apache.pdfbox.pdmodel.PDPage> pageIter = doc.getPages().iterator();
			while (pageIter.hasNext()) {
				for (PDAnnotation annot : pageIter.next().getAnnotations()) {
					if ("Link".equals(annot.getSubtype())) count++;
				}
			}
			return count;
		}
	}

	public static PDDocument loadPDF(String pdfPath) throws IOException {
		return Loader.loadPDF(new File(pdfPath));
	}

	public static String getPDFFontNamesList(String pdfPath) throws IOException {
		File file = new File(pdfPath);
		try (PDDocument doc = Loader.loadPDF(file)) {
			List<String> names = new ArrayList<>();
			Iterator<org.apache.pdfbox.pdmodel.PDPage> pageIter = doc.getPages().iterator();
			while (pageIter.hasNext()) {
				org.apache.pdfbox.pdmodel.PDResources resources = pageIter.next().getResources();
				if (resources == null) continue;
				for (org.apache.pdfbox.cos.COSName fontName : resources.getFontNames()) {
					names.add(resources.getFont(fontName).getName());
				}
			}
			return String.join(",", names);
		}
	}
}
