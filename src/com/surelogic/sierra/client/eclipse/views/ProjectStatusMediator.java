package com.surelogic.sierra.client.eclipse.views;

import java.sql.Connection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.actions.NewScanDialogAction;
import com.surelogic.sierra.client.eclipse.model.*;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.jdbc.finding.SynchOverview;

public final class ProjectStatusMediator extends AbstractSierraViewMediator 
implements IViewUpdater {
	private final Tree f_statusTree;

	public ProjectStatusMediator(IViewCallback cb, Tree statusTree) {
		super(cb);
		f_statusTree = statusTree;
	}

	public String getHelpId() {
		return "com.surelogic.sierra.client.eclipse.view-project-status";
	}

	public String getNoDataId() {
		return "sierra.eclipse.noDataProjectStatus";
	}
	
	@Override
	public Listener getNoDataListener() {
		return new Listener() {
			public void handleEvent(Event event) {
				new NewScanDialogAction().run(null);
			}
		};
	}
	
	@Override 
	public void init() {
		super.init();
		/*
		f_statusTree.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				updateEventTable();
			}
		});
		*/
		asyncUpdateContents();
	}

	public void setFocus() {
		f_statusTree.setFocus();
	}

	@Override
	public void projectDeleted() {
		asyncUpdateContents();
	}

	@Override
	public void serverSynchronized() {
		asyncUpdateContents();
	}

	private void asyncUpdateContents() {
		final Job job = new DatabaseJob(
				"Updating project status") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Updating list", IProgressMonitor.UNKNOWN);
				try {
					updateContents();
				} catch (Exception e) {
					final int errNo = 58; // FIX
					final String msg = I18N.err(errNo);
					return SLStatus.createErrorStatus(errNo, msg, e);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void updateContents() throws Exception {
		Connection c = Data.transactionConnection();
		Exception exc = null;
		try {
			final List<SynchOverview> synchList = SynchOverview
					.listOverviews(c);
			asyncUpdateContentsForUI(this);
			c.commit();
			DatabaseHub.getInstance().notifyFindingMutated();
		} catch (Exception e) {
			c.rollback();
			exc = e;
		} finally {
			try {
				c.close();
			} finally {
				if (exc != null) {
					throw exc;
				}
			}
		}
	}

	public void updateContentsForUI() {
		final boolean hideEmpty = PreferenceConstants.hideEmptySynchronizeEntries();
		//final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm:ss");
		f_statusTree.removeAll();
		/*
		for (SynchOverview so : synchList) {
			if (hideEmpty && so.isEmpty()) {
				continue;
			}
			final TableItem item = new TableItem(f_syncTable, SWT.NONE);
			final String projectName = so.getProject();
			final SierraServer server = SierraServerManager.getInstance()
					.getServer(projectName);
			final String serverName;
			if (server != null) {
				serverName = server.getLabel();
			} else {
				serverName = "(unknown)";
			}
			item.setText(0, projectName);
			item.setImage(0, SLImages
					.getWorkbenchImage(IDE.SharedImages.IMG_OBJ_PROJECT));
			item.setText(1, serverName);
			item.setImage(SLImages.getImage(SLImages.IMG_SIERRA_SERVER));
			item.setText(2, dateFormat.format(so.getTime()));
			item.setData(so);
		}

		if ((synchList.isEmpty()) || (Projects.getInstance().isEmpty())) {
			f_statusTree.setVisible(false);
		}
		*/
		f_statusTree.pack();
	}
}
