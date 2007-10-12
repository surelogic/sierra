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
import com.surelogic.sierra.client.eclipse.model.IProjectsObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.jdbc.finding.ArtifactDetail;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;
import com.surelogic.sierra.jdbc.finding.CommentDetail;
import com.surelogic.sierra.jdbc.finding.FindingDetail;
import com.surelogic.sierra.jdbc.finding.FindingStatus;
import com.surelogic.sierra.tool.message.Importance;

public class FindingDetailsMediator implements IProjectsObserver {

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
	private final Button[] f_importanceButtons;
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
			Button[] importanceButtons, Text commentText, Button commentButton,
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
		f_importanceButtons = importanceButtons;
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

	public void init() {
		f_importanceButtons[0].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final boolean selection = f_importanceButtons[0].getSelection();
				if (selection) {
					f_executor.execute(new UpdateImportanceRunnable(
							Importance.CRITICAL, f_finding.getFindingId()));
				}

			}
		});
		f_importanceButtons[1].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final boolean selection = f_importanceButtons[1].getSelection();
				if (selection) {
					f_executor.execute(new UpdateImportanceRunnable(
							Importance.HIGH, f_finding.getFindingId()));
				}
			}
		});

		f_importanceButtons[2].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final boolean selection = f_importanceButtons[2].getSelection();
				if (selection) {
					f_executor.execute(new UpdateImportanceRunnable(
							Importance.MEDIUM, f_finding.getFindingId()));
				}
			}
		});

		f_importanceButtons[3].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final boolean selection = f_importanceButtons[3].getSelection();
				if (selection) {
					f_executor.execute(new UpdateImportanceRunnable(
							Importance.LOW, f_finding.getFindingId()));
				}
			}
		});

		f_importanceButtons[4].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final boolean selection = f_importanceButtons[4].getSelection();
				if (selection) {
					f_executor.execute(new UpdateImportanceRunnable(
							Importance.IRRELEVANT, f_finding.getFindingId()));
				}
			}
		});

		f_commentButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final String commentText = f_commentText.getText();

				f_executor.execute(new Runnable() {
					public void run() {
						try {
							Connection conn = Data.getConnection();
							conn.setAutoCommit(false);
							ClientFindingManager manager = ClientFindingManager
									.getInstance(conn);

							// TODO: Add check for empty comments
							manager.comment(f_finding.getFindingId(),
									commentText);
							conn.commit();
							conn.close();

							PlatformUI.getWorkbench().getDisplay().asyncExec(
									new Runnable() {
										public void run() {
											asyncQueryAndShow(f_finding
													.getFindingId());
										}
									});
						} catch (SQLException se) {
							SLLogger
									.getLogger("sierra")
									.log(
											Level.SEVERE,
											"SQL exception when trying to get add comments",
											se);
						}
					}
				});
			}
		});

		f_quickAudit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Quick Audited");
				f_quickAudit.setEnabled(false);
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

		Projects.getInstance().addObserver(this);
	}

	public void dispose() {
		// TODO This gets called when we exit the workbench
	}

	public void setFocus() {
		// TODO something reasonable
	}

	private class UpdateImportanceRunnable implements Runnable {

		private final Importance f_importance;

		private final long findingIdInternal;

		public UpdateImportanceRunnable(Importance importance, long findingID) {
			f_importance = importance;
			findingIdInternal = findingID;
		}

		public void run() {
			try {
				Connection conn = Data.getConnection();
				conn.setAutoCommit(false);
				ClientFindingManager manager = ClientFindingManager
						.getInstance(conn);
				manager.setImportance(findingIdInternal, f_importance);
				conn.commit();
				conn.close();

				// Do not refresh the view as it's messing with scrolled
				// composite

				// PlatformUI.getWorkbench().getDisplay().asyncExec(
				// new Runnable() {
				// public void run() {
				// refreshDetailsPage(findingIdInternal);
				// }
				//
				// });
			} catch (SQLException se) {
				SLLogger.getLogger("sierra").log(Level.SEVERE,
						"SQL exception when trying to set critical importance",
						se);
			}
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
		f_summaryText.setText(f_finding.getSummary());

		f_findingSynopsis.setText(getFindingSynopsis());

		f_projectName.setText(f_finding.getProjectName());
		f_packageName.setText(f_finding.getPackageName());
		f_className.setText(getClassName());

		// Remove the tabspaces and newline
		String details = f_finding.getFindingTypeDetail();
		details = details.replaceAll("\\t", "");
		details = details.replaceAll("\\n", "");
		f_detailsText.setText(details);

		// Clear importance buttons
		for (Button button : f_importanceButtons) {
			button.setSelection(false);
		}

		// Set importance
		if (f_finding.getImportance() == Importance.CRITICAL) {
			f_importanceButtons[0].setSelection(true);
		} else if (f_finding.getImportance() == Importance.HIGH) {
			f_importanceButtons[1].setSelection(true);
		} else if (f_finding.getImportance() == Importance.MEDIUM) {
			f_importanceButtons[2].setSelection(true);
		} else if (f_finding.getImportance() == Importance.LOW) {
			f_importanceButtons[3].setSelection(true);
		} else {
			f_importanceButtons[4].setSelection(true);
		}

		List<CommentDetail> commentDetails = f_finding.getComments();

		// Add label

		f_scrollingLabelComposite.removeAll();

		if (commentDetails != null) {
			for (int i = commentDetails.size() - 1; i >= 0; i--) {
				final CommentDetail cd = commentDetails.get(i);
				String userName = cd.getUser();
				if (userName == null) {
					userName = "Local";
				}
				final String holder = userName + " (" + cd.getTime().toString()
						+ ") : " + cd.getComment();
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
			b.append("2 times</a> ");
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

	public void notify(Projects p) {
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
}
