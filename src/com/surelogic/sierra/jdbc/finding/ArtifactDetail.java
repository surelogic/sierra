package com.surelogic.sierra.jdbc.finding;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.surelogic.sierra.tool.message.IdentifierType;

public class ArtifactDetail {
	private final String tool;
	private final String message;
	private final SourceDetail primary;

	ArtifactDetail(ResultSet set) throws SQLException {
		int idx = 1;
		this.tool = set.getString(idx++);
		this.message = set.getString(idx++);
		this.primary = new SourceDetail(set, idx);
	}

	public String getTool() {
		return tool;
	}

	public String getMessage() {
		return message;
	}

	public String getClassName() {
		return primary.getClassName();
	}

	public int getEndLineOfCode() {
		return primary.getEndLineOfCode();
	}

	public String getIdentifier() {
		return primary.getIdentifier();
	}

	public IdentifierType getIdentifierType() {
		return primary.getIdentifierType();
	}

	public int getLineOfCode() {
		return primary.getLineOfCode();
	}

	public String getPackageName() {
		return primary.getPackageName();
	}

}
