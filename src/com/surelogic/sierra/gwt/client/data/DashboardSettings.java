package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DashboardSettings implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -54036448093951530L;
	private List<DashboardWidget> widgets;

	public DashboardSettings() {
		// Do nothing
	}

	public List<DashboardWidget> getWidgets() {
		if (widgets == null) {
			widgets = new ArrayList<DashboardWidget>();
		}
		return widgets;
	}

}
