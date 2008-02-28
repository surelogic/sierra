package com.surelogic.sierra.eclipse.teamserver.model;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
	 * <p>
	 * All accesses must be protected by a lock on <code>this</code>.
	 */
	private boolean f_isRunning = false;

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
	public synchronized boolean isRunning() {
		return f_isRunning;
	}

	/**
	 * This flag indicates that this has positively detected a team server is
	 * not running. It is only updated periodically.
	 * <p>
	 * It is not useful to use this flag to detect that a team server is
	 * running.
	 * <p>
	 * All accesses must be protected by a lock on <code>this</code>.
	 */
	private boolean f_isNotRunning = false;

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
	public synchronized boolean isNotRunning() {
		return f_isNotRunning;
	}

	/**
	 * Indicates that a team server start has been run and we are waiting to
	 * detect the server is running.
	 * <p>
	 * All accesses must be protected by a lock on <code>this</code>.
	 */
	private boolean f_inStart = false;

	/**
	 * Indicates that a team server stop has been run and we are waiting to
	 * detect the server is not running.
	 * <p>
	 * All accesses must be protected by a lock on <code>this</code>.
	 */
	private boolean f_inStop = false;

	private final ScheduledExecutorService f_executor = Executors
			.newScheduledThreadPool(1);

	public void init() {
		final Runnable checkIfServerIsRunning = new Runnable() {
			public void run() {
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

				boolean notifyObservers = false;
				synchronized (TeamServer.this) {
					final boolean oldIsRunning = f_isRunning;
					final boolean oldIsNotRunning = f_isNotRunning;

					if (f_inStart) {
						if (isRunning) {
							f_inStart = false;
							f_isRunning = true;
							notifyObservers = true;
						}
					} else if (f_inStop) {
						if (isNotRunning) {
							f_inStop = false;
							f_isNotRunning = true;
							notifyObservers = true;
						}
					} else {
						if (oldIsRunning != isRunning) {
							f_isRunning = isRunning;
							notifyObservers = true;
						}
						if (oldIsNotRunning != isNotRunning) {
							f_isNotRunning = isNotRunning;
							notifyObservers = true;
						}
					}

				}
				if (notifyObservers) {
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

	/**
	 * Callers should never be holding a lock due to the potential for deadlock.
	 */
	private void notifyObservers() {
		for (ITeamServerObserver o : f_observers) {
			o.notify(this);
		}
	}

	private static final String JETTY_DIR = "jetty" + File.separator;
	private static final String START_JAR = JETTY_DIR + "start.jar";
	private static final String JETTY_CONFIG = JETTY_DIR + "etc"
			+ File.separator + "sierra-embedded-derby.xml";
	private static final String JETTY_STOP_PORT = "STOP.PORT";
	private static final String JETTY_STOP_KEY = "STOP.KEY";
	private static final String JETTY_STOP_ARG = "--stop";

	public void start() {
		CommandlineJava command = getJettyTemplate();

		command.setMaxmemory("512m"); // TODO configure this better

		final String jettyConfig = launder(f_pluginDir + JETTY_CONFIG);
		Argument jettyConfigFile = command.createArgument();
		jettyConfigFile.setValue(jettyConfig);

		runJava(command);

		synchronized (this) {
			f_inStart = true;
			f_isRunning = false;
			f_isNotRunning = false;
		}
		notifyObservers();
	}

	public void stop() {
		CommandlineJava command = getJettyTemplate();

		Argument jettyStop = command.createArgument();
		jettyStop.setValue(JETTY_STOP_ARG);

		runJava(command);

		synchronized (this) {
			f_inStop = true;
			f_isRunning = false;
			f_isNotRunning = false;
		}
		notifyObservers();
	}

	private CommandlineJava getJettyTemplate() {
		final CommandlineJava command = new CommandlineJava();

		final String startJar = launder(f_pluginDir + START_JAR);
		command.setJar(startJar);

		final Environment.Variable stopPort = new Environment.Variable();
		stopPort.setKey(JETTY_STOP_PORT);
		stopPort.setValue(Integer.toString(f_port.get() + 1));
		command.addSysproperty(stopPort);

		final Environment.Variable stopKey = new Environment.Variable();
		stopKey.setKey(JETTY_STOP_KEY);
		stopKey.setValue("local");
		command.addSysproperty(stopKey);

		return command;
	}

	private void runJava(final CommandlineJava command) {
		if (SLLogger.getLogger().isLoggable(Level.FINE)) {
			SLLogger.getLogger().fine(command.toString());
		}
		ProcessBuilder b = new ProcessBuilder(command.getCommandline());
		b.directory(launderToFile(f_pluginDir + JETTY_DIR));
		try {
			b.start();
		} catch (IOException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					I18N.err(65, command.toString()), e);
		}
	}

	private String launder(final String pathfile) {
		return launderToFile(pathfile).getAbsolutePath();
	}

	private File launderToFile(final String pathfile) {
		final File file = new File(pathfile);
		if (!file.exists()) {
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(74, pathfile));
		}
		return file;
	}
}
