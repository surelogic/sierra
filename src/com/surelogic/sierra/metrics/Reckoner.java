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
import java.util.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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
 * @author Edwin.Chan
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
		final List<File> targets = new ArrayList<File>();

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

			if (!targets.isEmpty()) {
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

    List<Metrics> metricsList = computeMetrics(targets, new NullProgressMonitor());		
		Map<Metrics,Metrics> metrics = new HashMap<Metrics,Metrics>();		
		for (Metrics m : metricsList) {
		  Metrics removed = metrics.put(m, m);
		  if (removed != null) {
		    // m is already "in"
		    final String msg = "Ignoring due to 'duplicate': "+removed.getPath();
		    System.err.println(msg);
		    f_mrg.error(msg);
		  }
		}
		for (Metrics m : metrics.keySet()) {    
    //for (Metrics m : metricsList) {
			f_mrg.write(m);
		}
		f_mrg.close();
	}

  public List<Metrics> computeMetrics(List<File> targets, IProgressMonitor mon) {
    List<Metrics> metricsList = new ArrayList<Metrics>();
    mon.beginTask("Reckoner", targets.size());
    
		for (File file : targets) {
		  mon.subTask("Computing metrics for "+file.getName());
			if (file.exists() && file.isDirectory()) {
				Set<String> sourceFiles = getJavaFiles(file);
				for (String s : sourceFiles) {
					final File holder = new File(s);
					final Metrics metrics = countLOC(holder);
					if (metrics != null) {
						metricsList.add(metrics);
						mon.subTask("Done with "+metrics.getPath());
					}
				}
			} else if (file.exists() && !file.isDirectory()) {
				if (file.getName().endsWith(".java")) {
					final Metrics metrics = countLOC(file);
          if (metrics != null) {
            metricsList.add(metrics);
            mon.subTask("Done with "+metrics.getPath());
          }
				}
			}
			mon.worked(1);
		}
		mon.done();
    return metricsList;
  }

	public Metrics countLOC(File target) {
		try {			
			StringBuffer buffer = readSourcefile(target);

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

	private StringBuffer readSourcefile(File target) throws FileNotFoundException, IOException {
		BufferedReader in = new BufferedReader(new FileReader(target));
		long length = target.length();
		StringBuffer buffer = new StringBuffer(length > 65536 ? 65536 : (int) length);
		char[] buf = new char[4096];
		int len;
		while ((len = in.read(buf)) > 0) {
		  buffer.append(buf, 0, len);
		}
		in.close();
		return buffer;
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
