package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlRootElement
public class Artifact {
	public static class Builder {

		private FindingType findingType;
		private Priority priority;
		private Severity severity;

		private List<SourceLocation> sources = new ArrayList<SourceLocation>();

		private String message;

		private SourceLocation primarySourceLocation;

		Builder() {
			clear();
		}

		public Builder findingType(String tool, String version, String mnemonic) {
			this.findingType = new FindingType(tool, version, mnemonic);
			return this;
		}

		public Builder priority(Priority priority) {
			this.priority = priority;
			return this;
		}

		public Builder severity(Severity severity) {
			this.severity = severity;
			return this;
		}

		public Builder sourceLocation(SourceLocation sourceLocation) {
			sources.add(sourceLocation);
			return this;
		}

		public Builder message(String message) {
			this.message = message;
			return this;
		}

		public Builder primarySourceLocation(
				SourceLocation primarySourceLocation) {
			this.primarySourceLocation = primarySourceLocation;
			return this;
		}

		public Artifact build() {
			Artifact a = new Artifact(this);
			clear();
			return a;
		}

		private void clear() {
			this.findingType = null;
			this.priority = null;
			this.severity = null;
			this.sources.clear();
			this.message = null;
			this.primarySourceLocation = null;
		}
	}

	private FindingType findingType;
	private SourceLocation primarySourceLocation;
	private List<SourceLocation> additionalSources;
	private Priority priority;
	private Severity severity;
	private String message;

	public Artifact() {
		// Nothing to do
	}

	public Artifact(Builder builder) {
		this.findingType = builder.findingType;
		this.primarySourceLocation = builder.primarySourceLocation;
		this.additionalSources = new ArrayList<SourceLocation>(builder.sources);
		if (builder.priority != null) {
			this.priority = builder.priority;
		}
		if (builder.severity != null) {
			this.severity = builder.severity;
		}
		this.message = builder.message;
	}

	public FindingType getFindingType() {
		return findingType;
	}

	public SourceLocation getPrimarySourceLocation() {
		return primarySourceLocation;
	}

	public List<SourceLocation> getAdditionalSources() {
		return additionalSources;
	}

	public Priority getPriority() {
		return priority;
	}

	public Severity getSeverity() {
		return severity;
	}

	public String getMessage() {
		return message;
	}

	public void setFindingType(FindingType findingType) {
		this.findingType = findingType;
	}

	public void setPrimarySourceLocation(SourceLocation primarySourceLocation) {
		this.primarySourceLocation = primarySourceLocation;
	}

	public void setAdditionalSources(List<SourceLocation> additionalSources) {
		this.additionalSources = additionalSources;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((additionalSources == null) ? 0 : additionalSources
						.hashCode());
		result = prime * result
				+ ((findingType == null) ? 0 : findingType.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime
				* result
				+ ((primarySourceLocation == null) ? 0 : primarySourceLocation
						.hashCode());
		result = prime * result
				+ ((priority == null) ? 0 : priority.hashCode());
		result = prime * result
				+ ((severity == null) ? 0 : severity.hashCode());
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
		final Artifact other = (Artifact) obj;
		if (additionalSources == null) {
			if (other.additionalSources != null)
				return false;
		} else if (!additionalSources.equals(other.additionalSources))
			return false;
		if (findingType == null) {
			if (other.findingType != null)
				return false;
		} else if (!findingType.equals(other.findingType))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (primarySourceLocation == null) {
			if (other.primarySourceLocation != null)
				return false;
		} else if (!primarySourceLocation.equals(other.primarySourceLocation))
			return false;
		if (priority == null) {
			if (other.priority != null)
				return false;
		} else if (!priority.equals(other.priority))
			return false;
		if (severity == null) {
			if (other.severity != null)
				return false;
		} else if (!severity.equals(other.severity))
			return false;
		return true;
	}

}
