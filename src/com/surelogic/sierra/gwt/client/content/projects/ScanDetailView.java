package com.surelogic.sierra.gwt.client.content.projects;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.ScanDetail;

public final class ScanDetailView extends VerticalPanel {

	public ScanDetailView() {
		super();
	}

	public ScanDetailView(final ScanDetail scan) {
		super();
		setScan(scan);
	}

	public void setScan(final ScanDetail scan) {
		clear();
		if (scan != null) {
			add(new HTML("Last scanned: " + scan.getDate()));
			add(new HTML("# of Findings: " + scan.getFindings()));
			add(new HTML("kLoC: " + scan.getLinesOfCode()));
		}
	}
}
