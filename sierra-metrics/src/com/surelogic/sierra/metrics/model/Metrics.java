package com.surelogic.sierra.metrics.model;

public class Metrics {

	/** Name of the class */
	private String className = null;

	/** Name of the package */
	private String packageName = null;

	/** Lines of Code */
	private long loc;

	/**
	 * The complete path of the file - This is dependent on the system, it's
	 * there to avoid the condition when the package and class name are same
	 */
	private String path;

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

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	@Override
	public int hashCode() {
	  if (className == null) {
	    return packageName == null ? 0 : packageName.hashCode();
	  }
	  return className.hashCode() + 
	        (packageName == null ? 0 : packageName.hashCode());
	}
	
	@Override
	public boolean equals(Object o) {
	  if (o instanceof Metrics) {
	    Metrics m = (Metrics) o;
	    if (this.className == null) {
	      return m.className == null &&
	            (this.packageName == null ? 
	             m.packageName == null : this.packageName.equals(m.packageName));
	    }
	    return this.className.equals(m.className) && 
            (this.packageName == null ? 
             m.packageName == null : this.packageName.equals(m.packageName));

	  }
	  return false;  
	}
}
