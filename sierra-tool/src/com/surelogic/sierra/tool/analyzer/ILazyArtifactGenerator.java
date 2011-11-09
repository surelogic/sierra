package com.surelogic.sierra.tool.analyzer;

import com.surelogic.sierra.tool.IToolFactory;
import com.surelogic.sierra.tool.message.ArtifactGenerator;

/**
 * Delays the creation of the ArtifactGenerator until actually needed
 * to allow it to be specialized to the given ITool
 */
public interface ILazyArtifactGenerator {
	/**
	 * @param tool The tool to be run
	 * @return The ArtifactGenerator to be used for the given tool's results
	 */
	ArtifactGenerator create(IToolFactory tool);

	/**	
	 * @return true if the tool should call finished() on the ArtifactGenerator
	 */
	boolean closeWhenDone();
	
	/**
	 * To be called when all the tools are done scanning
	 */
	void finished();
}
