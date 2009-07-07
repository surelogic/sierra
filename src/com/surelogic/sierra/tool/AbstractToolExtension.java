package com.surelogic.sierra.tool;

import java.util.Set;

public abstract class AbstractToolExtension implements IToolExtension {
	private final String id; 
	private final String version;
	private final Set<ArtifactType> types;
	
	protected AbstractToolExtension(String id, String version, Set<ArtifactType> types) {
		this.id = id;
		this.version = version;
		this.types = types;
	}
	
	protected AbstractToolExtension(String id, Set<ArtifactType> types) {
		String version;
		final int index = id.indexOf(".v");
		final int afterV = index+2;
		
		// See if we have a digit following the 'v' (e.g. foo.v1)
		if (index >= 0 && afterV < id.length() && Character.isDigit(id.charAt(afterV))) {		
			version = id.substring(index+1);
			id = id.substring(0, index);
		} else {
			version = "";
		}
		this.id = id;
		this.version = version;
		this.types = types;
	}
	
	public final String getId() {
		return id;
	}
	
	public final String getVersion() {
		return version;
	}	
	
	public final Set<ArtifactType> getArtifactTypes() {
		return types;
	}
}
