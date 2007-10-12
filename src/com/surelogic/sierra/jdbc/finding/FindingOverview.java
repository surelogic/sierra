package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

	private static View view = new View();

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
		this.importance = Importance
				.valueOf(set.getString(idx++).toUpperCase());
		this.status = FindingStatus.valueOf(set.getString(idx++).toUpperCase());
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
	
	public static View getView() {
		return view;
	}

	/**
	 * Provides views of the FINDINGS_OVERVIEW table.
	 * 
	 * @author nathan
	 * 
	 */
	public static class View {

		/**
		 * Get the latest findings for the given class, and any inner classes. Only findings with
		 * status New or Unchanged are returned, fixed findings are not shown.
		 * 
		 * TODO do we want to also show fixed findings?
		 * 
		 * @param projectName
		 * @param className
		 * @param packageName
		 * @return
		 */
		public List<FindingOverview> showFindingsForClass(Connection conn,
				String projectName, String packageName, String className)
				throws SQLException {
			List<FindingOverview> findings = new ArrayList<FindingOverview>();
			PreparedStatement selectFindingsByClass = conn
					.prepareStatement("SELECT FINDING_ID,EXAMINED,LAST_CHANGED,IMPORTANCE,STATUS,LINE_OF_CODE,ARTIFACT_COUNT,COMMENT_COUNT,PROJECT,PACKAGE,CLASS,FINDING_TYPE,TOOL,SUMMARY"
							+ " FROM FINDINGS_OVERVIEW WHERE PROJECT = ? AND PACKAGE = ? AND (CLASS = ? OR CLASS LIKE ?)");
			int idx = 1;
			selectFindingsByClass.setString(idx++, projectName);
			selectFindingsByClass.setString(idx++, packageName);
			selectFindingsByClass.setString(idx++, className);
			selectFindingsByClass.setString(idx++, className + "$%");
			ResultSet set = selectFindingsByClass.executeQuery();
			try {
				while (set.next()) {
					findings.add(new FindingOverview(set));
				}
			} finally {
				set.close();
			}
			return findings;
		}
	}
}
