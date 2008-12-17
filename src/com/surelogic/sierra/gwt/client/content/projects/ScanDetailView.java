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
		root.setText(0, 0, "Last Scanned:");
		root.setWidget(0, 1, lastScan);
		root.setText(1, 0, "# of Findings:");
		root.setWidget(1, 1, numFindings);
		final Label klocLabel = new Label("KLoC:", false);
		klocLabel.setTitle("1000's of lines of code.");
		root.setWidget(2, 0, klocLabel);
		root.setWidget(2, 1, kloc);
		root.setText(3, 0, "Scan Filter:");
		scanFilterPanel.add(scanFilter);
		final SimplePanel sfSpace = new SimplePanel();
		sfSpace.setWidth("10px");
		scanFilterPanel.add(sfSpace);
		final Label changeScanFilter = StyleHelper.add(new Label("(Change)",
				false), Style.CLICKABLE);
		changeScanFilter.addClickListener(changeScanListener);
		scanFilterPanel.add(changeScanFilter);
		root.setWidget(3, 1, scanFilterPanel);
		scanFilterPanel.setVisible(false);
	}

	public void setScan(final Project project, final ScanDetail scan) {
		if (scan != null) {
			lastScan.setText(scan.getDate());
			numFindings.setText(scan.getFindings());
			kloc.setText(scan.getLinesOfCode());
			final ScanFilter filter = project.getScanFilter();
			if (filter != null) {
				scanFilter.setTarget(filter.getName(), ScanFiltersContent
						.getInstance(), filter.getUuid());
				scanFilterPanel.setVisible(true);
			} else {
				// filter should never be null
				scanFilterPanel.setVisible(false);
			}
		} else {
			lastScan.setText("");
			numFindings.setText("");
			kloc.setText("");
			scanFilterPanel.setVisible(false);
		}
	}
}
