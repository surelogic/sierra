package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.Collection;

import com.surelogic.sierra.tool.analyzer.ArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.DefaultArtifactGenerator;

/**
 * Generator for use in building an in-memory message.
 * 
 * @author nathan
 * 
 */
class PublishMessageArtifactGenerator extends DefaultArtifactGenerator
		implements ArtifactGenerator {

	private final Run r;

	private ArtifactBuilderAdapter artifactAdapter;

	public Run getRun() {
		return r;
	}

	PublishMessageArtifactGenerator(Run r) {
		this.r = r;
		ToolOutput to = new ToolOutput();
		to.getArtifacts().setArtifact(new ArrayList<Artifact>());
		to.getErrors().setErrors(new ArrayList<Error>());
		r.setToolOutput(to);
		artifactAdapter = new ArtifactBuilderAdapter(r.getToolOutput()
				.getArtifacts().getArtifact());
	}

	@Override
	public ArtifactBuilder artifact() {
		return artifactAdapter;
	}

	private static class ArtifactBuilderAdapter implements ArtifactBuilder {
		private final Artifact.Builder artBuilder;
		private final Collection<Artifact> artifacts;

		public ArtifactBuilderAdapter(Collection<Artifact> artifacts) {
			artBuilder = new Artifact.Builder();
			this.artifacts = artifacts;
		}

		public void build() {

			Artifact a = artBuilder.build();
			artifacts.add(a);
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

	@Override
	public void finished() {
		TigerServiceClient client = new TigerServiceClient();
		client.getTigerServicePort().publishRun(r);
	}

}
