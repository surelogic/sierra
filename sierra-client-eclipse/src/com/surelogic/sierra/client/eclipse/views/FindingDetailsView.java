package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPage;

import com.surelogic.common.CommonImages;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.AuditTrail;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.TableUtility;
import com.surelogic.common.ui.dialogs.ErrorDialogUtility;
import com.surelogic.sierra.tool.message.Importance;

public class FindingDetailsView extends AbstractSierraView<FindingDetailsMediator> {

  public static final String ID = "com.surelogic.sierra.client.eclipse.views.FindingDetailsView";

  private static final String STAMP_TOOLTIP_MESSAGE = "Mark this finding as being examined by me.";

  @Override
  protected FindingDetailsMediator createMorePartControls(final Composite findingPage) {
    GridLayout layout = new GridLayout();
    findingPage.setLayout(layout);
    GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
    findingPage.setLayoutData(layoutData);

    /*
     * Top of the page showing the mutable finding summary and its importance
     * icon
     */
    final Composite top = new Composite(findingPage, SWT.NONE);
    layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    top.setLayout(layout);
    layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
    top.setLayoutData(layoutData);

    /*
     * Summary panel (importance icon and summary text).
     */
    final Composite summaryPane = new Composite(top, SWT.NONE);
    summaryPane.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
    layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.numColumns = 2;
    layout.verticalSpacing = 0;
    summaryPane.setLayout(layout);

    final ToolBar importanceBar = new ToolBar(summaryPane, SWT.HORIZONTAL | SWT.FLAT);
    importanceBar.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER, false, false));
    final ToolItem summaryIcon = new ToolItem(importanceBar, SWT.DROP_DOWN);
    final Text summaryText = new Text(summaryPane, SWT.MULTI | SWT.WRAP);
    layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
    summaryText.setLayoutData(layoutData);

    /*
     * Tab folder
     */
    final TabFolder folder = new TabFolder(findingPage, SWT.NONE);
    layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
    folder.setLayoutData(layoutData);

    /*
     * Synopsis tab
     */
    final TabItem synopsisTab = new TabItem(folder, SWT.NONE);
    synopsisTab.setText("Synopsis");

    final Composite synopsisPane = new Composite(folder, SWT.NONE);
    layout = new GridLayout();
    synopsisPane.setLayout(layout);

    final Composite quickSynopsis = new Composite(synopsisPane, SWT.NONE);
    layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    quickSynopsis.setLayoutData(layoutData);
    layout = new GridLayout();
    layout.verticalSpacing = 0;
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    layout.numColumns = 2;
    quickSynopsis.setLayout(layout);

    final Button synopsisAudit = new Button(quickSynopsis, SWT.FLAT | SWT.PUSH);
    synopsisAudit.setImage(SLImages.getImage(CommonImages.IMG_SIERRA_STAMP_SMALL));
    synopsisAudit.setToolTipText(STAMP_TOOLTIP_MESSAGE);

    final Link findingSynopsis = new Link(quickSynopsis, SWT.NONE);
    layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    findingSynopsis.setLayoutData(layoutData);

    SashForm synopsisSash = new SashForm(synopsisPane, SWT.VERTICAL | SWT.SMOOTH);
    synopsisSash.setLayout(new FillLayout());
    layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
    synopsisSash.setLayoutData(layoutData);

    /*
     * Show where the finding is located.
     */
    final Group where = new Group(synopsisSash, SWT.NONE);
    where.setText("Location");
    where.setLayout(new FillLayout());

    final Tree locationTree = new Tree(where, SWT.NONE);

    /*
     * Show a detailed description of the finding.
     */
    final Group description = new Group(synopsisSash, SWT.NONE);
    description.setText("Description");
    description.setLayout(new FillLayout());

    Browser detailsText = null;
    try {
      detailsText = new Browser(description, SWT.NONE);
    } catch (SWTError e) {
      final int errNo = 26;
      final String msg = I18N.err(errNo);
      final IStatus reason = SLEclipseStatusUtility.createErrorStatus(errNo, msg, e);
      ErrorDialogUtility.open(null, "Browser Failure", reason);
    }
    synopsisSash.setWeights(new int[] { 50, 50 });
    synopsisTab.setControl(synopsisPane);

    /*
     * Audit tab
     */
    final TabItem auditTab = new TabItem(folder, SWT.NONE);

    final Composite auditPane = new Composite(folder, SWT.NONE);
    layout = new GridLayout();
    layout.numColumns = 2;
    auditPane.setLayout(layout);

    /*
     * Importance selection and quick stamp.
     */
    final Composite lhs = new Composite(auditPane, SWT.NONE);
    layoutData = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, true);
    lhs.setLayoutData(layoutData);
    RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
    rowLayout.wrap = false;
    rowLayout.fill = true;
    lhs.setLayout(rowLayout);

    final Button quickAudit = new Button(lhs, SWT.PUSH | SWT.FLAT);
    quickAudit.setToolTipText(STAMP_TOOLTIP_MESSAGE);
    auditPane.addListener(SWT.Resize, new Listener() {

      @Override
      public void handleEvent(final Event event) {
        final Point size = auditPane.getSize();
        final Image image;
        if (size.y < 250) {
          image = SLImages.getImage(CommonImages.IMG_SIERRA_STAMP_SMALL);
        } else {
          image = SLImages.getImage(CommonImages.IMG_SIERRA_STAMP);
        }
        quickAudit.setImage(image);
        auditPane.layout();
      }
    });

    final Group importanceGroup = new Group(lhs, SWT.NONE);
    layout = new GridLayout(1, false);
    importanceGroup.setLayout(layout);
    layoutData = new GridData(SWT.TOP, SWT.LEFT, false, true);
    importanceGroup.setText("Importance");

    final Button criticalButton = new Button(importanceGroup, SWT.RADIO);
    criticalButton.setText("Critical");
    criticalButton.setImage(SLImages.getImage(CommonImages.IMG_ASTERISK_ORANGE_100));
    criticalButton.setToolTipText("An urgent issue\u2014need to handle immediately");
    criticalButton.setData(Importance.CRITICAL);

    final Button highButton = new Button(importanceGroup, SWT.RADIO);
    highButton.setText("High");
    highButton.setImage(SLImages.getImage(CommonImages.IMG_ASTERISK_ORANGE_75));
    highButton.setToolTipText("A serious issue\u2014need to handle soon");
    highButton.setData(Importance.HIGH);

    final Button mediumButton = new Button(importanceGroup, SWT.RADIO);
    mediumButton.setText("Medium");
    mediumButton.setImage(SLImages.getImage(CommonImages.IMG_ASTERISK_ORANGE_50));
    mediumButton.setToolTipText("An issue\u2014handle as we get time");
    mediumButton.setData(Importance.MEDIUM);

    final Button lowButton = new Button(importanceGroup, SWT.RADIO);
    lowButton.setText("Low");
    lowButton.setImage(SLImages.getImage(CommonImages.IMG_ASTERISK_ORANGE_25));
    lowButton.setToolTipText("A minor issue\u2014handle later if at all");
    lowButton.setData(Importance.LOW);

    final Button irrelevantButton = new Button(importanceGroup, SWT.RADIO);
    irrelevantButton.setText("Irrelevant");
    irrelevantButton.setImage(SLImages.getImage(CommonImages.IMG_ASTERISK_ORANGE_0));
    irrelevantButton.setToolTipText("Not an issue in our code");
    irrelevantButton.setData(Importance.IRRELEVANT);

    /*
     * Showing the audit trail (on the right-hand-side).
     */
    SashForm rhs = new SashForm(auditPane, SWT.VERTICAL | SWT.SMOOTH);
    layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
    rhs.setLayoutData(layoutData);
    rhs.setLayout(new FillLayout());

    /*
     * Top pane used to enter new audit entries.
     */
    final Composite commentComposite = new Composite(rhs, SWT.NONE);
    layout = new GridLayout();
    layout.numColumns = 2;
    commentComposite.setLayout(layout);

    final Text commentText = new Text(commentComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
    layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
    commentText.setLayoutData(layoutData);

    final Button commentButton = new Button(commentComposite, SWT.PUSH);
    commentButton.setText("Add");
    layoutData = new GridData(SWT.FILL, SWT.FILL, false, true);
    commentButton.setLayoutData(layoutData);

    /*
     * Bottom pane used to show existing audits.
     */
    final AuditTrail scrollingLabelComposite = new AuditTrail(rhs);
    rhs.setWeights(new int[] { 20, 80 });

    auditTab.setControl(auditPane);

    /*
     * Artifact tab
     */
    final TabItem artifactTab = new TabItem(folder, SWT.NONE);

    final Composite artifactsPane = new Composite(folder, SWT.NONE);
    layout = new GridLayout();
    artifactsPane.setLayout(layout);

    final Label artifactDescription = new Label(artifactsPane, SWT.NONE);
    StringBuilder b = new StringBuilder();
    b.append("An artifact is a report from an analysis tool ");
    b.append("run during a scan.");
    artifactDescription.setText(b.toString());

    final Table artifacts = new Table(artifactsPane, SWT.FULL_SELECTION);
    layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
    artifacts.setLayoutData(layoutData);
    artifacts.setLinesVisible(true);
    artifacts.setHeaderVisible(true);
    TableColumn col = new TableColumn(artifacts, SWT.NONE);
    col.setText("Tool");
    col.addListener(SWT.Selection, TableUtility.SORT_COLUMN_ALPHABETICALLY);
    col.setMoveable(true);
    col = new TableColumn(artifacts, SWT.NONE);
    col.setText("Summary");
    col.addListener(SWT.Selection, TableUtility.SORT_COLUMN_ALPHABETICALLY);
    col.setMoveable(true);
    col = new TableColumn(artifacts, SWT.NONE);
    col.setText("Package");
    col.addListener(SWT.Selection, TableUtility.SORT_COLUMN_ALPHABETICALLY);
    col.setMoveable(true);
    col = new TableColumn(artifacts, SWT.NONE);
    col.setText("Class");
    col.addListener(SWT.Selection, TableUtility.SORT_COLUMN_ALPHABETICALLY);
    col.setMoveable(true);
    col = new TableColumn(artifacts, SWT.NONE);
    col.setText("Line");
    col.addListener(SWT.Selection, TableUtility.SORT_COLUMN_NUMERICALLY);
    col.setMoveable(true);

    artifactTab.setControl(artifactsPane);

    return new FindingDetailsMediator(this, findingPage, summaryIcon, summaryText, folder, synopsisTab, synopsisSash,
        synopsisAudit, findingSynopsis, locationTree, detailsText, auditTab, quickAudit, criticalButton, highButton, mediumButton,
        lowButton, irrelevantButton, commentText, commentButton, scrollingLabelComposite, artifactTab, artifacts);
  }

  /**
   * Shows the details about the selected finding in this view.
   * 
   * @param findingID
   *          the finding identifier.
   * @param moveFocus
   *          {@code true} if this view should be given the focus, {@code false}
   *          otherwise.
   */
  public static void findingSelected(final long findingID, final boolean moveFocus) {
    final FindingDetailsView view;
    if (moveFocus) {
      view = (FindingDetailsView) EclipseUIUtility.showView(ID);
    } else {
      view = (FindingDetailsView) EclipseUIUtility.showView(ID, null, IWorkbenchPage.VIEW_CREATE);
    }
    if (view != null) {
      view.f_mediator.asyncQueryAndShow(findingID);
    }
  }
}
