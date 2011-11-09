package com.surelogic.sierra.tool.message;

import java.util.Collection;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class Artifacts {
	private Collection<Artifact> artifact;

	public Collection<Artifact> getArtifact() {
		return artifact;
	}

	public void setArtifact(final Collection<Artifact> artifact) {
		this.artifact = artifact;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((artifact == null) ? 0 : artifact.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Artifacts other = (Artifacts) obj;
		if (artifact == null) {
			if (other.artifact != null) {
				return false;
			}
		} else if (!artifact.equals(other.artifact)) {
			return false;
		}
		return true;
	}

}
