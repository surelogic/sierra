/**
 * 
 */
package com.surelogic.sierra.jdbc.run;

import static com.surelogic.sierra.jdbc.JDBCUtils.setNullableLong;
import static com.surelogic.sierra.jdbc.JDBCUtils.setNullableString;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import com.surelogic.sierra.jdbc.LongRecord;
import com.surelogic.sierra.tool.message.IdentifierType;

class SourceRecord extends LongRecord {

	private Long hash;
	private Integer lineOfCode;
	private Integer endLineOfCode;
	private IdentifierType type;
	private String identifier;

	private CompilationUnitRecord compUnit;

	SourceRecord() {
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

	// COMPILATION_UNIT_ID,HASH,LINE_OF_CODE,END_LINE_OF_CODE,LOCATION_TYPE,IDENTIFIER
	public int fill(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, compUnit.getId());
		setNullableLong(idx++, st, hash);
		st.setInt(idx++, lineOfCode);
		st.setInt(idx++, endLineOfCode);
		if (type != null) {
			st.setString(idx++, type.name());
		} else {
			st.setNull(idx++, Types.VARCHAR);
		}
		setNullableString(idx++, st, identifier);
		return idx;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((compUnit == null) ? 0 : compUnit.hashCode());
		result = prime * result + endLineOfCode;
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
		result = prime * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + lineOfCode;
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
		if (compUnit == null) {
			if (other.compUnit != null)
				return false;
		} else if (!compUnit.equals(other.compUnit))
			return false;
		if (endLineOfCode != other.endLineOfCode)
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
		if (lineOfCode != other.lineOfCode)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}