package com.surelogic.sierra.eclipse.teamserver.model;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;

public abstract class ServerLog {

	protected final ScheduledExecutorService f_executor;

	protected final StringBuffer f_logText = new StringBuffer();

	public final String getText() {
		return f_logText.toString();
	}

	public ServerLog(ScheduledExecutorService executor) {
		f_executor = executor;
	}

	abstract public void init();

	abstract public void dispose();

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
	protected void notifyObservers() {
		for (IServerLogObserver o : f_observers) {
			o.notify(this);
		}
	}
}
