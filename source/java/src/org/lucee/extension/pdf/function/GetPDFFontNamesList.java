package org.lucee.extension.pdf.function;

import org.lucee.extension.pdf.util.PDFBoxHelper;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.loader.engine.CFMLEngineFactory;

public final class GetPDFFontNamesList extends BIF implements Function {

	private static final long serialVersionUID = 1L;

	public static String call(PageContext pc, String path) throws PageException {
		try {
			return PDFBoxHelper.getPDFFontNamesList(path);
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length != 1) throw CFMLEngineFactory.getInstance().getExceptionUtil().createFunctionException(pc, "GetPDFFontNamesList", 1, 1, args.length);
		return call(pc, CFMLEngineFactory.getInstance().getCastUtil().toString(args[0]));
	}
}
