package com.surelogic.sierra.client.eclipse.model;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;

import com.surelogic.common.logging.SLLogger;

public final class DatabaseHub {

	private static final DatabaseHub INSTANCE = new DatabaseHub();

	public static DatabaseHub getInstance() {
		return INSTANCE;
	}

	private DatabaseHub() {
		// singleton
	}

	private final Set<IDatabaseObserver> f_observers = new CopyOnWriteArraySet<IDatabaseObserver>();

	public void addObserver(final IDatabaseObserver o) {
		if (o == null) {
			return;
		}
		f_observers.add(o);
	}

	public void removeObserver(final IDatabaseObserver o) {
		f_observers.remove(o);
	}

	public void notifyScanLoaded() {
		if (SLLogger.getLogger().isLoggable(Level.FINE)) {
			SLLogger.getLogger().log(Level.FINE,
					"DatabaseHub notifyScanLoaded() to " + f_observers);
		}
		for (final IDatabaseObserver o : f_observers) {
			o.scanLoaded();
			o.changed();
		}
	}

	public void notifyProjectDeleted() {
		if (SLLogger.getLogger().isLoggable(Level.FINE)) {
			SLLogger.getLogger().log(Level.FINE,
					"DatabaseHub notifyProjectDeleted() to " + f_observers);
		}
		for (final IDatabaseObserver o : f_observers) {
			o.projectDeleted();
			o.changed();
		}
	}

	public void notifyServerSynchronized() {
		if (SLLogger.getLogger().isLoggable(Level.FINE)) {
			SLLogger.getLogger().log(Level.FINE,
					"DatabaseHub notifyServerSynchronized() to " + f_observers);
		}
		for (final IDatabaseObserver o : f_observers) {
			o.serverSynchronized();
			o.changed();
		}
	}
	
	public void notifyProjectSynchronized() {
		if (SLLogger.getLogger().isLoggable(Level.FINE)) {
			SLLogger.getLogger().log(Level.FINE,
					"DatabaseHub notifyProjectSynchronized() to " + f_observers);
		}
		for (final IDatabaseObserver o : f_observers) {
			o.projectSynchronized();
			o.changed();
		}
	}

	public void notifyFindingMutated() {
		if (SLLogger.getLogger().isLoggable(Level.FINE)) {
			SLLogger.getLogger().log(Level.FINE,
					"DatabaseHub notifyFindingMutated() to " + f_observers);
		}
		for (final IDatabaseObserver o : f_observers) {
			o.findingMutated();
			o.changed();
		}
	}

	public void notifyDatabaseDeleted() {
		if (SLLogger.getLogger().isLoggable(Level.FINE)) {
			SLLogger.getLogger().log(Level.FINE,
					"DatabaseHub notifyDatabaseDeleted() to " + f_observers);
		}
		for (final IDatabaseObserver o : f_observers) {
			o.databaseDeleted();
			o.changed();
		}
	}
}
