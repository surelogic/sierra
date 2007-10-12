package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.eclipse.FontUtility;
import com.surelogic.common.eclipse.PageBook;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.ScrollingLabelComposite;

public class FindingsDetailsView extends ViewPart {

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
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		top.setLayout(layout);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		top.setLayoutData(layoutData);

		final Label importanceIcon = new Label(top, SWT.NONE);
		layoutData = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
		importanceIcon.setLayoutData(layoutData);
		// TODO: remove set this in the mediator
		importanceIcon.setImage(SLImages
				.getImage(SLImages.IMG_ASTERISK_ORANGE_0));

		final Text summaryText = new Text(top, SWT.SINGLE);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		summaryText.setLayoutData(layoutData);

		final TabFolder folder = new TabFolder(findingPage, SWT.NONE);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		folder.setLayoutData(layoutData);

		final TabItem synopsisTab = new TabItem(folder, SWT.NONE);
		synopsisTab.setText("Synopsis");
		layout = new GridLayout();

		final Composite synopsisPane = new Composite(folder, SWT.NONE);
		layout = new GridLayout();
		synopsisPane.setLayout(layout);

		/*
		 * Show where the finding is located.
		 */
		final Group where = new Group(synopsisPane, SWT.NONE);
		where.setText("Location");
		layout = new GridLayout();
		layout.numColumns = 3;
		where.setLayout(layout);
		layoutData = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		where.setLayoutData(layoutData);

		final Label packageIcon = new Label(where, SWT.NONE);
		packageIcon.setImage(SLImages
				.getJDTImage(ISharedImages.IMG_OBJS_PACKAGE));
		layoutData = new GridData(SWT.DEFAULT, SWT.CENTER, false, false);
		packageIcon.setLayoutData(layoutData);

		final Label packageName = new Label(where, SWT.NONE);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		packageName.setLayoutData(layoutData);

		final Label spacer = new Label(where, SWT.NONE);
		layoutData = new GridData(SWT.NONE, SWT.NONE, false, false);
		spacer.setLayoutData(layoutData);

		final Label classIcon = new Label(where, SWT.NONE);
		classIcon.setImage(SLImages.getJDTImage(ISharedImages.IMG_OBJS_CLASS));
		layoutData = new GridData(SWT.DEFAULT, SWT.CENTER, false, false);
		classIcon.setLayoutData(layoutData);

		final Link className = new Link(where, SWT.NONE);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		className.setLayoutData(layoutData);

		final Label moreInfoLabel = new Label(synopsisPane, SWT.NONE);
		moreInfoLabel.setFont(FontUtility.getDefaultBoldFont());
		moreInfoLabel.setText("More Information");

		final Label detailsText = new Label(synopsisPane, SWT.WRAP);
		// detailsText.setText("Using the java.lang.String(String)constructor "
		// + "wastes memory because the object so constructed will be "
		// + "functionally indistinguishable from the String passed as "
		// + "a parameter. \n\nJust use the argument String directly.");
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = 200;
		layoutData.horizontalIndent = 10;
		detailsText.setLayoutData(layoutData);

		synopsisTab.setControl(synopsisPane);

		final TabItem auditTab = new TabItem(folder, SWT.NONE);
		// auditTab.setText("Audit");

		final Composite auditComposite = new Composite(folder, SWT.NONE);
		layout = new GridLayout(1, false);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		auditComposite.setLayoutData(layoutData);
		auditComposite.setLayout(layout);

		// BOTTOM LEFT BLOCK

		final Group auditGroup = new Group(auditComposite, SWT.NONE);
		layout = new GridLayout(2, false);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
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
		quickAudit.setToolTipText("Select to quickly audit a finding.");

		final Group importanceGroup = new Group(radioButtonsComposite, SWT.NONE);
		layout = new GridLayout(1, false);
		importanceGroup.setLayout(layout);
		layoutData = new GridData(SWT.TOP, SWT.LEFT, false, true);
		importanceGroup.setText("Importance");

