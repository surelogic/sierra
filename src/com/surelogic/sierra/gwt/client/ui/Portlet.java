package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Portlet extends Composite {
	private static final String PRIMARY_STYLE = "sl-Portlet";
	private final DockPanel rootPanel = new DockPanel();
	private final HorizontalPanel titlePanel = new HorizontalPanel();
	private final Label portletTitle = new Label();
	private final Label dataTitle = new Label();
	private final Label moreDetail = new Label("more detail >>");
	private final VerticalPanel contentPanel = new VerticalPanel();

	public Portlet(String title) {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName(PRIMARY_STYLE);
		rootPanel.setWidth("100%");
		rootPanel.setHeight("100%");

		rootPanel.add(titlePanel, DockPanel.NORTH);
		titlePanel.addStyleName(PRIMARY_STYLE + "-titlepanel");

		titlePanel.add(portletTitle);
		if (!title.endsWith(":")) {
			title += ":";
		}
		portletTitle.setText(title);
		portletTitle.addStyleName(PRIMARY_STYLE + "-title");
		titlePanel.add(dataTitle);
		titlePanel.setCellHorizontalAlignment(dataTitle,
				HorizontalPanel.ALIGN_CENTER);
		dataTitle.addStyleName(PRIMARY_STYLE + "-datatitle");
		titlePanel.add(moreDetail);
		moreDetail.addStyleName(PRIMARY_STYLE + "-moredetail");

		titlePanel.setCellHorizontalAlignment(moreDetail,
				HorizontalPanel.ALIGN_RIGHT);

		rootPanel.add(contentPanel, DockPanel.CENTER);
		contentPanel.addStyleName(PRIMARY_STYLE + "-contentpanel");
	}

	public VerticalPanel getContentPanel() {
		return contentPanel;
	}

	public String getDataTitle() {
		return dataTitle.getText();
	}

	public void setDataTitle(String dataTitle) {
		this.dataTitle.setText(dataTitle);
	}
}
