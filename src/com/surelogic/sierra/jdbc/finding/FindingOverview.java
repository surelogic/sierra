package com.surelogic.sierra.jdbc.finding;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.surelogic.sierra.tool.message.Importance;

/**
 * FindingOverview represents an overview of a finding on the client as of the
 * latest scan. Some properties, such as lastChanged, lineOfCode,
 * numberOfArtifacts, and tool may be null if the finding has actually been
 * fixed.
 * 
 * @author nathan
 * 
 */
public class FindingOverview {

	private long findingId;

	private String project;
	private String packageName;
	private String className;
	private String tool;
	private String findingType;
	private String summary;

	private boolean examined;
	private Date lastChanged;
	private Importance importance;
	private FindingStatus status;
	private int lineOfCode;
	private int numberOfArtifacts;
	private int numberOfComments;

	FindingOverview(ResultSet set) throws SQLException {
		int idx = 1;
		this.findingId = set.getLong(idx++);
		this.examined = "Yes".equals(set.getString(idx++));
		this.lastChanged = set.getTimestamp(idx++);
		this.importance = Importance.valueOf(set.getString(idx++));
		this.status = FindingStatus.valueOf(set.getString(idx++));
		this.lineOfCode = set.getInt(idx++);
		this.numberOfArtifacts = set.getInt(idx++);
		this.numberOfComments = set.getInt(idx++);
		this.project = set.getString(idx++);
		this.packageName = set.getString(idx++);
		this.className = set.getString(idx++);
		this.findingType = set.getString(idx++);
		this.tool = set.getString(idx++);
		this.summary = set.getString(idx++);
	}

	public long getFindingId() {
		return findingId;
	}

	public String getProject() {
		return project;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getClassName() {
		return className;
	}

	public String getTool() {
		return tool;
	}

	public String getFindingType() {
		return findingType;
	}

	public String getSummary() {
		return summary;
	}

	public boolean isExamined() {
		return examined;
	}

	public Date getLastChanged() {
		return lastChanged;
	}

	public Importance getImportance() {
		return importance;
	}

	public FindingStatus getStatus() {
		return status;
	}

	public int getLineOfCode() {
		return lineOfCode;
	}

	public int getNumberOfArtifacts() {
		return numberOfArtifacts;
	}

	public int getNumberOfComments() {
		return numberOfComments;
	}

}
