package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.surelogic.sierra.tool.analyzer.ArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.DefaultArtifactGenerator;

/**
 * Generator for use in building an in-memory message.
 * 
 * @author nathan
 * 
 */
public class MessageArtifactGenerator extends DefaultArtifactGenerator
		implements ArtifactGenerator {

	private ArtifactBuilderAdapter artifactAdapter;

	public MessageArtifactGenerator() {
		artifactAdapter = new ArtifactBuilderAdapter();
	}

	@Override
	public ArtifactBuilder artifact() {
		return artifactAdapter;
	}

	public List<Artifact> getArtifacts() {
		return artifactAdapter.artifacts;
	}

	public List<Error> getErrors() {
		return new LinkedList<Error>();
	}

	private static class ArtifactBuilderAdapter implements ArtifactBuilder {
		private final Artifact.Builder artBuilder;

		private List<Artifact> artifacts = new ArrayList<Artifact>();

		public ArtifactBuilderAdapter() {
			artBuilder = new Artifact.Builder();
		}

		public void build() {

			Artifact a = artBuilder.build();
			artifacts.add(a);
		}

		public ArtifactBuilder findingType(String tool, String mnemonic) {
			artBuilder.findingType(tool, mnemonic);
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
