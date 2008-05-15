package com.surelogic.sierra.client.eclipse.views;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBTransaction;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.actions.SynchronizeProjectDialogAction;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.jdbc.finding.AuditDetail;
import com.surelogic.sierra.jdbc.finding.SynchDetail;
import com.surelogic.sierra.jdbc.finding.SynchOverview;

public class SynchronizeDetailsMediator extends AbstractSierraViewMediator {

	protected SynchronizeDetailsMediator(IViewCallback cb) {
		super(cb);
		// TODO Auto-generated constructor stub
	}

	public String getHelpId() {
		return "com.surelogic.sierra.client.eclipse.view-synchronize-history";
	}

	public String getNoDataI18N() {
		// TODO
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

	public void setFocus() {

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
//		TableItem[] items = f_syncTable.getSelection();
//		if (items.length > 0) {
//			TableItem item = items[0];
//			SynchOverview so = (SynchOverview) item.getData();
//			if (so != null) {
//				// System.out.println("updating event table contents");
//				asyncEventTableContents(so);
//			}
//		}
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


}
