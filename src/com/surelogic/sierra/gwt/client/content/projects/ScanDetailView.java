package com.surelogic.sierra.gwt.client.content.projects;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.surelogic.sierra.gwt.client.data.ScanDetail;

public final class ScanDetailView extends Composite {
	private final Grid root = new Grid(3, 2);
	private final Label lastScan = new Label("", false);
	private final Label numFindings = new Label("", false);
	private final Label kloc = new Label("", false);

	public ScanDetailView() {
		super();
		initWidget(root);
		root.setWidth("100%");
		root.getColumnFormatter().setWidth(0, "50%");
		root.getColumnFormatter().setWidth(1, "50%");
		root.setText(0, 0, "Last Scanned:");
		root.setWidget(0, 1, lastScan);
		root.setText(1, 0, "# of Findings:");
		root.setWidget(1, 1, numFindings);
		root.setText(2, 0, "kLoC:");
		root.setWidget(2, 1, kloc);
	}

	public ScanDetailView(final ScanDetail scan) {
		super();
		setScan(scan);
	}

	public void setScan(final ScanDetail scan) {
		if (scan != null) {
			lastScan.setText(scan.getDate());
			numFindings.setText(scan.getFindings());
			kloc.setText(scan.getLinesOfCode());
		} else {
			lastScan.setText("");
			numFindings.setText("");
			kloc.setText("");
		}
	}
}
