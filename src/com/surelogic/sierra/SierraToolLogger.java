package com.surelogic.sierra;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class SierraToolLogger extends Formatter {
	public static final boolean f_useFluidLog = System
			.getProperty("no-fluid-logger") == null;

	/**
	 * Everyone can reuse the same instance of this formatter because the
	 * format() method uses no instance state.
	 */
	public static final SierraToolLogger f_fluidFormatter;

	/**
	 * A simple cache of loggers we have already configured.
	 */
	public static final Map<String, Logger> f_nameToLogger;

	static {
		if (f_useFluidLog) {
			f_fluidFormatter = new SierraToolLogger();
			f_nameToLogger = new HashMap<String, Logger>();
		} else {
			f_fluidFormatter = null;
			f_nameToLogger = null;
		}
	}

	/**
	 * Find or create a logger for a named subsystem. If a logger has already
	 * been created with the given name it is returned. Otherwise a new logger
	 * is created. If a new logger is created its log level will be configured
	 * to use a concise output format to the console (defined by this class) and
	 * it will <i>not</i> send logging output to its parent's handlers. It will
	 * be registered in the LogManager global namespace.
	 * <P>
	 * if the system property <tt>no-fluid-logger</tt> was set as a virtual
	 * machine argument then this method is equivalent to invoking:
	 * 
	 * <pre>
	 * java.util.logging.Logger.getLogger(name);
	 * </pre>
	 * 
	 * @param name
	 *            name for the logger. This should be a dot-separated name and
	 *            should normally be based on the package name or class name of
	 *            the subsystem, such as java.net or javax.swing
	 * @return a suitable Logger
	 * @throws NullPointerException
	 *             if the name is null
	 */
	public static Logger getLogger(final String name) {
		if (name == null)
			throw new NullPointerException("name must be non-null");

		if (f_useFluidLog) {
			synchronized (f_nameToLogger) {
				Logger resultLogger = f_nameToLogger.get(name); // lookup in
				// cache
				if (resultLogger == null) {
					resultLogger = Logger.getLogger(name);
					resultLogger.setUseParentHandlers(false);

					Handler consoleHandler = new ConsoleHandler();
					consoleHandler.setFormatter(f_fluidFormatter);
					resultLogger.addHandler(consoleHandler);

					try {
						Handler fileHandler = new FileHandler(
								"sierraToolLog.txt", true);
						fileHandler.setFormatter(f_fluidFormatter);
						resultLogger.addHandler(fileHandler);

					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					f_nameToLogger.put(name, resultLogger); // add to cache
				}
				return resultLogger;
			}
		} else {
			return Logger.getLogger(name);
		}
	}

	/**
	 * Format the given log record and return the formatted string. The
	 * resulting formatted String for a ConciseFormatter outputs single lines
	 * using the following pattern:
	 * <p>[ <i>LEVEL </i>" <i>thread name </i>"] <i>message </i>[
	 * <i>package.class.method() </i>]
	 * <p>
	 * The {@link #formatMessage(LogRecord)} convenience method is used to
	 * localize and format the message field.
	 * 
	 * @param record
	 *            the log record to be formatted.
	 * @return the formatted log record.
	 */
	@Override
	public String format(final LogRecord record) {
		StringBuilder buf = new StringBuilder(1000);
		buf.append("[");
		buf.append(record.getLevel().getName());
		buf.append(" \"");
		buf.append(Thread.currentThread().getName());
		buf.append("\"");
		buf.append(Calendar.getInstance().getTime().toString());
		buf.append("]");
		buf.append(formatMessage(record));
		buf.append(" [");
		buf.append(record.getSourceClassName());
		buf.append(".");
		buf.append(record.getSourceMethodName());
		buf.append("()]");
		buf.append('\n');
		Throwable t = record.getThrown();
		if (t != null) {
			StringWriter sw = new StringWriter();
			t.printStackTrace(new PrintWriter(sw));
			buf.append(sw.toString());
		}
		return buf.toString();
	}
}
