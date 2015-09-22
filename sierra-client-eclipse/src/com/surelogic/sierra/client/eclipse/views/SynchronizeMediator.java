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

import com.surelogic.common.CommonImages;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.NullDBTransaction;
import com.surelogic.common.ui.SLImages;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.actions.SynchronizeProjectDialogAction;
import com.surelogic.sierra.client.eclipse.jobs.AbstractSierraDatabaseJob;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;
import com.surelogic.sierra.jdbc.finding.SynchOverview;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;

public final class SynchronizeMediator extends AbstractSierraViewMediator {

	final Table f_syncTable;

	public SynchronizeMediator(final IViewCallback cb, final Table syncTable) {
		super(cb);
		f_syncTable = syncTable;
	}

	@Override
  public String getHelpId() {
		return "com.surelogic.sierra.client.eclipse.view-synchronize-history";
	}

	@Override
  public String getNoDataI18N() {
		return "sierra.eclipse.noDataSynchronizeHistory";
	}

	@Override
	public Listener getNoDataListener() {
		return new Listener() {
      @Override
      @SuppressWarnings("deprecation")
      public void handleEvent(final Event event) {
				new SynchronizeProjectDialogAction().run();
			}
		};
	}

	@Override
	public void init() {
		super.init();
		f_syncTable.addListener(SWT.Selection, new Listener() {
			@Override
      public void handleEvent(final Event event) {
				final TableItem[] items = f_syncTable.getSelection();
				if (items.length > 0) {
					final TableItem item = items[0];
					final Object data = item.getData();
					if (data instanceof SynchOverview) {
						final SynchOverview so = (SynchOverview) data;
						SynchronizeDetailsView.eventSelected(so, false);
					}
				}
			}
		});
		asyncUpdateContents();
	}

	@Override
  public void setFocus() {
		f_syncTable.setFocus();
	}

	@Override
	public void projectDeleted() {
		asyncUpdateContents();
	}

	@Override
	public void databaseDeleted() {
		asyncUpdateContents();
	}

	@Override
	public void serverSynchronized() {
		asyncUpdateContents();
	}

	@Override
	public void projectSynchronized() {
		asyncUpdateContents();
	}

	private void asyncUpdateContents() {
		final Job job = new AbstractSierraDatabaseJob(
				"Updating set of server synchronization events") {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask("Updating list", IProgressMonitor.UNKNOWN);
				try {
					updateContents();
				} catch (final Exception e) {
					final int errNo = 58;
					final String msg = I18N.err(errNo);
					return SLEclipseStatusUtility.createErrorStatus(errNo, msg,
							e);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	void updateContents() throws Exception {
		Data.getInstance().withReadOnly(new NullDBTransaction() {
			@Override
			public void doPerform(final Connection conn) throws Exception {
				final List<SynchOverview> synchList = SynchOverview
						.listOverviews(conn);
				asyncUpdateContentsForUI(new IViewUpdater() {
					@Override
          public void updateContentsForUI() {
						updateSyncTableContents(synchList);
					}
				});
			}
		});
	}

	/**
	 * Must be called from the SWT thread.
	 */
	void updateSyncTableContents(final List<SynchOverview> synchList) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy/MM/dd 'at' HH:mm:ss");
		f_syncTable.removeAll();

		for (final SynchOverview so : synchList) {
			if (EclipseUtility
					.getBooleanPreference(SierraPreferencesUtility.HIDE_EMPTY_SYNCHRONIZE_ENTRIES)
					&& so.isEmpty()) {
				continue;
			}
			final TableItem item = new TableItem(f_syncTable, SWT.NONE);
			final String projectName = so.getProject();
			final ConnectedServer server = ConnectedServerManager.getInstance()
					.getServer(projectName);
			final String serverName;
			if (server != null) {
				serverName = server.getName();
			} else {
				serverName = "(unknown)";
			}
			item.setText(0, dateFormat.format(so.getTime()));
			item.setText(1, serverName);
			final int numCommitted = so.getNumCommitted();
			final int numReceived = so.getNumReceived();
			final StringBuilder b = new StringBuilder();
			b.append(projectName).append(" had ");
			b.append(numCommitted);
			b.append(numCommitted == 1 ? " audit" : " audits");
			b.append(" sent and ");
			b.append(numReceived);
			b.append(numReceived == 1 ? " audit" : " audits");
			b.append(" received.");
			item.setText(2, b.toString());
			item.setImage(2, SLImages.getImage(CommonImages.IMG_COMMENT));
			item.setData(so);
		}

		f_view.hasData(!synchList.isEmpty()
				&& !Projects.getInstance().isEmpty());
		f_syncTable.setSortColumn(f_syncTable.getColumn(0));
		f_syncTable.setSortDirection(SWT.DOWN);
		packTable(f_syncTable);

		// clear out the details view because nothing is selected
		SynchronizeDetailsView.eventSelected(null, false);
	}

	private void packTable(final Table table) {
		for (final TableColumn col : table.getColumns()) {
			col.pack();
		}
	}

	public void setHideEmptyEntries(final boolean hide) {
		final boolean old = EclipseUtility
				.getBooleanPreference(SierraPreferencesUtility.HIDE_EMPTY_SYNCHRONIZE_ENTRIES);
		if (old != hide) {
			EclipseUtility.setBooleanPreference(
					SierraPreferencesUtility.HIDE_EMPTY_SYNCHRONIZE_ENTRIES,
					hide);
			asyncUpdateContents();
		}
	}
}
