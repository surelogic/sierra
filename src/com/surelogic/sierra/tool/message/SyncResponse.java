package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SyncResponse {
	private List<SyncProjectResponse> projects;

	public List<SyncProjectResponse> getProjects() {
		if (projects == null) {
			projects = new ArrayList<SyncProjectResponse>();
		}
		return projects;
	}

	public void setProjects(final List<SyncProjectResponse> projects) {
		this.projects = projects;
	}

}
