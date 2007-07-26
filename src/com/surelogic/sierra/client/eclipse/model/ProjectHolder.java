package com.surelogic.sierra.client.eclipse.model;

import java.util.Collection;

import com.surelogic.sierra.entity.Run;

public class ProjectHolder {

	private String projectName;
	private Collection<Run> runs;

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Collection<Run> getRuns() {
		return runs;
	}

	public void setRuns(Collection<Run> runs) {
		this.runs = runs;
	}

}
