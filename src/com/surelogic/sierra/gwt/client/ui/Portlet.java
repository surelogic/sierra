package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class Portlet extends Composite {
	private static final String PRIMARY_STYLE = "sl-Portlet";
	private final DockPanel rootPanel = new DockPanel();
	private final DockPanel titlePanel = new DockPanel();
	private final Label portletTitle = new Label();
	private final Label dataTitle = new Label();
	private final HorizontalPanel actionPanel = new HorizontalPanel();
	private final VerticalPanel contentPanel = new VerticalPanel();

	public Portlet(String portletName, String dataName) {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName(PRIMARY_STYLE);
		rootPanel.setWidth("100%");
		rootPanel.setHeight("100%");

		rootPanel.add(titlePanel, DockPanel.NORTH);
		titlePanel.addStyleName(PRIMARY_STYLE + "-titlepanel");
		titlePanel.add(portletTitle, DockPanel.WEST);

		if (!portletName.endsWith(":")) {
			portletName += ":";
		}
		portletTitle.setText(portletName);
		portletTitle.addStyleName(PRIMARY_STYLE + "-title");

		titlePanel.add(dataTitle, DockPanel.CENTER);
		titlePanel.setCellHorizontalAlignment(dataTitle,
				HorizontalPanel.ALIGN_CENTER);
		dataTitle.addStyleName(PRIMARY_STYLE + "-datatitle");
		dataTitle.setText(dataName);

		titlePanel.add(actionPanel, DockPanel.EAST);
		actionPanel.addStyleName(PRIMARY_STYLE + "-actionpanel");
		titlePanel.setCellHorizontalAlignment(actionPanel,
				HorizontalPanel.ALIGN_RIGHT);

		titlePanel.setCellWidth(portletTitle, "25%");
		titlePanel.setCellWidth(dataTitle, "50%");
		titlePanel.setCellWidth(actionPanel, "25%");

		rootPanel.add(contentPanel, DockPanel.CENTER);
		contentPanel.addStyleName(PRIMARY_STYLE + "-contentpanel");
	}

	public VerticalPanel getContentPanel() {
		return contentPanel;
	}

	public void addAction(Widget w) {
		actionPanel.add(w);
	}

	public String getDataTitle() {
		return dataTitle.getText();
	}

	public void setDataTitle(String dataTitle) {
		this.dataTitle.setText(dataTitle);
	}
}
