package com.surelogic.sierra.client.eclipse.views;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.CommonImages;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ui.AuditTrail;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.FocusAction;
import com.surelogic.common.ui.HTMLPrinter;
import com.surelogic.common.ui.JDTUIUtility;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.TextEditedListener;
import com.surelogic.common.ui.jobs.SLUIJob;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.SierraUIUtility;
import com.surelogic.sierra.client.eclipse.StyleSheetHelper;
import com.surelogic.sierra.client.eclipse.jobs.AbstractSierraDatabaseJob;
import com.surelogic.sierra.client.eclipse.model.FindingMutationUtility;
import com.surelogic.sierra.jdbc.finding.ArtifactDetail;
import com.surelogic.sierra.jdbc.finding.AuditDetail;
import com.surelogic.sierra.jdbc.finding.FindingDetail;
import com.surelogic.sierra.jdbc.finding.FindingStatus;
import com.surelogic.sierra.jdbc.finding.SourceDetail;
import com.surelogic.sierra.tool.message.Importance;

public class FindingDetailsMediator extends AbstractSierraViewMediator implements IViewUpdater {

  public static final String STAMP_COMMENT = "I examined this finding.";
  final RGB f_ForegroundColorRGB;
  final RGB f_BackgroundColorRGB;

  final Composite f_parent;
  final Menu f_importanceRadioPopupMenu;
  final ToolItem f_summaryIcon;
  final Text f_summaryText;

  final TabFolder f_folder;

  final TabItem f_synopsisTab;
  final SashForm f_synopsisSash;
  final Button f_synopsisAudit;
  final Link f_findingSynopsis;
  final Tree f_locationTree;
  final Browser f_detailsText;

  final TabItem f_auditTab;
  final Button f_quickAudit;
  final Button f_criticalButton;
  final Button f_highButton;
  final Button f_mediumButton;
  final Button f_lowButton;
  final Button f_irrelevantButton;
  final Text f_commentText;
  final Button f_commentButton;
  final AuditTrail f_scrollingLabelComposite;

  final TabItem f_artifactTab;
  final Table f_artifacts;

  final Action f_viewCut;
  final Action f_viewCopy;
  final Action f_viewPaste;
  final Action f_viewSelectAll;

  volatile FindingDetail f_finding;

  /*
   * For view state persistence only.
   */
  int f_sashLocationWeight = 50;
  int f_sashDescriptionWeight = 50;

  static private final String AUDIT_TAB = "audit";
  static private final String ARTIFACT_TAB = "artifact";
  static private final String SYNOPSIS_TAB = "synopsis";

  /*
   * For view state persistence only.
   */
  String f_tabNameShown = SYNOPSIS_TAB;

  void memoTabShown() {
    final int index = f_folder.getSelectionIndex();
    if (index != -1) {
      final TabItem tab = f_folder.getItem(index);
      if (f_auditTab == tab) {
        f_tabNameShown = AUDIT_TAB;
      } else if (f_artifactTab == tab) {
        f_tabNameShown = ARTIFACT_TAB;
      } else if (f_synopsisTab == tab) {
        f_tabNameShown = SYNOPSIS_TAB;
      }
    }
  }

  void showTab(final String tabName) {
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

  public FindingDetailsMediator(final FindingDetailsView view, final Composite parent, final ToolItem summaryIcon,
      final Text summaryText, final TabFolder folder, final TabItem synopsisTab, final SashForm synopsisSash,
      final Button synopsisAudit, final Link findingSynopsis, final Tree locationTree, final Browser detailsText,
      final TabItem auditTab, final Button quickAudit, final Button criticalButton, final Button highButton,
      final Button mediumButton, final Button lowButton, final Button irrelevantButton, final Text commentText,
      final Button commentButton, final AuditTrail scrollingLabelComposite, final TabItem artifactTab, final Table artifacts) {
    super(view);
    f_parent = parent;
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

    f_ForegroundColorRGB = parent.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND).getRGB();
    f_BackgroundColorRGB = parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB();

    f_viewCut = new FocusAction(parent) {
      @Override
      protected void takeActionOnText(final Text c) {
        c.cut();
      }
    };
    f_viewCopy = new FocusAction(parent) {
      @Override
      protected void takeActionOnText(final Text c) {
        c.copy();
      }
    };
    f_viewPaste = new FocusAction(parent) {
      @Override
      protected void takeActionOnText(final Text c) {
        c.paste();
      }
    };
    f_viewSelectAll = new FocusAction(parent) {
      @Override
      protected void takeActionOnText(final Text c) {
        c.selectAll();
      }
    };

    f_importanceRadioPopupMenu = new Menu(parent.getShell(), SWT.POP_UP);
  }

