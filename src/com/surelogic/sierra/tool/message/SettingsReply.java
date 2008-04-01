package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement
public class SettingsReply {
	private List<FilterSet> filterSets;
	private List<Settings> settings;
	private List<ProjectSettings> projectSettings;

	public List<Settings> getSettings() {
		if (settings == null) {
			settings = new ArrayList<Settings>();
		}
		return settings;
	}

	public List<ProjectSettings> getProjectSettings() {
		if (projectSettings == null) {
			projectSettings = new ArrayList<ProjectSettings>();
		}
		return projectSettings;
	}

	public List<FilterSet> getFilterSets() {
		if (filterSets == null) {
			filterSets = new ArrayList<FilterSet>();
		}
		return filterSets;
	}
}
