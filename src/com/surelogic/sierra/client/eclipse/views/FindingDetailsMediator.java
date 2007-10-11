package com.surelogic.sierra.client.eclipse.views;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.PageBook;
import com.surelogic.common.eclipse.ScrollingLabelComposite;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.dialogs.SummaryChangeDialog;
import com.surelogic.sierra.jdbc.finding.ArtifactDetail;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;
import com.surelogic.sierra.jdbc.finding.CommentDetail;
import com.surelogic.sierra.jdbc.finding.FindingDetail;
import com.surelogic.sierra.tool.message.Importance;

public class FindingDetailsMediator {

	private final PageBook f_pages;
	private final Control f_noFindingsDetailsPage;
	private final Control f_findingsDetailsPage;
	private final Label f_summaryText;
	private final Button f_summaryChangeButton;
	private final Label f_packageNameText;
	private final Label f_classNameText;
	private final Label f_detailsText;
	private final TabItem f_auditTab;
	private final Button f_quickAudit;
	private final Button[] f_importanceButtons;
	private final Text f_commentText;
	private final Button f_commentButton;
	private final ScrollingLabelComposite f_scrollingLabelComposite;
	private final TabItem f_artifactsTab;
	private final Tree f_artifactsTree;
	private long f_findingId;

	private final Executor f_executor = Executors.newSingleThreadExecutor();

	public FindingDetailsMediator(PageBook pages, Control noFindingsPage,
			Control findingsPage, Label summaryText,
			Button summaryChangeButton, Label packageNameText,
			Label classNameText, Label detailsText, TabItem auditTab,
			Button quickAudit, Button[] importanceButtons, Text commentText,
			Button commentButton,
			ScrollingLabelComposite scrollingLabelComposite,
			TabItem artifactsTab, Tree artifactsTree) {
		f_pages = pages;
		f_noFindingsDetailsPage = noFindingsPage;
		f_findingsDetailsPage = findingsPage;
		f_summaryText = summaryText;
		f_summaryChangeButton = summaryChangeButton;
		f_packageNameText = packageNameText;
		f_classNameText = classNameText;
		f_detailsText = detailsText;
		f_auditTab = auditTab;
		f_quickAudit = quickAudit;
		f_importanceButtons = importanceButtons;
		f_commentText = commentText;
		f_commentButton = commentButton;
		f_scrollingLabelComposite = scrollingLabelComposite;
		f_artifactsTab = artifactsTab;
		f_artifactsTree = artifactsTree;

	}

	public void refreshDetailsPage(long findingID) {
		f_findingId = findingID;

		// Execute the query in a different thread
		f_executor.execute(new Runnable() {

			public void run() {
				try {
					Connection conn = Data.getConnection();
					final FindingDetail details = FindingDetail.getDetail(conn,
							f_findingId);

					if (details != null) {
						// got details, update the view in the UI thread
						PlatformUI.getWorkbench().getDisplay().asyncExec(
								new UpdateViewRunnable(details));
					}

					conn.close();
				} catch (SQLException e) {
					SLLogger.getLogger("sierra").log(Level.SEVERE,
							"SQL exception when trying to get finding details",
							e);
				}

			}

		});
	}