  @Override
  public String getHelpId() {
    return "com.surelogic.sierra.client.eclipse.view-finding-details";
  }

  @Override
  public String getNoDataI18N() {
    return "sierra.eclipse.noDataFindingDetails";
  }

  @Override
  public Listener getNoDataListener() {
    return new Listener() {
      @Override
      public void handleEvent(final Event event) {
        EclipseUIUtility.showView(FindingsView.ID);
      }
    };
  }

  void asyncQueryAndShow(final long findingId) {
    final Job job = new AbstractSierraDatabaseJob("Querying details of finding " + findingId) {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {
        monitor.beginTask("Querying finding data", IProgressMonitor.UNKNOWN);
        try {
          final Connection c = Data.getInstance().readOnlyConnection();
          try {
            final FindingDetail detail = FindingDetail.getDetailOrNull(c, findingId);
            f_finding = detail;

            // got details, update the view in the UI thread
            asyncUpdateContentsForUI(FindingDetailsMediator.this);
          } catch (final IllegalArgumentException iae) {
            f_finding = null;
            asyncUpdateContentsForUI(FindingDetailsMediator.this);
          } finally {
            c.close();
          }
          monitor.done();
          return Status.OK_STATUS;
        } catch (final SQLException e) {
          final int errNo = 57;
          final String msg = I18N.err(errNo, findingId);
          return SLEclipseStatusUtility.createErrorStatus(errNo, msg, e);
        }
      }
    };
    job.schedule();
  }

  void asyncSetSynopsisSashWeights(final int sashLocationWeight, final int sashDescriptionWeight) {
    if (SLLogger.getLogger().isLoggable(Level.FINER)) {
      SLLogger.getLogger().finer(
          "Sash weights set to: location at " + sashLocationWeight + " and description at " + sashDescriptionWeight + ".");
    }
    final UIJob job = new SLUIJob() {
      @Override
      public IStatus runInUIThread(final IProgressMonitor monitor) {
        f_synopsisSash.setWeights(new int[] { sashLocationWeight, sashDescriptionWeight });
        return Status.OK_STATUS;
      }
    };
    job.schedule();
  }

  void asyncSetTabShown(final String tabName) {
    if (SLLogger.getLogger().isLoggable(Level.FINER)) {
      SLLogger.getLogger().finer("Finding Details showing tab " + tabName + ".");
    }
    final UIJob job = new SLUIJob() {
      @Override
      public IStatus runInUIThread(final IProgressMonitor monitor) {
        showTab(tabName);
        return Status.OK_STATUS;
      }
    };
    job.schedule();
  }

  private final Listener f_radioListener = new Listener() {
    @Override
    public void handleEvent(final Event event) {
      final Importance current = f_finding.getImportance();
      if (event.widget != null) {
        if (event.widget.getData() instanceof Importance) {
          final Importance desired = (Importance) event.widget.getData();
          if (desired != current) {
            FindingMutationUtility.asyncChangeImportance(f_finding.getFindingId(), current, desired);
          }
        }
      }
    }
  };

  private final Listener f_locationListener = new Listener() {
    @Override
    public void handleEvent(final Event event) {
      final TreeItem item = (TreeItem) event.item;
      if (item == null) {
        return;
      }
      final SourceDetail src = (SourceDetail) item.getData();
      if (src == null) {
        return;
      }

      JDTUIUtility.tryToOpenInEditor(f_finding.getProjectName(), src.getPackageName(), src.getClassName(), src.getLineOfCode(),
          src.getEndLineOfCode());
    }
  };

