package com.surelogic.sierra.client.eclipse.model;

import java.util.concurrent.atomic.AtomicLong;

public class AbstractUpdatable {
	private final AtomicLong latestUpdate = new AtomicLong(System.currentTimeMillis());
	
	/**	 
	 * To be called when starting the update
	 * @return the current time
	 */
	protected final long startingUpdate() {
		long now = System.currentTimeMillis();
		latestUpdate.set(now);
		return now;
	}
	
	/**
	 * Called to check if we should continue or cancel
	 * the update, due to newer updates
	 * 
	 * @return true if continuing the update
	 */
	protected final boolean continueUpdate(long startTime) {
		final long latest = latestUpdate.get();
		return startTime >= latest;
	}
	
	/**
	 * To be called when finishing the update
	 * @param startTime
	 */
	protected final void finishedUpdate(long startTime) {
		latestUpdate.compareAndSet(startTime, startTime+1);
	}
}
