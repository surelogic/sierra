package com.surelogic.sierra.tool;

public class ArtifactType implements Comparable<ArtifactType> {
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
	
	@Override
	public int hashCode() {
		return tool.hashCode() + version.hashCode() + type.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ArtifactType) {
			ArtifactType a = (ArtifactType) o;
			return tool.equals(a.tool) && version.equals(a.version) && type.equals(a.type);
		}
		return false;
	}

	public int compareTo(ArtifactType a) {
		int rv = tool.compareTo(a.tool);
		if (rv == 0) {
			rv = version.compareTo(a.version);
			if (rv == 0) {
				rv = type.compareTo(a.type);
			}
		}
		return rv;
	}
}
