// Generates the AcroForm test fixtures form.pdf and form-prefilled.pdf with PDFBox 3.0.x:
//   javac -cp pdfbox.jar:pdfbox-io.jar:fontbox.jar:commons-logging.jar GenForm.java
//   java -cp ...:. GenForm form.pdf form-prefilled.pdf
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.*;
import org.apache.pdfbox.cos.COSName;
import java.util.*;

public class GenForm {
  public static void main(String[] a) throws Exception {
    gen(a[0], new String[][]{{"firstName",""},{"lastName",""},{"email",""},{"phone",""}});
    gen(a[1], new String[][]{{"firstName","John"},{"lastName","Doe"},{"email","john@example.com"}});
  }
  static void gen(String out, String[][] fields) throws Exception {
    try (PDDocument doc = new PDDocument()) {
      doc.getDocument().setVersion(1.7f);
      PDPage page = new PDPage(PDRectangle.A4);
      doc.addPage(page);
      PDType1Font helv = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
      PDResources res = new PDResources();
      COSName fn = res.add(helv);
      PDAcroForm form = new PDAcroForm(doc);
      doc.getDocumentCatalog().setAcroForm(form);
      form.setDefaultResources(res);
      form.setDefaultAppearance("/" + fn.getName() + " 12 Tf 0 g");
      try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
        cs.beginText(); cs.setFont(helv, 18); cs.newLineAtOffset(50, 780); cs.showText("Test Form"); cs.endText();
        float y = 730;
        for (String[] f : fields) {
          cs.beginText(); cs.setFont(helv, 12); cs.newLineAtOffset(50, y + 6); cs.showText(f[0] + ":"); cs.endText();
          y -= 40;
        }
      }
      float y = 730;
      for (String[] f : fields) {
        PDTextField tf = new PDTextField(form);
        tf.setPartialName(f[0]);
        tf.setDefaultAppearance("/" + fn.getName() + " 12 Tf 0 g");
        form.getFields().add(tf);
        PDAnnotationWidget w = tf.getWidgets().get(0);
        w.setRectangle(new PDRectangle(150, y, 300, 22));
        w.setPage(page);
        w.setPrinted(true);
        page.getAnnotations().add(w);
        if (!f[1].isEmpty()) tf.setValue(f[1]);
        else tf.setValue("");
        y -= 40;
      }
      doc.save(out);
    }
  }
}
