package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.eclipse.FontUtility;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.ScrollingLabelComposite;

public class FindingsDetailsView extends ViewPart {

	@Override
	public void createPartControl(Composite parent) {

		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayoutData(layoutData);
		composite.setLayout(layout);

		createSummary(composite);
		createMainTab(composite);

	}

	private void createSummary(Composite parent) {
		// TOP LEFT BLOCK
		final Composite overviewComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		overviewComposite.setLayoutData(layoutData);
		overviewComposite.setLayout(layout);

		// Summary text : can be changed

		final Label summaryLabel = new Label(overviewComposite, SWT.NONE);
		layoutData = new GridData(SWT.TOP, SWT.LEFT, false, false);
		summaryLabel.setLayoutData(layoutData);
		summaryLabel.setImage(SLImages.getImage(SLImages.IMG_SIERRA_LOGO));

		final Label summaryText = new Label(overviewComposite, SWT.NONE);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.widthHint = 300;
		summaryText.setLayoutData(layoutData);
		summaryText
				.setText("Method invokes inefficient new String(String) constructor");

		final Button summaryChangeButton = new Button(overviewComposite,
				SWT.PUSH);
		layoutData = new GridData(SWT.TOP, SWT.LEFT, false, false);
		summaryChangeButton.setLayoutData(layoutData);
		summaryChangeButton.setText("Change");
		summaryChangeButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// Open dialog
				// Accept text
				// Update DB
				// Change text
				summaryText
						.setText("Don't use String(String) constructor, it's inefficient");
			}

		});

	}

	private void createMainTab(Composite parent) {
		final TabFolder mainTab = new TabFolder(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);

		mainTab.setLayout(layout);
		mainTab.setLayoutData(layoutData);

		final TabItem detailsTab = new TabItem(mainTab, SWT.NONE);
		detailsTab.setText("Details");

		final Composite detailsComposite = new Composite(mainTab, SWT.NONE);
		layout = new GridLayout(2, false);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		detailsComposite.setLayoutData(layoutData);
		detailsComposite.setLayout(layout);

		createOverviewBlock(detailsComposite);
		detailsTab.setControl(detailsComposite);

		final TabItem auditTab = new TabItem(mainTab, SWT.NONE);
		auditTab.setText("Audit");

		final Composite auditComposite = new Composite(mainTab, SWT.NONE);
		layout = new GridLayout(1, false);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		auditComposite.setLayoutData(layoutData);
		auditComposite.setLayout(layout);

		createAuditBlock(auditComposite);
		auditTab.setControl(auditComposite);

		final TabItem artifactsTab = new TabItem(mainTab, SWT.NONE);
		artifactsTab.setText("Artifacts (2)");

		final Composite artifactsComposite = new Composite(mainTab, SWT.NONE);
		layout = new GridLayout(1, false);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		artifactsComposite.setLayoutData(layoutData);
		artifactsComposite.setLayout(layout);

		createArtifactsBlock(artifactsComposite);
		artifactsTab.setControl(artifactsComposite);

	}

	private void createArtifactsBlock(Composite parent) {
		// RIGHT BLOCK

		// final Group staticInfoGroup = new Group(parent, SWT.NONE);
		// GridLayout layout = new GridLayout(1, false);
		// GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		// staticInfoGroup.setText("Artifacts");
		//
		// staticInfoGroup.setLayout(layout);
		// staticInfoGroup.setLayoutData(layoutData);

		final Tree artifactsTree = new Tree(parent, SWT.V_SCROLL | SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		artifactsTree.setLayoutData(layoutData);
		artifactsTree.setLinesVisible(true);

		final TreeItem fbTreeItem = new TreeItem(artifactsTree, SWT.NONE);
		fbTreeItem.setText("FindBugs\u2122 (1)");

		final TreeItem fbArtifact = new TreeItem(fbTreeItem, SWT.NONE);
		fbArtifact
				.setText("sample.fb.Testy.main(String[]) uses reflection to check "
						+ "for the presence of an "
						+ "annotation that has default retention");

		final TreeItem pmdTreeItem = new TreeItem(artifactsTree, SWT.NONE);
		pmdTreeItem.setText("PMD\u2122 (1)");

		final TreeItem pmdArtifact = new TreeItem(pmdTreeItem, SWT.NONE);
		pmdArtifact.setText("All methods are static. Consider using Singleton"
				+ " instead. Alternatively, you could add a private "
				+ "constructor or make the class abstract to silence "
				+ "this warning.");

	}

	private void createAuditBlock(Composite parent) {
		// BOTTOM LEFT BLOCK

		final Group auditGroup = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		auditGroup.setLayoutData(layoutData);
		auditGroup.setLayout(layout);
		auditGroup.setText("Audit");

		final Composite radioButtonsComposite = new Composite(auditGroup,
				SWT.NONE);
		layout = new GridLayout(1, false);
		layoutData = new GridData(SWT.TOP, SWT.LEFT, false, false);
		radioButtonsComposite.setLayout(layout);
		radioButtonsComposite.setLayoutData(layoutData);

		final Button quickAudit = new Button(radioButtonsComposite, SWT.RADIO);
		layoutData = new GridData(SWT.TOP, SWT.LEFT, false, false);
		quickAudit.setLayoutData(layoutData);
		quickAudit.setText("Quick Audit");
		quickAudit.setToolTipText("Select to audit a finding.");
		quickAudit.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Quick Audited");
				quickAudit.setEnabled(false);
			}

		});

		final Group importanceGroup = new Group(radioButtonsComposite, SWT.NONE);
		layout = new GridLayout(1, false);
		importanceGroup.setLayout(layout);
		layoutData = new GridData(SWT.TOP, SWT.LEFT, false, true);
		importanceGroup.setText("Importance");

		final Button highPriorityButton = new Button(importanceGroup, SWT.RADIO);
		highPriorityButton.setText("High");

		final Button mediumPriorityButton = new Button(importanceGroup,
				SWT.RADIO);
		mediumPriorityButton.setText("Medium");

		final Button lowPriorityButton = new Button(importanceGroup, SWT.RADIO);
		lowPriorityButton.setText("Low");

		final Button irrelevantPriorityButton = new Button(importanceGroup,
				SWT.RADIO);
		irrelevantPriorityButton.setText("Irrelevant");

		// Comment text

		final Composite completeCommentsComposite = new Composite(auditGroup,
				SWT.NONE);
		layout = new GridLayout(1, false);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		completeCommentsComposite.setLayout(layout);
		completeCommentsComposite.setLayoutData(layoutData);

		final Composite commentComposite = new Composite(
				completeCommentsComposite, SWT.NONE);
		layout = new GridLayout(3, false);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		commentComposite.setLayoutData(layoutData);
		commentComposite.setLayout(layout);

		final Label commentLabel = new Label(commentComposite, SWT.NONE);
		commentLabel.setText("Comment :");

		final Text commentText = new Text(commentComposite, SWT.BORDER);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = 300;
		commentText.setLayoutData(layoutData);
		commentText.setText("Some comment");

		// Add Comment button

		final Button commentButton = new Button(commentComposite, SWT.PUSH);
		commentButton.setText("Add");
		commentButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Comment added");
			}

		});

		// Scrolled Composite label

		final ScrollingLabelComposite scrollingLabelComposite = new ScrollingLabelComposite(
				completeCommentsComposite, SWT.NONE);

		for (int i = 0; i < 10; i++) {
			scrollingLabelComposite.addLabel(i + 1
					+ ".Jane Doe (Comment, Oct 2, 2007 11:32 AM) : "
					+ "Fixed on John Doe's suggestion "
					+ "Audited the finding "
					+ "Audited the finding Audited the finding "
					+ "Audited the finding Audited the finding "
					+ "Audited the finding Audited the finding");
		}

	}

	private void createOverviewBlock(Composite parent) {
		// TOP LEFT BLOCK
		final Composite overviewComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		overviewComposite.setLayoutData(layoutData);
		overviewComposite.setLayout(layout);

		final Label locationDetailsLabel = new Label(overviewComposite,
				SWT.NONE);
		locationDetailsLabel.setFont(FontUtility.getDefaultBoldFont());
		locationDetailsLabel.setText("Location");

		// Summary information

		final Composite locationInfoComposite = new Composite(
				overviewComposite, SWT.NONE);
		layout = new GridLayout(2, false);
		layoutData = new GridData(SWT.TOP, SWT.LEFT, true, false);
		locationInfoComposite.setLayoutData(layoutData);
		locationInfoComposite.setLayout(layout);

		final Label packageLabel = new Label(locationInfoComposite, SWT.NONE);
		packageLabel.setImage(SLImages
				.getJDTImage(ISharedImages.IMG_OBJS_PACKAGE));

		final Label packageNameText = new Label(locationInfoComposite, SWT.NONE);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.widthHint = 300;
		packageNameText.setLayoutData(layoutData);
		packageNameText
				.setText("com.surelogic.sierra.client.eclipse.preferences");

		final Label classLabel = new Label(locationInfoComposite, SWT.NONE);
		classLabel.setImage(SLImages.getJDTImage(ISharedImages.IMG_OBJS_CLASS));

		final Label classNameText = new Label(locationInfoComposite, SWT.NONE);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.widthHint = 300;
		classNameText.setLayoutData(layoutData);
		classNameText.setText("AdHocQueryResultsViewMediator " + "(Line 2)");

		final Label moreInfoLabel = new Label(overviewComposite, SWT.NONE);
		moreInfoLabel.setFont(FontUtility.getDefaultBoldFont());
		moreInfoLabel.setText("More Information");

		final Label detailsText = new Label(overviewComposite, SWT.WRAP);
		detailsText.setText("Using the java.lang.String(String)constructor "
				+ "wastes memory because the object so constructed will be "
				+ "functionally indistinguishable from the String passed as "
				+ "a parameter. \n\nJust use the argument String directly.");
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = 200;
		layoutData.horizontalIndent = 10;
		detailsText.setLayoutData(layoutData);
	}

	@Override
	public void setFocus() {
		// Nothing for now
	}

}
