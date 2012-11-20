package com.surelogic.sierra.tool.analyzer;

import java.io.File;

import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.IdentifierType;
import com.surelogic.sierra.tool.message.MetricBuilder;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

/**
 * Does nothing
 */
class DefaultArtifactGenerator implements ArtifactGenerator {

    @Override
    public ArtifactBuilder artifact() {

        return new DefaultArtifactBuilder();
    }

    public static class DefaultArtifactBuilder implements ArtifactBuilder {

        @Override
        public void build() {
            // Nothing to do
        }

        @Override
        public ArtifactBuilder findingType(String tool, String version,
                String mnemonic) {

            return this;
        }

        @Override
        public ArtifactBuilder message(String message) {

            return this;
        }

        @Override
        public SourceLocationBuilder primarySourceLocation() {

            return new DefaultSourceBuilder();
        }

        @Override
        public ArtifactBuilder priority(Priority priority) {

            return this;
        }

        @Override
        public ArtifactBuilder severity(Severity severity) {

            return this;
        }

        @Override
        public SourceLocationBuilder sourceLocation() {

            return new DefaultSourceBuilder();
        }

        @Override
        public ArtifactBuilder scanNumber(Integer number) {
            return this;
        }

    }

    public static class DefaultSourceBuilder implements SourceLocationBuilder {

        @Override
        public void build() {
            // Nothing to do
        }

        @Override
        public SourceLocationBuilder compilation(String compilation) {

            return null;
        }

        @Override
        public SourceLocationBuilder className(String className) {

            return this;
        }

        @Override
        public SourceLocationBuilder endLine(int line) {

            return this;
        }

        @Override
        public SourceLocationBuilder hash(Long hash) {

            return this;
        }

        @Override
        public SourceLocationBuilder identifier(String name) {

            return this;
        }

        @Override
        public SourceLocationBuilder lineOfCode(int line) {

            return this;
        }

        @Override
        public SourceLocationBuilder packageName(String packageName) {

            return this;
        }

        public SourceLocationBuilder path(String path) {

            return this;
        }

        @Override
        public SourceLocationBuilder type(IdentifierType type) {

            return this;
        }

    }

    @Override
    public ErrorBuilder error() {
        return new DefaultErrorBuilder();
    }

    public static class DefaultErrorBuilder implements ErrorBuilder {

        @Override
        public void build() {
            // Nothing to do
        }

        @Override
        public ErrorBuilder message(String message) {
            return this;
        }

        @Override
        public ErrorBuilder tool(String tool) {
            return this;
        }

    }

    @Override
    public MetricBuilder metric() {
        return new DefaultMetricBuilder();
    }

    public static class DefaultMetricBuilder implements MetricBuilder {

        @Override
        public void build() {
            // Nothing to do
        }

        @Override
        public MetricBuilder compilation(String name) {
            return this;
        }

        @Override
        public MetricBuilder linesOfCode(int line) {
            return this;
        }

        @Override
        public MetricBuilder packageName(String name) {
            return this;
        }

        public MetricBuilder path(String path) {
            return this;
        }

    }

    @Override
    public void finished(SLProgressMonitor monitor) {
        // Nothing to do
    }

    public void writeMetrics(File absoluteFile) {
        // Nothing to do
    }

    @Override
    public void rollback() {
        // Nothing to do
    }

    @Override
    public void relation(int parentNumber, int childNumber, String type) {
        // Nothing to do
    }
}
