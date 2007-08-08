package com.surelogic.sierra.tool.message;

import java.util.Collection;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class ToolOutput {

	private Collection<Artifact> artifacts;
	private Collection<Error> errors;

	public Collection<Artifact> getArtifact() {
		return artifacts;
	}

	public void setArtifact(Collection<Artifact> artifacts) {
		this.artifacts = artifacts;
	}

	public Collection<Error> getErrors() {
		return errors;
	}

	public void setErrors(Collection<Error> errors) {
		this.errors = errors;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((artifacts == null) ? 0 : artifacts.hashCode());
		result = prime * result + ((errors == null) ? 0 : errors.hashCode());
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
		final ToolOutput other = (ToolOutput) obj;
		if (artifacts == null) {
			if (other.artifacts != null)
				return false;
		} else if (!artifacts.equals(other.artifacts))
			return false;
		if (errors == null) {
			if (other.errors != null)
				return false;
		} else if (!errors.equals(other.errors))
			return false;
		return true;
	}

}
