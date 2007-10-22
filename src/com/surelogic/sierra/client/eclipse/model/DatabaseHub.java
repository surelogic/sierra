package com.surelogic.sierra.client.eclipse.model;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

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
		if (o == null)
			return;
		f_observers.add(o);
	}

	public void removeObserver(final IDatabaseObserver o) {
		f_observers.remove(o);
	}

	public void notifyScanLoaded() {
		for (IDatabaseObserver o : f_observers) {
			o.scanLoaded();
			o.changed();
		}
	}

	public void notifyProjectDeleted() {
		for (IDatabaseObserver o : f_observers) {
			o.projectDeleted();
			o.changed();
		}
	}

	public void notifyServerSynchronized() {
		for (IDatabaseObserver o : f_observers) {
			o.serverSynchronized();
			o.changed();
		}
	}

	public void notifyFindingMutated() {
		for (IDatabaseObserver o : f_observers) {
			o.findingMutated();
			o.changed();
		}
	}
}
