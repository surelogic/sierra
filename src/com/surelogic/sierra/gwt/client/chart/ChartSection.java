package com.surelogic.sierra.gwt.client.chart;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ui.Chart;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;

public abstract class ChartSection extends SectionPanel {
	private Chart chart;

	@Override
	protected void onInitialize(final VerticalPanel contentPanel) {
		// nothing to do
	}

	@Override
	protected void onUpdate(final Context context) {
		final VerticalPanel content = getContentPanel();
		if (chart != null) {
			content.remove(chart);
		}
		chart = buildChart(context);
		content.add(chart);
		content.setCellHorizontalAlignment(chart, VerticalPanel.ALIGN_CENTER);
	}

	@Override
	protected void onDeactivate() {
		if (chart != null) {
			getContentPanel().remove(chart);
		}
	}

	protected abstract Chart buildChart(Context context);

}
