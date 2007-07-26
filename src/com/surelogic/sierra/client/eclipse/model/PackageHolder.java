package com.surelogic.sierra.client.eclipse.model;

import java.util.Vector;

public class PackageHolder {

	private String name;

	private Vector<ClassHolder> classes;

	private String path;

	public PackageHolder(String packageName) {
		this.name = packageName;

	}

	public Vector<ClassHolder> getClasses() {
		return classes;
	}

	public void setClasses(Vector<ClassHolder> classes) {
		this.classes = classes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
