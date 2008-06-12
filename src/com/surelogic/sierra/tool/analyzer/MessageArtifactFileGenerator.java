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
import java.text.MessageFormat;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import com.surelogic.common.JavaConstants;
import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.message.Artifact;
import com.surelogic.sierra.tool.message.AssuranceType;
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
public class MessageArtifactFileGenerator extends DefaultArtifactGenerator {

	private static final String XML_START = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
	private static final String TOOL_OUTPUT_START = "<toolOutput>";
	private static final String TOOL_OUTPUT_END = "</toolOutput>";

	private static final Logger log = SLLogger.getLogger("sierra");
	private static final String RUN_START = "<scan version=\"{0}\" >";
	private static final String RUN_END = "</scan>";
	private static final String UID_START = "<uid>";
	private static final String UID_END = "</uid>";
	private static final String ARTIFACTS_START = "<artifacts>";
	private static final String ARTIFACTS_END = "</artifacts>";
	private static final String ERROR_START = "<errors>";
	private static final String ERROR_END = "</errors>";
	private static final String METRICS_START = "<metrics>";
	private static final String METRICS_END = "</metrics>";
	private static final String ENCODING = "UTF-8";

	private ArtifactBuilderAdapter artifactAdapter;

	private FileOutputStream artOut;

	private final File parsedFile;
	private final Config config;
	private File artifactsHolder;
	private File metricsHolder;
	private File errorsHolder;
	private FileOutputStream errOut;
	private FileOutputStream metricsOut;

