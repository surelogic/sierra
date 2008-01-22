package com.surelogic.sierra.client.eclipse.jetty;

import java.util.logging.Level;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.log.Log;
import org.mortbay.log.Logger;

import com.surelogic.common.logging.SLLogger;

public final class EmbeddedJettyUtility {

	private EmbeddedJettyUtility() {
		// no instances
	}

	private static volatile Server f_server = null;

	public static void startup() throws Exception {
		/*
		 * As best we can wrap the Jetty logger to the SureLogic logger.
		 */
		Log.setLog(new Logger() {
			final java.util.logging.Logger f_log = SLLogger.getLogger();

			public void debug(String arg0, Throwable arg1) {
				f_log.log(Level.FINEST, arg0, arg1);
			}

			public void debug(String arg0, Object arg1, Object arg2) {
				final StringBuilder b = new StringBuilder(arg0);
				if (arg1 != null) {
					b.append(" <").append(arg1).append(">");
				}
				if (arg2 != null) {
					b.append(" <").append(arg2).append(">");
				}
				f_log.log(Level.FINEST, b.toString());
			}

			public Logger getLogger(String arg0) {
				return this;
			}

			public void info(String arg0, Object arg1, Object arg2) {
				final StringBuilder b = new StringBuilder(arg0);
				if (arg1 != null) {
					b.append(" <").append(arg1).append(">");
				}
				if (arg2 != null) {
					b.append(" <").append(arg2).append(">");
				}
				f_log.log(Level.INFO, b.toString());
			}

			public boolean isDebugEnabled() {
				return f_log.isLoggable(Level.FINEST);
			}

			public void setDebugEnabled(boolean arg0) {
				// TODO Auto-generated method stub
			}

			public void warn(String arg0, Throwable arg1) {
				f_log.log(Level.WARNING, arg0, arg1);
			}

			public void warn(String arg0, Object arg1, Object arg2) {
				final StringBuilder b = new StringBuilder(arg0);
				if (arg1 != null) {
					b.append(" <").append(arg1).append(">");
				}
				if (arg2 != null) {
					b.append(" <").append(arg2).append(">");
				}
				f_log.log(Level.WARNING, b.toString());
			}
		});
		int port = 8080;
		final String portString = System.getProperty("sierra.jetty.port",
				Integer.toString(port));
		try {
			port = Integer.parseInt(portString);
		} catch (NumberFormatException ignore) {
			// use the default value
		}
		f_server = new Server(port);
		final Context root = new Context(f_server, "/chart", Context.SESSIONS);

		root.addServlet(new ServletHolder(new ImportanceChartServlet()),
				"/importance.png");
		final Context sc = new Context(f_server, "/static", Context.SESSIONS);
		sc.setResourceBase(System.getProperty("webbase", "set -Dwebbase"));
		sc.addServlet(new ServletHolder(new DefaultServlet()), "/");

		f_server.start();
		SLLogger.getLogger().info(
				"Embedded Jetty started listening on port " + port);
	}

	public static void shutdown() throws Exception {
		if (f_server != null) {
			f_server.stop();
			SLLogger.getLogger().info("Embedded Jetty shutdown");
			f_server = null;
		}
	}
}
