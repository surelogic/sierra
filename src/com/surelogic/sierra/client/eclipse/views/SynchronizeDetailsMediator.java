package com.surelogic.sierra.client.eclipse.views;

import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.surelogic.common.eclipse.LinkTrail;
import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBTransactionNoResult;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.finding.AuditDetail;
import com.surelogic.sierra.jdbc.finding.SynchDetail;
import com.surelogic.sierra.jdbc.finding.SynchOverview;

public class SynchronizeDetailsMediator extends AbstractSierraViewMediator {

	private final LinkTrail f_detailsComposite;

	protected SynchronizeDetailsMediator(IViewCallback cb,
			LinkTrail detailsComposite) {
		super(cb);
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

	private void updateEventTableContents(final SynchOverview so)
			throws Exception {
		Data.withReadOnly(new DBTransactionNoResult() {
			@Override
			public void doPerform(Connection conn) throws Exception {
				SynchDetail sd = SynchDetail.getSyncDetail(conn, so);
				final List<AuditDetail> auditList = sd.getAudits();
				asyncUpdateContentsForUI(new IViewUpdater() {
					public void updateContentsForUI() {
						updateEventTableContents(auditList);
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

	private void updateEventTableContents(final List<AuditDetail> auditList) {
		f_detailsComposite.removeAll();
		Map<String, Set<Long>> userToAudits = new HashMap<String, Set<Long>>();
		for (AuditDetail ad : auditList) {
			final String userName = ad.getUser();
			if (userName == null)
				continue;
			Set<Long> audits = userToAudits.get(userName);
			if (audits == null) {
				audits = new HashSet<Long>();
				userToAudits.put(userName, audits);
			}
			audits.add(ad.getFindingId());
		}
		for (Map.Entry<String, Set<Long>> entry : userToAudits.entrySet()) {
			StringBuilder b = new StringBuilder();
			b.append("Audited finding");
			if (entry.getValue().size() > 1)
				b.append("s");
			for (Long l : entry.getValue()) {
				b.append(" <a href=\"").append(l).append("\">");
				b.append(l).append("</a>");
			}
			f_detailsComposite.addEntry(entry.getKey(), b.toString(),
					f_linkListener);
		}
		f_view.hasData(!userToAudits.isEmpty());
	}

	private void focusOnFindingId(long findingId) {
		FindingDetailsView.findingSelected(findingId, false);
	}

	public void asyncQueryAndShow(final SynchOverview syncOverview) {
		final Job job = new DatabaseJob(
				"Updating reading events from server synchronize") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Updating list", IProgressMonitor.UNKNOWN);
				try {
					updateEventTableContents(syncOverview);
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
}
