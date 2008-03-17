package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.List;

/**
 * Overview of a finding residing on the server.
 * 
 * @author nathan
 * 
 */
public class FindingOverview implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6080219515417976528L;

	private String category;
	private String description;
	private String importance;

	private String project;
	private String packageName;
	private String className;
	/**
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.AuditOverview>
	 */
	private List audits;
	/**
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.ArtifactOverview>
	 */
	private List artifacts;

	public FindingOverview() {
		// Do nothing
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImportance() {
		return importance;
	}

	public void setImportance(String importance) {
		this.importance = importance;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public List getAudits() {
		return audits;
	}

	public void setAudits(List audits) {
		this.audits = audits;
	}

	public List getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List artifacts) {
		this.artifacts = artifacts;
	}

}
