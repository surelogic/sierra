package com.surelogic.sierra.tool;

import java.util.Set;

public abstract class AbstractToolExtension implements IToolExtension {
	private final String id; 
	private final Set<ArtifactType> types;
	
	protected AbstractToolExtension(String id, Set<ArtifactType> types) {
		this.id = id;
		this.types = types;
	}
	
	public final String getId() {
		return id;
	}
	
	public final Set<ArtifactType> getArtifactTypes() {
		return types;
	}
}
