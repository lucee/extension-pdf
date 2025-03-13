package org.lucee.extension.fs;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

public class Test {

	public static void main(String[] args) throws Exception {

		PdfRendererBuilder builder = new PdfRendererBuilder();

		// if you have html source in hand, use it to generate document object
		String html = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:html=\"http://www.w3.org/1999/xhtml\"><body><h1>Title</h1>\n" + "This is the text\n" + "</body></html>";

		String fileNameWithPath = "/Users/mic/Tmp3/PDF-FromHtmlString.pdf";
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		FileOutputStream fos = new FileOutputStream(fileNameWithPath);

		builder.withHtmlContent(html, null);
		builder.toStream(outputStream);
		builder.run();
		fos.close();

		System.out.println("File 2: '" + fileNameWithPath + "' created.");

	}
}
