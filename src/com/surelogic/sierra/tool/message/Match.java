package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class Match {
	private String packageName;
	private String className;
	private Long hash;
	private FindingType findingType;

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
