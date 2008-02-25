package com.surelogic.sierra.gwt.client.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class GridPanel extends Composite {
	private static final String PRIMARY_STYLE = "sl-GridPanel";

	private final VerticalPanel rootPanel = new VerticalPanel();
	private final HorizontalPanel gridActionPanel = new HorizontalPanel();
	private final List actions = new ArrayList();
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
		for (Iterator i = actions.iterator(); i.hasNext();) {
			Button b = (Button) i.next();
			b.setEnabled(enabled);
		}
	}

}
