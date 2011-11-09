package com.surelogic.sierra.client.eclipse.views;

import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.ui.jobs.SLUIJob;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.model.AbstractDatabaseObserver;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;

public abstract class AbstractSierraViewMediator extends
		AbstractDatabaseObserver implements IViewMediator {
	public static final Logger LOG = SLLogger.getLogger();

	protected final IViewCallback f_view;

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
}
