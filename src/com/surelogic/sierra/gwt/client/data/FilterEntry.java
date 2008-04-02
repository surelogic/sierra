package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public class FilterEntry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 47046359782973349L;

	private String name;

	private boolean filtered;
	private String uid;
	private String shortMessage;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isFiltered() {
		return filtered;
	}

	public void setFiltered(boolean filtered) {
		this.filtered = filtered;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String id) {
		uid = id;
	}

	public String getShortMessage() {
		return shortMessage;
	}

	public void setShortMessage(String shortMessage) {
		this.shortMessage = shortMessage;
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
		final FilterEntry other = (FilterEntry) obj;
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
