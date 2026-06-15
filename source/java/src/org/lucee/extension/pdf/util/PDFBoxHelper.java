package org.lucee.extension.pdf.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.openpdf.text.pdf.PdfArray;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfString;

/**
 * Test and CFML-facing helpers for PDF introspection on extension classpath.
 */
public final class PDFBoxHelper {

	private PDFBoxHelper() {}

	public static List<String> getLinkURIs(String pdfPath) throws IOException {
		List<String> uris = getLinkURIsViaPDFBox(pdfPath);
		if (!uris.isEmpty()) return uris;
		return getLinkURIsViaOpenPDF(pdfPath);
	}

	public static int countLinkAnnotations(String pdfPath) throws IOException {
		int count = countLinkAnnotationsViaPDFBox(pdfPath);
		if (count > 0) return count;
		return countLinkAnnotationsViaOpenPDF(pdfPath);
	}

	private static List<String> getLinkURIsViaPDFBox(String pdfPath) throws IOException {
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

	private static int countLinkAnnotationsViaPDFBox(String pdfPath) throws IOException {
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

	private static List<String> getLinkURIsViaOpenPDF(String pdfPath) throws IOException {
		List<String> uris = new ArrayList<>();
		PdfReader reader = new PdfReader(pdfPath);
		try {
			collectOpenPDFLinkURIs(reader, uris);
		}
		finally {
			reader.close();
		}
		return uris;
	}

	private static int countLinkAnnotationsViaOpenPDF(String pdfPath) throws IOException {
		PdfReader reader = new PdfReader(pdfPath);
		try {
			return collectOpenPDFLinkURIs(reader, new ArrayList<>());
		}
		finally {
			reader.close();
		}
	}

	private static int collectOpenPDFLinkURIs(PdfReader reader, List<String> uris) throws IOException {
		int count = 0;
		int pages = reader.getNumberOfPages();
		for (int p = 1; p <= pages; p++) {
			PdfDictionary page = reader.getPageN(p);
			PdfArray annots = page.getAsArray(PdfName.ANNOTS);
			if (annots == null) continue;
			for (int i = 0; i < annots.size(); i++) {
				PdfDictionary annot = annots.getAsDict(i);
				if (annot == null) continue;
				if (!PdfName.LINK.equals(annot.get(PdfName.SUBTYPE))) continue;
				count++;
				PdfDictionary action = annot.getAsDict(PdfName.A);
				if (action == null || !PdfName.URI.equals(action.get(PdfName.S))) continue;
				PdfString uri = action.getAsString(PdfName.URI);
				if (uri != null) uris.add(uri.toString());
			}
		}
		return count;
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
