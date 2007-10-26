package com.surelogic.sierra.client.eclipse.views;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.surelogic.adhoc.DatabaseJob;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.model.AbstractDatabaseObserver;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;
import com.surelogic.sierra.jdbc.finding.SynchOverview;

public final class SynchronizeMediator extends AbstractDatabaseObserver {

	private final Table f_syncTable;
	private final Table f_eventsTable;

	public SynchronizeMediator(Table syncTable, Table eventsTable) {
		f_syncTable = syncTable;
		f_eventsTable = eventsTable;
	}

	public void init() {
		DatabaseHub.getInstance().addObserver(this);
	}

	public void dispose() {
		DatabaseHub.getInstance().removeObserver(this);
	}

	public void setFocus() {
		f_syncTable.setFocus();
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
				"Updating set of server synchronization events") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Updating list", IProgressMonitor.UNKNOWN);
				try {
					updateContents();
				} catch (Exception e) {
					return SLStatus
							.createErrorStatus(
									"Failed to update the set of server synchronization events",
									e);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void updateContents() throws Exception {
		Connection c = Data.getConnection();
		Exception exc = null;
		try {
			c.setAutoCommit(false);
			final List<SynchOverview> synchList = SynchOverview
					.listOverviews(c);
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					updateSyncTableContents(synchList);
				}
			});
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

	/**
	 * Must be called from the SWT thread.
	 */
	private void updateSyncTableContents(List<SynchOverview> synchList) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy/MM/dd 'at' HH:mm:ss");
		f_syncTable.removeAll();
		for (SynchOverview so : synchList) {
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
			item.setText(dateFormat.format(so.getTime()));
		}
		packTable(f_syncTable);
	}

	private void packTable(final Table table) {
		for (TableColumn col : table.getColumns()) {
			col.pack();
		}
	}
}
