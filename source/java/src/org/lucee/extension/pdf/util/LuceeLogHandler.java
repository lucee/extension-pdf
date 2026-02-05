package org.lucee.extension.pdf.util;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import lucee.commons.io.log.Log;

/**
 * Bridges java.util.logging (used by OpenHTMLToPDF) to Lucee's logging framework.
 * This allows OpenHTMLToPDF log output to be controlled via Lucee admin.
 */
public class LuceeLogHandler extends Handler {

	private final Log log;

	public LuceeLogHandler(Log log) {
		this.log = log;
	}

	@Override
	public void publish(LogRecord record) {
		if (log == null || record == null) return;

		String src = record.getLoggerName();
		String msg = record.getMessage();
		Level level = record.getLevel();

		if (level.intValue() >= Level.SEVERE.intValue()) {
			log.error(src, msg);
		}
		else if (level.intValue() >= Level.WARNING.intValue()) {
			log.warn(src, msg);
		}
		else if (level.intValue() >= Level.INFO.intValue()) {
			log.info(src, msg);
		}
		else {
			log.debug(src, msg);
		}
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() {
	}
}
