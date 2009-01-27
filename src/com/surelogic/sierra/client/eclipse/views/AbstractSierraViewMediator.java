package com.surelogic.sierra.client.eclipse.views;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.model.AbstractDatabaseObserver;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;

public abstract class AbstractSierraViewMediator extends
		AbstractDatabaseObserver implements IViewMediator {
	public static final Logger LOG = SLLogger.getLogger();

	protected final IViewCallback f_view;
	private final AtomicLong latestUpdate = new AtomicLong(System.currentTimeMillis());
	
	protected AbstractSierraViewMediator(IViewCallback cb) {
		f_view = cb;
	}

	public Listener getNoDataListener() {
		return null;
	}

	public void init() {
		DatabaseHub.getInstance().addObserver(this);
	}

	public void dispose() {
		DatabaseHub.getInstance().removeObserver(this);
	}

	protected static final void asyncUpdateContentsForUI(final IViewUpdater vu) {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				vu.updateContentsForUI();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
	
	/**	 
	 * @return the current time
	 */
	protected final long startingUpdate() {
		long now = System.currentTimeMillis();
		latestUpdate.set(now);
		return now;
	}
	
	protected final boolean continueUpdate(long startTime) {
		final long latest = latestUpdate.get();
		return startTime >= latest;
	}
	
	protected final void finishedUpdate(long startTime) {
		latestUpdate.compareAndSet(startTime, startTime+1);
	}
}
