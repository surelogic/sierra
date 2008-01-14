package com.surelogic.sierra.tool.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.message.Artifact;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.ClassMetric;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.Error;
import com.surelogic.sierra.tool.message.IdentifierType;
import com.surelogic.sierra.tool.message.MessageWarehouse;
import com.surelogic.sierra.tool.message.MetricBuilder;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;
import com.surelogic.sierra.tool.message.SourceLocation;

/**
 * The scan document generator
 * 
 * This class generates the run document. It generates 3 separate temporary
 * files and stores the artifacts, errors and config in them. It finally
 * combines all of them with proper xml tags and generates a run doucment.
 * 
 * @author Tanmay.Sinha
 * 
 */
public class MessageArtifactFileGenerator extends DefaultArtifactGenerator
		implements ArtifactGenerator {

	private static final String XML_START = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
	private static final String TOOL_OUTPUT_START = "<toolOutput>";
	private static final String TOOL_OUTPUT_END = "</toolOutput>";

	private static final Logger log = SLLogger.getLogger("sierra");
	private static final String RUN_START = "<scan>";
	private static final String RUN_END = "</scan>";
	private static final String UID_START = "<uid>";
	private static final String UID_END = "</uid>";
	private static final String ARTIFACTS_START = "<artifacts>";
	private static final String ARTIFACTS_END = "</artifacts>";
	private static final String ERROR_START = "<errors>";
	private static final String ERROR_END = "</errors>";
	private static final String ENCODING = "UTF-8";

	private ArtifactBuilderAdapter artifactAdapter;

	private FileOutputStream artOut;

	private File parsedFile;
	private Config config;
	private File artifactsHolder;
	private File metricsFile;
	private File errorsHolder;
	private FileOutputStream errOut;

	// private final Set<String> files;

	public MessageArtifactFileGenerator(File parsedFile, Config config) {
		this.parsedFile = parsedFile;
		this.config = config;

		// files = new HashSet<String>();

		try {

			artifactsHolder = File.createTempFile("artifacts", "tmp");
			artOut = new FileOutputStream(artifactsHolder, true);
			artifactAdapter = new ArtifactBuilderAdapter();

			errorsHolder = File.createTempFile("errors", "tmp");
			errOut = new FileOutputStream(errorsHolder, true);

		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, "Unable to locate the file", e);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Unable to read/write from/to the file", e);
		}

	}

	// public void addFile(String fileName) {
	// files.add(fileName);
	// }

	public void writeMetrics(File file) {
		this.metricsFile = file;

	}

	@Override
	public ArtifactBuilder artifact() {
		return artifactAdapter;
	}

	@Override
	public MetricBuilder metric() {
		return new MessageMetricBuilder();
	}

	@Override
	public ErrorBuilder error() {
		return new MessageErrorBuilder();
	}

	@Override
	public void finished() {
		try {
			OutputStream stream = new FileOutputStream(parsedFile);
			stream = new GZIPOutputStream(stream, 4096);
			OutputStreamWriter osw = new OutputStreamWriter(stream, ENCODING);
			PrintWriter finalFile = new PrintWriter(osw);
			finalFile.write(XML_START);
			finalFile.write('\n');
			finalFile.write(RUN_START);
			finalFile.write('\n');
			finalFile.write(UID_START);
			finalFile.write(UUID.randomUUID().toString());
			finalFile.write(UID_END);
			finalFile.write('\n');
			finalFile.write(TOOL_OUTPUT_START);

			BufferedReader in;
			String line = null;

			if (metricsFile != null && metricsFile.exists()) {
				in = new BufferedReader(new FileReader(metricsFile));
				while ((line = in.readLine()) != null) {
					finalFile.write(line);
					finalFile.write("\n");
				}
				in.close();
				finalFile.flush();
			}

			if (artifactsHolder.exists()) {
				finalFile.write(ARTIFACTS_START);
				in = new BufferedReader(new FileReader(artifactsHolder));
				line = null;
				while ((line = in.readLine()) != null) {
					finalFile.write(line);
					finalFile.write("\n");
				}
				in.close();
				finalFile.flush();
				finalFile.write(ARTIFACTS_END);
			}

			if (artifactsHolder.exists()) {
				finalFile.write(ERROR_START);
				in = new BufferedReader(new FileReader(errorsHolder));
				line = null;
				while ((line = in.readLine()) != null) {
					finalFile.write(line);
					finalFile.write("\n");
				}
				in.close();
				finalFile.flush();
				finalFile.write(ERROR_END);
			}
			finalFile.write(TOOL_OUTPUT_END);

			File configOutput = File.createTempFile("config", "tmp");
			FileOutputStream fos = new FileOutputStream(configOutput);
			MessageWarehouse.getInstance().writeConfig(config, fos);
      fos.close();
			
			in = new BufferedReader(new FileReader(configOutput));
			line = null;
			while ((line = in.readLine()) != null) {
				finalFile.write(line);
				finalFile.write("\n");
			}
			in.close();
			finalFile.write(RUN_END);
			finalFile.flush();
			finalFile.close();
			osw.close();
			stream.close();

			// // Create a buffer for reading the files
			// byte[] buf = new byte[1024];
			// try {
			// // Create the ZIP file
			// String outFilename = "C:/outfile.zip";
			// ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
			// outFilename));

			// for (String s : files) {

			// FileInputStream zipIn = new FileInputStream(s);
			//
			// // Add ZIP entry to output stream.
			// out.putNextEntry(new ZipEntry(s));
			//
			// // Transfer bytes from the file to the ZIP file
			// int len;
			// while ((len = zipIn.read(buf)) > 0) {
			// out.write(buf, 0, len);
			// }
			//
			// // Complete the entry
			// out.closeEntry();
			// zipIn.close();
			// System.out.println(s);
			// }
			//
			// // Complete the ZIP file
			// out.close();
			// } catch (IOException e) {
			// // Testing
			// }

			// Delete temp files
			errOut.close();
			artOut.close();
			fos.close();

			errorsHolder.delete();
			artifactsHolder.delete();
			configOutput.delete();

		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, "Unable to locate the file", e);
			throw new RuntimeException(e.getMessage());
		} catch (IOException e) {
			log.log(Level.SEVERE, "Unable to read/write from/to the file", e);
			throw new RuntimeException(e.getMessage());
		} catch (Exception e) {
			log.log(Level.SEVERE,
					"Error when trying to generate the scan document", e);
			throw new RuntimeException(e.getMessage());
		}

	}

	private class MessageErrorBuilder implements ErrorBuilder {

		private String message;
		private String tool;
		private final MessageWarehouse mw = MessageWarehouse.getInstance();

		public void build() {

			Error error = new Error();
			error.setMessage(message);
			error.setTool(tool);
			mw.writeError(error, errOut);
		}

		public ErrorBuilder message(String message) {
			this.message = message;
			return this;
		}

		public ErrorBuilder tool(String tool) {
			this.tool = tool;
			return this;
		}

	}

	private class MessageMetricBuilder implements MetricBuilder {

		private String clazz;
		private String pakkage;
		private int linesOfCode;
		private final MessageWarehouse mw = MessageWarehouse.getInstance();

		public void build() {
			ClassMetric metric = new ClassMetric();
			metric.setName(clazz);
			metric.setPackage(pakkage);
			metric.setLoc(linesOfCode);
			mw.writeClassMetric(metric, artOut);
		}

		public MetricBuilder compilation(String name) {
			this.clazz = name;
			return this;
		}

		public MetricBuilder linesOfCode(int line) {
			this.linesOfCode = line;
			return this;
		}

		public MetricBuilder packageName(String name) {
			this.pakkage = name;
			return this;
		}

	}

	private class ArtifactBuilderAdapter implements ArtifactBuilder {
		private final Artifact.Builder artBuilder;
		private final MessageWarehouse mw = MessageWarehouse.getInstance();

		ArtifactBuilderAdapter() {
			artBuilder = new Artifact.Builder();
		}

		public void build() {
			Artifact a = artBuilder.build();
			mw.writeArtifact(a, artOut);
		}

		public ArtifactBuilder findingType(String tool, String version,
				String mnemonic) {
			artBuilder.findingType(tool, version, mnemonic);
			return this;
		}

		public ArtifactBuilder message(String message) {
			artBuilder.message(message);
			return this;
		}

		public SourceLocationBuilder primarySourceLocation() {
			return new SourceLocationAdapter(true);
		}

		public ArtifactBuilder priority(Priority priority) {
			artBuilder.priority(priority);
			return this;
		}

		public ArtifactBuilder severity(Severity severity) {
			artBuilder.severity(severity);
			return this;
		}

		public SourceLocationBuilder sourceLocation() {
			return new SourceLocationAdapter(false);
		}

		private class SourceLocationAdapter implements SourceLocationBuilder {
			private final boolean isPrimary;
			private final SourceLocation.Builder sourceBuilder;

			public SourceLocationAdapter(boolean isPrimary) {
				this.isPrimary = isPrimary;
				this.sourceBuilder = new SourceLocation.Builder();
			}

			public void build() {
				if (isPrimary) {
					artBuilder.primarySourceLocation(sourceBuilder.build());
				} else {
					artBuilder.sourceLocation(sourceBuilder.build());
				}
			}

			public SourceLocationBuilder className(String className) {
				sourceBuilder.className(className);
				return this;
			}

			public SourceLocationBuilder endLine(int line) {
				sourceBuilder.endLine(line);
				return this;
			}

			public SourceLocationBuilder hash(Long hash) {
				sourceBuilder.hash(hash);
				return this;
			}

			public SourceLocationBuilder identifier(String name) {
				sourceBuilder.identifier(name);
				return this;
			}

			public SourceLocationBuilder lineOfCode(int line) {
				sourceBuilder.lineOfCode(line);
				return this;
			}

			public SourceLocationBuilder packageName(String packageName) {
				sourceBuilder.packageName(packageName);
				return this;
			}

			public SourceLocationBuilder type(IdentifierType type) {
				sourceBuilder.type(type);
				return this;
			}

			public SourceLocationBuilder compilation(String compilation) {
				sourceBuilder.compilation(compilation);
				return null;
			}

		}
	}

}