	public MessageArtifactFileGenerator(File parsedFile, Config config) {
		this.parsedFile = parsedFile;
		this.config = config;

		try {

			artifactsHolder = File.createTempFile("artifacts", "tmp");
			artOut = new FileOutputStream(artifactsHolder, true);
			artifactAdapter = new ArtifactBuilderAdapter();

			errorsHolder = File.createTempFile("errors", "tmp");
			errOut = new FileOutputStream(errorsHolder, true);

			metricsHolder = File.createTempFile("metrics", "tmp");
			metricsOut = new FileOutputStream(metricsHolder, true);
		} catch (final FileNotFoundException e) {
			log.log(Level.SEVERE, "Unable to locate the file", e);
		} catch (final IOException e) {
			log.log(Level.SEVERE, "Unable to read/write from/to the file", e);
		}

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
	public void finished(SLProgressMonitor monitor) {
		monitor.beginTask("Scan Document", 20);
		try {
			artOut.close();
			errOut.close();
			metricsOut.close();

			monitor.subTask("Writing header");
			OutputStream stream = new FileOutputStream(parsedFile);
			stream = new GZIPOutputStream(stream, 4096);
			final OutputStreamWriter osw = new OutputStreamWriter(stream,
					ENCODING);
			final PrintWriter finalFile = new PrintWriter(osw);
			finalFile.write(XML_START);
			finalFile.write('\n');
			finalFile.write(MessageFormat.format(RUN_START, I18N
					.msg("sierra.teamserver.version")));
			finalFile.write('\n');
			finalFile.write(UID_START);
			finalFile.write(UUID.randomUUID().toString());
			finalFile.write(UID_END);
			finalFile.write('\n');
			finalFile.write(TOOL_OUTPUT_START);
			monitor.worked(1);

			if (metricsHolder.exists()) {
				monitor.subTask("Writing metrics");

				finalFile.write(METRICS_START);
				copyContents(metricsHolder, finalFile);
				finalFile.flush();
				finalFile.write(METRICS_END);
				monitor.worked(1);
				metricsHolder.delete();
			}

			if (artifactsHolder.exists()) {
				monitor.subTask("Writing artifacts");

				finalFile.write(ARTIFACTS_START);
				copyContents(artifactsHolder, finalFile);
				/*
				 * BufferedReader in = new BufferedReader(new
				 * FileReader(artifactsHolder)); String line = null; while
				 * ((line = in.readLine()) != null) { finalFile.write(line);
				 * finalFile.write("\n"); } in.close();
				 */
				finalFile.flush();
				finalFile.write(ARTIFACTS_END);
				monitor.worked(1);
				artifactsHolder.delete();
			}

			if (errorsHolder.exists()) {
				monitor.subTask("Writing errors");

				finalFile.write(ERROR_START);
				copyContents(errorsHolder, finalFile);
				finalFile.write(ERROR_END);
				monitor.worked(1);
				errorsHolder.delete();
			}
			finalFile.write(TOOL_OUTPUT_END);

			monitor.subTask("Writing config");
			final File configOutput = File.createTempFile("config", "tmp");
			final FileOutputStream fos = new FileOutputStream(configOutput);
			MessageWarehouse.getInstance().writeConfig(config, fos);
			fos.close();

			copyContents(configOutput, finalFile);
			configOutput.delete();
			finalFile.write(RUN_END);
			finalFile.flush();
			finalFile.close();
			osw.close();
			stream.close();
			monitor.worked(1);
		} catch (final FileNotFoundException e) {
			log.log(Level.SEVERE, "Unable to locate the file", e);
			throw new RuntimeException(e.getMessage());
		} catch (final IOException e) {
			log.log(Level.SEVERE, "Unable to read/write from/to the file", e);
			throw new RuntimeException(e.getMessage());
		} catch (final Exception e) {
			log.log(Level.SEVERE,
					"Error when trying to generate the scan document", e);
			throw new RuntimeException(e.getMessage());
		}

	}

	private static void copyContents(File file, PrintWriter out)
			throws FileNotFoundException, IOException {
		final BufferedReader in = new BufferedReader(new FileReader(file));
		/*
		 * String line;
		 * 
		 * while ((line = in.readLine()) != null) { out.write(line);
		 * out.write("\n"); } in.close(); out.flush();
		 */
		// Transfer bytes from in to out
		final char[] buf = new char[4096];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
	}

	/*
	 * private static void copyContents(File file, FileChannel dstChannel)
	 * throws FileNotFoundException, IOException { // Create channel on the
	 * source FileChannel srcChannel = new FileInputStream(file).getChannel(); //
	 * Copy file contents from source to destination
	 * dstChannel.transferFrom(srcChannel, 0, srcChannel.size()); // Close the
	 * channel srcChannel.close(); }
	 */

	private class MessageErrorBuilder implements ErrorBuilder {

		private String message;
		private String tool;
		private final MessageWarehouse mw = MessageWarehouse.getInstance();

		public void build() {

			final Error error = new Error();
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
			final ClassMetric metric = new ClassMetric();
			metric.setName(clazz);
			metric.setPackage(pakkage);
			metric.setLoc(linesOfCode);
			mw.writeClassMetric(metric, metricsOut);
		}

		public MetricBuilder compilation(String name) {
			clazz = name;
			return this;
		}

		public MetricBuilder linesOfCode(int line) {
			linesOfCode = line;
			return this;
		}

		public MetricBuilder packageName(String name) {
			if ((name == null) || "".equals(name)) {
				name = JavaConstants.DEFAULT_PACKAGE;
			}
			pakkage = name;
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
			final Artifact a = artBuilder.build();
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

		public ArtifactBuilder scanNumber(int number) {
			artBuilder.scanNumber(number);
			return this;
		}

		public ArtifactBuilder assurance(AssuranceType type) {
			artBuilder.assurance(type);
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
				sourceBuilder = new SourceLocation.Builder();
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

			public SourceLocationBuilder packageName(String name) {
				if ((name == null) || "".equals(name)) {
					name = JavaConstants.DEFAULT_PACKAGE;
				}
				sourceBuilder.packageName(name);
				return this;
			}

			public SourceLocationBuilder type(IdentifierType type) {
				sourceBuilder.type(type);
				return this;
			}

			public SourceLocationBuilder compilation(String compilation) {
				sourceBuilder.compilation(compilation);
				return this;
			}

		}

	}

}
