package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class Match {
	private String packageName;
	private String className;
	private Long hash;
	private FindingType findingType;

	public Match() {
		// Do nothing
	}

	public Match(String packageName, String className, Long hash, String tool,
			String version, String mnemonic) {
		this.packageName = packageName;
		this.className = className;
		this.hash = hash;
		this.findingType = new FindingType(tool, version, mnemonic);
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Long getHash() {
		return hash;
	}

	public void setHash(Long hash) {
		this.hash = hash;
	}

	public FindingType getFindingType() {
		return findingType;
	}

	public void setFindingType(FindingType findingType) {
		this.findingType = findingType;
	}

}
