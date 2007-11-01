package com.surelogic.sierra.jdbc.finding;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.surelogic.sierra.tool.message.IdentifierType;

public class SourceDetail {
	private final String packageName;
	private final String className;
	private final int lineOfCode;
	private final int endLineOfCode;
	private final IdentifierType identifierType;
	private final String identifier;

	SourceDetail(ResultSet set, int idx) throws SQLException {
		this.packageName = set.getString(idx++);
		this.className = set.getString(idx++);
		this.lineOfCode = set.getInt(idx++);
		this.endLineOfCode = set.getInt(idx++);
		String identType = set.getString(idx++);
		this.identifierType = (identType == null) ? null : IdentifierType
				.valueOf(identType);
		this.identifier = set.getString(idx++);
	}

	SourceDetail(ResultSet set) throws SQLException {
		int idx = 1;
		this.packageName = set.getString(idx++);
		this.className = set.getString(idx++);
		this.lineOfCode = set.getInt(idx++);
		this.endLineOfCode = set.getInt(idx++);
		String identType = set.getString(idx++);
		this.identifierType = (identType == null) ? null : IdentifierType
				.valueOf(identType);
		this.identifier = set.getString(idx++);
	}

	public String getPackageName() {
		return packageName;
	}

	public String getClassName() {
		return className;
	}

	public int getLineOfCode() {
		return lineOfCode;
	}

	public int getEndLineOfCode() {
		return endLineOfCode;
	}

	public IdentifierType getIdentifierType() {
		return identifierType;
	}

	public String getIdentifier() {
		return identifier;
	}

}
