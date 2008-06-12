package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
@XmlRootElement
public class Artifact {
	private ArtifactType artifactType;
	private SourceLocation primarySourceLocation;
	private List<SourceLocation> additionalSources;
	private Priority priority;
	private Severity severity;
	private String message;
	private Integer scanNumber;
	private AssuranceType assurance;

	public Artifact() {
		// Nothing to do
	}

	public Artifact(Builder builder) {
		artifactType = builder.artifactType;
		primarySourceLocation = builder.primarySourceLocation;
		additionalSources = new ArrayList<SourceLocation>(builder.sources);

		if (builder.priority != null) {
			priority = builder.priority;
		}

		if (builder.severity != null) {
			severity = builder.severity;
		}

		message = builder.message;
		scanNumber = builder.scanNumber;
		assurance = builder.assurance;
	}

	public ArtifactType getArtifactType() {
		return artifactType;
	}

	public void setArtifactType(ArtifactType artifactType) {
		this.artifactType = artifactType;
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

	public Integer getScanNumber() {
		return scanNumber;
	}

	public void setScanNumber(Integer scanNumber) {
		this.scanNumber = scanNumber;
	}

	public AssuranceType getAssurance() {
		return assurance;
	}

	public void setAssurance(AssuranceType assurance) {
		this.assurance = assurance;
	}

	public static class Builder {
		private ArtifactType artifactType;
		private Priority priority;
		private Severity severity;
		private final List<SourceLocation> sources = new ArrayList<SourceLocation>();
		private String message;
		private SourceLocation primarySourceLocation;
		private Integer scanNumber;
		private AssuranceType assurance;

		public Builder() {
			clear();
		}

		public Builder findingType(String tool, String version, String mnemonic) {
			artifactType = new ArtifactType();
			artifactType.setTool(tool);
			artifactType.setVersion(version);
			artifactType.setMnemonic(mnemonic);

			return this;
		}

		public Builder assurance(AssuranceType assurance) {
			this.assurance = assurance;
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

		public Builder scanNumber(int scanNumber) {
			this.scanNumber = scanNumber;

			return this;
		}

		public Artifact build() {
			validate();

			final Artifact a = new Artifact(this);
			clear();

			return a;
		}

		private void validate() {
			if (artifactType == null) {
				throw new IllegalArgumentException(
						"An artifact is being built with no artifact type.");
			}

			if (primarySourceLocation == null) {
				throw new IllegalArgumentException(
						"An artifact is being build with no primary source location");
			}
		}

		private void clear() {
			artifactType = null;
			priority = null;
			severity = null;
			sources.clear();
			message = null;
			primarySourceLocation = null;
			assurance = null;
			scanNumber = null;
		}
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
				+ ((artifactType == null) ? 0 : artifactType.hashCode());
		result = prime * result
				+ ((assurance == null) ? 0 : assurance.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime
				* result
				+ ((primarySourceLocation == null) ? 0 : primarySourceLocation
						.hashCode());
		result = prime * result
				+ ((priority == null) ? 0 : priority.hashCode());
		result = prime * result
				+ ((scanNumber == null) ? 0 : scanNumber.hashCode());
		result = prime * result
				+ ((severity == null) ? 0 : severity.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Artifact other = (Artifact) obj;
		if (additionalSources == null) {
			if (other.additionalSources != null) {
				return false;
			}
		} else if (!additionalSources.equals(other.additionalSources)) {
			return false;
		}
		if (artifactType == null) {
			if (other.artifactType != null) {
				return false;
			}
		} else if (!artifactType.equals(other.artifactType)) {
			return false;
		}
		if (assurance == null) {
			if (other.assurance != null) {
				return false;
			}
		} else if (!assurance.equals(other.assurance)) {
			return false;
		}
		if (message == null) {
			if (other.message != null) {
				return false;
			}
		} else if (!message.equals(other.message)) {
			return false;
		}
		if (primarySourceLocation == null) {
			if (other.primarySourceLocation != null) {
				return false;
			}
		} else if (!primarySourceLocation.equals(other.primarySourceLocation)) {
			return false;
		}
		if (priority == null) {
			if (other.priority != null) {
				return false;
			}
		} else if (!priority.equals(other.priority)) {
			return false;
		}
		if (scanNumber == null) {
			if (other.scanNumber != null) {
				return false;
			}
		} else if (!scanNumber.equals(other.scanNumber)) {
			return false;
		}
		if (severity == null) {
			if (other.severity != null) {
				return false;
			}
		} else if (!severity.equals(other.severity)) {
			return false;
		}
		return true;
	}

}
