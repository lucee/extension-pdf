/**
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
package org.lucee.extension.pdf.function;

import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.xml.DomXmpParser;
import org.lucee.extension.pdf.PDFStruct;
import org.lucee.extension.pdf.util.PDFUtil;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;

/**
 * Checks whether a PDF file claims PDF/A conformance via XMP metadata.
 * Returns true if the PDF contains a pdfaid:part declaration (PDF/A-1, 2, 3, or 4).
 * This checks the document's self-declaration, not full conformance validation.
 */
public final class IsPDFArchive extends BIF implements Function {

	private static final long serialVersionUID = 6909679675833681678L;

	public static boolean call( PageContext pc, String path ) throws PageException {
		try {
			Resource res = CFMLEngineFactory.getInstance().getResourceUtil().toResourceExisting( pc, path );
			PDFStruct pdfStruct = PDFUtil.toPDFStruct( pc, res, null );

			try (PDDocument pdDoc = pdfStruct.toPDDocument()) {
				PDMetadata meta = pdDoc.getDocumentCatalog().getMetadata();
				if (meta == null) return false;

				try (InputStream xmpStream = meta.createInputStream()) {
					DomXmpParser parser = new DomXmpParser();
					XMPMetadata xmp = parser.parse( xmpStream );
					PDFAIdentificationSchema pdfaId = xmp.getPDFAIdentificationSchema();
					return pdfaId != null && pdfaId.getPart() != null;
				}
			}
		}
		catch (Exception e) {
			return false;
		}
	}

	@Override
	public Object invoke( PageContext pc, Object[] args ) throws PageException {
		if (args.length != 1)
			throw CFMLEngineFactory.getInstance().getExceptionUtil()
				.createFunctionException( pc, "IsPDFArchive", 1, 1, args.length );

		return call( pc, CFMLEngineFactory.getInstance().getCastUtil().toString( args[0] ) );
	}
}
