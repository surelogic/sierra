package com.surelogic.sierra.tool.analyzer;

import java.util.Collection;

/**
 * Implementors of RunGenerator generally allow someone to build a
 * representation of a run. Possible implementations might represent a run in
 * memory, in the database, or in a message sent to a remote server. The output
 * of a RunGenerator is implementation specific, RunGenerator merely provides an
 * interface that allows runs to be built.
 * 
 * @author nathan
 * 
 */
public interface RunGenerator {

	RunGenerator javaVersion(String version);

	RunGenerator javaVendor(String vendor);

	RunGenerator project(String projectName);

	/**
	 * The generated run will belong to the specified set of qualifiers. This
	 * method should never be called to build a run in the client database.
	 * 
	 * @param qualifiers
	 * @return
	 */
	RunGenerator qualifiers(Collection<String> qualifiers);

	ArtifactGenerator build();
}
