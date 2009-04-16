package com.surelogic.sierra.tool.analyzer;

import java.io.*;
import java.util.logging.Level;

import com.surelogic.common.FileUtility;
import com.surelogic.common.jobs.NullSLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.IToolFactory;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.MessageWarehouse;

/**
 * Creates results in a temporary directory before creating the final zip file
 * Not to be used by clients
 *
 * @author edwin
 */
public final class LazyZipDirArtifactGenerator implements ILazyArtifactGenerator {
	final Config config;
	final File tempDir;
	
	public static File computeTempDir(File scanDoc) {
		final String dirName = scanDoc.getName().substring(0, scanDoc.getName().lastIndexOf(".sierra"));
		return new File(scanDoc.getParentFile(), dirName);
	}
	
	public static void createConfigStream(File tempDir, Config config) {
		// Create config stream
		final File metadata = new File(tempDir, MessageWarehouse.CONFIG_STREAM_NAME);
		new MessageArtifactFileGenerator(metadata, config, false).finished(new NullSLProgressMonitor());
	}
	
	public LazyZipDirArtifactGenerator(Config config) {
		this.config = config;

		tempDir = computeTempDir(config.getScanDocument());
		tempDir.mkdir();
			
		createConfigStream(tempDir, config);		
	}
	
	public ArtifactGenerator create(IToolFactory tool) {
		final String name = tool.getId().toLowerCase() + MessageWarehouse.TOOL_STREAM_SUFFIX;
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
