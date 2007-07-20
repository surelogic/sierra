package com.surelogic.sierra.message;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType
public class Run {

	public static class Builder {
		private Date runDateTime;
		private String project;
		private String javaVersion;
		private String javaVendor;
		private List<String> qualifiers;
		private List<Artifact> artifacts;

		public Builder runDateTime(Date runDateTime) {
			this.runDateTime = runDateTime;
			return this;
		}

		public Builder project(String project) {
			this.project = project;
			return this;
		}

		public Builder javaVersion(String javaVersion) {
			this.javaVersion = javaVersion;
			return this;
		}

		public Builder javaVendor(String javaVendor) {
			this.javaVendor = javaVendor;
			return this;
		}

		public Builder qualifiers(List<String> qualifiers) {
			this.qualifiers = qualifiers;
			return this;
		}

		public Builder artifacts(List<Artifact> artifacts) {
			this.artifacts = artifacts;
			return this;
		}

		public Run build() {
			return new Run(this);
		}

	}

	private String project;
	private List<String> qualifiers;
	private String javaVersion;
	private String javaVendor;
	private Date runDateTime;
	private List<Artifact> artifacts;

	public Run() {
	}

	public Run(Builder builder) {
		this.runDateTime = builder.runDateTime;
		this.project = builder.project;
		this.javaVendor = builder.javaVendor;
		this.javaVersion = builder.javaVersion;
		this.artifacts = Collections.unmodifiableList(builder.artifacts);
		this.qualifiers = Collections.unmodifiableList(builder.qualifiers);
	}

	public String getProject() {
		return project;
	}

	public List<String> getQualifiers() {
		return qualifiers;
	}

	public String getJavaVersion() {
		return javaVersion;
	}

	public String getJavaVendor() {
		return javaVendor;
	}

	public Date getRunDateTime() {
		return runDateTime;
	}

	public List<Artifact> getArtifacts() {
		return artifacts;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public void setQualifiers(List<String> qualifiers) {
		this.qualifiers = qualifiers;
	}

	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}

	public void setJavaVendor(String javaVendor) {
		this.javaVendor = javaVendor;
	}

	public void setRunDateTime(Date runDateTime) {
		this.runDateTime = runDateTime;
	}

	public void setArtifacts(List<Artifact> artifacts) {
		this.artifacts = artifacts;
	}

	
}
