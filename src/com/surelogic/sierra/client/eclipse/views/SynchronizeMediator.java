package com.surelogic.sierra.client.eclipse.views;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ide.IDE;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBTransaction;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.actions.SynchronizeProjectDialogAction;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.jdbc.finding.AuditDetail;
import com.surelogic.sierra.jdbc.finding.SynchDetail;
import com.surelogic.sierra.jdbc.finding.SynchOverview;

public final class SynchronizeMediator extends AbstractSierraViewMediator {

	private final Table f_syncTable;

	public SynchronizeMediator(IViewCallback cb, Table syncTable) {
		super(cb);
		f_syncTable = syncTable;
	}

	public String getHelpId() {
		return "com.surelogic.sierra.client.eclipse.view-synchronize-history";
	}

	public String getNoDataId() {
		return "sierra.eclipse.noDataSynchronizeHistory";
	}

	@Override
	public Listener getNoDataListener() {
		return new Listener() {
			public void handleEvent(Event event) {
				new SynchronizeProjectDialogAction().run();
			}
		};
	}

	@Override
	public void init() {
		super.init();
		// f_syncTable.addListener(SWT.Selection, new Listener() {
		// public void handleEvent(Event event) {
		// //updateEventTable();
		// }
		// });
		// f_eventsTable.addListener(SWT.Selection, new Listener() {
		// public void handleEvent(Event event) {
		// focusOnFindingId();
		// }
		// });
		asyncUpdateContents();
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
					final int errNo = 58;
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
		Data.withReadOnly(new DBTransaction<Void>() {
			public Void perform(Connection conn) throws Exception {
				final List<SynchOverview> synchList = SynchOverview
						.listOverviews(conn);
				asyncUpdateContentsForUI(new IViewUpdater() {
					public void updateContentsForUI() {
						updateSyncTableContents(synchList);
					}
				});
				return null;
			}
		});
	}

	/**
	 * Must be called from the SWT thread.
	 */
	private void updateSyncTableContents(List<SynchOverview> synchList) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy/MM/dd 'at' HH:mm:ss");
		f_syncTable.removeAll();

		System.out.println("updateSyncTableContents: " + synchList);

		for (SynchOverview so : synchList) {
			if (PreferenceConstants.hideEmptySynchronizeEntries()
					&& so.isEmpty()) {
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
			item.setText(2, dateFormat.format(so.getTime()));
			item.setData(so);
			final int numCommitted = so.getNumCommitted();
			final int numReceived = so.getNumReceived();
			item.setText(3, numCommitted
					+ (numCommitted == 1 ? " audit" : " audits") + " sent and "
					+ numReceived + (numReceived == 1 ? " audit" : " audits")
					+ " received");
		}

		f_view.hasData(!synchList.isEmpty()
				&& !Projects.getInstance().isEmpty());
		packTable(f_syncTable);
	}

	private void asyncEventTableContents(final SynchOverview so) {
		final Job job = new DatabaseJob(
				"Updating reading events from server synchronize") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Updating list", IProgressMonitor.UNKNOWN);
				try {
					updateEventTableContents(so);
				} catch (Exception e) {
					final int errNo = 59;
					final String msg = I18N.err(errNo);
					return SLStatus.createErrorStatus(errNo, msg, e);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void updateEventTableContents(final SynchOverview so)
			throws Exception {
		Data.withReadOnly(new DBTransaction<Void>() {
			public Void perform(Connection conn) throws Exception {
				// TODO Auto-generated method stub
				return null;
			}
		});
		Connection c = Data.transactionConnection();
		Exception exc = null;
		try {
			SynchDetail sd = SynchDetail.getSyncDetail(c, so);
			final List<AuditDetail> auditList = sd.getAudits();
			asyncUpdateContentsForUI(new IViewUpdater() {
				public void updateContentsForUI() {
					updateEventTableContents(auditList);
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

	private void updateEventTableContents(final List<AuditDetail> auditList) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy/MM/dd 'at' HH:mm:ss");
		// f_eventsTable.removeAll();
		// f_eventsTable.setVisible(true);
		// for (AuditDetail ad : auditList) {
		// final TableItem item = new TableItem(f_eventsTable, SWT.NONE);
		// item.setText(0, ad.getUser());
		// item.setText(1, dateFormat.format(ad.getTime()));
		// item.setText(2, Long.toString(ad.getFindingId()));
		// item.setText(3, ad.getText());
		// }
		// packTable(f_eventsTable);
	}

	private void updateEventTable() {
		TableItem[] items = f_syncTable.getSelection();
		if (items.length > 0) {
			TableItem item = items[0];
			SynchOverview so = (SynchOverview) item.getData();
			if (so != null) {
				// System.out.println("updating event table contents");
				asyncEventTableContents(so);
			}
		}
	}

	private void focusOnFindingId() {
		// TableItem[] items = f_eventsTable.getSelection();
		// if (items.length > 0) {
		// TableItem item = items[0];
		// String findingIdString = item.getText(2);
		// long findingId = Long.parseLong(findingIdString);
		// // System.out.println("focus on finding " + findingId);
		// /*
		// * Ensure the view is visible but don't change the focus.
		// */
		// FindingDetailsView.findingSelected(findingId, false);
		// }
	}

	private void packTable(final Table table) {
		for (TableColumn col : table.getColumns()) {
			col.pack();
		}
	}

	public void setHideEmptyEntries(boolean hide) {
		boolean old = PreferenceConstants.hideEmptySynchronizeEntries();
		if (old != hide) {
			PreferenceConstants.setHideEmptySynchronizeEntries(hide);
			asyncUpdateContents();
		}
	}
}
