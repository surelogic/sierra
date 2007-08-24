package com.surelogic.sierra.tool.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;

/**
 * The instance hash generator calculates the 30 alphanumeric characters before
 * and after the given line number
 * 
 * THIS IS NOT IN USE
 * 
 * @author Tanmay.Sinha
 * 
 */

@Deprecated
public class DifferentHashGenerator {

	private static final Logger log = SLLogger.getLogger("sierra");

	private static final String FIRST = "FIRST";

	private static final String LAST = "LAST";

	private static long hashValue;

	private static int previousLine = -1;

	private static String previousFileName;

	private static String currentFileName;

	private static LinkedList<String> representation = new LinkedList<String>();

	private DifferentHashGenerator() {
		// Nothing do to
	}

	public static DifferentHashGenerator getInstance() {
		return Singleton.hashGenerator;
	}

	private static class Singleton {
		static final DifferentHashGenerator hashGenerator = new DifferentHashGenerator();
	}

	/**
	 * For testing
	 * 
	 * @param args
	 */
	public static void main(String args[]) {

		DifferentHashGenerator hashGenerator = DifferentHashGenerator
				.getInstance();
		hashGenerator.getHash("parsing.java", 1);
		hashGenerator.getHash("parsing.java", 1);
		hashGenerator.getHash("parsing.java", 6);
		hashGenerator.getHash("parsing.java", 7);

	}

	private void makeReprestentation(String fileName) {

		try {

			File file = new File(fileName);
			BufferedReader in = new BufferedReader(new FileReader(file));

			representation.add(FIRST);
			String holder = in.readLine();
			while (holder != null) {
				String tempHolder[] = holder.split("\\s+");
				StringBuffer result = new StringBuffer();

				if (tempHolder.length > 0) {
					for (int i = 0; i < tempHolder.length; i++) {
						result.append(tempHolder[i]);
					}
				}

				holder = result.toString();

				representation.add(holder);
				holder = in.readLine();

			}

		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, "The file " + fileName + " was not found."
					+ e);
		} catch (IOException e) {
			log.log(Level.SEVERE, "I/O exception while hash generation." + e);
		}

	}

	public Long getHash(String fileName, int lineNumber) {
		currentFileName = fileName;

		if ((representation.size() == 0)
				|| (!previousFileName.equals(currentFileName))) {

			representation.clear();
			makeReprestentation(currentFileName);
			previousFileName = currentFileName;
			previousLine = -1;
		}

		if (previousLine == -1) {
			previousLine = lineNumber;
		} else if ((previousFileName).equals(currentFileName)
				&& (previousLine == lineNumber)) {
			return hashValue;
		} else {
			previousLine = lineNumber;
		}

		String valueUp = "";
		String valueDown = "";

		int line = lineNumber;

		if (lineNumber <= 1) {
			valueUp = FIRST;
		} else {
			while (line > 0 || valueUp.length() < 30) {
				line--;
				if (line >= 1) {
					valueUp += representation.get(line);
					if (valueUp.length() > 30) {
						int extraChars = valueUp.length() - 30;
						valueUp = valueUp.substring(extraChars, valueUp
								.length());
					}
				}

			}
		}

		if (lineNumber == (representation.size())) {
			valueDown = LAST;
		} else if (lineNumber < representation.size()) {
			line = lineNumber;
			while (valueDown.length() < 30 || line < representation.size()) {
				line++;
				valueDown += representation.get(line);
				if (valueDown.length() > 30) {
					valueDown = valueDown.substring(0, 30);
				}
			}
		}

		int hashUp = valueUp.hashCode();
		int hashDown = valueDown.hashCode();

		hashValue = (((long) hashDown) << 32) + hashUp;
		return hashValue;

	}
}