		final Button[] importanceButtons = new Button[5];

		importanceButtons[0] = new Button(importanceGroup, SWT.RADIO);
		importanceButtons[0].setText("Critical");

		importanceButtons[1] = new Button(importanceGroup, SWT.RADIO);
		importanceButtons[1].setText("High");

		importanceButtons[2] = new Button(importanceGroup, SWT.RADIO);
		importanceButtons[2].setText("Medium");

		importanceButtons[3] = new Button(importanceGroup, SWT.RADIO);
		importanceButtons[3].setText("Low");

		importanceButtons[4] = new Button(importanceGroup, SWT.RADIO);
		importanceButtons[4].setText("Irrelevant");

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
		// commentText.setText("Some comment");

		// Add Comment button

		final Button commentButton = new Button(commentComposite, SWT.PUSH);
		commentButton.setText("Add");

		// Scrolled Composite label

		final ScrollingLabelComposite scrollingLabelComposite = new ScrollingLabelComposite(
				completeCommentsComposite, SWT.NONE);
		scrollingLabelComposite.setLayout(new GridLayout(1, false));
		scrollingLabelComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true));

		// for (int i = 0; i < 10; i++) {
		// scrollingLabelComposite.addLabel(i + 1
		// + ".Jane Doe (Comment, Oct 2, 2007 11:32 AM) : "
		// + "Fixed on John Doe's suggestion "
		// + "Audited the finding "
		// + "Audited the finding Audited the finding "
		// + "Audited the finding Audited the finding "
		// + "Audited the finding Audited the finding");
		// }

		auditTab.setControl(auditComposite);

		final TabItem artifactsTab = new TabItem(folder, SWT.NONE);
		// artifactsTab.setText("Artifacts (2)");

		final Composite artifactsComposite = new Composite(folder, SWT.NONE);
		layout = new GridLayout(1, false);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		artifactsComposite.setLayoutData(layoutData);
		artifactsComposite.setLayout(layout);

		// RIGHT BLOCK

		// final Group staticInfoGroup = new Group(parent, SWT.NONE);
		// GridLayout layout = new GridLayout(1, false);
		// GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		// staticInfoGroup.setText("Artifacts");
		//
		// staticInfoGroup.setLayout(layout);
		// staticInfoGroup.setLayoutData(layoutData);

		final Tree artifactsTree = new Tree(artifactsComposite, SWT.V_SCROLL
				| SWT.BORDER);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		artifactsTree.setLayoutData(layoutData);
		artifactsTree.setLinesVisible(true);

		// final TreeItem fbTreeItem = new TreeItem(artifactsTree, SWT.NONE);
		// fbTreeItem.setText("FindBugs\u2122 (1)");
		//
		// final TreeItem fbArtifact = new TreeItem(fbTreeItem, SWT.NONE);
		// fbArtifact
		// .setText("sample.fb.Testy.main(String[]) uses reflection to check "
		// + "for the presence of an "
		// + "annotation that has default retention");
		//
		// final TreeItem pmdTreeItem = new TreeItem(artifactsTree, SWT.NONE);
		// pmdTreeItem.setText("PMD\u2122 (1)");
		//
		// final TreeItem pmdArtifact = new TreeItem(pmdTreeItem, SWT.NONE);
		// pmdArtifact.setText("All methods are static. Consider using
		// Singleton"
		// + " instead. Alternatively, you could add a private "
		// + "constructor or make the class abstract to silence "
		// + "this warning.");
		artifactsTab.setControl(artifactsComposite);

		f_mediator = new FindingDetailsMediator(pages, noFindingPage,
				findingPage, summaryText, packageName, className, detailsText,
				auditTab, quickAudit, importanceButtons, commentText,
				commentButton, scrollingLabelComposite, artifactsTab,
				artifactsTree);

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
		f_mediator.refreshDetailsPage(findingID);
	}

}
