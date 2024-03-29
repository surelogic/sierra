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

    private final long findingId;

    private final String project;
    private final String packageName;
    private final String className;
    private final String compilation;
    private final String tool;
    private final String findingType;
    private final String summary;

    private final boolean examined;
    private final Date lastChanged;
    private final Importance importance;
    private final FindingStatus status;
    private final int lineOfCode;
    private final int numberOfArtifacts;
    private final int numberOfComments;

    FindingOverview(final ResultSet set) throws SQLException {
        this(set, 1);
    }

    FindingOverview(final ResultSet set, int idx) throws SQLException {
        findingId = set.getLong(idx++);
        examined = "Yes".equals(set.getString(idx++));
        lastChanged = set.getTimestamp(idx++);
        importance = Importance.valueOf(set.getString(idx++).toUpperCase());
        status = FindingStatus.valueOf(set.getString(idx++).toUpperCase());
        lineOfCode = set.getInt(idx++);
        numberOfArtifacts = set.getInt(idx++);
        numberOfComments = set.getInt(idx++);
        project = set.getString(idx++);
        packageName = set.getString(idx++);
        className = set.getString(idx++);
        compilation = set.getString(idx++);
        findingType = set.getString(idx++);
        tool = set.getString(idx++);
        summary = set.getString(idx++);
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
        private static final String SELECT_COLUMNS = "SELECT FINDING_ID,AUDITED,LAST_CHANGED,IMPORTANCE,STATUS,LINE_OF_CODE,"
                + "ARTIFACT_COUNT,AUDIT_COUNT,PROJECT,PACKAGE,CLASS,CU,FINDING_TYPE,"
                + "TOOL,SUMMARY";

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
        public List<FindingOverview> showFindingsForClass(
                final Connection conn, final String projectName,
                final String packageName, final String className)
                throws SQLException {
            final List<FindingOverview> findings = new ArrayList<FindingOverview>();
            final PreparedStatement selectFindingsByClass = conn
                    .prepareStatement(SELECT_COLUMNS
                            + " FROM FINDINGS_OVERVIEW WHERE PROJECT = ? AND PACKAGE = ? AND CU = ?");
            int idx = 1;
            selectFindingsByClass.setString(idx++, projectName);
            selectFindingsByClass.setString(idx++, packageName);
            selectFindingsByClass.setString(idx++, className);
            final ResultSet set = selectFindingsByClass.executeQuery();
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
                final Connection conn, final String projectName,
                final String packageName, final String compilation)
                throws SQLException {
            final List<FindingOverview> findings = new ArrayList<FindingOverview>();
            final PreparedStatement selectFindingsByClass = conn
                    .prepareStatement(SELECT_COLUMNS
                            + " FROM FINDINGS_OVERVIEW WHERE PROJECT = ? AND PACKAGE = ? AND CU = ? AND IMPORTANCE != 'Irrelevant'");
            int idx = 1;
            selectFindingsByClass.setString(idx++, projectName);
            selectFindingsByClass.setString(idx++, packageName);
            selectFindingsByClass.setString(idx++, compilation);
            final ResultSet set = selectFindingsByClass.executeQuery();
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
         * Get the latest findings that are above the given importance for the
         * given class, and any inner classes. Only findings with status New or
         * Unchanged are returned, fixed findings are not shown.
         * 
         * TODO do we want to also show fixed findings?
         */
        public List<FindingOverview> showImportantEnoughFindingsForClass(
                final Connection conn, final String projectName,
                final String packageName, final String compilation,
                final Importance importance) throws SQLException {
            if (importance.ordinal() == 0) {
                return showFindingsForClass(conn, projectName, packageName,
                        compilation);
            }
            if (importance.ordinal() == 1
                    && Importance.IRRELEVANT.ordinal() == 0) {
                return showRelevantFindingsForClass(conn, projectName,
                        packageName, compilation);
            }
            boolean first = true;
            final StringBuilder importanceClause = new StringBuilder("(");
            for (final Importance i : Importance.values()) {
                if (i.ordinal() >= importance.ordinal()) {
                    if (first) {
                        first = false;
                    } else {
                        importanceClause.append(" OR ");
                    }
                    importanceClause.append("IMPORTANCE = '")
                            .append(i.toStringSentenceCase()).append('\'');
                }
            }
            importanceClause.append(')');

            final List<FindingOverview> findings = new ArrayList<FindingOverview>();

            final PreparedStatement selectFindingsByClass = conn
                    .prepareStatement(SELECT_COLUMNS
                            + " FROM FINDINGS_OVERVIEW WHERE PROJECT = ? AND PACKAGE = ? AND CU = ? AND "
                            + importanceClause);
            int idx = 1;
            selectFindingsByClass.setString(idx++, projectName);
            selectFindingsByClass.setString(idx++, packageName);
            selectFindingsByClass.setString(idx++, compilation);
            final ResultSet set = selectFindingsByClass.executeQuery();
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
