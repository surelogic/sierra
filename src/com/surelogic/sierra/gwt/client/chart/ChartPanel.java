package com.surelogic.sierra.gwt.client.chart;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.ui.panel.BlockPanel;

public abstract class ChartPanel extends BlockPanel {
	private Chart chart;

	@Override
	protected void onInitialize(final VerticalPanel content) {
		if (chart != null) {
			content.remove(chart);
		}
		chart = buildChart();
		content.add(chart);
		content.setCellHorizontalAlignment(chart, VerticalPanel.ALIGN_CENTER);
	}

	protected abstract Chart buildChart();

}
