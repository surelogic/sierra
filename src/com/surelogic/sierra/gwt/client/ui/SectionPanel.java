package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SectionPanel extends Composite {
	private static final String PRIMARY_STYLE = "sl-Section";
	private final DockPanel rootPanel = new DockPanel();
	private final DockPanel titlePanel = new DockPanel();
	private final Label sectionTitle = new Label();
	private final Label sectionInfo = new Label();
	private final HorizontalPanel actionPanel = new HorizontalPanel();
	private final VerticalPanel contentPanel = new VerticalPanel();

	public SectionPanel(String title, String info) {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName(PRIMARY_STYLE);
		rootPanel.setWidth("100%");
		rootPanel.setHeight("100%");

		rootPanel.add(titlePanel, DockPanel.NORTH);
		titlePanel.addStyleName(PRIMARY_STYLE + "-titlepanel");
		titlePanel.add(sectionTitle, DockPanel.WEST);

		if (!title.endsWith(":")) {
			title += ":";
		}
		sectionTitle.setText(title);
		sectionTitle.addStyleName(PRIMARY_STYLE + "-title");

		titlePanel.add(sectionInfo, DockPanel.CENTER);
		titlePanel.setCellHorizontalAlignment(sectionInfo,
				HorizontalPanel.ALIGN_CENTER);
		sectionInfo.addStyleName(PRIMARY_STYLE + "-info");
		sectionInfo.setText(info);

		titlePanel.add(actionPanel, DockPanel.EAST);
		actionPanel.addStyleName(PRIMARY_STYLE + "-actionpanel");
		titlePanel.setCellHorizontalAlignment(actionPanel,
				HorizontalPanel.ALIGN_RIGHT);
		titlePanel.setCellVerticalAlignment(actionPanel,
				HorizontalPanel.ALIGN_MIDDLE);

		titlePanel.setCellWidth(sectionTitle, "25%");
		titlePanel.setCellWidth(sectionInfo, "50%");
		titlePanel.setCellWidth(actionPanel, "25%");

		rootPanel.add(contentPanel, DockPanel.CENTER);
		contentPanel.addStyleName(PRIMARY_STYLE + "-contentpanel");
	}

	public DockPanel getTitlePanel() {
		return titlePanel;
	}

	public Label getSectionTitle() {
		return sectionTitle;
	}

	public Label getSectionInfo() {
		return sectionInfo;
	}

	public HorizontalPanel getActionPanel() {
		return actionPanel;
	}

	public void addAction(Widget w) {
		actionPanel.add(w);
		w.addStyleName("sl-Section-actionpanel-item");
	}

	public void addAction(String text, ClickListener clickListener) {
		final Label textLabel = new Label(text);
		textLabel.addStyleName("clickable");
		textLabel.addClickListener(clickListener);
		addAction(textLabel);
	}

	public void removeAction(Widget w) {
		actionPanel.remove(w);
	}

	public void removeActions() {
		while (actionPanel.getWidgetCount() > 0) {
			actionPanel.remove(0);
		}
	}

	public VerticalPanel getContentPanel() {
		return contentPanel;
	}

}
