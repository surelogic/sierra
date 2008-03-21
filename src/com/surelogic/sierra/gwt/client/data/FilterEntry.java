package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public class FilterEntry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 47046359782973349L;

	private String name;

	private boolean filtered;
	private String id;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getShortMessage() {
		return shortMessage;
	}

	public void setShortMessage(String shortMessage) {
		this.shortMessage = shortMessage;
	}

}
