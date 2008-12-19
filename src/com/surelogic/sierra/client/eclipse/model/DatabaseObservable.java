package com.surelogic.sierra.client.eclipse.model;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class DatabaseObservable<L> 
extends AbstractDatabaseObserver {
	private final Set<L> f_observers = new CopyOnWriteArraySet<L>();

	public final void addObserver(final L o) {
		if (o == null)
			return;
		f_observers.add(o);
	}

	public final void removeObserver(final L o) {
		f_observers.remove(o);
	}

	public void notifyObservers() {
		for (L o : f_observers) {
			notifyObserver(o);
		}
	}

	protected abstract void notifyObserver(L o);
}
