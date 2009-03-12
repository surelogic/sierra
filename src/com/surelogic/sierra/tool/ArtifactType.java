package com.surelogic.sierra.tool;

public class ArtifactType {
	public final String tool;
	public final String version;
	public final String plugin;
	public final String type;
	public final String category;
	
	public ArtifactType(String tool, String version, String plugin, String type, String category) {
		this.tool = tool;
		this.version = version;
		this.plugin = plugin;
		this.type = type;
		this.category = category;
	}
}
