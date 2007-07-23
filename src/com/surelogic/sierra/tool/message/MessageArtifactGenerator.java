package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.tool.analyzer.ArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.DefaultArtifactGenerator;

public class MessageArtifactGenerator extends DefaultArtifactGenerator
		implements ArtifactGenerator {

	private ArtifactBuilderAdapter artifactAdapter = new ArtifactBuilderAdapter();

	@Override
	public ArtifactBuilder artifact() {
		return artifactAdapter;
	}

	public List<Artifact> getArtifacts() {
		return artifactAdapter.artifacts;
	}

	public void write(String dest) {
		ToolOutput to = new ToolOutput();
		to.setArtifacts(getArtifacts());
		MessageWarehouse.getInstance().writeToolOutput(to, dest);
	}

	private static class ArtifactBuilderAdapter implements ArtifactBuilder {
		private Artifact.Builder builder;

		private List<Artifact> artifacts = new ArrayList<Artifact>();

		public ArtifactBuilderAdapter() {
			builder = new Artifact.Builder();
		}

		public void build() {
			artifacts.add(builder.build());
		}

		public ArtifactBuilder findingType(String tool, String mnemonic) {
			builder.findingType(tool, mnemonic);
			return this;
		}

		public ArtifactBuilder message(String message) {
			builder.message(message);
			return this;
		}

		public SourceLocationBuilder primarySourceLocation() {
			return new SourceLocationAdapter(true);
		}

		public ArtifactBuilder priority(Priority priority) {
			builder.priority(priority);
			return this;
		}

		public ArtifactBuilder severity(Severity severity) {
			builder.severity(severity);
			return this;
		}

		public SourceLocationBuilder sourceLocation() {
			return new SourceLocationAdapter(false);
		}

		private class SourceLocationAdapter implements SourceLocationBuilder {
			private final boolean isPrimary;

			SourceLocation.Builder sBuilder;

			public SourceLocationAdapter(boolean isPrimary) {
				this.isPrimary = isPrimary;
				sBuilder = new SourceLocation.Builder();
			}

			public void build() {
				if (isPrimary) {
					builder.primarySourceLocation(sBuilder.build());
				} else {
					builder.sourceLocation(sBuilder.build());
				}
			}

			public SourceLocationBuilder className(String className) {
				sBuilder.className(className);
				return this;
			}

			public SourceLocationBuilder endLine(int line) {
				sBuilder.endLine(line);
				return this;
			}

			public SourceLocationBuilder hash(String hash) {
				sBuilder.hash(hash);
				return this;
			}

			public SourceLocationBuilder identifier(String name) {
				sBuilder.identifier(name);
				return this;
			}

			public SourceLocationBuilder lineOfCode(int line) {
				sBuilder.lineOfCode(line);
				return this;
			}

			public SourceLocationBuilder packageName(String packageName) {
				sBuilder.packageName(packageName);
				return this;
			}

			public SourceLocationBuilder path(String path) {
				sBuilder.path(path);
				return this;
			}

			public SourceLocationBuilder type(IdentifierType type) {
				sBuilder.type(type);
				return this;
			}

		}
	}

}
