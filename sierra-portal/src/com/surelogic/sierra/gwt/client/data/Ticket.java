package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public final class Ticket implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2692715252416248239L;
	private String uuid;

	public Ticket() {
		// Do nothing
	}

	public Ticket(String uuid) {
		this.uuid = uuid;
	}

	public String getUUID() {
		return uuid;
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
	}

}
