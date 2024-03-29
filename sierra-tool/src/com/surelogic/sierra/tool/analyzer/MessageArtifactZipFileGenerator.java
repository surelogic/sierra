package com.surelogic.sierra.tool.analyzer;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.surelogic.sierra.tool.message.Config;

/**
 * The scan document generator, output to a ZipOutputStream
 * 
 * Not to be used by clients
 *
 * @author Edwin.Chan
 */
final class MessageArtifactZipFileGenerator extends AbstractArtifactFileGenerator {
	private final ZipOutputStream zip;
	private final String name;

	public MessageArtifactZipFileGenerator(Config config, ZipOutputStream zip, String name) {
		super(config);
		this.zip = zip;
		this.name = name;
	}

	@Override
	protected OutputStream openOutputStream() throws IOException {
		ZipEntry ze = new ZipEntry(name);
		zip.putNextEntry(ze);
		return zip;
	}
	
	@Override
	protected void closeOutputStream() throws IOException {
		zip.closeEntry();
	}
}
