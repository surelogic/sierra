package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SyncRequest {

	private List<SyncProjectRequest> projects;

	public List<SyncProjectRequest> getProjects() {
		if (projects == null) {
			projects = new ArrayList<SyncProjectRequest>();
		}
		return projects;
	}

	public void setProjects(final List<SyncProjectRequest> projects) {
		this.projects = projects;
	}

}
