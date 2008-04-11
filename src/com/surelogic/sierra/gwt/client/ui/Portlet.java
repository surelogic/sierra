package com.surelogic.sierra.gwt.client.ui;

public class Portlet extends SectionPanel {

	public Portlet(String portletName, String dataName) {
		super(portletName, dataName);
	}

	public String getPortletTitle() {
		return getSectionTitle().getText();
	}

	public void setPortletTitle(String title) {
		getSectionTitle().setText(title);
	}

	public String getDataName() {
		return getSectionInfo().getText();
	}

	public void setDataName(String dataName) {
		getSectionInfo().setText(dataName);
	}
}
