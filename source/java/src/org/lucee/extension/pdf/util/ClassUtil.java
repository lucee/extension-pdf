package org.lucee.extension.pdf.util;

import java.io.OutputStream;
import java.lang.reflect.Method;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;

public class ClassUtil {

	private static final Class<?>[] EMPTY_CLASS = new Class[0];
	private static final Object[] EMPTY_OBJ = new Object[0];

	public static OutputStream getResponseStream(PageContext pc) throws PageException, RuntimeException {
		try {
			Method method = pc.getClass().getMethod("getResponseStream", EMPTY_CLASS);
			return (OutputStream) method.invoke(pc, EMPTY_OBJ);
		} 
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	public static Object getRootOut(PageContext pc) throws PageException {
		try {
			Method method = pc.getClass().getMethod("getRootOut", EMPTY_CLASS);
			return method.invoke(pc, EMPTY_OBJ);
		} 
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	public static void setClosed(Object cfmlWriter, boolean closed) throws PageException, RuntimeException { 
		try {
			Method method = cfmlWriter.getClass().getMethod("setClosed", new Class[]{boolean.class});
			method.invoke(cfmlWriter, new Object[]{closed});
		} 
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	public static String ContractPath(PageContext pc, String abs) throws PageException {
		
		try {
			BIF bif = CFMLEngineFactory.getInstance().getClassUtil().loadBIF(pc, "lucee.runtime.functions.system.ContractPath");
			return (String)bif.invoke(pc, new Object[]{abs});
		} 
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	public static String GetDirectoryFromPath(PageContext pc, String path) throws PageException {
		
		try {
			BIF bif = CFMLEngineFactory.getInstance().getClassUtil().loadBIF(pc, "lucee.runtime.functions.system.GetDirectoryFromPath");
			return (String)bif.invoke(pc, new Object[]{path});
		} 
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}
}
