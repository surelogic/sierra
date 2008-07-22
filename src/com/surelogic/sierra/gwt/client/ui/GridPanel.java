package com.surelogic.sierra.gwt.client.ui;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.ui.grid.SelectableGrid;

public class GridPanel extends Composite {
	private static final String PRIMARY_STYLE = "sl-GridPanel";

	private final VerticalPanel rootPanel = new VerticalPanel();
	private final HorizontalPanel gridActionPanel = new HorizontalPanel();
	private final List<ButtonBase> actions = new ArrayList<ButtonBase>();
	private final SelectableGrid grid;

	private boolean enabled;

	public GridPanel(boolean rowSelection) {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName(PRIMARY_STYLE);

		gridActionPanel.addStyleName(PRIMARY_STYLE + "-actions");
		rootPanel.add(gridActionPanel);

		grid = new SelectableGrid(rowSelection);
		grid.addStyleName(PRIMARY_STYLE + "-table");

		rootPanel.add(grid);
		enabled = true;
	}

	public void addGridOption(String text, ClickListener actionListener,
			boolean checked) {
		final CheckBox box = new CheckBox(text);
		box.addClickListener(actionListener);
		box.setEnabled(enabled);
		box.setChecked(checked);
		gridActionPanel.add(box);
		actions.add(box);
	}

	public void addGridAction(String text, ClickListener actionListener) {
		final Button action = new Button(text);
		action.addClickListener(actionListener);
		action.setEnabled(enabled);
		gridActionPanel.add(action);
		actions.add(action);
	}

	public SelectableGrid getGrid() {
		return grid;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		for (ButtonBase b : actions) {
			b.setEnabled(enabled);
		}
	}

}
