package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public class DashboardWidget implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7901425991551477631L;
	Point size;
	Point position;
	String settingsUuid;
	WidgetType type;

	public DashboardWidget() {

	}

	public Point getSize() {
		return size;
	}

	public void setSize(final Point size) {
		this.size = size;
	}

	public Point getPosition() {
		return position;
	}

	public void setPosition(final Point position) {
		this.position = position;
	}

	public String getSettingsUuid() {
		return settingsUuid;
	}

	public void setSettingsUuid(final String settingsUuid) {
		this.settingsUuid = settingsUuid;
	}

	public WidgetType getType() {
		return type;
	}

	public void setType(final WidgetType type) {
		this.type = type;
	}

}
