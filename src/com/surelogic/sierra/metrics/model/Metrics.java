package com.surelogic.sierra.metrics.model;

public class Metrics {

	/** Name of the class */
	private String className = null;

	/** Name of the package */
	private String packageName = null;

	/** Lines of Code */
	private long loc;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public long getLoc() {
		return loc;
	}

	public void setLoc(long loc) {
		this.loc = loc;
	}
}
