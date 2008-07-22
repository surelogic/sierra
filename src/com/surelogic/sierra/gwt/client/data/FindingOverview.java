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
	private static final long serialVersionUID = -6080219515417976528L;

	private long findingId;

	private String findingType;
	private String category;
	private String summary;
	private ImportanceView importance;

	private String project;
	private String packageName;
	private String className;
	/**
	 * Ordered by time.
	 * 
	 */
	private List<AuditOverview> audits;
	/**
	 * Ordered by time.
	 * 
	 */
	private List<ArtifactOverview> artifacts;

	public FindingOverview() {
		// Do nothing
	}

	public long getFindingId() {
		return findingId;
	}

	public void setFindingId(final long findingId) {
		this.findingId = findingId;
	}

	public String getFindingType() {
		return findingType;
	}

	public void setFindingType(final String findingType) {
		this.findingType = findingType;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(final String category) {
		this.category = category;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(final String summary) {
		this.summary = summary;
	}

	public ImportanceView getImportance() {
		return importance;
	}

	public void setImportance(final ImportanceView importance) {
		this.importance = importance;
	}

	public String getProject() {
		return project;
	}

	public void setProject(final String project) {
		this.project = project;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(final String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(final String className) {
		this.className = className;
	}

	public List<AuditOverview> getAudits() {
		return audits;
	}

	public void setAudits(final List<AuditOverview> audits) {
		this.audits = audits;
	}

	public List<ArtifactOverview> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(final List<ArtifactOverview> artifacts) {
		this.artifacts = artifacts;
	}

}
