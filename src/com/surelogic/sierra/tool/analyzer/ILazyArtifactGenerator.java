package com.surelogic.sierra.tool.analyzer;

import com.surelogic.sierra.tool.ITool;
import com.surelogic.sierra.tool.message.ArtifactGenerator;

/**
 * Delays the creation of the ArtifactGenerator until actually needed
 * to allow it to be specialized to the given ITool
 */
public interface ILazyArtifactGenerator {
	ArtifactGenerator create(ITool tool);
	/**	
	 * @return true if the tool should call finished() on the ArtifactGenerator
	 */
	boolean closeWhenDone();
	void finished();
}
