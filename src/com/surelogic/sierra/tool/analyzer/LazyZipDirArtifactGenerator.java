package com.surelogic.sierra.tool.analyzer;

import java.io.*;
import java.util.logging.Level;

import com.surelogic.common.FileUtility;
import com.surelogic.common.jobs.NullSLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.ITool;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.MessageWarehouse;

/**
 * Creates results in a temporary directory before creating the final zip file
 *
 * @author edwin
 */
public class LazyZipDirArtifactGenerator implements ILazyArtifactGenerator {
	final Config config;
	final File tempDir;
	
	public LazyZipDirArtifactGenerator(Config config) {
		this.config = config;

		final File scanDoc = config.getScanDocument();
		final String dirName = scanDoc.getName().substring(0, scanDoc.getName().lastIndexOf(".sierra"));
		tempDir = new File(scanDoc.getParentFile(), dirName);
		tempDir.mkdir();
			
		// Create config stream
		final File metadata = new File(tempDir, MessageWarehouse.CONFIG_STREAM_NAME);
		new MessageArtifactFileGenerator(metadata, config, false).finished(new NullSLProgressMonitor());		
	}
	
	public ArtifactGenerator create(ITool tool) {
		final String name = tool.getName().toLowerCase() + MessageWarehouse.TOOL_STREAM_SUFFIX;
		return new MessageArtifactFileGenerator(new File(tempDir, name), config, false);
	}

	public boolean closeWhenDone() {
		return true;
	}
	
	public void finished() {				
		try {
			FileUtility.zipDir(tempDir, config.getScanDocument());
			//FileUtility.recursiveDelete(tempDir);
		} catch (IOException e) {
			SLLogger.getLogger().log(Level.SEVERE, "Couldn't create scan document", e);
		}
	}
}
