package com.surelogic.sierra.metrics.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.surelogic.sierra.metrics.model.Metrics;

/**
 * Tool to calculate lines of code
 * 
 * @author Tanmay.Sinha
 * 
 */
public class Main {

	private static HashSet<String> files = new HashSet<String>();
	private static boolean reflectResult = false;

	private static final String HELP_MESSAGE = "java -jar reckoner.jar [options] -target <name>";

	/**
	 * The command line usage of the LOC counter
	 * 
	 * @param args
	 */
	public static void main(String args[]) {

		boolean reflect = false;
		String target = "";
		String outputFile = null;

		Options options = new Options();
		options.addOption("reflect", true,
				"[optional] reflect the file (true or false)");
		options.addOption("target", true,
				"path of the file/directory to analyze (absolute path)");
		options.addOption("help", false, "print this message");
		// options.addOption("xml", false, "[optional] generate xml output");
		options.addOption("outputFile", true,
				"[optional] name of the output file (absolute path)");

		CommandLineParser parser = new PosixParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp(HELP_MESSAGE, options);
			}

			else {
				if (cmd.hasOption("reflect")) {
					String holder = cmd.getOptionValue("reflect");
					reflect = Boolean.parseBoolean(holder);
				}

				if (cmd.hasOption("target")) {
					target = cmd.getOptionValue("target");
				}

				if (cmd.hasOption("outputFile")) {
					outputFile = cmd.getOptionValue("outputFile");
				} else {
					reflectResult = true;
				}

				if (!"".equals(target)) {
					Main llocc = new Main();
					llocc.getLogicalLOC(target, reflect, outputFile);
				} else {
					throw new ParseException(null);
				}
			}

		} catch (ParseException e) {
			System.out.println("Invalid option");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(HELP_MESSAGE, options);
		} catch (FileNotFoundException fnfe) {
			System.out.println("Invalid file");
		} catch (InvalidFileException ife) {
			System.out.println("Non java file in target");
		}

	}

	public void getLogicalLOC(String target, boolean reflect, String outputFile)
			throws FileNotFoundException, InvalidFileException {

		boolean toFile = (outputFile != null);
		long totalCount = 0L;
		if (toFile) {
			MetricsResultsGenerator.startResultsFile(outputFile);
		}

		File file = new File(target);
		if (file.exists() && file.isDirectory()) {
			String sourceFiles[] = getJavaFiles(file);
			for (String s : sourceFiles) {
				File holder = new File(s);
				Metrics metrics = countLOC(holder, reflect);
				if (reflectResult) {
					totalCount += metrics.getLoc();
				}
				if (toFile) {
					MetricsResultsGenerator.writeInFile(metrics);
				}
			}

			if (reflectResult) {
				System.out.println("Total lines of code in the project :"
						+ totalCount);
			}

		} else if (file.exists() && !file.isDirectory()) {
			File holder = new File(target);
			if (holder.getName().endsWith(".java")) {
				Metrics metrics = countLOC(holder, reflect);
				if (toFile) {
					MetricsResultsGenerator.writeInFile(metrics);
				}
			} else {
				if (toFile) {
					MetricsResultsGenerator.endResultsFile();
				}
				throw new InvalidFileException();
			}
		} else {
			if (toFile) {
				MetricsResultsGenerator.endResultsFile();
			}
			throw new FileNotFoundException();
		}

		if (toFile) {
			MetricsResultsGenerator.endResultsFile();
		}

	}

	private Metrics countLOC(File target, boolean reflect) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(target));

			StringBuffer buffer = new StringBuffer();
			String line = null;
			while (null != (line = in.readLine())) {
				buffer.append("\t" + line);
				buffer.append("\n");
			}
			in.close();

			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			final String text = buffer.toString();
			parser.setSource(text.toCharArray());
			final CompilationUnit node = (CompilationUnit) parser
					.createAST(null);

			LOCASTVisitor visitor = new LOCASTVisitor(reflect);
			node.accept(visitor);

			Metrics metrics = visitor.getMetrics();
			String holder = target.getName().substring(0,
					target.getName().length() - 5);
			metrics.setClassName(holder);

			if (reflectResult) {
				System.out.println("For Class \"" + metrics.getClassName()
						+ "\" in package \"" + metrics.getPackageName()
						+ "\" LOC : " + metrics.getLoc());
			}
			return metrics;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;

	}

	private String[] getJavaFiles(File file) {
		JavaFilter filter = new JavaFilter();
		files.clear();
		filterdirs(file, filter);

		if (files != null) {
			// Iterator<String> filesIterator = files.iterator();
			// while (filesIterator.hasNext()) {
			// String holder = filesIterator.next();
			// System.out.println(holder);
			//
			// }

			String listOffiles[] = files.toArray(new String[files.size()]);
			return listOffiles;
		} else {
			return null;
		}

	}

	private static class JavaFilter implements FilenameFilter {

		public boolean accept(File dir, String name) {
			if (name.endsWith(".java")) {
				return true;
			}

			return false;
		}
	}

	private static void filterdirs(File root, FilenameFilter filter) {

		File filesHolder[] = root.listFiles(filter);

		for (File f : filesHolder) {
			files.add(f.getAbsolutePath());
		}

		for (File f : root.listFiles()) {
			if (f.isDirectory()) {
				filterdirs(f, filter);
			}
		}
	}
}
