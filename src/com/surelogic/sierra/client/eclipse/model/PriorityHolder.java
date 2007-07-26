package com.surelogic.sierra.client.eclipse.model;

import java.util.Collection;

import com.surelogic.sierra.entity.Artifact;
import com.surelogic.sierra.tool.message.Priority;

public class PriorityHolder {
	private Priority priority;

	private Collection<Artifact> findings;

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public Collection<Artifact> getFindings() {
		return findings;
	}

	public void setFindings(Collection<Artifact> findings) {
		this.findings = findings;
	}
}
