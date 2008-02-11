package com.surelogic.sierra.tool.analyzer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
	
	private static final String TOO_FAR = "TOOFAR";

	//private int countFileAccess = 0;

	private String cachedFileName;

	private List<String> cachedFileLines;

	private long lastHashValue;

	private int lastHashLine = -1;

	private static class Singleton {
		static final HashGenerator hashGenerator = new HashGenerator();
	}

	HashGenerator() {
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
		try {
			// adjust line number to be zero indexed
			if (lineNumber > 0) {
				lineNumber--;
			}
			if (cachedFileLines == null) {
			  cachedFileName = null; 
			}
			if (cachedFileName == null || !cachedFileName.equals(fileName)) {
				cachedFileName = fileName;
				cachedFileLines = buildCachedLines(fileName);
			} else if (lastHashLine == lineNumber) {
				return lastHashValue;
			}
	     
			if (lineNumber >= cachedFileLines.size()) {
			  log.severe("line# too big: "+lineNumber+" >= "+cachedFileLines.size());
			  cachedFileLines = buildCachedLines(fileName);
			}			
			lastHashLine = lineNumber;

			String valueUp = getChunkBefore(cachedFileLines, lineNumber, 30);
			String valueDown = getChunkAfter(cachedFileLines, lineNumber, 30);

			int hashUp = valueUp.hashCode();
			int hashDown = valueDown.hashCode();

			lastHashValue = (((long) hashDown) << 32) + hashUp;
			return lastHashValue;
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
		for(Map.Entry<String, Map<Integer, Long>> entry : hashHolder.entrySet()) {
			String fileName = entry.getKey();
			Map<Integer, Long> lineHashMap = entry.getValue();
			Set<Integer> lineNumbers = lineHashMap.keySet();
			Iterator<Integer> lineNumberIterator = lineNumbers.iterator();

			while (lineNumberIterator.hasNext()) {
				int lineNumber = lineNumberIterator.next();
				Long hashValue = getHash(fileName, lineNumber);
				lineHashMap.put(lineNumber, hashValue);
			}

			//countFileAccess++;

		}

		// System.out.println("File Access from the new way : " +
		// countFileAccess);

	}

	private List<String> buildCachedLines(String fileName) throws IOException {
	  if (fileName == null) {
	    throw new IllegalArgumentException("Null filename");
	  }
		List<String> cachedLines = new ArrayList<String>();
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		try {
			String inLine = in.readLine();
			while (inLine != null) {
				String[] lineElements = inLine.split("\\s+");
				// TODO replace split with replaceAll?
				StringBuilder cachedLine = new StringBuilder();
				for (String element : lineElements) {
					cachedLine.append(element);
				}
				cachedLines.add(cachedLine.toString());

				inLine = in.readLine();
			}
		} finally {
			in.close();
		}

		return cachedLines;
	}

	private String getChunkBefore(List<String> cachedLines, int lineNumber,
			int maxChunkSize) {
		int chunkLine = lineNumber;
		if (chunkLine < 0) {
			return FIRST;
		}
    if (chunkLine >= cachedLines.size()) {
      log.severe("line# too big: "+chunkLine+" >= "+cachedLines.size());
      return TOO_FAR;
    }
		StringBuilder chunkBuf = new StringBuilder();
		while (chunkLine >= 0 && chunkBuf.length() < maxChunkSize) {
			chunkBuf.insert(0, cachedLines.get(chunkLine));
			chunkLine--;
		}

		if (chunkBuf.length() > maxChunkSize) {
			int len = chunkBuf.length();
			return chunkBuf.substring(len - maxChunkSize, len);
		}
		return chunkBuf.toString();
	}

	private String getChunkAfter(List<String> cachedLines, int lineNumber,
			int maxChunkSize) {
		int chunkLine = lineNumber + 1;
		if (chunkLine >= cachedLines.size()) {
			return LAST;
		}

		StringBuilder chunkBuf = new StringBuilder();
		while (chunkLine < cachedLines.size()
				&& chunkBuf.length() < maxChunkSize) {
			chunkBuf.append(cachedLines.get(chunkLine));
			chunkLine++;
		}

		if (chunkBuf.length() > maxChunkSize) {
			return chunkBuf.substring(0, maxChunkSize);
		}
		return chunkBuf.toString();
	}

}
