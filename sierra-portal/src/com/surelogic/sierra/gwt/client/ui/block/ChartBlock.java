package com.surelogic.sierra.gwt.client.ui.block;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.ui.chart.Chart;

public class ChartBlock extends ReportBlock<Chart> {

	public ChartBlock() {
		super(new Chart());
	}

	public ChartBlock(final ReportSettings report) {
		super(new Chart(), report);
	}

	@Override
	public HorizontalAlignmentConstant getHorizontalAlignment() {
		return HasHorizontalAlignment.ALIGN_CENTER;
	}

	@Override
	protected final void reportChanged(final ReportSettings report) {
		getRoot().setReportSettings(report);
	}

}
