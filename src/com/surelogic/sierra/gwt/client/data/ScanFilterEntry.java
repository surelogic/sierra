package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public class ScanFilterEntry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6408580757889309477L;

	private String uid;
	private String name;
	private String shortMessage;
	private ImportanceView importance;
	private boolean category;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortMessage() {
		return shortMessage;
	}

	public void setShortMessage(String shortMessage) {
		this.shortMessage = shortMessage;
	}

	public boolean isCategory() {
		return category;
	}

	public void setCategory(boolean category) {
		this.category = category;
	}

	public ImportanceView getImportance() {
		return importance;
	}

	public void setImportance(ImportanceView importance) {
		this.importance = importance;
	}

}