  @Override
  public void init() {
    f_criticalButton.addListener(SWT.Selection, f_radioListener);
    f_highButton.addListener(SWT.Selection, f_radioListener);
    f_mediumButton.addListener(SWT.Selection, f_radioListener);
    f_lowButton.addListener(SWT.Selection, f_radioListener);
    f_irrelevantButton.addListener(SWT.Selection, f_radioListener);

    f_folder.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetDefaultSelected(final SelectionEvent e) {
        // I'm not sure when this is called (if ever) for a tab folder.
      }

      @Override
      public void widgetSelected(final SelectionEvent e) {
        /*
         * Invoked when the tab selection changes.
         */
        memoTabShown();
      }
    });

    final Listener commentListener = new Listener() {
      @Override
      public void handleEvent(final Event event) {
        f_commentButton.setEnabled(false);
        /*
         * All this job stuff is to try and avoid pressing the button twice and
         * getting a double comment. It simple disables the button and runs a
         * job.
         * 
         * The job then does the work and submits a second job to enable the
         * button after a slight delay.
         */
        final UIJob job = new SLUIJob() {
          @Override
          public IStatus runInUIThread(IProgressMonitor monitor) {
            final String commentText = f_commentText.getText();
            if (!(commentText == null || commentText.trim().equals(""))) {
              FindingMutationUtility.asyncComment(f_finding.getFindingId(), commentText);
            }
            final UIJob job = new SLUIJob() {
              @Override
              public IStatus runInUIThread(IProgressMonitor monitor) {
                f_commentButton.setEnabled(true);
                return Status.OK_STATUS;
              }
            };
            job.schedule(10);
            return Status.OK_STATUS;
          }
        };
        job.schedule();
      }
    };
    f_commentText.addListener(SWT.KeyDown, new Listener() {
      @Override
      public void handleEvent(final Event e) {
        if (e.character != SWT.CR) {
          return;
        }
        // Some kind of return
        final int ctrl = e.stateMask & SWT.CONTROL;
        if (ctrl != 0) {
          // Got Ctrl-Return
          commentListener.handleEvent(e);
        }
      }
    });
    f_commentButton.addListener(SWT.Selection, commentListener);

    final SelectionAdapter stampAction = new SelectionAdapter() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        FindingMutationUtility.asyncComment(f_finding.getFindingId(), STAMP_COMMENT);
      }
    };
    f_synopsisAudit.addSelectionListener(stampAction);
    f_quickAudit.addSelectionListener(stampAction);

    f_locationTree.addListener(SWT.Selection, f_locationListener);

    final MenuItem criticalItem = new MenuItem(f_importanceRadioPopupMenu, SWT.RADIO);
    criticalItem.setText("Critical");
    criticalItem.setImage(SLImages.getImage(CommonImages.IMG_ASTERISK_ORANGE_100));
    criticalItem.setData(Importance.CRITICAL);
    criticalItem.addListener(SWT.Selection, f_radioListener);

    final MenuItem highItem = new MenuItem(f_importanceRadioPopupMenu, SWT.RADIO);
    highItem.setText("High");
    highItem.setImage(SLImages.getImage(CommonImages.IMG_ASTERISK_ORANGE_75));
    highItem.setData(Importance.HIGH);
    highItem.addListener(SWT.Selection, f_radioListener);

    final MenuItem mediumItem = new MenuItem(f_importanceRadioPopupMenu, SWT.RADIO);
    mediumItem.setText("Medium");
    mediumItem.setImage(SLImages.getImage(CommonImages.IMG_ASTERISK_ORANGE_50));
    mediumItem.setData(Importance.MEDIUM);
    mediumItem.addListener(SWT.Selection, f_radioListener);

    final MenuItem lowItem = new MenuItem(f_importanceRadioPopupMenu, SWT.RADIO);
    lowItem.setText("Low");
    lowItem.setImage(SLImages.getImage(CommonImages.IMG_ASTERISK_ORANGE_25));
    lowItem.setData(Importance.LOW);
    lowItem.addListener(SWT.Selection, f_radioListener);

    final MenuItem irrelevantItem = new MenuItem(f_importanceRadioPopupMenu, SWT.RADIO);
    irrelevantItem.setText("Irrelevant");
    irrelevantItem.setImage(SLImages.getImage(CommonImages.IMG_ASTERISK_ORANGE_0));
    irrelevantItem.setData(Importance.IRRELEVANT);
    irrelevantItem.addListener(SWT.Selection, f_radioListener);

    f_summaryIcon.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        Point point = new Point(event.x, event.y);
        point = f_parent.getDisplay().map(f_summaryIcon.getParent(), null, point);
        f_importanceRadioPopupMenu.setLocation(point);
        f_importanceRadioPopupMenu.setVisible(true);
      }
    });
    f_findingSynopsis.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        final String target = event.text;
        showTab(target);
      }
    });

    final Listener tel = new TextEditedListener(new TextEditedListener.TextEditedAction() {
      @Override
      public void textEditedAction(final String newText) {
        if (!f_finding.getSummary().equals(newText)) {
          FindingMutationUtility.asyncChangeSummary(f_finding.getFindingId(), newText);
        }
      }
    });
    f_summaryText.addListener(SWT.Modify, tel);
    f_summaryText.addListener(SWT.FocusOut, tel);

    f_artifacts.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event arg0) {
        final TableItem[] items = f_artifacts.getSelection();
        if (items.length > 0) {
          final TableItem item = items[0];
          final String projectName = f_finding.getProjectName();
          final String packageName = item.getText(2);
          final String className = item.getText(3);
          final int lineNumber = Integer.valueOf(item.getText(4));
          Object data = item.getData();
          if (data instanceof ArtifactDetail) {
            ArtifactDetail artifactDetail = (ArtifactDetail) data;
            SierraUIUtility.tryToOpenInEditor(projectName, packageName, className, lineNumber, artifactDetail);
          } else
            SierraUIUtility.tryToOpenInEditor(projectName, packageName, className, lineNumber, f_finding.getFindingId());
        }
      }
    });

    /*
     * When the location tree is resized we'll just guess that the sash is
     * involved. Hopefully, this is conservative. This seems to be the only way
     * to do this.
     */
    f_locationTree.addListener(SWT.Resize, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        final int[] weights = f_synopsisSash.getWeights();
        if (weights != null && weights.length == 2) {
          f_sashLocationWeight = weights[0];
          f_sashDescriptionWeight = weights[1];
          if (SLLogger.getLogger().isLoggable(Level.FINER)) {
            SLLogger.getLogger().finer(
                "Sash weights changed to: location at " + f_sashLocationWeight + " and description at " + f_sashDescriptionWeight
                    + ".");
          }
        }
      }
    });

    f_view.setGlobalActionHandler(ActionFactory.CUT.getId(), f_viewCut);
    f_view.setGlobalActionHandler(ActionFactory.COPY.getId(), f_viewCopy);
    f_view.setGlobalActionHandler(ActionFactory.PASTE.getId(), f_viewPaste);
    f_view.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), f_viewSelectAll);

    super.init();

    updateContentsForUI();
    FindingDetailsPersistence.load(this);
  }

  @Override
  public void dispose() {
    if (f_finding == null) {
      FindingDetailsPersistence.saveNoFindingShown(f_sashLocationWeight, f_sashDescriptionWeight, f_tabNameShown);
    } else {
      FindingDetailsPersistence.save(f_finding.getFindingId(), f_sashLocationWeight, f_sashDescriptionWeight, f_tabNameShown);
    }
    super.dispose();
  }

  @Override
  public void setFocus() {
    final TabItem[] items = f_folder.getSelection();
    if (items.length > 0) {
      final TabItem item = items[0];
      if (item == f_auditTab) {
        f_commentText.setFocus();
      } else {
        f_summaryText.setFocus();
      }
    }
  }

  /**
   * Must be invoked from the SWT thread.
   */
  @Override
  public void updateContentsForUI() {
    final boolean showFinding = f_finding != null;

    // Page doesn't match our state
    if (!f_view.matchesStatus(showFinding)) {
      f_view.hasData(showFinding);
      /*
       * For some reason on the Mac the browser shows up as a little square
       * window covering the view's icon. This seems to fix that.
       */
      f_detailsText.setVisible(showFinding);
    }
    if (!showFinding) {
      return;
    }

    /*
     * We have a finding so show the details about it.
     */
    f_summaryIcon.setImage(SierraUIUtility.getImageFor(f_finding.getImportance()));
    f_summaryIcon.setToolTipText("The importance of this finding is " + f_finding.getImportance().toStringSentenceCase());
    f_summaryText.setText(f_finding.getSummary());

    for (final MenuItem i : f_importanceRadioPopupMenu.getItems()) {
      i.setSelection(f_finding.getImportance() == i.getData());
    }

    f_findingSynopsis.setText(getFindingSynopsis());

    initLocationTree(f_locationTree, f_finding);

    final StringBuffer b = new StringBuffer();
    HTMLPrinter.insertPageProlog(b, 0, f_ForegroundColorRGB, f_BackgroundColorRGB, StyleSheetHelper.getInstance().getStyleSheet());
    b.append("<b>Finding Type: ").append(f_finding.getFindingTypeName()).append("</b>");
    final String details = f_finding.getFindingTypeDetail();
    b.append(details);
    HTMLPrinter.addPageEpilog(b);
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

    final List<AuditDetail> auditDetails = f_finding.getAudits();

    // Add label

    f_scrollingLabelComposite.removeAll();

    final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy 'at' HH:mm:ss");
    boolean first = true;
    if (auditDetails != null) {
      for (int i = auditDetails.size() - 1; i >= 0; i--) {
        final AuditDetail cd = auditDetails.get(i);
        final String auditText = cd.getText();
        if (first) {
          /*
           * Bug 1024: We only clear out the comment text if it is exactly the
           * same as the most recent audit's text.
           * 
           * This solution depends upon the getAudits() method on FindingDetail
           * returning the audits in most recent to least recent order, i.e.,
           * that the newest audit is first.
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
        f_scrollingLabelComposite.addEntry(userName + " on " + dateFormat.format(cd.getTime()), auditText);
      }
    }

    f_artifacts.removeAll();

    for (final ArtifactDetail artifactDetail : f_finding.getArtifacts()) {
      final TableItem item = new TableItem(f_artifacts, SWT.NONE);
      item.setData(artifactDetail);

      final String tool = artifactDetail.getTool();
      item.setText(0, tool);
      item.setImage(SierraUIUtility.getImageForTool(tool));

      item.setText(1, artifactDetail.getMessage());

      item.setText(2, artifactDetail.getPackageName());
      item.setImage(2, SLImages.getImage(CommonImages.IMG_PACKAGE));
      item.setText(3, artifactDetail.getClassName());
      item.setImage(3, SierraUIUtility.getImageForType(null, artifactDetail.getPackageName(), artifactDetail.getClassName()));
      item.setText(4, Integer.toString(artifactDetail.getLineOfCode()));
    }
    for (final TableColumn c : f_artifacts.getColumns()) {
      c.pack();
    }

    updateTabTitles();
    f_parent.layout(true, true);
  }

  String getFindingSynopsis() {
    final String importance = f_finding.getImportance().toString().toLowerCase();

    final int auditCount = f_finding.getNumberOfAudits();
    final String tool = f_finding.getTool();
    final FindingStatus status = f_finding.getStatus();

    final StringBuilder b = new StringBuilder();
    b.append("This finding (id=");
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
        } else {
          b.append(tool);
        }
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

  void initLocationTree(final Tree tree, final FindingDetail finding) {
    tree.removeAll();

    // TODO reuse old TreeItems?
    final TreeItem proj = new TreeItem(tree, SWT.NULL);
    proj.setText(finding.getProjectName());
    proj.setImage(SLImages.getImageForProject(finding.getProjectName()));

    final int numArtifacts = finding.getNumberOfArtifacts();
    if (numArtifacts < 1) {
      throw new IllegalArgumentException("Bad number of artifacts: " + numArtifacts);
    }
    if (numArtifacts != finding.getArtifacts().size()) {
      LOG.severe("Number of artifacts don't match");
      return;
    }
    final ArtifactDetail firstArtifact = finding.getArtifacts().get(0);
    if (finding.getNumberOfArtifacts() == 1 && firstArtifact.getAdditionalSources().isEmpty()) {
      // Just one source location to show
      final TreeItem pkg = new TreeItem(proj, SWT.NULL);
      pkg.setText(firstArtifact.getPackageName());
      pkg.setImage(SLImages.getImage(CommonImages.IMG_PACKAGE));

      final TreeItem clazz = new TreeItem(pkg, SWT.NULL);
      clazz.setText(firstArtifact.getClassName() + " at line " + firstArtifact.getLineOfCode());
      clazz.setImage(SierraUIUtility.getImageForType(finding.getProjectName(), finding.getPackageName(), finding.getClassName()));
      clazz.setData(firstArtifact.getPrimarySource());
      showAsLink(clazz);
      tree.showItem(clazz);
    } else {
      final List<SourceDetail> srcs = new ArrayList<>();
      for (final ArtifactDetail artifact : finding.getArtifacts()) {
        srcs.add(artifact.getPrimarySource());
        for (final SourceDetail src : artifact.getAdditionalSources()) {
          srcs.add(src);
        }
      }
      Collections.sort(srcs);

      // Deal with multiple artifacts, and multiple locations
      // TODO eliminate these if the sources are ordered?
      final Map<String, TreeItem> packages = new HashMap<>();
      final Map<String, TreeItem> classes = new HashMap<>();
      final Map<String, TreeItem> lines = new HashMap<>();
      TreeItem first = null;
      for (final SourceDetail src : srcs) {
        final TreeItem loc = createLocation(proj, packages, classes, lines, src);
        tree.showItem(loc);
        if (first == null) {
          first = loc;
        }
      }
      tree.showItem(first);
    }
  }

  void showAsLink(final TreeItem item) {
    final Display d = item.getDisplay();
    item.setForeground(d.getSystemColor(SWT.COLOR_BLUE));
  }

  TreeItem createLocation(final TreeItem proj, final Map<String, TreeItem> packages, final Map<String, TreeItem> classes,
      final Map<String, TreeItem> lines, final SourceDetail loc) {
    TreeItem pkg = packages.get(loc.getPackageName());
    TreeItem clazz;
    TreeItem line;
    String qualifiedClassName = null;
    String qualifiedClassLine;
    if (pkg == null) {
      pkg = new TreeItem(proj, SWT.NULL);
      pkg.setText(loc.getPackageName());
      pkg.setImage(SLImages.getImage(CommonImages.IMG_PACKAGE));
      packages.put(loc.getPackageName(), pkg);
      clazz = null; // This can't exist if the package didn't
    } else {
      qualifiedClassName = loc.getPackageName() + '.' + loc.getClassName();
      clazz = classes.get(qualifiedClassName);
    }
    if (clazz == null) {
      clazz = new TreeItem(pkg, SWT.NULL);
      clazz.setText(loc.getClassName());
      clazz.setImage(SierraUIUtility.getImageForType(null, loc.getPackageName(), loc.getClassName()));
      if (qualifiedClassName == null) {
        qualifiedClassName = loc.getPackageName() + '.' + loc.getClassName();
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
        line.setText("lines " + loc.getLineOfCode() + " to " + loc.getEndLineOfCode());
      } else {
        line.setText("line " + loc.getLineOfCode());
      }
      showAsLink(line);
      line.setData(loc);
      lines.put(qualifiedClassLine, line);
    }
    return line;
  }

  /**
   * Must be invoked from the SWT thread.
   */
  void updateTabTitles() {
    final int auditCount = f_finding.getNumberOfAudits();
    final int artifactCount = f_finding.getNumberOfArtifacts();
    if (auditCount == 0) {
      f_auditTab.setText("No Audits");
    } else {
      f_auditTab.setText(auditCount + " Audit" + (auditCount > 1 ? "s" : ""));
    }
    if (artifactCount == 0) {
      f_artifactTab.setText("No Artifacts");
    } else {
      f_artifactTab.setText(artifactCount + " Artifact" + (artifactCount > 1 ? "s" : ""));
    }
  }

  @Override
  public void changed() {
    if (f_finding != null) {
      asyncQueryAndShow(f_finding.getFindingId());
    }
  }

  @Override
  public void projectSynchronized() {
    findingMutated();
  }
}
