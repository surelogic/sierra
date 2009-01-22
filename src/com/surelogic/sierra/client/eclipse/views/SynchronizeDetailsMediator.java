package com.surelogic.sierra.client.eclipse.views;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.surelogic.common.eclipse.LinkTrail;
import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.NullDBTransaction;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.finding.AuditDetail;
import com.surelogic.sierra.jdbc.finding.SynchDetail;
import com.surelogic.sierra.jdbc.finding.SynchOverview;

public class SynchronizeDetailsMediator extends AbstractSierraViewMediator {

	private final Composite f_panel;
	private final Label f_eventInfo;
	private final LinkTrail f_detailsComposite;

	protected SynchronizeDetailsMediator(IViewCallback cb, Composite panel,
			Label eventInfo, LinkTrail detailsComposite) {
		super(cb);
		f_panel = panel;
		f_eventInfo = eventInfo;
		f_detailsComposite = detailsComposite;
	}

	public String getHelpId() {
		return "com.surelogic.sierra.client.eclipse.view-synchronize-history";
	}

	public String getNoDataI18N() {
		return "sierra.eclipse.noDataSynchronizeDetails";
	}

	@Override
	public Listener getNoDataListener() {
		return new Listener() {
			public void handleEvent(Event event) {
				ViewUtility.showView(SynchronizeView.ID);
			}
		};
	}

	public void setFocus() {
		f_detailsComposite.setFocus();
	}

	@Override
	public void init() {
		super.init();
		f_view.setStatus(IViewCallback.Status.NO_DATA);
	}
	
	private void updateEventTableContents(final SynchOverview so)
			throws Exception {
		Data.getInstance().withReadOnly(new NullDBTransaction() {
			@Override
			public void doPerform(Connection conn) throws Exception {
				SynchDetail sd = SynchDetail.getSyncDetail(conn, so);
				final List<AuditDetail> auditList = sd.getAudits();
				asyncUpdateContentsForUI(new IViewUpdater() {
					public void updateContentsForUI() {
						updateEventTableContents(so, auditList);
					}
				});
			}
		});
	}

	private final Listener f_linkListener = new Listener() {
		public void handleEvent(Event event) {
			final String name = event.text;
			final long findingId = Long.parseLong(name);
			focusOnFindingId(findingId);
		}
	};

	private void updateEventTableContents(final SynchOverview syncOverview,
			final List<AuditDetail> auditList) {
		f_detailsComposite.removeAll();
		if (syncOverview == null) {
			// clearing the view.
			f_view.hasData(false);
			return;
		}

		final SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy/MM/dd 'at' HH:mm:ss");
		final String projectName = syncOverview.getProject();
		final int numCommitted = syncOverview.getNumCommitted();
		final int numReceived = syncOverview.getNumReceived();
		StringBuilder b = new StringBuilder();
		b.append(projectName).append(" had ");
		b.append(numCommitted);
		b.append(numCommitted == 1 ? " audit" : " audits");
		b.append(" sent and ");
		b.append(numReceived);
		b.append(numReceived == 1 ? " audit" : " audits");
		b.append(" received on ");
		b.append(dateFormat.format(syncOverview.getTime()));
		f_eventInfo.setText(b.toString());

		Map<String, List<Long>> userToAudits = new HashMap<String, List<Long>>();
		for (AuditDetail ad : auditList) {
			final String userName = ad.getUser();
			if (userName == null)
				continue;
			List<Long> audits = userToAudits.get(userName);
			if (audits == null) {
				audits = new ArrayList<Long>();
				userToAudits.put(userName, audits);
			}
			audits.add(ad.getFindingId());
		}
		for (Map.Entry<String, List<Long>> entry : userToAudits.entrySet()) {
			b = new StringBuilder();
			List<Long> userAuditList = entry.getValue();
			Set<Long> userAuditSet = new HashSet<Long>(userAuditList);
			b.append(userAuditList.size());
			b.append(" audit");
			if (userAuditList.size() > 1)
				b.append("s");
			b.append(" made on ");
			b.append(userAuditSet.size());
			b.append(" finding");
			if (userAuditSet.size() > 1)
				b.append("s");
			for (Long l : userAuditSet) {
				b.append(" <a href=\"").append(l).append("\">");
				b.append(l).append("</a>");
			}
			f_detailsComposite.addEntry(entry.getKey(), b.toString(),
					f_linkListener);
		}
		f_view.hasData(true);
		f_panel.layout();
	}

	private void focusOnFindingId(long findingId) {
		FindingDetailsView.findingSelected(findingId, false);
	}

	public void asyncQueryAndShow(final SynchOverview syncOverview) {
		if (syncOverview == null) {
			final Job job = new SLUIJob() {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					updateEventTableContents(syncOverview,
							new ArrayList<AuditDetail>());
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		} else {
			final Job job = new DatabaseJob(
					"Updating reading events from server synchronize", Job.INTERACTIVE) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor
							.beginTask("Updating list",
									IProgressMonitor.UNKNOWN);
					try {
						updateEventTableContents(syncOverview);
					} catch (Exception e) {
						final int errNo = 59;
						final String msg = I18N.err(errNo);
						return SLEclipseStatusUtility.createErrorStatus(errNo, msg, e);
					}
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}
	}
}
