/**
 * 
 */
package com.surelogic.sierra.jdbc.run;

import java.sql.PreparedStatement;
import java.sql.SQLException;

class ArtifactSourceRecord {
	private ArtifactRecord artifact;
	private SourceRecord source;

	ArtifactSourceRecord(ArtifactRecord artifact, SourceRecord source) {
		this.artifact = artifact;
		this.source = source;
	}

	// ARTIFACT_ID,SOURCE_LOCATION_ID
	public int fill(PreparedStatement st, int idx) throws SQLException {
		st.setLong(idx++, artifact.getId());
		st.setLong(idx++, source.getId());
		return idx;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((artifact == null) ? 0 : artifact.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
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
		final ArtifactSourceRecord other = (ArtifactSourceRecord) obj;
		if (artifact == null) {
			if (other.artifact != null)
				return false;
		} else if (!artifact.equals(other.artifact))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		return true;
	}

}