	public void setPages() {
		final Control page;
		if (f_findingId == 0) {
			page = f_noFindingsDetailsPage;
		} else {
			page = f_findingsDetailsPage;
		}

		// beware the thread context this method call might be made in.
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (page != f_pages.getPage()) {
					f_pages.showPage(page);
				}
			}

		});
	}

	public void init() {
		setPages();
		f_importanceButtons[0].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final boolean selection = f_importanceButtons[0].getSelection();
				if (selection) {
					f_executor.execute(new UpdateImportanceRunnable(
							Importance.CRITICAL, f_findingId));
				}

			}
		});
		f_importanceButtons[1].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final boolean selection = f_importanceButtons[1].getSelection();
				if (selection) {
					f_executor.execute(new UpdateImportanceRunnable(
							Importance.HIGH, f_findingId));
				}
			}
		});

		f_importanceButtons[2].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final boolean selection = f_importanceButtons[2].getSelection();
				if (selection) {
					f_executor.execute(new UpdateImportanceRunnable(
							Importance.MEDIUM, f_findingId));
				}
			}
		});

		f_importanceButtons[3].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final boolean selection = f_importanceButtons[3].getSelection();
				if (selection) {
					f_executor.execute(new UpdateImportanceRunnable(
							Importance.LOW, f_findingId));
				}
			}
		});

		f_importanceButtons[4].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final boolean selection = f_importanceButtons[4].getSelection();
				if (selection) {
					f_executor.execute(new UpdateImportanceRunnable(
							Importance.IRRELEVANT, f_findingId));
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
							manager.comment(f_findingId, commentText);
							conn.commit();
							conn.close();

							PlatformUI.getWorkbench().getDisplay().asyncExec(
									new Runnable() {
										public void run() {
											refreshDetailsPage(f_findingId);
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

		f_summaryChangeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SummaryChangeDialog scd = new SummaryChangeDialog(Display
						.getCurrent().getActiveShell(), f_summaryText.getText());
				if (Window.OK == scd.open()) {
					final String summary = scd.getText();
					f_summaryText.setText(summary.trim());
					f_executor.execute(new Runnable() {

						public void run() {
							try {
								Connection conn = Data.getConnection();
								conn.setAutoCommit(false);
								ClientFindingManager manager = ClientFindingManager
										.getInstance(conn);
								manager.changeSummary(f_findingId, summary);
								conn.commit();
								conn.close();
								// TODO: Add check for empty comment
								PlatformUI.getWorkbench().getDisplay()
										.asyncExec(new Runnable() {
											public void run() {
												refreshDetailsPage(f_findingId);
											}

										});
							} catch (SQLException se) {
								SLLogger
										.getLogger("sierra")
										.log(
												Level.SEVERE,
												"SQL exception when trying to set critical importance",
												se);
							}

						}

					});
				}

			}

		});

	}

	public void dispose() {
		// TODO This gets called when we exit the workbench
	}

	public void setFocus() {
		// Nothing to do for now

	}

	public void setFindingID(long findingID) {
		f_findingId = findingID;

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

	private class UpdateViewRunnable implements Runnable {

		private final FindingDetail f_details;

		public UpdateViewRunnable(FindingDetail details) {
			f_details = details;
		}

		public void run() {
			f_summaryText.setText(f_details.getSummary());
			f_packageNameText.setText(f_details.getPackageName());
			f_classNameText.setText(f_details.getClassName() + " ("
					+ f_details.getLineOfCode() + ")");

			// Remove the tabspaces and newline
			String details = f_details.getFindingTypeDetail();
			details = details.replaceAll("\\t", "");
			details = details.replaceAll("\\n", "");
			f_detailsText.setText(details);

			f_auditTab.setText("Audit");

			final Importance importance = f_details.getImportance();

			// Clear importance buttons
			for (Button b : f_importanceButtons) {
				b.setSelection(false);
			}

			// Set importance
			if (importance.equals(Importance.CRITICAL)) {
				f_importanceButtons[0].setSelection(true);
			} else if (importance.equals(Importance.HIGH)) {
				f_importanceButtons[1].setSelection(true);
			} else if (importance.equals(Importance.MEDIUM)) {
				f_importanceButtons[2].setSelection(true);
			} else if (importance.equals(Importance.LOW)) {
				f_importanceButtons[3].setSelection(true);
			} else {
				f_importanceButtons[4].setSelection(true);
			}

			List<CommentDetail> commentDetails = f_details.getComments();

			// Add label

			f_scrollingLabelComposite.removeAll();

			if (commentDetails != null) {
				for (int i = commentDetails.size() - 1; i >= 0; i--) {
					final CommentDetail cd = commentDetails.get(i);
					final String holder = cd.getUser() + " ("
							+ cd.getTime().toString() + ") : "
							+ cd.getComment();
					f_scrollingLabelComposite.addLabel(holder);
				}
			}
			f_scrollingLabelComposite.reflow(true);

			List<ArtifactDetail> artifacts = f_details.getArtifacts();

			f_artifactsTree.removeAll();
			if (artifacts != null) {
				if (artifacts.size() == 0) {
					f_artifactsTab.setText("No Artifacts");
				} else if (artifacts.size() == 1) {
					f_artifactsTab.setText(artifacts.size() + " Artifact");
				} else {
					f_artifactsTab.setText(artifacts.size() + " Artifacts");
				}

				for (ArtifactDetail ad : artifacts) {
					final TreeItem ti = new TreeItem(f_artifactsTree, SWT.NONE);
					ti.setText(ad.getTool() + " : " + ad.getMessage());
				}
			}

			f_commentText.setText("");
			setPages();

		}
	}

}
