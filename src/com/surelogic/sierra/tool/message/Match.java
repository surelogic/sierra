package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result
				+ ((findingType == null) ? 0 : findingType.hashCode());
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
		result = prime * result
				+ ((packageName == null) ? 0 : packageName.hashCode());
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
		final Match other = (Match) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (findingType == null) {
			if (other.findingType != null)
				return false;
		} else if (!findingType.equals(other.findingType))
			return false;
		if (hash == null) {
			if (other.hash != null)
				return false;
		} else if (!hash.equals(other.hash))
			return false;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Match(" + packageName + "," + className + "," + hash + ","
				+ findingType + ")";
	}
}
