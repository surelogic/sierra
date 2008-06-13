package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.surelogic.sierra.tool.message.AssuranceType;
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

	private final long findingId;

	private final String project;
	private final String packageName;
	private final String className;
	private final String compilation;
	private final String tool;
	private final String findingType;
	private final String category;
	private final String summary;
	private final AssuranceType assuranceType;

	private final boolean examined;
	private final Date lastChanged;
	private final Importance importance;
	private final FindingStatus status;
	private final int lineOfCode;
	private final int numberOfArtifacts;
	private final int numberOfComments;

	FindingOverview(ResultSet set) throws SQLException {
    this(set, 1);
	}

	FindingOverview(ResultSet set, int idx) throws SQLException {
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
		this.compilation = set.getString(idx++);
		this.findingType = set.getString(idx++);
		this.category = set.getString(idx++);
		this.tool = set.getString(idx++);
		this.summary = set.getString(idx++);
		
		String aType = set.getString(idx++);
		this.assuranceType = AssuranceType.fromFlag(aType);
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

	public String getCompilation() {
		return compilation;
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

	public AssuranceType getAssuranceType() {
		return assuranceType;
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

	/**
	 * Returns the number of artifacts matching this finding in the latest scan.
	 * 
	 * @return
	 */
	public int getNumberOfArtifacts() {
		return numberOfArtifacts;
	}

	public int getNumberOfAudits() {
		return numberOfComments;
	}

	public String getCategory() {
		return category;
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
		private static final String SELECT_COLUMNS =
			"SELECT FINDING_ID,AUDITED,LAST_CHANGED,IMPORTANCE,STATUS,LINE_OF_CODE,"+
			"ARTIFACT_COUNT,AUDIT_COUNT,PROJECT,PACKAGE,CLASS,CU,FINDING_TYPE,CATEGORY,"+
			"TOOL,SUMMARY,ASSURANCE_TYPE";
		
		/**
		 * Get the latest findings for the given class, and any inner classes.
		 * Only findings with status New or Unchanged are returned, fixed
		 * findings are not shown.
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
					.prepareStatement(SELECT_COLUMNS
							+ " FROM FINDINGS_OVERVIEW WHERE PROJECT = ? AND PACKAGE = ? AND CU = ?");
			int idx = 1;
			selectFindingsByClass.setString(idx++, projectName);
			selectFindingsByClass.setString(idx++, packageName);
			selectFindingsByClass.setString(idx++, className);
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

		/**
		 * Get the latest findings for the given class, and any inner classes.
		 * Only findings with status New or Unchanged are returned, fixed
		 * findings are not shown.
		 * 
		 * TODO do we want to also show fixed findings?
		 * 
		 * @param projectName
		 * @param compilation
		 * @param packageName
		 * @return
		 */
		public List<FindingOverview> showRelevantFindingsForClass(
				Connection conn, String projectName, String packageName,
				String compilation) throws SQLException {
			List<FindingOverview> findings = new ArrayList<FindingOverview>();
			PreparedStatement selectFindingsByClass = conn
					.prepareStatement(SELECT_COLUMNS
							+ " FROM FINDINGS_OVERVIEW WHERE PROJECT = ? AND PACKAGE = ? AND CU = ? AND IMPORTANCE != 'Irrelevant'");
			int idx = 1;
			selectFindingsByClass.setString(idx++, projectName);
			selectFindingsByClass.setString(idx++, packageName);
			selectFindingsByClass.setString(idx++, compilation);
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
		
    /**
     * Get the latest findings that are above the given importance 
     * for the given class, and any inner classes.
     * Only findings with status New or Unchanged are returned, fixed
     * findings are not shown.
     * 
     * TODO do we want to also show fixed findings?
     */
    public List<FindingOverview> showImportantEnoughFindingsForClass(
        Connection conn, String projectName, String packageName,
        String compilation, Importance importance) throws SQLException {
      if (importance.ordinal() == 0) {
        return showFindingsForClass(conn, projectName, packageName, compilation);
      }
      if (importance.ordinal() == 1 && Importance.IRRELEVANT.ordinal() == 0) {
        return showRelevantFindingsForClass(conn, projectName, packageName, compilation);
      }
      boolean first = true;
      StringBuilder importanceClause = new StringBuilder("(");
      for(Importance i : Importance.values()) {
        if (i.ordinal() >= importance.ordinal()) {
          if (first) {
            first = false;
          } else {
            importanceClause.append(" OR ");
          }
          importanceClause.append("IMPORTANCE = '").append(i.toStringSentenceCase()).append('\'');
        }
      }      
      importanceClause.append(')');
      
      List<FindingOverview> findings = new ArrayList<FindingOverview>();
      
      PreparedStatement selectFindingsByClass = conn
          .prepareStatement(SELECT_COLUMNS
              + " FROM FINDINGS_OVERVIEW WHERE PROJECT = ? AND PACKAGE = ? AND CU = ? AND "+importanceClause);
      int idx = 1;
      selectFindingsByClass.setString(idx++, projectName);
      selectFindingsByClass.setString(idx++, packageName);
      selectFindingsByClass.setString(idx++, compilation);
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
