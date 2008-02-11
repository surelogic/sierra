package com.surelogic.sierra.client.eclipse.views;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.AuditTrail;
import com.surelogic.common.eclipse.HTMLPrinter;
import com.surelogic.common.eclipse.JDTUtility;
import com.surelogic.common.eclipse.PageBook;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.TextEditedListener;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.StyleSheetHelper;
import com.surelogic.sierra.client.eclipse.Utility;
import com.surelogic.sierra.client.eclipse.model.AbstractDatabaseObserver;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.model.FindingMutationUtility;
import com.surelogic.sierra.jdbc.finding.ArtifactDetail;
import com.surelogic.sierra.jdbc.finding.AuditDetail;
import com.surelogic.sierra.jdbc.finding.FindingDetail;
import com.surelogic.sierra.jdbc.finding.FindingStatus;
import com.surelogic.sierra.jdbc.finding.SourceDetail;
import com.surelogic.sierra.tool.message.Importance;

public class FindingDetailsMediator extends AbstractDatabaseObserver {

	public static final String STAMP_COMMENT = "I examined this finding.";

	private final Display f_display = PlatformUI.getWorkbench().getDisplay();

	private final RGB fBackgroundColorRGB = f_display.getSystemColor(
			SWT.COLOR_LIST_BACKGROUND).getRGB();

	private final PageBook f_pages;
	private final Control f_noFindingPage;
	private final Composite f_findingPage;
	private final Menu f_importanceRadioPopupMenu;
	private final ToolItem f_summaryIcon;
	private final Text f_summaryText;

	private final TabFolder f_folder;

	private final TabItem f_synopsisTab;
	private final SashForm f_synopsisSash;
	private final Button f_synopsisAudit;
	private final Link f_findingSynopsis;
	private final Tree f_locationTree;
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

	/*
	 * For view state persistence only.
	 */
	private int f_sashLocationWeight = 50;
	private int f_sashDescriptionWeight = 50;

	static private final String AUDIT_TAB = "audit";
	static private final String ARTIFACT_TAB = "artifact";
	static private final String SYNOPSIS_TAB = "synopsis";

	/*
	 * For view state persistence only.
	 */
	private String f_tabNameShown = SYNOPSIS_TAB;

	private void memoTabShown() {
		int index = f_folder.getSelectionIndex();
		if (index != -1) {
			TabItem tab = f_folder.getItem(index);
			if (f_auditTab == tab) {
				f_tabNameShown = AUDIT_TAB;
			} else if (f_artifactTab == tab) {
				f_tabNameShown = ARTIFACT_TAB;
			} else if (f_synopsisTab == tab) {
				f_tabNameShown = SYNOPSIS_TAB;
			}
		}
	}

	private void showTab(final String tabName) {
		if (AUDIT_TAB.equals(tabName)) {
			f_folder.setSelection(f_auditTab);
			f_tabNameShown = AUDIT_TAB;
		} else if (ARTIFACT_TAB.equals(tabName)) {
			f_folder.setSelection(f_artifactTab);
			f_tabNameShown = ARTIFACT_TAB;
		} else if (SYNOPSIS_TAB.equals(tabName)) {
			f_folder.setSelection(f_synopsisTab);
			f_tabNameShown = SYNOPSIS_TAB;
		}
	}

	/**
	 * This listener helps the hyperlinks control the three tabs.
	 */
	private final Listener f_tabLinkListener = new Listener() {
		public void handleEvent(Event event) {
			final String target = event.text;
			showTab(target);
		}
	};

