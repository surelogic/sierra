package com.surelogic.sierra.client.eclipse.model;

import java.sql.Connection;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.surelogic.adhoc.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;
import com.surelogic.sierra.tool.message.Importance;

public final class FindingMutationUtility {

	private FindingMutationUtility() {
		// no instances
	}

	public static void asyncChangeSummary(final long finding_id,
			final String summary) {
		final Job job = new DatabaseJob("Changing the summary of finding id "
				+ finding_id + " to \"" + summary + "\"") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Updating finding data",
						IProgressMonitor.UNKNOWN);
				try {
					changeSummary(finding_id, summary);
				} catch (Exception e) {
					return SLStatus
							.createErrorStatus(
									"Failed to change the summary of finding id "
											+ finding_id + " to \"" + summary
											+ "\"", e);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private static void changeSummary(final long finding_id,
			final String summary) throws Exception {
		if (summary == null || summary.equals(""))
			return;
		Connection c = Data.getConnection();
		try {
			c.setAutoCommit(false);
			ClientFindingManager manager = ClientFindingManager.getInstance(c);

			manager.changeSummary(finding_id, summary);
			c.commit();
			DatabaseHub.getInstance().notifyFindingMutated();
		} finally {
			c.close();
		}
	}

	public static void asyncComment(final long finding_id, final String comment) {
		final Job job = new DatabaseJob("Adding the comment \"" + comment
				+ "\" to finding id " + finding_id) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Updating finding data",
						IProgressMonitor.UNKNOWN);
				try {
					comment(finding_id, comment);
				} catch (Exception e) {
					return SLStatus.createErrorStatus(
							"Failed to add the comment \"" + comment
									+ "\" to finding id " + finding_id, e);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private static void comment(final long finding_id, final String comment)
			throws Exception {
		if (comment == null || comment.trim().equals(""))
			return;
		Connection c = Data.getConnection();
		try {
			c.setAutoCommit(false);
			ClientFindingManager manager = ClientFindingManager.getInstance(c);
			manager.comment(finding_id, comment);
			c.commit();
			DatabaseHub.getInstance().notifyFindingMutated();
		} finally {
			c.close();
		}
	}

	public static void asyncChangeImportance(final long finding_id,
			final Importance from, final Importance to) {
		final Job job = new DatabaseJob(
				"Changing the importance of finding id " + finding_id
						+ " from " + from + " to " + to) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Updating finding data",
						IProgressMonitor.UNKNOWN);
				try {
					changeImportance(finding_id, to);
				} catch (Exception e) {
					return SLStatus.createErrorStatus(
							"Failed to change the importance of finding id "
									+ finding_id + " from " + from + " to "
									+ to, e);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	public static void asyncChangeImportance(final Set<Long> finding_ids,
			final Importance to) {
		final Job job = new DatabaseJob("Changing the importance of "
				+ finding_ids.size() + " finding(s) to " + to) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Updating finding data", finding_ids.size());
				try {
					for (long finding_id : finding_ids) {
						changeImportance(finding_id, to);
						monitor.worked(1);
					}
				} catch (Exception e) {
					return SLStatus.createErrorStatus(
							"Failed to change the importance of "
									+ finding_ids.size() + " finding(s) to "
									+ to, e);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	private static void changeImportance(final long finding_id,
			final Importance to) throws Exception {
		Connection c = Data.getConnection();
		try {
			c.setAutoCommit(false);
			ClientFindingManager manager = ClientFindingManager.getInstance(c);
			manager.setImportance(finding_id, to);
			c.commit();
			DatabaseHub.getInstance().notifyFindingMutated();
		} finally {
			c.close();
		}
	}

}
