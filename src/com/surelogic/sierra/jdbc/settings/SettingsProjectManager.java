package com.surelogic.sierra.jdbc.settings;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.RelationRecord;
import com.surelogic.sierra.jdbc.record.SettingsProjectRecord;
import com.surelogic.sierra.jdbc.record.SettingsRecord;

public class SettingsProjectManager {

	@SuppressWarnings("unused")
	private final Connection conn;

	private SettingsProjectRecordFactory sprFactory;
	private ProjectRecordFactory pFactory;

	private SettingsProjectManager(Connection conn) throws SQLException {
		this.conn = conn;
	}

	public void addRelation(SettingsRecord settings, String projectName)
			throws SQLException {

		if (settings == null)
			throw new SQLException();

		ProjectRecord project = pFactory.newProject();
		project.setName(projectName);
		if (!project.select()) {
			// XXX fill in
			throw new SQLException();
		}

		SettingsProjectRecord spr = sprFactory.newSettingsProject();
		spr.setId(new RelationRecord.PK<ProjectRecord, SettingsRecord>(project,
				settings));
		spr.insert();
	}
}
