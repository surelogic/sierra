package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public class Project implements Serializable, Cacheable {
	private static final long serialVersionUID = -6729902565459490855L;

	private String uuid;

	private String name;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
