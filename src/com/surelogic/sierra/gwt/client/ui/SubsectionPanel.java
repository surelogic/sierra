package com.surelogic.sierra.gwt.client.ui;

public abstract class SubsectionPanel extends SectionPanel {
	private static final String PRIMARY_STYLE = "sl-Subsection";

	public SubsectionPanel() {
		super();
		getTitlePanel().addStyleName(PRIMARY_STYLE + "-titlepanel");
		getContentPanel().addStyleName(PRIMARY_STYLE + "-contentpanel");
	}

}
