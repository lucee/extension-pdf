package org.lucee.extension.fs;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.xhtmlrenderer.pdf.ITextRenderer;

public class Test {

	public static void main(String[] args) throws Exception {

		ITextRenderer renderer = new ITextRenderer();

		// if you have html source in hand, use it to generate document object
		renderer.setDocumentFromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:html=\"http://www.w3.org/1999/xhtml\"><body><h1>Title</h1>\n" + "This is the text\n" + "</body></html>");
		renderer.layout();

		String fileNameWithPath = "/Users/mic/Tmp3/PDF-FromHtmlString.pdf";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FileOutputStream fos = new FileOutputStream(fileNameWithPath);
		renderer.createPDF(baos);
		fos.close();

		System.out.println("File 2: '" + fileNameWithPath + "' created.");

	}
}
