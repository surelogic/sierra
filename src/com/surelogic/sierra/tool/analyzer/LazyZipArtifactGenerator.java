package com.surelogic.sierra.tool.analyzer;

import java.io.*;
import java.util.logging.Level;
import java.util.zip.ZipOutputStream;

import com.surelogic.common.jobs.NullSLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.ITool;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.MessageWarehouse;

/**
 * Directly creates a zip file to contain the results
 * 
 * @author edwin
 */
public class LazyZipArtifactGenerator implements ILazyArtifactGenerator {
	final Config config;
	final ZipOutputStream stream;
	
	public LazyZipArtifactGenerator(Config config) {
		this.config = config;
		
		OutputStream os = null;
		try {
			os = new FileOutputStream(config.getScanDocument());
		} catch (FileNotFoundException e) {
			SLLogger.getLogger().log(Level.SEVERE, "Couldn't create scan document", e);
		}
		if (os == null) {
			stream = null;
		} else {		
			stream = new ZipOutputStream(os);
			
			// Create config stream
			new MessageArtifactZipFileGenerator(config, stream, MessageWarehouse.CONFIG_STREAM_NAME).finished(new NullSLProgressMonitor());
		}
	}
	
	public ArtifactGenerator create(ITool tool) {
		final String name = tool.getName().toLowerCase() + MessageWarehouse.TOOL_STREAM_SUFFIX;
		return new MessageArtifactZipFileGenerator(config, stream, name);
	}

	public boolean closeWhenDone() {
		return true;
	}
	
	public void finished() {
		try {
			stream.close();
		} catch (IOException e) {
			SLLogger.getLogger().log(Level.SEVERE, "Couldn't close scan document", e);
		}
	}
}
