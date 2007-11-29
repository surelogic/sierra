package com.surelogic.sierra.jdbc.settings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.FindingTypeFilterRecord;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.tool.message.FindingTypeFilter;
import com.surelogic.sierra.tool.message.Settings;

public class ClientSettingsManager extends SettingsManager {

	private final PreparedStatement deleteProjectSettings;
	private final PreparedStatement deleteFilterByFindingType;
	private final PreparedStatement updateSettingsRevision;
	private final PreparedStatement getSettingsRevision;
	private final PreparedStatement getFiltersByProjectId;
	private final BaseMapper findingTypeFilterMapper;

	private ClientSettingsManager(Connection conn) throws SQLException {
		super(conn);
		getSettingsRevision = conn
				.prepareStatement("SELECT SETTINGS_REVISION FROM PROJECT WHERE NAME = ?");
		updateSettingsRevision = conn
				.prepareStatement("UPDATE PROJECT SET SETTINGS_REVISION = ? WHERE ID = ?");
		deleteProjectSettings = conn
				.prepareStatement("DELETE FROM PROJECT_FILTERS WHERE PROJECT_ID = ?");
		this.deleteFilterByFindingType = conn
				.prepareStatement("DELETE FROM PROJECT_FILTERS WHERE PROJECT_ID = ? AND FINDING_TYPE_ID = ?");
		findingTypeFilterMapper = new BaseMapper(
				conn,
				"INSERT INTO PROJECT_FILTERS (PROJECT_ID, FINDING_TYPE_ID,DELTA,IMPORTANCE,FILTERED) VALUES (?,?,?,?,?)",
				null, null, false);
		getFiltersByProjectId = conn
				.prepareStatement("SELECT FT.UUID,F.DELTA,F.IMPORTANCE,F.FILTERED FROM PROJECT_FILTERS F, FINDING_TYPE FT WHERE F.PROJECT_ID = ? AND FT.ID = F.FINDING_TYPE_ID");
	}

	/**
	 * Retrieve settings for the given project. Returns <code>null</code> if
	 * no settings currently exist for the project.
	 * 
	 * @param project
	 * @return
	 * @throws SQLException
	 */
	public Settings getSettings(String project) throws SQLException {
		ProjectRecord record = ProjectRecordFactory.getInstance(conn)
				.newProject();
		record.setName(project);
		if (record.select()) {
			getSettingsRevision.setString(1, project);
			ResultSet set = getSettingsRevision.executeQuery();
			set.next();
			set.getLong(1);
			if (set.wasNull()) {
				return null;
			}
			getFiltersByProjectId.setLong(1, record.getId());
			set = getFiltersByProjectId.executeQuery();
			try {
				return readSettings(set);
			} finally {
				set.close();
			}
		} else {
			return null;
		}
	}

	/**
	 * Look up the settings revision of the current project.
	 * 
	 * @param project
	 * @return
	 * @throws SQLException
	 */
	public Long getSettingsRevision(String project) throws SQLException {
		getSettingsRevision.setString(1, project);
		ResultSet set = getSettingsRevision.executeQuery();
		try {
			if (set.next()) {
				return set.getLong(1);
			} else {
				throw new IllegalArgumentException("No project with name "
						+ project + " exists");
			}
		} finally {
			set.close();
		}
	}

	/**
	 * Overwrite the current project settings with the given settings.
	 * 
	 * @param project
	 * @param revision
	 * @param settings
	 * @throws SQLException
	 */
	public void writeSettings(String project, Long revision, Settings settings)
			throws SQLException {
		ProjectRecord record = ProjectRecordFactory.getInstance(conn)
				.newProject();
		record.setName(project);
		if (record.select()) {
			deleteProjectSettings.setLong(1, record.getId());
			deleteProjectSettings.execute();
			List<FindingTypeFilter> filters = settings.getFilter();
			if (filters != null) {
				applyFilters(record.getId(), filters);
			}
			updateSettingsRevision.setLong(1, revision);
			updateSettingsRevision.setLong(2, record.getId());
			updateSettingsRevision.execute();
		} else {
			throw new IllegalArgumentException("No project with name "
					+ project + " exists.");
		}
	}

	public static ClientSettingsManager getInstance(Connection conn)
			throws SQLException {
		return new ClientSettingsManager(conn);
	}

	@Override
	protected FindingTypeFilterRecord newFilterRecord() {
		return new FindingTypeFilterRecord(findingTypeFilterMapper);
	}

	@Override
	protected PreparedStatement getDeleteFilterByFindingType() {
		return deleteFilterByFindingType;
	}

}
