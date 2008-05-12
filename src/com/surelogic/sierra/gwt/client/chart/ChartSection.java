package com.surelogic.sierra.gwt.client.chart;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ui.Chart;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;

public abstract class ChartSection extends SectionPanel {
	private Chart chart;

	protected void onInitialize(VerticalPanel contentPanel) {
		// nothing to do
	}

	protected void onActivate(Context context) {
		updateChart(context);
	}

	protected void onDeactivate() {
		if (chart != null) {
			getContentPanel().remove(chart);
		}
	}

	protected void onUpdate(Context context) {
		updateChart(context);
	}

	protected abstract Chart buildChart(Context context);

	private void updateChart(Context context) {
		final VerticalPanel content = getContentPanel();
		if (chart != null) {
			content.remove(chart);
		}
		chart = buildChart(context);
		content.add(chart);
		content.setCellHorizontalAlignment(chart, VerticalPanel.ALIGN_CENTER);
	}

}
