package com.surelogic.sierra.metrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.surelogic.sierra.metrics.analysis.LOCASTVisitor;
import com.surelogic.sierra.metrics.model.Metrics;
import com.surelogic.sierra.metrics.output.MetricsResultsGenerator;

/**
 * Tool to calculate lines of code
 * 
 * @author Tanmay.Sinha
 */
public class Reckoner {

	private static final String HELP_MESSAGE = "java -jar reckoner.jar [options] -target <name>";

	/**
	 * The command line usage of the LOC counter
	 * 
	 * @param args
	 */
	public static void main(String args[]) {

		String outputFile = null;
		List<File> targets = null;

		final Options options = new Options();
		options.addOption("target", true,
				"path of the file/directory to analyze (absolute path)");
		options.addOption("help", false, "print this message");
		options.addOption("outputFile", true,
				"[optional] name of the output file (absolute path)");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.toString());
			printCommandLineHelp(options);
			return;
		}

		if (cmd.hasOption("help")) {
			printCommandLineHelp(options);
		}

		else {
			if (cmd.hasOption("target")) {
				String target = cmd.getOptionValue("target");
				List<?> holder = cmd.getArgList();
				targets = new ArrayList<File>();

				if (!"".equals(target)) {
					targets.add(new File(target));
					for (Object o : holder) {
						if (o instanceof String) {
							File fileHolder = new File(o.toString());
							targets.add(fileHolder);
						}
					}
				}

			}

			if (cmd.hasOption("outputFile")) {
				outputFile = cmd.getOptionValue("outputFile");
			}

			if (targets.size() > 0) {
				Reckoner llocc = new Reckoner();
				try {
					llocc.getLogicalLOC(targets, outputFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				printCommandLineHelp(options);
				return;
			}
		}
	}

	private static void printCommandLineHelp(final Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(HELP_MESSAGE, options);
	}

	public void getLogicalLOC(List<File> targets, String outputFile)
			throws IOException {

		List<Metrics> metricsList = new ArrayList<Metrics>();

		PrintWriter w;
		if (outputFile == null) {
			w = new PrintWriter(System.out);
		} else {
			File dataFile = new File(outputFile);
			OutputStream stream = new FileOutputStream(dataFile);
			OutputStreamWriter osw = new OutputStreamWriter(stream,
					MetricsResultsGenerator.ENCODING);
			w = new PrintWriter(osw);
		}
		final MetricsResultsGenerator f_mrg = new MetricsResultsGenerator(w);

		for (File file : targets) {
			if (file.exists() && file.isDirectory()) {
				Set<String> sourceFiles = getJavaFiles(file);
				for (String s : sourceFiles) {
					final File holder = new File(s);
					final Metrics metrics = countLOC(holder);
					if (metrics != null)
						metricsList.add(metrics);
				}
			} else if (file.exists() && !file.isDirectory()) {
				if (file.getName().endsWith(".java")) {
					final Metrics metrics = countLOC(file);
					if (metrics != null)
						metricsList.add(metrics);
				}
			}
		}

		for (Metrics m : metricsList) {
			f_mrg.write(m);
		}
		f_mrg.close();
	}

	private Metrics countLOC(File target) {
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

			LOCASTVisitor visitor = new LOCASTVisitor();
			node.accept(visitor);

			Metrics metrics = visitor.getMetrics();
			String holder = target.getName().substring(0,
					target.getName().length() - 5);
			metrics.setClassName(holder);
			metrics.setPath(target.getAbsolutePath());

			return metrics;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Set<String> getJavaFiles(File file) {
		final Set<String> javaFiles = new HashSet<String>();
		JavaFilter filter = new JavaFilter();
		filterdirs(file, filter, javaFiles);
		return javaFiles;
	}

	private static class JavaFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(".java");
		}
	}

	private static void filterdirs(File root, FilenameFilter filter,
			Set<String> javaFiles) {
		File[] filesHolder = root.listFiles(filter); // can return null
		if (filesHolder != null) {
			for (File f : filesHolder) {
				if (f.isFile())
					javaFiles.add(f.getAbsolutePath());
			}
		}

		filesHolder = root.listFiles(); // can return null
		if (filesHolder != null) {
			for (File f : filesHolder) {
				if (f.isDirectory())
					filterdirs(f, filter, javaFiles);

			}
		}
	}
}
