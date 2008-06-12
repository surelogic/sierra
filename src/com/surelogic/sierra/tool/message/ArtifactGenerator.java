package com.surelogic.sierra.tool.message;

import com.surelogic.common.SLProgressMonitor;

/**
 * 
 * @author nathan
 * 
 */
public interface ArtifactGenerator {
	public MetricBuilder metric();

	public ArtifactBuilder artifact();

	public void relation(int parentNumber, int childNumber, String type);

	public ErrorBuilder error();

	public void finished(SLProgressMonitor monitor);

	public void rollback();

	public interface ArtifactBuilder {
		/**
		 * Add additional source locations to this artifact.
		 * 
		 * @return
		 */
		public SourceLocationBuilder sourceLocation();

		/**
		 * Call this to build the primary source location. This method should
		 * only be called once.
		 * 
		 * @return
		 */
		public SourceLocationBuilder primarySourceLocation();

		public ArtifactBuilder findingType(String tool, String version,
				String mnemonic);

		public ArtifactBuilder priority(Priority priority);

		public ArtifactBuilder severity(Severity severity);

		public ArtifactBuilder message(String message);

		/**
		 * Assigns the artifact a number that should be unique for this scan.
		 * 
		 * @param number
		 * @return
		 */
		public ArtifactBuilder scanNumber(int number);

		/**
		 * Assigns an assurance type, which indicates whether or not the
		 * artifact is consistent or inconsistent. May be <code>null</code> if
		 * the artifact is from a rule-based tool.
		 * 
		 * @param type
		 * @return
		 */
		public ArtifactBuilder assurance(AssuranceType type);

		public void build();
	}

	public interface SourceLocationBuilder {
		SourceLocationBuilder type(IdentifierType type);

		SourceLocationBuilder identifier(String name);

		/**
		 * The unqualified name of the compilation unit. For example, if a class
		 * name <code>Foo</code> is declared in a compilation unit located at
		 * <code>com/surelogic/baz/Bar.java</code>, then the name of the
		 * compilation unit is Bar.
		 * 
		 * @param compilation
		 * @return
		 */
		SourceLocationBuilder compilation(String compilation);

		/**
		 * The name of the most specific class this artifact was found in. This
		 * might not be the same as the name of the compilation unit.
		 * 
		 * @param className
		 * @return
		 */
		SourceLocationBuilder className(String className);

		/**
		 * The package name of the class, usind <code>.</code> as the
		 * separator. (ex. com.surelogic.baz)
		 * 
		 * @param packageName
		 * @return
		 */
		SourceLocationBuilder packageName(String packageName);

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
