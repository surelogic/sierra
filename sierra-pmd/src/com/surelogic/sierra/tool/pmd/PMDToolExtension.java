package com.surelogic.sierra.tool.pmd;

import java.io.File;
import java.util.Set;

import com.surelogic.sierra.tool.*;

public class PMDToolExtension extends AbstractToolExtension {
	private static final long serialVersionUID = -7383358806326686304L;
	
	private final boolean isCore;
	
	protected PMDToolExtension(String id, String version, File location, Set<ArtifactType> types, boolean core) {
		super("PMD", id, version, location, types);
		isCore = core;
	}

	@Override
	public boolean isCore() {
		return isCore;
	}
}
