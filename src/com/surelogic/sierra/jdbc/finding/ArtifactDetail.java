package com.surelogic.sierra.jdbc.finding;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.surelogic.sierra.tool.message.IdentifierType;

public class ArtifactDetail {
	private final String tool;
	private final String message;
	private final SourceDetail primary;
	private final List<SourceDetail> additionalSources;

	ArtifactDetail(ResultSet set, SourceDetail primary,
			List<SourceDetail> additionalSources) throws SQLException {
		int idx = 1;
		this.tool = set.getString(idx++);
		this.message = set.getString(idx++);
		this.primary = primary;
		this.additionalSources = additionalSources;
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

	public List<SourceDetail> getAdditionalSources() {
		return additionalSources;
	}

}
