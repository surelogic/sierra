package com.surelogic.sierra.client.eclipse.model;

import java.util.Collection;

import com.surelogic.sierra.entity.Artifact;

public class ClassHolder {

	private String name;

	private String packageName;

	private Collection<Artifact> findings;

	public ClassHolder(String className) {
		this.name = className;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public Collection<Artifact> getFindings() {
		return findings;
	}

	public void setFindings(Collection<Artifact> findings) {
		this.findings = findings;
	}

}
