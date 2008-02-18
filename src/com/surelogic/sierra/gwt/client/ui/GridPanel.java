package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

// TODO need public methods to access grid cells, once these are done switch internal methods to use them as well to avoid
// all of the extra math
public class GridPanel extends Composite {
	private static final String PRIMARY_STYLE = "sl-GridPanel";

	private final VerticalPanel rootPanel = new VerticalPanel();
	private final HorizontalPanel gridActionPanel = new HorizontalPanel();

	private final SLGrid grid;

	public GridPanel(boolean rowSelection) {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName(PRIMARY_STYLE);

		gridActionPanel.addStyleName(PRIMARY_STYLE + "-actions");
		rootPanel.add(gridActionPanel);

		grid = new SLGrid(rowSelection);
		grid.addStyleName(PRIMARY_STYLE + "-table");

		rootPanel.add(grid);
	}

	public void addGridAction(String text, ClickListener actionListener) {
		final Button action = new Button(text);
		action.addClickListener(actionListener);
		gridActionPanel.add(action);
	}

	public SLGrid getGrid() {
		return grid;
	}

}
