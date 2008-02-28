package com.surelogic.sierra.eclipse.teamserver.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.surelogic.common.FileUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

public abstract class ServerLog {

	private final ScheduledExecutorService f_executor;

	private final StringBuffer f_logText = new StringBuffer();

	public final String getText() {
		return f_logText.toString();
	}

	public ServerLog(ScheduledExecutorService executor) {
		f_executor = executor;
	}

	public final void init() {
		final Runnable checkIfServerIsRunning = new Runnable() {
			public void run() {
				boolean notifyObservers = false;
				final File logFile = getLatestLogFile();
				if (logFile == null) {
					if (!"".equals(f_logText.toString())) {
						f_logText.setLength(0);
						notifyObservers = true;
					}
				} else {
					final String contents = getTextFileContents(logFile);
					if (!contents.equals(f_logText.toString())) {
						final int lengthChar = f_logText.length();
						f_logText.replace(0, lengthChar, contents);
						notifyObservers = true;
					}
				}
				if (notifyObservers)
					notifyObservers();
			}
		};
		f_executor.scheduleWithFixedDelay(checkIfServerIsRunning, 1L, 5L,
				TimeUnit.SECONDS);
	}

	public final void dispose() {
		// nothing to do
	}

	private final CopyOnWriteArraySet<IServerLogObserver> f_observers = new CopyOnWriteArraySet<IServerLogObserver>();

	public final void addObserver(final IServerLogObserver observer) {
		f_observers.add(observer);
	}

	public final void removeObserver(final IServerLogObserver observer) {
		f_observers.remove(observer);
	}

	/**
	 * Callers should never be holding a lock due to the potential for deadlock.
	 */
	private void notifyObservers() {
		for (IServerLogObserver o : f_observers) {
			o.notify(this);
		}
	}

	private File getSierraServerDir() {
		File sd = new File(FileUtility.getSierraDataDirectory()
				+ File.separator + "server");
		return sd;
	}

	private File getLatestLogFile() {
		File result = null;
		File sd = getSierraServerDir();
		if (sd.exists() && sd.isDirectory()) {
			File[] contents = sd.listFiles(getFilenameFilter());
			if (contents.length > 0) {
				result = contents[0];
				for (File f : contents) {
					if (f.lastModified() > result.lastModified())
						result = f;
				}
			}
		}
		return result;
	}

	/**
	 * Returns the filename filter to files the contents of the Sierra team
	 * server directory to only those log file.
	 * <p>
	 * The set of files this filter returns will be examined to determine the
	 * log file that was modified last. This is the file that will be displayed.
	 * 
	 * @return a file name filter.
	 */
	protected abstract FilenameFilter getFilenameFilter();

	private String getTextFileContents(final File file) {
		final StringBuilder result = new StringBuilder();
		try {
			final BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			while ((line = in.readLine()) != null) {
				result.append(line);
				result.append("\n");
			}
			in.close();
		} catch (IOException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					I18N.err(40, file.getAbsolutePath()), e);
		}
		return result.toString();
	}
}
