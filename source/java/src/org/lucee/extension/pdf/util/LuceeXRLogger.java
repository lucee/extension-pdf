package org.lucee.extension.pdf.util;

import java.util.logging.Level;

import com.openhtmltopdf.util.Diagnostic;
import com.openhtmltopdf.util.XRLog;
import com.openhtmltopdf.util.XRLogger;

import lucee.commons.io.log.Log;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;

/**
 * Routes OpenHTMLToPDF log output to Lucee's pdf log by replacing the default
 * XRLogger via {@link XRLog#setLoggerImpl}. Looks up the current request's Log
 * on every call so the bridge is multi-context safe and picks up admin reconfig.
 */
public class LuceeXRLogger implements XRLogger {

	private static volatile boolean installed = false;

	public static synchronized void install() {
		if (installed) return;
		XRLog.setLoggerImpl(new LuceeXRLogger());
		installed = true;
	}

	@Override
	public void log(Diagnostic d) {
		Log log = currentLog();
		if (log == null) return;
		String src = trimSource(d.getLogMessageId().getWhere());
		String msg = d.getFormattedMessage();
		int level = mapLevel(d.getLevel());
		if (d.hasError()) log.log(level, src, msg, d.getError());
		else log.log(level, src, msg);
	}

	@Override
	public void log(String where, Level level, String msg) {
		Log log = currentLog();
		if (log == null) return;
		log.log(mapLevel(level), trimSource(where), msg);
	}

	@Override
	public void log(String where, Level level, String msg, Throwable th) {
		Log log = currentLog();
		if (log == null) return;
		log.log(mapLevel(level), trimSource(where), msg, th);
	}

	@Override
	public void setLevel(String logger, Level level) {
		// no-op: level filtering happens at the Lucee Log
	}

	@Override
	public boolean isLogLevelEnabled(Diagnostic d) {
		return true;
	}

	private static Log currentLog() {
		PageContext pc = CFMLEngineFactory.getInstance().getThreadPageContext();
		if (pc == null) return null;
		return pc.getConfig().getLog("pdf");
	}

	private static int mapLevel(Level level) {
		if (level == null) return Log.LEVEL_INFO;
		int v = level.intValue();
		if (v >= Level.SEVERE.intValue()) return Log.LEVEL_ERROR;
		if (v >= Level.WARNING.intValue()) return Log.LEVEL_WARN;
		if (v >= Level.INFO.intValue()) return Log.LEVEL_INFO;
		if (v >= Level.FINE.intValue()) return Log.LEVEL_DEBUG;
		return Log.LEVEL_TRACE;
	}

	private static String trimSource(String where) {
		if (where == null) return "openhtmltopdf";
		int dot = where.lastIndexOf('.');
		return dot < 0 ? where : where.substring(dot + 1);
	}
}
