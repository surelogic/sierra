package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class GridPanel extends Composite {
	private static final String PRIMARY_STYLE = "sl-GridPanel";

	private final VerticalPanel rootPanel = new VerticalPanel();
	private final HorizontalPanel gridActionPanel = new HorizontalPanel();

	private final SelectableGrid grid;

	public GridPanel(boolean rowSelection) {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName(PRIMARY_STYLE);

		gridActionPanel.addStyleName(PRIMARY_STYLE + "-actions");
		rootPanel.add(gridActionPanel);

		grid = new SelectableGrid(rowSelection);
		grid.addStyleName(PRIMARY_STYLE + "-table");

		rootPanel.add(grid);
	}

	public void addGridAction(String text, ClickListener actionListener) {
		final Button action = new Button(text);
		action.addClickListener(actionListener);
		gridActionPanel.add(action);
	}

	public SelectableGrid getGrid() {
		return grid;
	}

}
