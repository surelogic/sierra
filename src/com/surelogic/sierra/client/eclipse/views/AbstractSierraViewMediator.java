package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.sierra.client.eclipse.model.*;

public abstract class AbstractSierraViewMediator 
extends AbstractDatabaseObserver implements IViewMediator 
{
	public static final Listener DO_NOTHING = new Listener() {
		public void handleEvent(Event event) {
			// Do nothing
		}
	};
	
	protected final IViewCallback f_view;
	
	protected AbstractSierraViewMediator(IViewCallback cb) {
		f_view = cb;
	}
	
	public Listener getNoDataListener() {
		return DO_NOTHING;
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
