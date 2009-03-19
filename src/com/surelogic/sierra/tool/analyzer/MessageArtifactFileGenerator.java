package com.surelogic.sierra.tool.analyzer;

import java.io.*;
import java.util.zip.GZIPOutputStream;

import com.surelogic.sierra.tool.message.Config;

/**
 * The scan document generator
 * 
 * This class generates the run document. It generates 3 separate temporary
 * files and stores the artifacts, errors and config in them. It finally
 * combines all of them with proper xml tags and generates a run document.
 * 
 * @author Tanmay.Sinha
 * 
 */
public class MessageArtifactFileGenerator extends AbstractArtifactFileGenerator {
	private final File parsedFile;

	public MessageArtifactFileGenerator(File parsedFile, Config config) {
		super(config);
		this.parsedFile = parsedFile;
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		OutputStream stream = new FileOutputStream(parsedFile);
		return new GZIPOutputStream(stream, 4096);
	}
}
