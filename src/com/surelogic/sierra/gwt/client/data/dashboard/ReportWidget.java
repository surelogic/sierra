package com.surelogic.sierra.gwt.client.data.dashboard;

import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.Report.OutputType;

public class ReportWidget implements DashboardWidget {
	private static final long serialVersionUID = -9108006251638085723L;
	private ReportSettings settings;
	private OutputType outputType;

	public ReportWidget() {
		super();
	}

	public ReportWidget(final ReportSettings settings,
			final OutputType outputType) {
		super();
		this.settings = settings;
		this.outputType = outputType;
	}

	public ReportSettings getSettings() {
		return settings;
	}

	public void setSettings(final ReportSettings settings) {
		this.settings = settings;
	}

	public OutputType getOutputType() {
		return outputType;
	}

	public void setOutputType(final OutputType outputType) {
		this.outputType = outputType;
	}
}
