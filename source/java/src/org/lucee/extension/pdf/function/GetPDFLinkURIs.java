package org.lucee.extension.pdf.function;

import java.util.List;

import org.lucee.extension.pdf.util.PDFBoxHelper;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Array;
import lucee.loader.engine.CFMLEngineFactory;

public final class GetPDFLinkURIs extends BIF implements Function {

	private static final long serialVersionUID = 1L;

	public static Array call(PageContext pc, String path) throws PageException {
		try {
			List<String> uris = PDFBoxHelper.getLinkURIs(path);
			return CFMLEngineFactory.getInstance().getCastUtil().toArray(uris);
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length != 1) throw CFMLEngineFactory.getInstance().getExceptionUtil().createFunctionException(pc, "GetPDFLinkURIs", 1, 1, args.length);
		return call(pc, CFMLEngineFactory.getInstance().getCastUtil().toString(args[0]));
	}
}
