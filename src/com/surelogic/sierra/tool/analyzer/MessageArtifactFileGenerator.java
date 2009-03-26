package com.surelogic.sierra.tool.analyzer;

import java.io.*;
import java.util.zip.GZIPOutputStream;

import com.surelogic.sierra.tool.ITool;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
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
public class MessageArtifactFileGenerator extends AbstractArtifactFileGenerator
implements ILazyArtifactGenerator {
	private final File parsedFile;
	private final boolean compress;
	private OutputStream stream;
	
	public MessageArtifactFileGenerator(File parsedFile, Config config, boolean compress) {
		super(config);
		this.parsedFile = parsedFile;
		this.compress = compress;
	}
	
	public MessageArtifactFileGenerator(File parsedFile, Config config) {
		this(parsedFile, config, true);
	}

	@Override
	protected OutputStream openOutputStream() throws IOException {
		stream = new FileOutputStream(parsedFile);
		if (compress) {
			stream = new GZIPOutputStream(stream, 4096);
		}
		return stream;
	}
	
	@Override
	protected void closeOutputStream() throws IOException {
		//finalFile.close();
		//osw.close();
		stream.close();
		stream = null;
	}

	public ArtifactGenerator create(ITool tool) {
		// Ok to return itself
		// since it doesn't need to be specialized to a particular tool
		return this;
	}

	public boolean closeWhenDone() {
		return false;
	}
	
	public void finished() {
		// Nothing extra to do
	}
}
