package com.surelogic.sierra.client.eclipse.views;

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
import org.eclipse.ui.part.ViewPart;

public class FindingsDetailsView extends ViewPart {

	private static final String TEXT_SUMMARY = "Summary :";
	private static final String TEXT_CLASS = "Class :";
	private static final String TEXT_PACKAGE = "Package :";
	private static final String TEXT_LINENO = "Line Number :";

	@Override
	public void createPartControl(Composite parent) {

		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayoutData(layoutData);
		composite.setLayout(layout);

		final Composite infoComposite = new Composite(composite, SWT.NONE);
		layout = new GridLayout(1, false);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		infoComposite.setLayoutData(layoutData);
		infoComposite.setLayout(layout);

		// TOP LEFT BLOCK
		final Group overviewGroup = new Group(infoComposite, SWT.NONE);
		layout = new GridLayout(1, false);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		overviewGroup.setLayoutData(layoutData);
		overviewGroup.setLayout(layout);
		overviewGroup.setText("Overview");

		// Summary information

		final Composite locationInfoComposite = new Composite(overviewGroup,
				SWT.NONE);
		layout = new GridLayout(2, false);
		layoutData = new GridData(SWT.TOP, SWT.LEFT, true, false);
		locationInfoComposite.setLayout(layout);
		locationInfoComposite.setLayoutData(layoutData);

		final Composite sourceLocationInfoComposite = new Composite(
				locationInfoComposite, SWT.NONE);
		layout = new GridLayout(2, false);
		layoutData = new GridData(SWT.TOP, SWT.LEFT, true, false);
		sourceLocationInfoComposite.setLayoutData(layoutData);
		sourceLocationInfoComposite.setLayout(layout);

		final Label packageLabel = new Label(sourceLocationInfoComposite,
				SWT.NONE);
		packageLabel.setText(TEXT_PACKAGE);

		final Label packageNameText = new Label(sourceLocationInfoComposite,
				SWT.NONE);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.widthHint = 400;
		packageNameText.setLayoutData(layoutData);
		packageNameText.setText("com.surelogic.adhoc.views");

		final Label classLabel = new Label(sourceLocationInfoComposite,
				SWT.NONE);
		classLabel.setText(TEXT_CLASS);

		final Label classNameText = new Label(sourceLocationInfoComposite,
				SWT.NONE);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.widthHint = 400;
		classNameText.setLayoutData(layoutData);
		classNameText.setText("AdHocQueryResultsViewMediator");

		final Label lineLabel = new Label(sourceLocationInfoComposite, SWT.NONE);
		lineLabel.setText(TEXT_LINENO);

		final Label lineNoText = new Label(sourceLocationInfoComposite,
				SWT.NONE);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.widthHint = 20;
		lineNoText.setLayoutData(layoutData);
		lineNoText.setText("2");

		final Button quickAudit = new Button(locationInfoComposite, SWT.RADIO);
		layoutData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
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

		final Composite summaryComposite = new Composite(overviewGroup,
				SWT.NONE);
		layout = new GridLayout(1, false);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		summaryComposite.setLayoutData(layoutData);
		summaryComposite.setLayout(layout);

		// Summary text : can be changed
		final Label summaryLabel = new Label(summaryComposite, SWT.NONE);
		summaryLabel.setText(TEXT_SUMMARY);

		final Text summaryText = new Text(summaryComposite, SWT.MULTI
				| SWT.BORDER);
		layoutData = new GridData(GridData.FILL, GridData.FILL, true, true, 1,
				3);
		layoutData.widthHint = 300;
		summaryText.setLayoutData(layoutData);
		summaryText.setText("Sample summary");

		// BOTTOM LEFT BLOCK

		final Group auditGroup = new Group(infoComposite, SWT.NONE);
		layout = new GridLayout(3, false);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		auditGroup.setLayoutData(layoutData);
		auditGroup.setLayout(layout);
		auditGroup.setText("Audit");

		final Group importanceGroup = new Group(auditGroup, SWT.NONE);
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

		final Composite commentComposite = new Composite(auditGroup, SWT.NONE);
		layout = new GridLayout(1, false);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		commentComposite.setLayoutData(layoutData);
		commentComposite.setLayout(layout);

		final Label commentLabel = new Label(commentComposite, SWT.NONE);
		commentLabel.setText("Comment :");

		final Text commentText = new Text(commentComposite, SWT.MULTI
				| SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		layoutData = new GridData(GridData.FILL, GridData.FILL, true, true, 1,
				3);
		layoutData.widthHint = 300;
		commentText.setLayoutData(layoutData);
		commentText.setText("Some comment");

		// Add Comment button

		final Button commentButton = new Button(auditGroup, SWT.PUSH);
		commentButton.setText("Add");
		commentButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Comment added");
			}

		});

		// RIGHT BLOCK

		final Composite staticInfoComposite = new Composite(composite, SWT.NONE);
		layout = new GridLayout(1, false);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);

		staticInfoComposite.setLayout(layout);
		staticInfoComposite.setLayoutData(layoutData);

		final TabFolder tabFolder = new TabFolder(staticInfoComposite, SWT.NONE);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		tabFolder.setLayoutData(layoutData);

		final TabItem logTab = new TabItem(tabFolder, SWT.NONE);
		logTab.setText("Logs");

		final TabItem artifactsTab = new TabItem(tabFolder, SWT.NONE);
		artifactsTab.setText("Artifacts");

	}

	@Override
	public void setFocus() {
		// Nothing for now
	}

}
