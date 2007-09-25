package com.surelogic.sierra.jdbc.settings;

public class CategoryView {

	private String name;
	private String uid;

	public CategoryView() {// Do nothing

	}

	public CategoryView(String uid, String name) {
		this.name = name;
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

}
