package com.surelogic.sierra.tool.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Simple scrubber for PMD file, replaces all occurrences of "&u" to "u"
 * 
 * Fix for Bug https://fluid.surelogic.com/bugzilla/show_bug.cgi?id=1042
 * 
 * @author Tanmay.Sinha
 * 
 */
public final class PMDScrubber {

	private static final PMDScrubber INSTANCE = new PMDScrubber();

	private PMDScrubber() {
		// Nothing to do
	}

	public static PMDScrubber getInstance() {
		return INSTANCE;
	}

	/**
	 * Scrubs the old file and outputs the result in the new file
	 * 
	 * @param oldFile -
	 *            file to be scrubbed
	 * @param newFile -
	 *            target file
	 * @throws IOException
	 */
	public void scrub(File oldFile, File newFile) throws IOException {
		OutputStream stream = new FileOutputStream(newFile);
		OutputStreamWriter osw = new OutputStreamWriter(stream);
		PrintWriter finalFile = new PrintWriter(osw);

		BufferedReader in = new BufferedReader(new FileReader(oldFile));
		String line = null;
		while ((line = in.readLine()) != null) {
			line = line.replace("&u", "u");
			finalFile.write(line);
			finalFile.write("\n");
		}
		in.close();
		finalFile.flush();
		finalFile.close();
	}
}
