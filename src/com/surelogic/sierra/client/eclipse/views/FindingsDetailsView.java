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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.eclipse.SLImages;

public class FindingsDetailsView extends ViewPart {

	@Override
	public void createPartControl(Composite parent) {

		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayoutData(layoutData);
		composite.setLayout(layout);

		final Composite infoComposite = new Composite(composite, SWT.NONE);
		layout = new GridLayout(1, false);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		infoComposite.setLayoutData(layoutData);
		infoComposite.setLayout(layout);

		createOverviewBlock(infoComposite);
		createAuditBlock(infoComposite);
		createArtifactsBlock(composite);

	}

	private void createArtifactsBlock(Composite parent) {
		// RIGHT BLOCK

		final Group staticInfoGroup = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		staticInfoGroup.setText("Artifacts");

		staticInfoGroup.setLayout(layout);
		staticInfoGroup.setLayoutData(layoutData);

		final Tree artifactsTree = new Tree(staticInfoGroup, SWT.V_SCROLL
				| SWT.BORDER);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		artifactsTree.setLayoutData(layoutData);
		artifactsTree.setLinesVisible(true);

		final TreeItem fbTreeItem = new TreeItem(artifactsTree, SWT.NONE);
		fbTreeItem.setText("FindBugs\u2122");

		final TreeItem fbArtifact = new TreeItem(fbTreeItem, SWT.NONE);
		fbArtifact
				.setText("sample.fb.Testy.main(String[]) uses reflection to check "
						+ "for the presence of an "
						+ "annotation that has default retention");

		final TreeItem pmdTreeItem = new TreeItem(artifactsTree, SWT.NONE);
		pmdTreeItem.setText("PMD\u2122");

		final TreeItem pmdArtifact = new TreeItem(pmdTreeItem, SWT.NONE);
		pmdArtifact.setText("All methods are static.  Consider using Singleton"
				+ " instead.  Alternatively, you could add a private "
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

		final List logList = new List(completeCommentsComposite, SWT.BORDER
				| SWT.V_SCROLL);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		logList.setLayoutData(layoutData);

		// Sample
		logList
				.add("Jane Doe (Comment, Oct 2, 2007 11:32 AM) : Fixed on John Doe's suggestion");

		logList
				.add("John Doe (Comment, Oct 1, 2007 10:32 AM) : Do this to fix this bug");
		logList
				.add("John Doe (Audit, Oct 1, 2007 10:31 AM) : Audited the finding");

	}

	private void createOverviewBlock(Composite parent) {
		// TOP LEFT BLOCK
		final Composite overviewComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		overviewComposite.setLayoutData(layoutData);
		overviewComposite.setLayout(layout);

		// Summary text : can be changed
		final Label summaryLabel = new Label(overviewComposite, SWT.NONE);
		layoutData = new GridData(SWT.TOP, SWT.LEFT, false, false);
		summaryLabel.setLayoutData(layoutData);
		summaryLabel.setImage(SLImages.getImage(SLImages.IMG_SIERRA_LOGO));

		final Text summaryText = new Text(overviewComposite, SWT.BORDER);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.widthHint = 300;
		summaryText.setLayoutData(layoutData);
		summaryText.setText("Sample summary");

		// Summary information

		// final Composite locationInfoComposite = new Composite(
		// overviewComposite, SWT.NONE);
		// layout = new GridLayout(2, false);
		// layoutData = new GridData(SWT.TOP, SWT.LEFT, true, false);
		// locationInfoComposite.setLayoutData(layoutData);
		// locationInfoComposite.setLayout(layout);

		final Label packageLabel = new Label(overviewComposite, SWT.NONE);
		packageLabel.setImage(SLImages
				.getJDTImage(ISharedImages.IMG_OBJS_PACKAGE));

		final Label packageNameText = new Label(overviewComposite, SWT.NONE);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.widthHint = 300;
		packageNameText.setLayoutData(layoutData);
		packageNameText.setText("com.surelogic.adhoc.views");

		final Label classLabel = new Label(overviewComposite, SWT.NONE);
		classLabel.setImage(SLImages.getJDTImage(ISharedImages.IMG_OBJS_CLASS));

		final Label classNameText = new Label(overviewComposite, SWT.NONE);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.widthHint = 300;
		classNameText.setLayoutData(layoutData);
		classNameText.setText("AdHocQueryResultsViewMediator " + "(Line 2)");
	}

	@Override
	public void setFocus() {
		// Nothing for now
	}

}
