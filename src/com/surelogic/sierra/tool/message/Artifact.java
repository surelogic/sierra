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

		public Builder findingType(String tool, String mnemonic) {
			this.findingType = new FindingType(tool, mnemonic);
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
			return new Artifact(this);
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

}
