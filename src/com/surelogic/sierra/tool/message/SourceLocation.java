package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class SourceLocation {
	public static class Builder {
		private IdentifierType type;
		private String identifier;
		private String className;
		private String packageName;
		private String path;
		private int lineOfCode;
		private int endLine;
		private Long hash;

		Builder() {
			clear();
		}

		public Builder type(IdentifierType type) {
			this.type = type;
			return this;
		}

		public Builder identifier(String identifier) {
			this.identifier = identifier;
			return this;
		}

		public Builder className(String className) {
			this.className = className;
			return this;
		}

		public Builder packageName(String packageName) {
			this.packageName = packageName;
			return this;
		}

		public Builder path(String path) {
			this.path = path;
			return this;
		}

		public Builder lineOfCode(int lineOfCode) {
			this.lineOfCode = lineOfCode;
			return this;
		}

		public Builder endLine(int endLine) {
			this.endLine = endLine;
			return this;
		}

		public Builder hash(Long hash) {
			this.hash = hash;
			return this;
		}

		private void clear() {
			this.type = null;
			this.identifier = null;
			this.className = null;
			this.packageName = null;
			this.path = null;
			this.lineOfCode = 0;
			this.endLine = 0;
			this.hash = null;
		}

		public SourceLocation build() {
			SourceLocation s = new SourceLocation(this);
			clear();
			return s;
		}
	}

	private String pathName;
	private String className;
	private Long hash;
	private String packageName;
	private int lineOfCode;
	private int endLineOfCode;
	private String identifier;
	private IdentifierType identifierType;

	public SourceLocation() {
		// Nothing to do
	}

	SourceLocation(Builder builder) {
		this.pathName = builder.path;
		this.className = builder.className;
		this.hash = builder.hash;
		this.packageName = builder.packageName;
		this.lineOfCode = builder.lineOfCode;
		this.endLineOfCode = builder.endLine;
		this.identifier = builder.identifier;
		this.identifierType = builder.type;
	}

	public String getPathName() {
		return pathName;
	}

	public String getClassName() {
		return className;
	}

	public String getPackageName() {
		return packageName;
	}

	public int getLineOfCode() {
		return lineOfCode;
	}

	public int getEndLineOfCode() {
		return endLineOfCode;
	}

	public String getIdentifier() {
		return identifier;
	}

	public IdentifierType getIdentifierType() {
		return identifierType;
	}

	public Long getHash() {
		return hash;
	}

	public void setPathName(String pathName) {
		this.pathName = pathName;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setHash(Long hash) {
		this.hash = hash;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setLineOfCode(int lineOfCode) {
		this.lineOfCode = lineOfCode;
	}

	public void setEndLineOfCode(int endLineOfCode) {
		this.endLineOfCode = endLineOfCode;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setIdentifierType(IdentifierType locationType) {
		this.identifierType = locationType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result + endLineOfCode;
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
		result = prime * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result
				+ ((identifierType == null) ? 0 : identifierType.hashCode());
		result = prime * result + lineOfCode;
		result = prime * result
				+ ((packageName == null) ? 0 : packageName.hashCode());
		result = prime * result
				+ ((pathName == null) ? 0 : pathName.hashCode());
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
		final SourceLocation other = (SourceLocation) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
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
		if (identifierType == null) {
			if (other.identifierType != null)
				return false;
		} else if (!identifierType.equals(other.identifierType))
			return false;
		if (lineOfCode != other.lineOfCode)
			return false;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		if (pathName == null) {
			if (other.pathName != null)
				return false;
		} else if (!pathName.equals(other.pathName))
			return false;
		return true;
	}

}
