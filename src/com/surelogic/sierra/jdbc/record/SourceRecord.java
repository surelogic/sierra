/**
 * 
 */
package com.surelogic.sierra.jdbc.record;

import static com.surelogic.sierra.jdbc.JDBCUtils.setNullableLong;
import static com.surelogic.sierra.jdbc.JDBCUtils.setNullableString;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.surelogic.sierra.tool.message.IdentifierType;

public final class SourceRecord extends LongRecord {

	private Long hash;
	private Integer lineOfCode;
	private Integer endLineOfCode;
	private IdentifierType type;
	private String className;
	private String identifier;

	private CompilationUnitRecord compUnit;

	public SourceRecord(RecordMapper mapper) {
		super(mapper);
	}

	public Long getHash() {
		return hash;
	}

	public void setHash(Long hash) {
		this.hash = hash;
	}

	public Integer getLineOfCode() {
		return lineOfCode;
	}

	public void setLineOfCode(Integer lineOfCode) {
		this.lineOfCode = lineOfCode;
	}

	public Integer getEndLineOfCode() {
		return endLineOfCode;
	}

	public void setEndLineOfCode(Integer endLineOfCode) {
		this.endLineOfCode = endLineOfCode;
	}

	public IdentifierType getType() {
		return type;
	}

	public void setType(IdentifierType type) {
		this.type = type;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public CompilationUnitRecord getCompUnit() {
		return compUnit;
	}

	public void setCompUnit(CompilationUnitRecord compUnit) {
		this.compUnit = compUnit;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, compUnit.getId());
		st.setString(idx++, className);
		setNullableLong(idx++, st, hash);
		st.setInt(idx++, lineOfCode);
		st.setInt(idx++, endLineOfCode);
		setNullableString(idx++, st, type == null ? null : type.name());
		setNullableString(idx++, st, identifier);
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		return fill(st, idx);
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		return idx;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result
				+ ((compUnit == null) ? 0 : compUnit.hashCode());
		result = prime * result
				+ ((endLineOfCode == null) ? 0 : endLineOfCode.hashCode());
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
		result = prime * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result
				+ ((lineOfCode == null) ? 0 : lineOfCode.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SourceRecord other = (SourceRecord) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (compUnit == null) {
			if (other.compUnit != null)
				return false;
		} else if (!compUnit.equals(other.compUnit))
			return false;
		if (endLineOfCode == null) {
			if (other.endLineOfCode != null)
				return false;
		} else if (!endLineOfCode.equals(other.endLineOfCode))
			return false;
		if (hash == null) {
			if (other.hash != null)
				return false;
		} else if (!hash.equals(other.hash))
			return false;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		if (lineOfCode == null) {
			if (other.lineOfCode != null)
				return false;
		} else if (!lineOfCode.equals(other.lineOfCode))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}