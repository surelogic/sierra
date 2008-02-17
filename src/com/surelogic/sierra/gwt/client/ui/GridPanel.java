package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class GridPanel extends Composite {
	private static final String PRIMARY_STYLE = "sl-GridPanel";

	private final VerticalPanel rootPanel = new VerticalPanel();
	private final HorizontalPanel gridActionPanel = new HorizontalPanel();
	private final Grid grid = new Grid(1, 1);
	private boolean rowCheckBoxes;
	private CheckBox selectAll;

	public GridPanel(boolean rowCheckBoxes) {
		super();
		this.rowCheckBoxes = rowCheckBoxes;

		initWidget(rootPanel);
		rootPanel.addStyleName(PRIMARY_STYLE);

		gridActionPanel.addStyleName(PRIMARY_STYLE + "-actions");
		rootPanel.add(gridActionPanel);

		grid.addStyleName(PRIMARY_STYLE + "-table");
		grid.getRowFormatter().addStyleName(0, PRIMARY_STYLE + "-table-header");
		rootPanel.add(grid);

		if (rowCheckBoxes) {
			selectAll = new CheckBox();
			grid.resizeColumns(2);
			grid.setWidget(0, 0, selectAll);
		}
	}

	public void addGridAction(String text, ClickListener actionListener) {
		final Button action = new Button(text);
		action.addClickListener(actionListener);
		gridActionPanel.add(action);
	}

	public void setHeaderColumn(int column, String text, String width) {
		if (rowCheckBoxes) {
			column++;
		}
		if (column >= grid.getColumnCount()) {
			grid.resizeColumns(column + 1);
		}
		grid.setText(0, column, text);
		grid.getColumnFormatter().setWidth(column, width);
	}
}
