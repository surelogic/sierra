package com.surelogic.sierra.gwt.client.ui;

public class SubsectionPanel extends SectionPanel {
	private static final String PRIMARY_STYLE = "sl-Subsection";

	public SubsectionPanel(String title, String info) {
		super(title, info);
		getTitlePanel().addStyleName(PRIMARY_STYLE + "-titlepanel");
		getContentPanel().addStyleName(PRIMARY_STYLE + "-contentpanel");
	}

}
