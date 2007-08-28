package com.surelogic.sierra.tool.analyzer;

import com.surelogic.sierra.tool.message.IdentifierType;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

/**
 * 
 * @author nathan
 * 
 */
public interface ArtifactGenerator {

	public MetricBuilder metric();
	
	public ArtifactBuilder artifact();

	public ErrorBuilder error();

	public void finished();
	
	public interface ArtifactBuilder {
		public SourceLocationBuilder sourceLocation();

		public SourceLocationBuilder primarySourceLocation();

		public ArtifactBuilder findingType(String tool, String version, String mnemonic);

		public ArtifactBuilder priority(Priority priority);

		public ArtifactBuilder severity(Severity severity);

		public ArtifactBuilder message(String message);

		public void build();
	}

	public interface SourceLocationBuilder {

		SourceLocationBuilder type(IdentifierType type);

		SourceLocationBuilder identifier(String name);

		SourceLocationBuilder className(String className);

		SourceLocationBuilder packageName(String packageName);

		SourceLocationBuilder path(String path);

		SourceLocationBuilder lineOfCode(int line);

		SourceLocationBuilder endLine(int line);

		SourceLocationBuilder hash(Long hash);

		void build();

	}

	public interface ErrorBuilder {

		ErrorBuilder message(String message);

		ErrorBuilder tool(String tool);

		void build();
	}

}
