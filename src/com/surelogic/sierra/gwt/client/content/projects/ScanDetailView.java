package com.surelogic.sierra.gwt.client.content.projects;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.surelogic.sierra.gwt.client.content.scanfilters.ScanFiltersContent;
import com.surelogic.sierra.gwt.client.data.Project;
import com.surelogic.sierra.gwt.client.data.ScanDetail;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.ui.StyleHelper;
import com.surelogic.sierra.gwt.client.ui.StyleHelper.Style;
import com.surelogic.sierra.gwt.client.ui.link.ContentLink;

public final class ScanDetailView extends Composite {
	private final Grid root = new Grid(4, 2);
	private final Label lastScan = new Label("", false);
	private final Label numFindings = new Label("", false);
	private final Label kloc = new Label("", false);
	private final HorizontalPanel scanFilterPanel = new HorizontalPanel();
	private final ContentLink scanFilter = new ContentLink();

	public ScanDetailView(final ClickListener changeScanListener) {
		super();
		initWidget(root);
		root.setWidth("100%");
		int row = 0;
		root.setText(row, 0, "Scan Filter:");
		scanFilterPanel.add(scanFilter);
		final SimplePanel sfSpace = new SimplePanel();
		sfSpace.setWidth("10px");
		scanFilterPanel.add(sfSpace);
		final Label changeScanFilter = StyleHelper.add(new Label("(Change)",
				false), Style.CLICKABLE);
		changeScanFilter.addClickListener(changeScanListener);
		scanFilterPanel.add(changeScanFilter);
		root.setWidget(row, 1, scanFilterPanel);
		scanFilterPanel.setVisible(false);
		row++;
		root.setText(row, 0, "Last Scanned:");
		root.setWidget(row, 1, lastScan);
		row++;
		root.setText(row, 0, "# of Findings:");
		root.setWidget(row, 1, numFindings);
		row++;
		final Label klocLabel = new Label("KLoC:", false);
		klocLabel.setTitle("1000's of lines of code.");
		root.setWidget(row, 0, klocLabel);
		root.setWidget(row, 1, kloc);
	}

	public void setScan(final Project project, final ScanDetail scan) {
		final ScanFilter filter = project.getScanFilter();
		if (filter != null) {
			scanFilter.setTarget(filter.getName(), ScanFiltersContent
					.getInstance(), filter.getUuid());
			scanFilterPanel.setVisible(true);
		} else {
			// filter should never be null
			scanFilterPanel.setVisible(false);
		}
		if (scan != null) {
			lastScan.setText(scan.getDate());
			numFindings.setText(scan.getFindings());
			kloc.setText(scan.getLinesOfCode());

		} else {
			lastScan.setText("None published.");
			numFindings.setText("N/A");
			kloc.setText("N/A");
		}
	}
}
