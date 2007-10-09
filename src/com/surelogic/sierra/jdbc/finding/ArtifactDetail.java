package com.surelogic.sierra.jdbc.finding;

public class ArtifactDetail {
	private final String tool;
	private final String message;

	ArtifactDetail(String tool, String message) {
		this.tool = tool;
		this.message = message;
	}

	public String getTool() {
		return tool;
	}

	public String getMessage() {
		return message;
	}

}
