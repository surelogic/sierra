package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

import com.google.gwt.core.client.GWT;

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

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!GWT.getTypeName(this).equals(GWT.getTypeName(obj))) {
			return false;
		}
		final ScanFilterEntry other = (ScanFilterEntry) obj;
		if (uid == null) {
			if (other.uid != null) {
				return false;
			}
		} else if (!uid.equals(other.uid)) {
			return false;
		}
		return true;
	}

}
