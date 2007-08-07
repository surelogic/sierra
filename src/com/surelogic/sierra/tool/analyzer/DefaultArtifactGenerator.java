package com.surelogic.sierra.tool.analyzer;

import com.surelogic.sierra.tool.message.IdentifierType;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

public class DefaultArtifactGenerator implements ArtifactGenerator {

	public ArtifactBuilder artifact() {

		return new DefaultArtifactBuilder();
	}

	public static class DefaultArtifactBuilder implements ArtifactBuilder {

		public void build() {
			// Nothing to do
		}

		public ArtifactBuilder findingType(String tool, String mnemonic) {

			return this;
		}

		public ArtifactBuilder message(String message) {

			return this;
		}

		public SourceLocationBuilder primarySourceLocation() {

			return new DefaultSourceBuilder();
		}

		public ArtifactBuilder priority(Priority priority) {

			return this;
		}

		public ArtifactBuilder severity(Severity severity) {

			return this;
		}

		public SourceLocationBuilder sourceLocation() {

			return new DefaultSourceBuilder();
		}

	}

	public static class DefaultSourceBuilder implements SourceLocationBuilder {

		public void build() {
			// Nothing to do
		}

		public SourceLocationBuilder className(String className) {

			return this;
		}

		public SourceLocationBuilder endLine(int line) {

			return this;
		}

		public SourceLocationBuilder hash(Long hash) {

			return this;
		}

		public SourceLocationBuilder identifier(String name) {

			return this;
		}

		public SourceLocationBuilder lineOfCode(int line) {

			return this;
		}

		public SourceLocationBuilder packageName(String packageName) {

			return this;
		}

		public SourceLocationBuilder path(String path) {

			return this;
		}

		public SourceLocationBuilder type(IdentifierType type) {

			return this;
		}

	}

	public ErrorBuilder error() {
		return new DefaultErrorBuilder();
	}

	public static class DefaultErrorBuilder implements ErrorBuilder {

		public void build() {
			// Nothing to do
		}

		public ErrorBuilder message(String message) {
			return this;
		}

		public ErrorBuilder tool(String tool) {
			return this;
		}

	}

	public void finished() {
		// TODO Auto-generated method stub
		
	}

}
