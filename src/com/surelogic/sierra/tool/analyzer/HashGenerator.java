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

import com.surelogic.sierra.tool.SierraLogger;

/**
 * The instance hash generator calculates the 30 alphanumeric characters before
 * and after the given line number
 * 
 * @author Tanmay.Sinha
 * 
 */
public class HashGenerator {

	private static final Logger log = SierraLogger.getLogger("Sierra");

	private static final String FIRST = "FIRST";

	private static final String LAST = "LAST";

	private int countFileAccess = 0;

	// private static List<String> valueUp = new ArrayList<String>();
	//
	// private static List<String> valueDown = new ArrayList<String>();

	private static long hashValue;

	private static int previousLine = -1;

	private static String previousFileName;

	private static String currentFileName;

	private static BufferedReader in;

	private static File currentFile;

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
		System.out.println("\t" + hashGenerator.getHash("fibonacci.java", 1));
		System.out.println("\t" + hashGenerator.getHash("fibonacci.java", 3));
		System.out.println("\t" + hashGenerator.getHash("fibonacci.java", 3));
		System.out.println("\t" + hashGenerator.getHash("fibonacci.java", 14));
		//
		// System.out.println("\t" + hashGenerator.getHash("Parsing.java", 3));
		// System.out.println("\t" + hashGenerator.getHash("Parsing.java", 3));
		// System.out.println("\t" + hashGenerator.getHash("Parsing.java", 7));

	}

	public Long getHash(String fileName, int lineNumber) {

		currentFileName = fileName;

		try {

			if ((currentFile == null)
					|| (!previousFileName.equals(currentFileName))) {

				// System.out.println("File :" + currentFileName);
				currentFile = new File(currentFileName);
				previousFileName = currentFileName;
				previousLine = -1;
				// valueDown.clear();
				// valueUp.clear();
			}

			if (previousLine == -1) {
				previousLine = lineNumber;
			} else if ((previousFileName).equals(currentFileName)
					&& (previousLine == lineNumber)) {
				return hashValue;
			} else {
				previousLine = lineNumber;
			}

			in = new BufferedReader(new FileReader(currentFile));
			String holder = in.readLine();
			// while (holder != null) {
			//
			// String tempHolder[] = holder.split("\\s+");
			// StringBuffer result = new StringBuffer();
			//
			// if (tempHolder.length > 0) {
			// for (int i = 0; i < tempHolder.length; i++) {
			// result.append(tempHolder[i]);
			// }
			// }
			//
			// holder = result.toString();
			//
			// valueUp.add(holder);
			// holder = in.readLine();
			// }

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
				if ((!"LAST".equals(valueDown)) && (lineCounter > lineNumber)) {
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

			// System.out.println("\tValue up :" + valueUp);
			// System.out.println("\tValue down :" + valueDown);
			int hashUp = valueUp.hashCode();
			int hashDown = valueDown.hashCode();

			hashValue = (((long) hashDown) << 32) + hashUp;
			return hashValue;

		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, "The file " + fileName + " was not found."
					+ e);
		} catch (IOException e) {
			log.log(Level.SEVERE, "I/O exception while hash generation." + e);
		}

		return null;

	}

	public Long getHash(File originalFile, int lineNumber) {

		try {
			in = new BufferedReader(new FileReader(currentFile));
			String holder = in.readLine();
			// while (holder != null) {
			//
			// String tempHolder[] = holder.split("\\s+");
			// StringBuffer result = new StringBuffer();
			//
			// if (tempHolder.length > 0) {
			// for (int i = 0; i < tempHolder.length; i++) {
			// result.append(tempHolder[i]);
			// }
			// }
			//
			// holder = result.toString();
			//
			// valueUp.add(holder);
			// holder = in.readLine();
			// }

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
				if ((!"LAST".equals(valueDown)) && (lineCounter > lineNumber)) {
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

			// System.out.println("\tValue up :" + valueUp);
			// System.out.println("\tValue down :" + valueDown);
			int hashUp = valueUp.hashCode();
			int hashDown = valueDown.hashCode();

			hashValue = (((long) hashDown) << 32) + hashUp;
			return hashValue;

		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, "The file " + originalFile.getAbsolutePath()
					+ " was not found." + e);
		} catch (IOException e) {
			log.log(Level.SEVERE, "I/O exception while hash generation." + e);
		}

		return null;

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
				lineHashMap.put((Integer) lineNumber, hashValue);
			}

			countFileAccess++;

		}

		// System.out.println("File Access from the new way : " +
		// countFileAccess);

	}
}
