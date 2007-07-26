package com.surelogic.sierra.client.eclipse.model;

import java.util.Collection;

import com.surelogic.sierra.entity.Artifact;

public class CategoryHolder {

	private String category;

	private Collection<Artifact> findings;

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Collection<Artifact> getFindings() {
		return findings;
	}

	public void setFindings(Collection<Artifact> findings) {
		this.findings = findings;
	}

}
