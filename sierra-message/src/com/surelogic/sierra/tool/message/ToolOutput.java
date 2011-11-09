package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "metrics", "artifacts", "errors" })
public class ToolOutput {
	private Metrics metrics;
	private Artifacts artifacts;
	private Errors errors;

	public Metrics getMetrics() {
		return metrics;
	}

	public void setMetrics(final Metrics metrics) {
		this.metrics = metrics;
	}

	public Artifacts getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(final Artifacts artifacts) {
		this.artifacts = artifacts;
	}

	public Errors getErrors() {
		return errors;
	}

	public void setErrors(final Errors errors) {
		this.errors = errors;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((artifacts == null) ? 0 : artifacts.hashCode());
		result = prime * result + ((errors == null) ? 0 : errors.hashCode());
		result = prime * result + ((metrics == null) ? 0 : metrics.hashCode());
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
		final ToolOutput other = (ToolOutput) obj;
		if (artifacts == null) {
			if (other.artifacts != null) {
				return false;
			}
		} else if (!artifacts.equals(other.artifacts)) {
			return false;
		}
		if (errors == null) {
			if (other.errors != null) {
				return false;
			}
		} else if (!errors.equals(other.errors)) {
			return false;
		}
		if (metrics == null) {
			if (other.metrics != null) {
				return false;
			}
		} else if (!metrics.equals(other.metrics)) {
			return false;
		}
		return true;
	}

}
