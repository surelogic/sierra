package com.surelogic.sierra.gwt.client.ui.panel;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ColumnPanel extends Composite {
	private final HorizontalPanel rootPanel = new HorizontalPanel();

	public ColumnPanel() {
		super();
		initWidget(rootPanel);
		rootPanel.setWidth("100%");
	}

	public void addWidget(final Widget w) {
		addWidget(rootPanel.getWidgetCount(), w);
	}

	public void addWidget(final int column, final Widget w) {
		while (rootPanel.getWidgetCount() <= column) {
			final VerticalPanel colPanel = new VerticalPanel();
			rootPanel.add(colPanel);
			rootPanel.setCellVerticalAlignment(colPanel,
					HasVerticalAlignment.ALIGN_TOP);
		}
		((VerticalPanel) rootPanel.getWidget(column)).add(w);
	}

}
