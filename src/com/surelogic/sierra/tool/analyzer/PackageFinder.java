/**
 * This is a simple class that finds what package a java file is part of given a File object.
 * This also serves as a test class for the unit test
 * 
 * Testing:
 * package com.wrong.company.name.package;
 package edu.wrong.package.name;
 package
 org.
 wrong.
 company;
 */
package com.surelogic.sierra.tool.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.surelogic.sierra.tool.SierraToolConstants;

public class PackageFinder {

	private static final String PACKAGE = "package";
	private static final PackageFinder INSTANCE = new PackageFinder();

	public static PackageFinder getInstance() {
		return INSTANCE;
	}

	public String getPackage(final File target) {
		BufferedReader in = null;
		String pkg = null;
		
		try {
			in = new BufferedReader(new FileReader(target));
			
			// Use to double-check the result against the filesystem location
			// Convert the path
			final String stringToReplace = Matcher.quoteReplacement(File.separator);
			final String pkgPath = target.getCanonicalPath().replace(stringToReplace, ".");
			System.out.println("Package path: " + pkgPath);
			

			final StringBuffer buffer = new StringBuffer();
			String line = null;
			//Read in the file...simple...though not elegant
			while (null != (line = in.readLine())) {
				buffer.append(line);
				buffer.append("\n");
			}

			// Regex: \\s == whitespace (including newline) \\w == word character
			Pattern pattern = Pattern.compile(
					"^\\s*package\\s*((\\w+\\s*?\\.\\s*?)*\\w+)\\s*?;",
					Pattern.MULTILINE | Pattern.DOTALL);

			final Matcher matcher = pattern.matcher(buffer.toString());
			int index = 0;
			while (matcher.find(index)) {
				System.out.println(matcher.group(1));
				pkg = matcher.group(1);
				pkg = pkg.replaceAll("\\s", ""); // get rid of any newlines
													// or whatnot
				System.out.println("Package: " + pkg);
				//If the package we found matches the path, we're done, otherwise continue
    			if (pkgPath.contains(pkg)) {
    				break;
    			}
    			else{
    				index = matcher.end();
    			}
			}

			if (pkg == null) {
				// Set to the default package
				pkg = SierraToolConstants.DEFAULT_PACKAGE;
				System.out.println("Default");
			}

		} catch (IOException e) {
			// Handle exception
			e.printStackTrace();
			//Set to the default package
			pkg = SierraToolConstants.DEFAULT_PACKAGE;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// can't do anything about this
				}
			}
		}

		return pkg;
	}
}