	public FindingDetailsMediator(PageBook pages, Control noFindingPage,
			Composite findingPage, ToolItem summaryIcon, Text summaryText,
			TabFolder folder, TabItem synopsisTab, SashForm synopsisSash,
			Button synopsisAudit, Link findingSynopsis, Tree locationTree,
			Browser detailsText, TabItem auditTab, Button quickAudit,
			Button criticalButton, Button highButton, Button mediumButton,
			Button lowButton, Button irrelevantButton, Text commentText,
			Button commentButton, AuditTrail scrollingLabelComposite,
			TabItem artifactTab, Table artifacts) {
		f_pages = pages;
		f_noFindingPage = noFindingPage;
		f_findingPage = findingPage;
		f_summaryIcon = summaryIcon;
		f_summaryText = summaryText;
		f_folder = folder;
		f_synopsisTab = synopsisTab;
		f_synopsisSash = synopsisSash;
		f_synopsisAudit = synopsisAudit;
		f_findingSynopsis = findingSynopsis;
		f_locationTree = locationTree;
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

		f_importanceRadioPopupMenu = new Menu(f_pages.getShell(), SWT.POP_UP);
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
						final UIJob job = new SLUIJob() {
							@Override
							public IStatus runInUIThread(
									IProgressMonitor monitor) {
								updateContents();
								return Status.OK_STATUS;
							}
						};
						job.schedule();

					} catch (IllegalArgumentException iae) {
						final UIJob job = new SLUIJob() {
							@Override
							public IStatus runInUIThread(
									IProgressMonitor monitor) {
								f_finding = null;
								updateContents();
								return Status.OK_STATUS;
							}
						};
						job.schedule();
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

	void asyncSetSynopsisSashWeights(final int sashLocationWeight,
			final int sashDescriptionWeight) {
		if (SLLogger.getLogger().isLoggable(Level.FINER)) {
			SLLogger.getLogger().finer(
					"Sash weights set to: location at " + sashLocationWeight
							+ " and description at " + sashDescriptionWeight
							+ ".");
		}
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				f_synopsisSash.setWeights(new int[] { sashLocationWeight,
						sashDescriptionWeight });
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	void asyncSetTabShown(final String tabName) {
		if (SLLogger.getLogger().isLoggable(Level.FINER)) {
			SLLogger.getLogger().finer(
					"Finding Details showing tab " + tabName + ".");
		}
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				showTab(tabName);
				return Status.OK_STATUS;
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

	private final Listener f_locationListener = new Listener() {
		public void handleEvent(Event event) {
			final TreeItem item = (TreeItem) event.item;
			if (item == null) {
				return;
			}
			final SourceDetail src = (SourceDetail) item.getData();
			if (src == null)
				return;

			JDTUtility.tryToOpenInEditor(f_finding.getProjectName(), src
					.getPackageName(), src.getClassName(), src.getLineOfCode());
		}
	};

	public void init() {
		f_criticalButton.addListener(SWT.Selection, f_radioListener);
		f_highButton.addListener(SWT.Selection, f_radioListener);
		f_mediumButton.addListener(SWT.Selection, f_radioListener);
		f_lowButton.addListener(SWT.Selection, f_radioListener);
		f_irrelevantButton.addListener(SWT.Selection, f_radioListener);

		f_folder.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// I'm not sure when this is called (if ever) for a tab folder.
			}

			public void widgetSelected(SelectionEvent e) {
				/*
				 * Invoked when the tab selection changes.
				 */
				memoTabShown();
			}
		});

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

		f_locationTree.addListener(SWT.Selection, f_locationListener);

		final MenuItem criticalItem = new MenuItem(f_importanceRadioPopupMenu,
				SWT.RADIO);
		criticalItem.setText("Critical");
		criticalItem.setImage(SLImages
				.getImage(SLImages.IMG_ASTERISK_ORANGE_100));
		criticalItem.setData(Importance.CRITICAL);
		criticalItem.addListener(SWT.Selection, f_radioListener);

		final MenuItem highItem = new MenuItem(f_importanceRadioPopupMenu,
				SWT.RADIO);
		highItem.setText("High");
		highItem.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_ORANGE_75));
		highItem.setData(Importance.HIGH);
		highItem.addListener(SWT.Selection, f_radioListener);

		final MenuItem mediumItem = new MenuItem(f_importanceRadioPopupMenu,
				SWT.RADIO);
		mediumItem.setText("Medium");
		mediumItem.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_ORANGE_50));
		mediumItem.setData(Importance.MEDIUM);
		mediumItem.addListener(SWT.Selection, f_radioListener);

		final MenuItem lowItem = new MenuItem(f_importanceRadioPopupMenu,
				SWT.RADIO);
		lowItem.setText("Low");
		lowItem.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_ORANGE_25));
		lowItem.setData(Importance.LOW);
		lowItem.addListener(SWT.Selection, f_radioListener);

		final MenuItem irrelevantItem = new MenuItem(
				f_importanceRadioPopupMenu, SWT.RADIO);
		irrelevantItem.setText("Irrelevant");
		irrelevantItem.setImage(SLImages
				.getImage(SLImages.IMG_ASTERISK_ORANGE_0));
		irrelevantItem.setData(Importance.IRRELEVANT);
		irrelevantItem.addListener(SWT.Selection, f_radioListener);

		f_summaryIcon.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Point point = new Point(event.x, event.y);
				point = f_pages.getDisplay().map(f_summaryIcon.getParent(),
						null, point);
				f_importanceRadioPopupMenu.setLocation(point);
				f_importanceRadioPopupMenu.setVisible(true);
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

		/*
		 * When the location tree is resized we'll just guess that the sash is
		 * involved. Hopefully, this is conservative. This seems to be the only
		 * way to do this.
		 */
		f_locationTree.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				int[] weights = f_synopsisSash.getWeights();
				if (weights != null && weights.length == 2) {
					f_sashLocationWeight = weights[0];
					f_sashDescriptionWeight = weights[1];
					if (SLLogger.getLogger().isLoggable(Level.FINER)) {
						SLLogger.getLogger().finer(
								"Sash weights changed to: location at "
										+ f_sashLocationWeight
										+ " and description at "
										+ f_sashDescriptionWeight + ".");
					}
				}
			}
		});

		DatabaseHub.getInstance().addObserver(this);

		updateContents();
		FindingDetailsPersistence.load(this);
	}

	public void dispose() {
		if (f_finding == null)
			FindingDetailsPersistence.saveNoFindingShown(f_sashLocationWeight,
					f_sashDescriptionWeight, f_tabNameShown);
		else
			FindingDetailsPersistence.save(f_finding.getFindingId(),
					f_sashLocationWeight, f_sashDescriptionWeight,
					f_tabNameShown);
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

		for (MenuItem i : f_importanceRadioPopupMenu.getItems()) {
			i.setSelection(f_finding.getImportance() == i.getData());
		}

		f_findingSynopsis.setText(getFindingSynopsis());

		initLocationTree(f_locationTree, f_finding);

		StringBuffer b = new StringBuffer();
		HTMLPrinter.insertPageProlog(b, 0, fBackgroundColorRGB,
				StyleSheetHelper.getInstance().getStyleSheet());
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

			item.setText(4, Integer.toString(artifactDetail.getLineOfCode()));
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
		b.append("<a href=\"");
		b.append(AUDIT_TAB);
		b.append("\">");
		b.append(importance);
		b.append("</a>");
		b.append(" importance. It has ");
		if (auditCount > 0) {
			b.append("been <a href=\"");
			b.append(AUDIT_TAB);
			b.append("\">audited ");
			b.append(auditCount);
			b.append(" times</a> ");
		} else {
			b.append("not been <a href=\"");
			b.append(AUDIT_TAB);
			b.append("\">audited</a> ");
		}
		if (status == FindingStatus.FIXED) {
			b.append("and was not found in the code during the last scan.");
		} else {
			if (status == FindingStatus.NEW) {
				b.append("and was discovered by ");
				b.append("<a href=\"");
				b.append(ARTIFACT_TAB);
				b.append("\">");
				if (tool.charAt(0) == '(') {
					b.append("by multiple tools (with ");
					b.append(f_finding.getNumberOfArtifacts());
					b.append(" artifacts reported)");
				} else
					b.append(tool);
				b.append("</a> during the last scan.");
			} else {
				b.append("and has been reported by ");
				b.append("<a href=\"");
				b.append(ARTIFACT_TAB);
				b.append("\">");
				b.append(tool);
				b.append("</a> for several scans.");
			}
		}
		return b.toString();
	}

	private void initLocationTree(Tree tree, FindingDetail finding) {
		tree.removeAll();

		// TODO reuse old TreeItems?
		final TreeItem proj = new TreeItem(tree, SWT.NULL);
		proj.setText(finding.getProjectName());
		proj.setImage(SLImages
				.getWorkbenchImage(IDE.SharedImages.IMG_OBJ_PROJECT));

		int numArtifacts = finding.getNumberOfArtifacts();
		if (numArtifacts < 1) {
			throw new IllegalArgumentException("Bad number of artifacts: "
					+ numArtifacts);
		}
		final ArtifactDetail firstArtifact = finding.getArtifacts().get(0);
		if (finding.getNumberOfArtifacts() == 1
				&& firstArtifact.getAdditionalSources().isEmpty()) {
			// Just one source location to show
			TreeItem pkg = new TreeItem(proj, SWT.NULL);
			pkg.setText(firstArtifact.getPackageName());
			pkg.setImage(SLImages.getJDTImage(ISharedImages.IMG_OBJS_PACKAGE));

			TreeItem clazz = new TreeItem(pkg, SWT.NULL);
			clazz.setText(firstArtifact.getClassName() + " at line "
					+ firstArtifact.getLineOfCode());
			clazz.setImage(SLImages.getJDTImage(ISharedImages.IMG_OBJS_CLASS));
			clazz.setData(firstArtifact.getPrimarySource());
			showAsLink(clazz);
			// clazz.addListener(SWT.Selection, f_locationListener);
			tree.showItem(clazz);
		} else {
			// Deal with multiple artifacts, and multiple locations
			Map<String, TreeItem> packages = new HashMap<String, TreeItem>();
			Map<String, TreeItem> classes = new HashMap<String, TreeItem>();
			Map<String, TreeItem> lines = new HashMap<String, TreeItem>();
			for (ArtifactDetail artifact : finding.getArtifacts()) {
				TreeItem loc = createLocation(proj, packages, classes, lines,
						artifact.getPrimarySource());
				tree.showItem(loc);
				boolean first = true;
				for (SourceDetail src : artifact.getAdditionalSources()) {
					loc = createLocation(proj, packages, classes, lines, src);
					if (first) {
						tree.showItem(loc);
						first = false;
					}
				}
			}
		}
	}

	private void showAsLink(TreeItem item) {
		Display d = item.getDisplay();
		item.setForeground(d.getSystemColor(SWT.COLOR_BLUE));
	}

	private TreeItem createLocation(TreeItem proj,
			Map<String, TreeItem> packages, Map<String, TreeItem> classes,
			Map<String, TreeItem> lines, SourceDetail loc) {
		TreeItem pkg = packages.get(loc.getPackageName());
		TreeItem clazz;
		TreeItem line;
		String qualifiedClassName = null;
		String qualifiedClassLine = null;
		if (pkg == null) {
			pkg = new TreeItem(proj, SWT.NULL);
			pkg.setText(loc.getPackageName());
			pkg.setImage(SLImages.getJDTImage(ISharedImages.IMG_OBJS_PACKAGE));
			packages.put(loc.getPackageName(), pkg);
			clazz = null; // This can't exist if the package didn't
		} else {
			qualifiedClassName = loc.getPackageName() + '.'
					+ loc.getClassName();
			clazz = classes.get(qualifiedClassName);
		}
		if (clazz == null) {
			clazz = new TreeItem(pkg, SWT.NULL);
			clazz.setText(loc.getClassName());
			clazz.setImage(SLImages.getJDTImage(ISharedImages.IMG_OBJS_CLASS));
			if (qualifiedClassName == null) {
				qualifiedClassName = loc.getPackageName() + '.'
						+ loc.getClassName();
			}
			classes.put(qualifiedClassName, clazz);
			line = null;
		} else {
			qualifiedClassLine = qualifiedClassName + ':' + loc.getLineOfCode();
			line = lines.get(qualifiedClassLine);
		}
		if (line == null) {
			qualifiedClassLine = qualifiedClassName + ':' + loc.getLineOfCode();
			line = new TreeItem(clazz, SWT.NULL);
			if (loc.getLineOfCode() != loc.getEndLineOfCode()) {
				line.setText("lines " + loc.getLineOfCode() + " to "
						+ loc.getEndLineOfCode());
			} else {
				line.setText("line " + loc.getLineOfCode());
			}
			showAsLink(line);
			line.setData(loc);
			lines.put(qualifiedClassLine, line);
		}
		return line;
	}

	/*
	 * private String getClassName() { StringBuilder b = new StringBuilder();
	 * int[] lines = f_finding.getLinesOfCode();
	 * b.append(f_finding.getClassName()); b.append(" at line"); if
	 * (lines.length > 1) b.append("s"); b.append(" "); boolean first = true;
	 * for (int line : lines) { if (first) first = false; else b.append(" ");
	 * b.append("<a href=\""); b.append(line); b.append("\">"); b.append(line);
	 * b.append("</a>"); } return b.toString(); }
	 */

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

	@Override
	public void changed() {
		if (f_finding != null) {
			asyncQueryAndShow(f_finding.getFindingId());
		}
	}

	@Override
	public void serverSynchronized() {
		findingMutated();
	}
}
