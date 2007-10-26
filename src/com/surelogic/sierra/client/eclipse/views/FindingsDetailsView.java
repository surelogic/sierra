package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.adhoc.views.TableUtility;
import com.surelogic.common.eclipse.AuditTrail;
import com.surelogic.common.eclipse.PageBook;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.tool.message.Importance;

public class FindingsDetailsView extends ViewPart {

	private static final String STAMP_TOOLTIP_MESSAGE = "Mark this finding as being examined by me.";
	private static final String SELECT_FINDINGS = "No finding is selected...select a finding to view its details";
	private FindingDetailsMediator f_mediator = null;

	@Override
	public void createPartControl(Composite parent) {

		final PageBook pages = new PageBook(parent, SWT.NONE);

		final Label noFindingPage = new Label(pages, SWT.WRAP);
		noFindingPage.setText(SELECT_FINDINGS);

		final Composite findingPage = new Composite(pages, SWT.NONE);
		GridLayout layout = new GridLayout();
		findingPage.setLayout(layout);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		findingPage.setLayoutData(layoutData);

		/*
		 * Top of the page showing the mutable finding summary and its
		 * importance icon
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
		summaryPane.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
				false));
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		layout.verticalSpacing = 0;
		summaryPane.setLayout(layout);

		final ToolBar importanceBar = new ToolBar(summaryPane, SWT.HORIZONTAL
				| SWT.FLAT);
		importanceBar.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER,
				false, false));
		final ToolItem summaryIcon = new ToolItem(importanceBar, SWT.PUSH);
		final Text summaryText = new Text(summaryPane, SWT.SINGLE);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
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

		final Button synopsisAudit = new Button(quickSynopsis, SWT.FLAT
				| SWT.PUSH);
		synopsisAudit.setImage(SLImages
				.getImage(SLImages.IMG_SIERRA_STAMP_SMALL));
		synopsisAudit.setToolTipText(STAMP_TOOLTIP_MESSAGE);

		final Link findingSynopsis = new Link(quickSynopsis, SWT.NONE);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		findingSynopsis.setLayoutData(layoutData);

		/*
		 * Show where the finding is located.
		 */
		final Group where = new Group(synopsisPane, SWT.NONE);
		where.setText("Location");
		layout = new GridLayout();
		layout.numColumns = 4;
		where.setLayout(layout);
		layoutData = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		where.setLayoutData(layoutData);

		final Label projectIcon = new Label(where, SWT.NONE);
		projectIcon.setImage(SLImages
				.getWorkbenchImage(IDE.SharedImages.IMG_OBJ_PROJECT));
		layoutData = new GridData(SWT.DEFAULT, SWT.CENTER, false, false);
		projectIcon.setLayoutData(layoutData);

		final Label projectName = new Label(where, SWT.NONE);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		projectName.setLayoutData(layoutData);

		Label spacer = new Label(where, SWT.NONE);
		layoutData = new GridData(SWT.NONE, SWT.NONE, false, false);
		spacer.setLayoutData(layoutData);

		final Label packageIcon = new Label(where, SWT.NONE);
		packageIcon.setImage(SLImages
				.getJDTImage(ISharedImages.IMG_OBJS_PACKAGE));
		layoutData = new GridData(SWT.DEFAULT, SWT.CENTER, false, false);
		packageIcon.setLayoutData(layoutData);

		final Label packageName = new Label(where, SWT.NONE);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		packageName.setLayoutData(layoutData);

		spacer = new Label(where, SWT.NONE);
		layoutData = new GridData(SWT.NONE, SWT.NONE, false, false);
		spacer.setLayoutData(layoutData);
		spacer = new Label(where, SWT.NONE);
		layoutData = new GridData(SWT.NONE, SWT.NONE, false, false);
		spacer.setLayoutData(layoutData);

		final Label classIcon = new Label(where, SWT.NONE);
		classIcon.setImage(SLImages.getJDTImage(ISharedImages.IMG_OBJS_CLASS));
		layoutData = new GridData(SWT.DEFAULT, SWT.CENTER, false, false);
		classIcon.setLayoutData(layoutData);

		final Link className = new Link(where, SWT.NONE);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		className.setLayoutData(layoutData);

		/*
		 * Show a detailed description of the finding.
		 */
		final Group description = new Group(synopsisPane, SWT.NONE);
		description.setText("Description");
		description.setLayout(new FillLayout());
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		description.setLayoutData(layoutData);

		Browser detailsText = null;
		try {
			detailsText = new Browser(description, SWT.NONE);
		} catch (SWTError e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Browser Failure", "Browser cannot be initialized.");
		}
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
		layoutData = new GridData(SWT.FILL, SWT.DEFAULT, false, true);
		lhs.setLayoutData(layoutData);
		RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
		rowLayout.fill = true;
		lhs.setLayout(rowLayout);

		final Button quickAudit = new Button(lhs, SWT.PUSH | SWT.FLAT);
		quickAudit.setImage(SLImages.getImage(SLImages.IMG_SIERRA_STAMP));
		quickAudit.setToolTipText(STAMP_TOOLTIP_MESSAGE);

		final Group importanceGroup = new Group(lhs, SWT.NONE);
		layout = new GridLayout(1, false);
		importanceGroup.setLayout(layout);
		layoutData = new GridData(SWT.TOP, SWT.LEFT, false, true);
		importanceGroup.setText("Importance");

		final Button criticalButton = new Button(importanceGroup, SWT.RADIO);
		criticalButton.setText("Critical");
		criticalButton.setImage(SLImages
				.getImage(SLImages.IMG_ASTERISK_ORANGE_100));
		criticalButton
				.setToolTipText("An urgent issue\u2014need to handle immediately");
		criticalButton.setData(Importance.CRITICAL);

		final Button highButton = new Button(importanceGroup, SWT.RADIO);
		highButton.setText("High");
		highButton.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_ORANGE_75));
		highButton.setToolTipText("A serious issue\u2014need to handle soon");
		highButton.setData(Importance.HIGH);

		final Button mediumButton = new Button(importanceGroup, SWT.RADIO);
		mediumButton.setText("Medium");
		mediumButton.setImage(SLImages
				.getImage(SLImages.IMG_ASTERISK_ORANGE_50));
		mediumButton.setToolTipText("An issue\u2014handle as we get time");
		mediumButton.setData(Importance.MEDIUM);

		final Button lowButton = new Button(importanceGroup, SWT.RADIO);
		lowButton.setText("Low");
		lowButton.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_ORANGE_25));
		lowButton.setToolTipText("A minor issue\u2014handle later if at all");
		lowButton.setData(Importance.LOW);

		final Button irrelevantButton = new Button(importanceGroup, SWT.RADIO);
		irrelevantButton.setText("Irrelevant");
		irrelevantButton.setImage(SLImages
				.getImage(SLImages.IMG_ASTERISK_ORANGE_0));
		irrelevantButton.setToolTipText("Not an issue in our code");
		irrelevantButton.setData(Importance.IRRELEVANT);

		/*
		 * Showing the audit trail (on the right-hand-side).
		 */
		SashForm rhs = new SashForm(auditPane, SWT.VERTICAL);
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

		final Text commentText = new Text(commentComposite, SWT.MULTI
				| SWT.BORDER | SWT.V_SCROLL);
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

		f_mediator = new FindingDetailsMediator(pages, noFindingPage,
				findingPage, summaryIcon, summaryText, folder, synopsisTab,
				synopsisAudit, findingSynopsis, projectName, packageName,
				className, detailsText, auditTab, quickAudit, criticalButton,
				highButton, mediumButton, lowButton, irrelevantButton,
				commentText, commentButton, scrollingLabelComposite,
				artifactTab, artifacts);

		f_mediator.init();

	}

	@Override
	public void setFocus() {
		f_mediator.setFocus();
	}

	@Override
	public void dispose() {
		if (f_mediator != null)
			f_mediator.dispose();
		super.dispose();
	}

	public void findingSelected(long findingID) {
		f_mediator.asyncQueryAndShow(findingID);
	}

}
