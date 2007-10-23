package com.surelogic.sierra.tool.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;

/**
 * The instance hash generator calculates the 30 alphanumeric characters before
 * and after the given line number
 * 
 * @author Tanmay.Sinha
 * 
 */
public class HashGenerator {

	private static final Logger log = SLLogger.getLogger("sierra");

	private static final String FIRST = "FIRST";

	private static final String LAST = "LAST";

	private int countFileAccess = 0;

	private long hashValue;

	private int previousLine = -1;

	private String previousFileName;

	private String currentFileName;

	private File currentFile;

	private static class Singleton {
		static final HashGenerator hashGenerator = new HashGenerator();
	}

	private HashGenerator() {
		// Nothing do to
	}

	public static HashGenerator getInstance() {
		return Singleton.hashGenerator;
	}

	/**
	 * For testing
	 * 
	 * @param args
	 */
	public static void main(String args[]) {

		HashGenerator hashGenerator = HashGenerator.getInstance();
		System.out
				.println("\t" + hashGenerator.getHash("C:/fibonacci.java", 1));
		System.out
				.println("\t" + hashGenerator.getHash("C:/fibonacci.java", 5));
		System.out
				.println("\t" + hashGenerator.getHash("C:/fibonacci.java", 5));
		System.out.println("\t"
				+ hashGenerator.getHash("C:/fibonacci.java", 18));

	}

	public Long getHash(String fileName, int lineNumber) {

		currentFileName = fileName;

		try {

			if ((currentFile == null)
					|| (!previousFileName.equals(currentFileName))) {
				currentFile = new File(currentFileName);
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

			BufferedReader in = new BufferedReader(new FileReader(currentFile));
			String holder = in.readLine();
			String valueUp = "";
			String valueDown = "";

			int lineCounter = 0;
			while (holder != null) {

				lineCounter++;

				String tempHolder[] = holder.split("\\s+");
				StringBuffer result = new StringBuffer();

				if (tempHolder.length > 0) {
					for (int i = 0; i < tempHolder.length; i++) {
						result.append(tempHolder[i]);
					}
				}

				holder = result.toString();

				if ((!"FIRST".equals(valueUp)) && (lineCounter < lineNumber)) {
					valueUp += holder;
					if (valueUp.length() > 30) {
						int extraChars = valueUp.length() - 30;
						valueUp = valueUp.substring(extraChars, valueUp
								.length());
					}
				}
				if ((!"LAST".equals(valueDown)) && (lineCounter >= lineNumber)) {
					if (valueDown.length() < 30) {
						valueDown += holder;
						if (valueDown.length() > 30) {
							valueDown = valueDown.substring(0, 30);
						}
					}
				}

				holder = in.readLine();
			}

			if (valueUp.equals("")) {
				valueUp = FIRST;
			}

			if (valueDown.equals("")) {
				valueDown = LAST;
			}

			// System.out.println("Value up :" + valueUp);
			// System.out.println("Value down :" + valueDown);
			int hashUp = valueUp.hashCode();
			int hashDown = valueDown.hashCode();

			hashValue = (((long) hashDown) << 32) + hashUp;
			in.close();
			return hashValue;

		} catch (FileNotFoundException e) {
			log
					.log(Level.SEVERE, "The file " + fileName
							+ " was not found.", e);
			throw new RuntimeException("While generating hash :"
					+ e.getMessage());
		} catch (IOException e) {
			log.log(Level.SEVERE, "I/O exception while hash generation.", e);
			throw new RuntimeException("While generating hash :"
					+ e.getMessage());
		}
	}

	public void generateHash(Map<String, Map<Integer, Long>> hashHolder) {

		Set<String> fileNames = hashHolder.keySet();

		Iterator<String> fileNameIterator = fileNames.iterator();

		while (fileNameIterator.hasNext()) {
			String fileName = fileNameIterator.next();

			Map<Integer, Long> lineHashMap = hashHolder.get(fileName);
			Set<Integer> lineNumbers = lineHashMap.keySet();
			Iterator<Integer> lineNumberIterator = lineNumbers.iterator();

			while (lineNumberIterator.hasNext()) {
				int lineNumber = lineNumberIterator.next();
				Long hashValue = getHash(fileName, lineNumber);
				lineHashMap.put(lineNumber, hashValue);
			}

			countFileAccess++;

		}

		// System.out.println("File Access from the new way : " +
		// countFileAccess);

	}
}
