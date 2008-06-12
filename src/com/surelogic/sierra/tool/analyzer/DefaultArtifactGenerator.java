package com.surelogic.sierra.tool.analyzer;

import java.io.File;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.AssuranceType;
import com.surelogic.sierra.tool.message.IdentifierType;
import com.surelogic.sierra.tool.message.MetricBuilder;
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

		public ArtifactBuilder findingType(String tool, String version,
				String mnemonic) {

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

		public ArtifactBuilder scanNumber(int number) {
			return this;
		}

		public ArtifactBuilder assurance(AssuranceType type) {
			return this;
		}

	}

	public static class DefaultSourceBuilder implements SourceLocationBuilder {

		public void build() {
			// Nothing to do
		}

		public SourceLocationBuilder compilation(String compilation) {

			return null;
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

	public MetricBuilder metric() {
		return new DefaultMetricBuilder();
	}

	public static class DefaultMetricBuilder implements MetricBuilder {

		public void build() {
			// Nothing to do
		}

		public MetricBuilder compilation(String name) {
			return this;
		}

		public MetricBuilder linesOfCode(int line) {
			return this;
		}

		public MetricBuilder packageName(String name) {
			return this;
		}

		public MetricBuilder path(String path) {
			return this;
		}

	}

	public void finished(SLProgressMonitor monitor) {
		// Nothing to do
	}

	public void writeMetrics(File absoluteFile) {
		// Nothing to do
	}

	public void rollback() {
		// Nothing to do
	}

	public void relation(int parentNumber, int childNumber, String type) {
		// Nothing to do
	}
}
