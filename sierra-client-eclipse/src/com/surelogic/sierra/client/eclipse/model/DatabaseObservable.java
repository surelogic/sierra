package com.surelogic.sierra.client.eclipse.model;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This is a mix-in class to allow observers of the database to also have
 * observers of their own. The type of the observers supported is parameterized
 * and filled in by the sub-class.
 * 
 * @param <L>
 *            the type of observers this supports.
 */
public abstract class DatabaseObservable<L> extends AbstractDatabaseObserver {

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
			notifyThisObserver(o);
		}
	}

	protected abstract void notifyThisObserver(L o);
}
