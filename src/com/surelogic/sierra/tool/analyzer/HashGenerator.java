package com.surelogic.sierra.tool.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * The instance hash generator calculates the 30 alphanumeric characters before
 * and after the given line number
 * 
 * @author Tanmay.Sinha
 * 
 */
public class HashGenerator {

	/**
	 * For testing
	 * 
	 * @param args
	 */
	public static void main(String args[]) {

		System.out.println(new HashGenerator().getHash("fibonacci.java", 8));

	}

	public String getHash(String fileName, int lineNumber) {

		String hash = "CANNOT_CALCULATE";

		try {

			File originalFile = new File(fileName);

			BufferedReader in = new BufferedReader(new FileReader(originalFile));

			// Calculate the number of lines
			int count = 0;
			while (in.readLine() != null) {
				count++;
			}
			// System.out.println("No. of lines :" + count + "\n");
			in.close();

			if (lineNumber > count || lineNumber < 0) {
				System.err.println("Incorrect line number");
				return hash;
			} else {
				in = new BufferedReader(new FileReader(originalFile));

				String valueDown = "";
				String valueUp = "";

				int lineCounter = 0;

				if (lineNumber == count) {
					valueDown = "LAST";
				} else if (lineNumber == 1) {
					valueUp = "FIRST";
				} else {
					while (lineCounter < count) {
						String holder = in.readLine();
						lineCounter++;

						String tempHolder[] = holder.split("\\s+");
						StringBuffer result = new StringBuffer();

						if (tempHolder.length > 0) {
							for (int i = 0; i < tempHolder.length; i++) {
								result.append(tempHolder[i]);
							}
						}

						holder = result.toString();

						if (lineCounter < lineNumber) {
							valueUp += holder;
							if (valueUp.length() > 30) {
								int extraChars = valueUp.length() - 30;
								valueUp = valueUp.substring(extraChars, valueUp.length());
							}
						}
						if (lineCounter > lineNumber) {
							if (valueDown.length() < 30) {
								valueDown += holder;
								if (valueDown.length() > 30) {
									valueDown = valueDown.substring(0, 30);
								}
							}
						}
					}
				}

				in.close();

				int hashUp = valueUp.hashCode();
				int hashDown = valueDown.hashCode();

				// System.out.println("Up :" + valueUp + " " + valueUp.length()
				// + " HS :" + hashUp);
				// System.out.println("Down :" + valueDown + " "
				// + valueDown.length() + " HS :" + hashDown);

				return String.valueOf(hashDown + hashUp);

			}

		} catch (FileNotFoundException e) {
			System.err.println("The file " + fileName + " was not found.");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return hash;

	}
}
