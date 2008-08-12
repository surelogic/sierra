package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ReportSettings implements Serializable {
	private static final long serialVersionUID = 2547178312564240154L;

	private String title;
	private String description;
	private String reportUuid;
	private List<ReportSetting> settings;

	public ReportSettings() {
		super();
	}

	public ReportSettings(String reportUuid) {
		super();
		this.reportUuid = reportUuid;
	}

	public ReportSettings(Report report) {
		super();
		this.reportUuid = report.getUuid();
		this.title = report.getTitle();
		this.description = report.getDescription();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getReportUuid() {
		return reportUuid;
	}

	public void setReportUuid(String reportUuid) {
		this.reportUuid = reportUuid;
	}

	public ReportSetting getSetting(String name) {
		return getSetting(name, false);
	}

	public ReportSetting getSetting(String name, boolean createIfNotExists) {
		if (settings == null) {
			settings = new ArrayList<ReportSetting>();
		}
		for (final ReportSetting setting : settings) {
			if (setting.getName().equalsIgnoreCase(name)) {
				return setting;
			}
		}
		if (createIfNotExists) {
			final ReportSetting setting = new ReportSetting(name, null);
			settings.add(setting);
			return setting;
		}
		return null;
	}

	public String getSettingValue(String name, int valueIndex) {
		final List<String> values = getSettingValue(name);
		if (values != null && valueIndex >= 0 && valueIndex < values.size()) {
			return values.get(valueIndex);
		}

		return null;
	}

	public List<String> getSettingValue(String name) {
		final ReportSetting setting = getSetting(name);
		return setting != null ? setting.getValues() : null;
	}

	public void setSettingValue(String name, String value) {
		setSettingValue(name, Arrays.asList(value));
	}

	public void setSettingValue(String name, List<String> values) {
		getSetting(name, true).setValues(values);
	}

	public void setSettingValue(String name, Set<String> values) {
		getSetting(name, true).setValues(new ArrayList<String>(values));
	}

	public ReportSettings copy() {
		final ReportSettings copy = new ReportSettings();
		copy.title = title;
		copy.description = description;
		copy.reportUuid = reportUuid;
		if (settings != null) {
			for (final ReportSetting setting : settings) {
				final List<String> value = setting.getValues();
				copy.setSettingValue(setting.getName(),
						value.size() == 0 ? null : value);
			}
		}
		return copy;
	}
}
