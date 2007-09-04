package com.surelogic.sierra.client.eclipse.model;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class DatabaseModel {

	private static final DatabaseModel INSTANCE = new DatabaseModel();

	public static DatabaseModel getInstance() {
		return INSTANCE;
	}

	private DatabaseModel() {
		// singleton
	}

	private final Set<IDatabaseObserver> f_observers = new CopyOnWriteArraySet<IDatabaseObserver>();

	public void addObserver(final IDatabaseObserver o) {
		f_observers.add(o);
	}

	public void removeObserver(final IDatabaseObserver o) {
		f_observers.remove(o);
	}

	public void notifyScanLoaded() {
		for (IDatabaseObserver o : f_observers)
			o.scanLoaded();
	}
}
