package com.surelogic.sierra.message;

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
		private String hash;

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

		public Builder hash(String hash) {
			this.hash = hash;
			return this;
		}

		public SourceLocation build() {
			return new SourceLocation(this);
		}
	}

	private String pathName;
	private String className;
	private String hash;
	private String packageName;
	private int lineOfCode;
	private int endLineOfCode;
	private String identifier;
	private IdentifierType identifierType;

	public SourceLocation() {
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

	public String getHash() {
		return hash;
	}

	public void setPathName(String pathName) {
		this.pathName = pathName;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setHash(String hash) {
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

}
