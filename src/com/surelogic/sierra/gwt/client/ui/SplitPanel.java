package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SplitPanel extends Composite {
	private final Grid rootGrid = new Grid(1, 2);
	private final VerticalPanel leftPanel = new VerticalPanel();
	private final VerticalPanel rightPanel = new VerticalPanel();

	public SplitPanel() {
		super();
		initWidget(rootGrid);
		rootGrid.setWidth("100%");
		rootGrid.getColumnFormatter().setWidth(0, "50%");
		rootGrid.getColumnFormatter().setWidth(1, "50%");

		leftPanel.setWidth("100%");
		rootGrid.setWidget(0, 0, leftPanel);
		rootGrid.getCellFormatter().setVerticalAlignment(0, 0,
				HasVerticalAlignment.ALIGN_TOP);
		rightPanel.setWidth("100%");
		rootGrid.setWidget(0, 1, rightPanel);
		rootGrid.getCellFormatter().setVerticalAlignment(0, 1,
				HasVerticalAlignment.ALIGN_TOP);
	}

	public void addLeft(Widget w) {
		leftPanel.add(w);
	}

	public void addRight(Widget w) {
		rightPanel.add(w);
	}
}
