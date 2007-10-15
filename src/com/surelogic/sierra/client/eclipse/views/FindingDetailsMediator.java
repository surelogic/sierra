package com.surelogic.sierra.client.eclipse.views;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.JDTUtility;
import com.surelogic.common.eclipse.PageBook;
import com.surelogic.common.eclipse.ScrollingLabelComposite;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.Utility;
import com.surelogic.sierra.client.eclipse.model.AbstractDatabaseObserver;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.model.IProjectsObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.jdbc.finding.ArtifactDetail;
import com.surelogic.sierra.jdbc.finding.AuditDetail;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;
import com.surelogic.sierra.jdbc.finding.FindingDetail;
import com.surelogic.sierra.jdbc.finding.FindingStatus;
import com.surelogic.sierra.tool.message.Importance;

public class FindingDetailsMediator extends AbstractDatabaseObserver implements
		IProjectsObserver {

	private final Display f_display = PlatformUI.getWorkbench().getDisplay();

	private final PageBook f_pages;
	private final Control f_noFindingPage;
	private final Composite f_findingPage;
	private final ToolItem f_summaryIcon;
	private final Text f_summaryText;

	private final TabFolder f_folder;

	private final TabItem f_synopsisTab;
	private final Link f_findingSynopsis;
	private final Label f_projectName;
	private final Label f_packageName;
	private final Link f_className;
	private final Label f_detailsText;

	private final TabItem f_auditTab;
	private final Button f_quickAudit;
	private final Button f_criticalButton;
	private final Button f_highButton;
	private final Button f_mediumButton;
	private final Button f_lowButton;
	private final Button f_irrelevantButton;
	private final Text f_commentText;
	private final Button f_commentButton;
	private final ScrollingLabelComposite f_scrollingLabelComposite;

	private final TabItem f_artifactTab;
	private final Tree f_artifactsTree;

	private volatile FindingDetail f_finding;

	private final Executor f_executor = Executors.newSingleThreadExecutor();

	private final Listener f_tabLinkListener = new Listener() {
		public void handleEvent(Event event) {
			final String target = event.text;
			if ("audit".equals(target)) {
				f_folder.setSelection(f_auditTab);
			} else if ("artifact".equals(target)) {
				f_folder.setSelection(f_artifactTab);
			} else if ("synopsis".equals(target)) {
				f_folder.setSelection(f_synopsisTab);
			}
		}
	};

	public FindingDetailsMediator(PageBook pages, Control noFindingPage,
			Composite findingPage, ToolItem summaryIcon, Text summaryText,
			TabFolder folder, TabItem synopsisTab, Link findingSynopsis,
			Label projectName, Label packageName, Link className,
			Label detailsText, TabItem auditTab, Button quickAudit,
			Button criticalButton, Button highButton, Button mediumButton,
			Button lowButton, Button irrelevantButton, Text commentText,
			Button commentButton,
			ScrollingLabelComposite scrollingLabelComposite,
			TabItem artifactTab, Tree artifactsTree) {
		f_pages = pages;
		f_noFindingPage = noFindingPage;
		f_findingPage = findingPage;
		f_summaryIcon = summaryIcon;
		f_summaryText = summaryText;
		f_folder = folder;
		f_synopsisTab = synopsisTab;
		f_findingSynopsis = findingSynopsis;
		f_projectName = projectName;
		f_packageName = packageName;
		f_className = className;
		f_detailsText = detailsText;
		f_auditTab = auditTab;
		f_quickAudit = quickAudit;
		f_criticalButton = criticalButton;
		f_highButton = highButton;
		f_mediumButton = mediumButton;
		f_lowButton = lowButton;
		f_irrelevantButton = irrelevantButton;
		f_commentText = commentText;
		f_commentButton = commentButton;
		f_scrollingLabelComposite = scrollingLabelComposite;
		f_artifactTab = artifactTab;
		f_artifactsTree = artifactsTree;
	}

	void asyncQueryAndShow(final long findingId) {
		f_executor.execute(new Runnable() {
			public void run() {
				try {
					Connection c = Data.getConnection();
					try {
						c.setReadOnly(true);

						final FindingDetail finding = FindingDetail.getDetail(
								c, findingId);

						if (finding != null) {
							f_finding = finding;
							// got details, update the view in the UI thread
							f_display.asyncExec(new Runnable() {
								public void run() {
									updateContents();
								}
							});
						}
					} finally {
						c.close();
					}
				} catch (SQLException e) {
					SLLogger.getLogger().log(
							Level.SEVERE,
							"SQL exception when trying to finding details for finding id="
									+ findingId, e);
				}
			}
		});
	}

	private final Listener f_radioListener = new Listener() {
		public void handleEvent(Event event) {
			final Importance current = f_finding.getImportance();
			if (event.widget != null) {
				if (event.widget.getData() instanceof Importance) {
					final Importance desired = (Importance) event.widget
							.getData();
					if (desired != current) {
						f_executor.execute(new Runnable() {
							public void run() {
								changeImportance(desired);
							}
						});
					}
				}
			}
		}
	};

	public void init() {
		f_criticalButton.addListener(SWT.Selection, f_radioListener);
		f_highButton.addListener(SWT.Selection, f_radioListener);
		f_mediumButton.addListener(SWT.Selection, f_radioListener);
		f_lowButton.addListener(SWT.Selection, f_radioListener);
		f_irrelevantButton.addListener(SWT.Selection, f_radioListener);

		f_commentButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final String commentText = f_commentText.getText();
				if (commentText == null || commentText.trim().equals(""))
					return;
				f_executor.execute(new Runnable() {
					public void run() {
						addComment(commentText);
					}
				});
			}
		});

		f_quickAudit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				f_executor.execute(new Runnable() {
					public void run() {
						addComment("I examined this finding.");
					}
				});
			}
		});

		f_className.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				final String target = event.text;
				if (target == null)
					return;
				/*
				 * The target should be the line number.
				 */
				int lineNumber = Integer.valueOf(target);
				JDTUtility.tryToOpenInEditor(f_finding.getProjectName(),
						f_finding.getPackageName(), f_finding.getClassName(),
						lineNumber);
			}
		});

		f_findingSynopsis.addListener(SWT.Selection, f_tabLinkListener);

		final Listener tel = new TextEditedListener(new Runnable() {
			public void run() {
				f_executor.execute(new Runnable() {
					public void run() {
						changeSummary(f_summaryText.getText());
					}
				});
			}
		});
		f_summaryText.addListener(SWT.Modify, tel);
		f_summaryText.addListener(SWT.FocusOut, tel);

		Projects.getInstance().addObserver(this);
		DatabaseHub.getInstance().addObserver(this);
	}

	public void dispose() {
		Projects.getInstance().removeObserver(this);
		DatabaseHub.getInstance().removeObserver(this);
	}

	public void setFocus() {
		if (f_pages.getPage() == f_findingPage) {
			TabItem[] items = f_folder.getSelection();
			if (items.length > 0) {
				TabItem item = items[0];
				if (item == f_auditTab) {
					f_commentText.setFocus();
				} else {
					f_summaryText.setFocus();
				}
			}
		} else {
			f_noFindingPage.setFocus();
		}
	}

	/**
	 * Must be invoked from the SWT thread.
	 */
	private void updateContents() {
		final boolean noFinding = f_finding == null;
		final Control page = noFinding ? f_noFindingPage : f_findingPage;
		if (page != f_pages.getPage()) {
			f_pages.showPage(page);
		}
		if (noFinding)
			return;

		/*
		 * We have a finding so show the details about it.
		 */
		f_summaryIcon.setImage(Utility.getImageFor(f_finding.getImportance()));
		f_summaryIcon.setToolTipText(f_finding.getImportance().toString());
		f_summaryText.setText(f_finding.getSummary());

		f_findingSynopsis.setText(getFindingSynopsis());

		f_projectName.setText(f_finding.getProjectName());
		f_packageName.setText(f_finding.getPackageName());
		f_className.setText(getClassName());

		// Remove the tabspaces and newline
		// TODO: Why?
		String details = f_finding.getFindingTypeDetail();
		details = details.replaceAll("\\t", "");
		details = details.replaceAll("\\n", "");
		f_detailsText.setText(details);

		f_criticalButton.setSelection(false);
		f_highButton.setSelection(false);
		f_mediumButton.setSelection(false);
		f_lowButton.setSelection(false);
		f_irrelevantButton.setSelection(false);

		// Set importance
		if (f_finding.getImportance() == Importance.CRITICAL) {
			f_criticalButton.setSelection(true);
		} else if (f_finding.getImportance() == Importance.HIGH) {
			f_highButton.setSelection(true);
		} else if (f_finding.getImportance() == Importance.MEDIUM) {
			f_mediumButton.setSelection(true);
		} else if (f_finding.getImportance() == Importance.LOW) {
			f_lowButton.setSelection(true);
		} else {
			f_irrelevantButton.setSelection(true);
		}

		List<AuditDetail> commentDetails = f_finding.getComments();

		// Add label

		f_scrollingLabelComposite.removeAll();

		if (commentDetails != null) {
			for (int i = commentDetails.size() - 1; i >= 0; i--) {
				final AuditDetail cd = commentDetails.get(i);
				String userName = cd.getUser();
				if (userName == null) {
					userName = "Local";
				}
				final String holder = userName + " (" + cd.getTime().toString()
						+ ") : " + cd.getText();
				f_scrollingLabelComposite.addLabel(holder);
			}
		}
		f_scrollingLabelComposite.reflow(true);

		f_artifactsTree.removeAll();
		for (ArtifactDetail ad : f_finding.getArtifacts()) {
			final TreeItem ti = new TreeItem(f_artifactsTree, SWT.NONE);
			ti.setText(ad.getTool() + " : " + ad.getMessage());
		}

		f_commentText.setText("");

		updateTabTitles();
		f_findingPage.layout(true, true);
	}

	private String getFindingSynopsis() {
		final String importance = f_finding.getImportance().toString()
				.toLowerCase();

		final int auditCount = f_finding.getNumberOfComments();
		final String tool = f_finding.getTool();
		final FindingStatus status = f_finding.getStatus();

		StringBuilder b = new StringBuilder();
		b.append("This finding (id=" + f_finding.getFindingId() + ") is of ");
		b.append("<a href=\"audit\">");
		b.append(importance);
		b.append("</a>");
		b.append(" importance. It has ");
		if (auditCount > 0) {
			b.append("been <a href=\"audit\">audited ");
			b.append(auditCount);
			b.append(" times</a> ");
		} else {
			b.append("not been <a href=\"audit\">audited</a> ");
		}
		if (status == FindingStatus.FIXED) {
			b.append("and was not found in the code during the last scan.");
		} else {
			if (status == FindingStatus.NEW) {
				b.append("and was discovered by ");
				b.append("<a href=\"artifact\">");
				if (tool.startsWith("(")) {
					b.append("by multiple tools (with ");
					b.append(f_finding.getNumberOfArtifacts());
					b.append(" artifacts reported)");
				} else
					b.append(tool);
				b.append("</a> during the last scan.");
			} else {
				b.append("and has been reported by ");
				b.append("<a href=\"artifact\">");
				b.append(tool);
				b.append("</a> for several scans.");
			}
		}
		return b.toString();
	}

	private String getClassName() {
		StringBuilder b = new StringBuilder();
		int[] lines = f_finding.getLinesOfCode();
		b.append(f_finding.getClassName());
		b.append(" at line");
		if (lines.length > 1)
			b.append("s");
		b.append(" ");
		boolean first = true;
		for (int line : lines) {
			if (first)
				first = false;
			else
				b.append(" ");
			b.append("<a href=\"");
			b.append(line);
			b.append("\">");
			b.append(line);
			b.append("</a>");
		}
		return b.toString();
	}

	/**
	 * Must be invoked from the SWT thread.
	 */
	private void updateTabTitles() {
		final int auditCount = f_finding.getNumberOfComments();
		final int artifactCount = f_finding.getNumberOfArtifacts();
		if (auditCount == 0)
			f_auditTab.setText("No Audits");
		else {
			f_auditTab.setText(auditCount + " Audit"
					+ (auditCount > 1 ? "s" : ""));
		}
		if (artifactCount == 0)
			f_artifactTab.setText("No Artifacts");
		else {
			f_artifactTab.setText(artifactCount + " Artifact"
					+ (artifactCount > 1 ? "s" : ""));
		}
	}

	private void changeSummary(String summary) {
		if (f_finding == null)
			return;
		summary = summary.trim();
		if (summary == null || summary.equals(""))
			return;
		final String oldSummary = f_finding.getSummary().trim();
		if (oldSummary.equals(summary))
			return;
		try {
			Connection c = Data.getConnection();
			try {
				c.setAutoCommit(false);
				ClientFindingManager manager = ClientFindingManager
						.getInstance(c);

				manager.changeSummary(f_finding.getFindingId(), summary);
				c.commit();
				DatabaseHub.getInstance().notifyFindingMutated();
			} finally {
				c.close();
			}
		} catch (SQLException e) {
			SLLogger.getLogger().log(
					Level.SEVERE,
					"Failure changing summary from \"" + oldSummary
							+ "\" to \"" + summary + "\" on finding "
							+ f_finding.getFindingId(), e);
		}
	}

	private void addComment(final String comment) {
		if (f_finding == null)
			return;
		if (comment == null || comment.trim().equals(""))
			return;
		try {
			Connection c = Data.getConnection();
			try {
				c.setAutoCommit(false);
				ClientFindingManager manager = ClientFindingManager
						.getInstance(c);
				manager.comment(f_finding.getFindingId(), comment);
				c.commit();
				DatabaseHub.getInstance().notifyFindingMutated();
			} finally {
				c.close();
			}
		} catch (SQLException e) {
			SLLogger.getLogger().log(
					Level.SEVERE,
					"Failure adding comment \"" + comment + "\" to finding "
							+ f_finding.getFindingId(), e);
		}
	}

	private void changeImportance(final Importance importance) {
		if (f_finding == null)
			return;
		if (importance == f_finding.getImportance())
			return;
		try {
			Connection c = Data.getConnection();
			try {
				c.setAutoCommit(false);
				ClientFindingManager manager = ClientFindingManager
						.getInstance(c);
				manager.setImportance(f_finding.getFindingId(), importance);
				c.commit();
				DatabaseHub.getInstance().notifyFindingMutated();
			} finally {
				c.close();
			}
		} catch (SQLException e) {
			SLLogger.getLogger()
					.log(
							Level.SEVERE,
							"Failure mutating the importance of finding "
									+ f_finding.getFindingId() + " to "
									+ importance, e);
		}
	}

	private static class TextEditedListener implements Listener {

		private final Runnable f_action;
		private boolean f_editInProgress = false;

		public TextEditedListener(Runnable action) {
			f_action = action;
		}

		public void handleEvent(Event event) {
			if (event.type == SWT.Modify) {
				f_editInProgress = true;
			} else if (event.type == SWT.FocusOut && f_editInProgress) {
				f_editInProgress = false;
				if (f_action != null)
					f_action.run();
			}
		}
	}

	public void notify(Projects p) {
		/*
		 * Something about the set of projects in the database has changed.
		 */
		if (f_finding == null)
			return;
		if (!p.getProjectNames().contains(f_finding.getProjectName())) {
			f_display.asyncExec(new Runnable() {
				public void run() {
					updateContents();
				}
			});
		}
	}

	@Override
	public void findingMutated() {
		if (f_finding != null) {
			asyncQueryAndShow(f_finding.getFindingId());
		}
	}
}
