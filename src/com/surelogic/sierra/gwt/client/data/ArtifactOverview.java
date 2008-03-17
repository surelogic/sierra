package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public class ArtifactOverview implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5777309957000545688L;

	public ArtifactOverview() {
		// Do nothing
	}

	private String tool;
	private String type;
	private String summary;

	// TODO do we want package, class, loc, etc? Probably.

	public String getTool() {
		return tool;
	}

	public void setTool(String tool) {
		this.tool = tool;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

}
