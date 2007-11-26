package com.surelogic.sierra.client.eclipse.views;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.surelogic.common.eclipse.AuditTrail;
import com.surelogic.common.eclipse.HTMLPrinter;
import com.surelogic.common.eclipse.JDTUtility;
import com.surelogic.common.eclipse.PageBook;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.TextEditedListener;
import com.surelogic.common.eclipse.job.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.Utility;
import com.surelogic.sierra.client.eclipse.model.AbstractDatabaseObserver;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.model.FindingMutationUtility;
import com.surelogic.sierra.client.eclipse.model.IProjectsObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.jdbc.finding.ArtifactDetail;
import com.surelogic.sierra.jdbc.finding.AuditDetail;
import com.surelogic.sierra.jdbc.finding.FindingDetail;
import com.surelogic.sierra.jdbc.finding.FindingStatus;
import com.surelogic.sierra.tool.message.Importance;

public class FindingDetailsMediator extends AbstractDatabaseObserver implements
		IProjectsObserver {

	public static final String STAMP_COMMENT = "I examined this finding.";

	private final Display f_display = PlatformUI.getWorkbench().getDisplay();

	private final StringBuilder fgStyleSheet = new StringBuilder();

	private RGB fBackgroundColorRGB = f_display.getSystemColor(
			SWT.COLOR_LIST_BACKGROUND).getRGB();

	private void loadStyleSheet() {
		Bundle bundle = Activator.getDefault().getBundle();
		URL styleSheetURL = bundle.getEntry("/lib/DetailsViewStyleSheet.css");
		if (styleSheetURL == null)
			return;

		try {
			styleSheetURL = FileLocator.toFileURL(styleSheetURL);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					styleSheetURL.openStream()));
			StringBuilder buffer = new StringBuilder(200);
			String line = reader.readLine();
			while (line != null) {
				buffer.append(line);
				buffer.append('\n');
				line = reader.readLine();
			}

			// FontData fontData = JFaceResources.getFontRegistry().getFontData(
			// PreferenceConstants.APPEARANCE_JAVADOC_FONT)[0];
			fgStyleSheet.append(buffer.toString());
		} catch (IOException ex) {
			SLLogger.getLogger().log(Level.SEVERE,
					"Failure loading style sheet for details view.", ex);
			return;
		}
	}

	private final PageBook f_pages;
	private final Control f_noFindingPage;
	private final Composite f_findingPage;
	private final ToolItem f_summaryIcon;
	private final Text f_summaryText;

	private final TabFolder f_folder;

	private final TabItem f_synopsisTab;
	private final Button f_synopsisAudit;
	private final Link f_findingSynopsis;
	private final Label f_projectName;
	private final Label f_packageName;
	private final Link f_className;
	private final Browser f_detailsText;

	private final TabItem f_auditTab;
	private final Button f_quickAudit;
	private final Button f_criticalButton;
	private final Button f_highButton;
	private final Button f_mediumButton;
	private final Button f_lowButton;
	private final Button f_irrelevantButton;
	private final Text f_commentText;
	private final Button f_commentButton;
	private final AuditTrail f_scrollingLabelComposite;

	private final TabItem f_artifactTab;
	private final Table f_artifacts;

	private volatile FindingDetail f_finding;

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
			TabFolder folder, TabItem synopsisTab, Button synopsisAudit,
			Link findingSynopsis, Label projectName, Label packageName,
			Link className, Browser detailsText, TabItem auditTab,
			Button quickAudit, Button criticalButton, Button highButton,
			Button mediumButton, Button lowButton, Button irrelevantButton,
			Text commentText, Button commentButton,
			AuditTrail scrollingLabelComposite, TabItem artifactTab,
			Table artifacts) {
		f_pages = pages;
		f_noFindingPage = noFindingPage;
		f_findingPage = findingPage;
		f_summaryIcon = summaryIcon;
		f_summaryText = summaryText;
		f_folder = folder;
		f_synopsisTab = synopsisTab;
		f_synopsisAudit = synopsisAudit;
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
		f_artifacts = artifacts;
	}

	void asyncQueryAndShow(final long findingId) {
		final Job job = new DatabaseJob("Querying details of finding "
				+ findingId) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Querying finding data",
						IProgressMonitor.UNKNOWN);
				try {
					Connection c = Data.readOnlyConnection();
					try {
						f_finding = FindingDetail.getDetailOrNull(c, findingId);

						// got details, update the view in the UI thread
						f_display.asyncExec(new Runnable() {
							public void run() {
								updateContents();
							}
						});

					} catch (IllegalArgumentException iae) {
						f_display.asyncExec(new Runnable() {
							public void run() {
								f_finding = null;
								updateContents();
							}

						});
					} finally {
						c.close();
					}
					monitor.done();
					return Status.OK_STATUS;
				} catch (SQLException e) {
					return SLStatus.createErrorStatus(
							"SQL exception when trying to finding details for finding id "
									+ findingId, e);
				}
			}
		};
		job.schedule();
	}

	private final Listener f_radioListener = new Listener() {
		public void handleEvent(Event event) {
			final Importance current = f_finding.getImportance();
			if (event.widget != null) {
				if (event.widget.getData() instanceof Importance) {
					final Importance desired = (Importance) event.widget
							.getData();
					if (desired != current) {
						FindingMutationUtility.asyncChangeImportance(f_finding
								.getFindingId(), current, desired);
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
				FindingMutationUtility.asyncComment(f_finding.getFindingId(),
						commentText);
			}
		});

		final SelectionAdapter stampAction = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FindingMutationUtility.asyncComment(f_finding.getFindingId(),
						STAMP_COMMENT);
			}
		};
		f_synopsisAudit.addSelectionListener(stampAction);
		f_quickAudit.addSelectionListener(stampAction);

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

		f_summaryIcon.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				f_folder.setSelection(f_auditTab);
			}
		});
		f_findingSynopsis.addListener(SWT.Selection, f_tabLinkListener);

		final Listener tel = new TextEditedListener(
				new TextEditedListener.TextEditedAction() {
					public void textEditedAction(final String newText) {
						if (!f_finding.getSummary().equals(newText)) {
							FindingMutationUtility.asyncChangeSummary(f_finding
									.getFindingId(), newText);
						}
					}
				});
		f_summaryText.addListener(SWT.Modify, tel);
		f_summaryText.addListener(SWT.FocusOut, tel);

		f_artifacts.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event arg0) {
				final TableItem[] items = f_artifacts.getSelection();
				if (items.length > 0) {
					final TableItem item = items[0];
					final String projectName = f_finding.getProjectName();
					final String packageName = item.getText(2);
					final String className = item.getText(3);
					int lineNumber = Integer.valueOf(item.getText(4));
					JDTUtility.tryToOpenInEditor(projectName, packageName,
							className, lineNumber);
				}
			}
		});

		loadStyleSheet();

		Projects.getInstance().addObserver(this);
		DatabaseHub.getInstance().addObserver(this);

		updateContents();
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
			/*
			 * For some reason on the Mac the browser shows up as a little
			 * square window covering the view's icon. This seems to fix that.
			 */
			f_detailsText.setVisible(page == f_findingPage);
		}
		if (noFinding)
			return;

		/*
		 * We have a finding so show the details about it.
		 */
		f_summaryIcon.setImage(Utility.getImageFor(f_finding.getImportance()));
		f_summaryIcon.setToolTipText("The importance of this finding is "
				+ f_finding.getImportance().toStringSentenceCase());
		f_summaryText.setText(f_finding.getSummary());

		f_findingSynopsis.setText(getFindingSynopsis());

		f_projectName.setText(f_finding.getProjectName());
		f_packageName.setText(f_finding.getPackageName());
		f_className.setText(getClassName());

		StringBuffer b = new StringBuffer();
		HTMLPrinter.insertPageProlog(b, 0, fBackgroundColorRGB, fgStyleSheet
				.toString());
		String details = f_finding.getFindingTypeDetail();
		b.append(details);
		f_detailsText.setText(b.toString());

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

		List<AuditDetail> auditDetails = f_finding.getAudits();

		// Add label

		f_scrollingLabelComposite.removeAll();

		final SimpleDateFormat dateFormat = new SimpleDateFormat(
				"dd MMM yyyy 'at' HH:mm:ss");
		boolean first = true;
		if (auditDetails != null) {
			for (int i = auditDetails.size() - 1; i >= 0; i--) {
				final AuditDetail cd = auditDetails.get(i);
				final String auditText = cd.getText();
				if (first) {
					/*
					 * Bug 1024: We only clear out the comment text if it is
					 * exactly the same as the most recent audit's text.
					 * 
					 * This solution depends upon the getAudits() method on
					 * FindingDetail returning the audits in most recent to
					 * least recent order, i.e., that the newest audit is first.
					 */
					if (f_commentText.getText().equals(auditText)) {
						f_commentText.setText("");
					}
					first = false;
				}
				String userName = cd.getUser();
				if (userName == null) {
					userName = "Local";
				}
				f_scrollingLabelComposite.addEntry(userName + " on "
						+ dateFormat.format(cd.getTime()), auditText);
			}
		}

		f_artifacts.removeAll();
		final Image findbugs = SLImages.getImage(SLImages.IMG_FINDBUGS_FINDING);
		final Image pmd = SLImages.getImage(SLImages.IMG_PMD_FINDING);
		final Image pkgImage = SLImages
				.getJDTImage(ISharedImages.IMG_OBJS_PACKAGE);
		final Image classImage = SLImages
				.getJDTImage(ISharedImages.IMG_OBJS_CLASS);

		for (ArtifactDetail artifactDetail : f_finding.getArtifacts()) {
			final TableItem item = new TableItem(f_artifacts, SWT.NONE);

			final String tool = artifactDetail.getTool();
			item.setText(0, tool);
			if ("FindBugs".equals(tool)) {
				item.setImage(0, findbugs);
			} else if ("PMD".equals(tool)) {
				item.setImage(0, pmd);
			}

			item.setText(1, artifactDetail.getMessage());

			item.setText(2, artifactDetail.getPackageName());
			item.setImage(2, pkgImage);
			item.setText(3, artifactDetail.getClassName());
			item.setImage(3, classImage);

			item.setText(4, "" + artifactDetail.getLineOfCode());
		}
		for (TableColumn c : f_artifacts.getColumns()) {
			c.pack();
		}

		updateTabTitles();
		f_findingPage.layout(true, true);
	}

	private String getFindingSynopsis() {
		final String importance = f_finding.getImportance().toString()
				.toLowerCase();

		final int auditCount = f_finding.getNumberOfAudits();
		final String tool = f_finding.getTool();
		final FindingStatus status = f_finding.getStatus();

		StringBuilder b = new StringBuilder();
		b.append("This '");
		b.append(f_finding.getCategory());
		b.append("' finding (id=");
		b.append(f_finding.getFindingId());
		b.append(") ");
		b.append("is of ");
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
		final int auditCount = f_finding.getNumberOfAudits();
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

	@Override
	public void serverSynchronized() {
		findingMutated();
	}
}
