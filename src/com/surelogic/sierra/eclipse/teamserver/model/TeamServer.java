package com.surelogic.sierra.eclipse.teamserver.model;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Commandline.Argument;

import com.surelogic.common.eclipse.Activator;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

public final class TeamServer {

	private static final String LOCALHOST = "localhost";

	private final AtomicInteger f_port;

	private final String f_pluginDir;

	public TeamServer(final int port) {
		f_port = new AtomicInteger(port);
		f_pluginDir = Activator.getDefault().getDirectoryOf(
				com.surelogic.sierra.eclipse.teamserver.Activator.PLUGIN_ID);
	}

	/**
	 * This flag indicates that this has positively detected a running team
	 * server. It is only updated periodically.
	 * <p>
	 * It is not useful to use this flag to detect that a team server is <i>not</i>
	 * running.
	 */
	private final AtomicBoolean f_isRunning = new AtomicBoolean(false);

	/**
	 * Indicates positive detection of a running team server. It is only updated
	 * periodically.
	 * <p>
	 * This method is not a reliable means of determining that a team server is
	 * <i>not</i> running. Use {@link #isNotRunning()} for this purpose.
	 * 
	 * @return <code>true</code> if this has positively detected a running
	 *         team server, <code>false</code> otherwise.
	 */
	public boolean isRunning() {
		return f_isRunning.get();
	}

	/**
	 * This flag indicates that this has positively detected a team server is
	 * not running. It is only updated periodically.
	 * <p>
	 * It is not useful to use this flag to detect that a team server is
	 * running.
	 */
	private final AtomicBoolean f_isNotRunning = new AtomicBoolean(false);

	/**
	 * Indicates positive detection that a team server is not running. It is
	 * only updated periodically.
	 * <p>
	 * This method is not a reliable means of determining that a team server is
	 * running. Use {@link #isRunning()} for this purpose.
	 * 
	 * @return <code>true</code> if this has positively detected that a team
	 *         server is not running, <code>false</code> otherwise.
	 */
	public boolean isNotRunning() {
		return f_isNotRunning.get();
	}

	private final ScheduledExecutorService f_executor = Executors
			.newScheduledThreadPool(1);

	public void init() {
		final Runnable checkIfServerIsRunning = new Runnable() {
			@Override
			public void run() {
				final boolean oldIsRunning = f_isRunning.get();
				final boolean oldIsNotRunning = f_isNotRunning.get();
				boolean isRunning;
				boolean isNotRunning;

				try {
					Socket s = new Socket(LOCALHOST, f_port.get());
					if (SLLogger.getLogger().isLoggable(Level.FINEST)) {
						SLLogger
								.getLogger()
								.finest(
										"(periodic check) A local team server is running.");
					}
					isRunning = true;
					isNotRunning = false;
					s.close();
				} catch (UnknownHostException e) {
					SLLogger.getLogger().log(Level.SEVERE,
							I18N.err(63, LOCALHOST), e);
					isRunning = false;
					isNotRunning = false;
				} catch (IOException e) {
					if (SLLogger.getLogger().isLoggable(Level.FINEST)) {
						SLLogger
								.getLogger()
								.finest(
										"(periodic check) A local team server is not running.");
					}
					isRunning = false;
					isNotRunning = true;
				}

				boolean somethingChanged = false;
				if (oldIsRunning != isRunning) {
					f_isRunning.set(isRunning);
					somethingChanged = true;
				}
				if (oldIsNotRunning != isNotRunning) {
					f_isNotRunning.set(isNotRunning);
					somethingChanged = true;
				}
				if (somethingChanged) {
					notifyObservers();
				}
			}
		};
		f_executor.scheduleWithFixedDelay(checkIfServerIsRunning, 2L, 5L,
				TimeUnit.SECONDS);
	}

	public void dispose() {
		f_executor.shutdown();
	}

	private final CopyOnWriteArraySet<ITeamServerObserver> f_observers = new CopyOnWriteArraySet<ITeamServerObserver>();

	public void addObserver(final ITeamServerObserver observer) {
		f_observers.add(observer);
	}

	public void removeObserver(final ITeamServerObserver observer) {
		f_observers.remove(observer);
	}

	private void notifyObservers() {
		for (ITeamServerObserver o : f_observers) {
			o.notify(this);
		}
	}

	private static final String START_JAR = "jetty" + File.separator
			+ "start.jar";

	private static final String JETTY_CONFIG = "jetty" + File.separator + "etc"
			+ File.separator + "sierra-embedded-derby.xml";

	private static final String JETTY_STOP_PORT = "STOP.PORT";

	private static final String JETTY_STOP_ARG = "--stop";

	public void start() {
		CommandlineJava command = getJettyTemplate();

		command.setMaxmemory("512m"); // TODO configure this better

		final String jettyConfig = f_pluginDir + JETTY_CONFIG;
		Argument jettyConfigFile = command.createArgument();
		jettyConfigFile.setValue(jettyConfig);

		runJava(command);
	}

	public void stop() {
		CommandlineJava command = getJettyTemplate();

		Argument jettyStop = command.createArgument();
		jettyStop.setValue(JETTY_STOP_ARG);

		runJava(command);
	}

	private CommandlineJava getJettyTemplate() {
		final CommandlineJava command = new CommandlineJava();

		final String startJar = f_pluginDir + START_JAR;
		command.setJar(startJar);

		final Environment.Variable stopPort = new Environment.Variable();
		stopPort.setKey(JETTY_STOP_PORT);
		stopPort.setValue(Integer.toString(f_port.get() + 1));
		command.addSysproperty(stopPort);

		return command;
	}

	private void runJava(final CommandlineJava command) {
		if (SLLogger.getLogger().isLoggable(Level.FINE)) {
			SLLogger.getLogger().fine(command.toString());
		}
		ProcessBuilder b = new ProcessBuilder(command.getCommandline());
		try {
			b.start();
		} catch (IOException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					I18N.err(65, command.toString()), e);
		}
	}
}
