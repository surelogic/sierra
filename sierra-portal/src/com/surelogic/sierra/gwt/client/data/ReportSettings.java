package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ReportSettings implements Serializable {
	private static final long serialVersionUID = 2547178312564240154L;

	private String uuid;
	private String title;
	private String description;
	private Report report;
	private List<ReportSetting> settings;

	public ReportSettings() {
		super();
	}

	public ReportSettings(final Report report) {
		super();
		this.report = report;
		this.title = report.getTitle();
		this.description = report.getShortDescription();
	}

	public ReportSettings(final Report report, final String title,
			final String description) {
		super();
		this.report = report;
		this.title = title;
		this.description = description;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * Returns an unmodifiable list of the report setting parameters
	 * 
	 * @return
	 */
	public List<ReportSetting> getSettingParams() {
		if (settings == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(settings);
	}

	public String getReportUuid() {
		return report.getUuid();
	}

	public Report getReport() {
		return report;
	}

	public void setReport(final Report report) {
		this.report = report;
	}

	public String getWidth() {
		return getSettingValue("width", 0);
	}

	public void setWidth(final String width) {
		setSettingValue("width", width);
	}

	public ReportSetting getSetting(final String name) {
		return getSetting(name, false);
	}

	public ReportSetting getSetting(final String name,
			final boolean createIfNotExists) {
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

	public String getSettingValue(final String name, final int valueIndex) {
		final List<String> values = getSettingValue(name);
		if (values != null && valueIndex >= 0 && valueIndex < values.size()) {
			return values.get(valueIndex);
		}

		return null;
	}

	public List<String> getSettingValue(final String name) {
		final ReportSetting setting = getSetting(name);
		return setting != null ? setting.getValues() : null;
	}

	public void setSettingValue(final String name, final String value) {
		setSettingValue(name, Arrays.asList(value));
	}

	public void setSettingValue(final String name, final List<String> values) {
		getSetting(name, true).setValues(values);
	}

	public void setSettingValue(final String name, final Set<String> values) {
		getSetting(name, true).setValues(new ArrayList<String>(values));
	}

	public ReportSettings copy() {
		final ReportSettings copy = new ReportSettings();
		copy.title = title;
		copy.description = description;
		copy.report = report;
		if (settings != null) {
			for (final ReportSetting setting : settings) {
				final List<String> value = setting.getValues();
				copy.setSettingValue(setting.getName(),
						value.size() == 0 ? null : value);
			}
		}
		return copy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((report == null) ? 0 : report.hashCode());
		result = prime
				* result
				+ ((settings == null || settings.isEmpty()) ? 0 : settings
						.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ReportSettings other = (ReportSettings) obj;
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (report == null) {
			if (other.report != null) {
				return false;
			}
		} else if (!report.equals(other.report)) {
			return false;
		}
		if (settings == null || settings.isEmpty()) {
			if (other.settings != null && !other.settings.isEmpty()) {
				return false;
			}
		} else if (!settings.equals(other.settings)) {
			return false;
		}
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		if (uuid == null) {
			if (other.uuid != null) {
				return false;
			}
		} else if (!uuid.equals(other.uuid)) {
			return false;
		}
		return true;
	}

}
