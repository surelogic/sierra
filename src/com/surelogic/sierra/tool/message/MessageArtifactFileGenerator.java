package com.surelogic.sierra.tool.message;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.analyzer.ArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.DefaultArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.MetricBuilder;
import com.surelogic.sierra.tool.config.Config;

//TODO implement error generation
public class MessageArtifactFileGenerator extends DefaultArtifactGenerator
		implements ArtifactGenerator {

	private static final String XML_START = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
	private static final String TOOL_OUTPUT_START = "<toolOutput>";
	private static final String TOOL_OUTPUT_END = "</toolOutput>";

	private static final Logger log = SLLogger.getLogger("sierra");
	private static final String RUN_START = "<run>";
	private static final String RUN_END = "</run>";
	private static final String UID_START = "<uid>";
	private static final String UID_END = "</uid>";

	private final MessageWarehouse mw;
	private ArtifactBuilderAdapter artifactAdapter;

	private FileOutputStream artOut;

	private String parsedFile;
	private Config config;

	public MessageArtifactFileGenerator(String parsedFile, Config config) {
		this.mw = MessageWarehouse.getInstance();
		this.parsedFile = parsedFile;
		this.config = config;

		try {

			artOut = new FileOutputStream(new File(parsedFile), true);
			FileWriter finalFile = new FileWriter(new File(parsedFile));
			finalFile.write(XML_START);
			finalFile.write('\n');
			finalFile.write(RUN_START);
			finalFile.write('\n');
			finalFile.write(UID_START);
			finalFile.write(UUID.randomUUID().toString());
			finalFile.write(UID_END);
			finalFile.write('\n');
			finalFile.write(TOOL_OUTPUT_START);
			finalFile.flush();
			artifactAdapter = new ArtifactBuilderAdapter();
		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, "Unable to locate the file" + e);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Unable to read/write from/to the file" + e);
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
	public void finished() {
		FileWriter finalFile;

		try {
			finalFile = new FileWriter(new File(parsedFile), true);
			finalFile.write(TOOL_OUTPUT_END);
			finalFile.flush();

			MessageWarehouse.getInstance().writeConfig(config, artOut);

			finalFile.write(RUN_END);
			finalFile.flush();
			finalFile.close();
			artOut.close();
		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, "Unable to locate the file" + e);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Unable to read/write from/to the file" + e);
		}

		// ToolOutput to = new ToolOutput();
		// to.setArtifact(getArtifacts());
		// MessageWarehouse.getInstance().writeToolOutput(to, dest);
	}

	private class MessageMetricBuilder implements MetricBuilder {

		private String path;
		private String clazz;
		private String pakkage;
		private int linesOfCode;

		public void build() {
			ClassMetric metric = new ClassMetric();
			metric.setClassName(clazz);
			metric.setPackageName(pakkage);
			metric.setLinesOfCode(linesOfCode);
			metric.setPath(path);
			mw.writeClassMetric(metric, artOut);
		}

		public MetricBuilder className(String name) {
			this.clazz = name;
			return this;
		}

		public MetricBuilder lineOfCode(int line) {
			this.linesOfCode = line;
			return this;
		}

		public MetricBuilder packageName(String name) {
			this.pakkage = name;
			return this;
		}

		public MetricBuilder path(String path) {
			this.path = path;
			return this;
		}

	}

	private class ArtifactBuilderAdapter implements ArtifactBuilder {
		private final Artifact.Builder artBuilder;

		ArtifactBuilderAdapter() {
			artBuilder = new Artifact.Builder();
		}

		public void build() {
			Artifact a = artBuilder.build();
			MessageWarehouse.getInstance().writeArtifact(a, artOut);
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

			public SourceLocationBuilder path(String path) {
				sourceBuilder.path(path);
				return this;
			}

			public SourceLocationBuilder type(IdentifierType type) {
				sourceBuilder.type(type);
				return this;
			}

		}
	}

}
