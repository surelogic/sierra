package com.surelogic.sierra.tool.message;


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
    
    public void rollback();

    public interface ArtifactBuilder {
    	/**
    	 * Add additional source locations to this artifact.
    	 * @return
    	 */
        public SourceLocationBuilder sourceLocation();

        /**
         * Call this to build the primary source location.  This method should only be called once.
         * @return
         */
        public SourceLocationBuilder primarySourceLocation();

        public ArtifactBuilder findingType(String tool, String version,
            String mnemonic);

        public ArtifactBuilder priority(Priority priority);

        public ArtifactBuilder severity(Severity severity);

        public ArtifactBuilder message(String message);

        public void build();
    }

    public interface SourceLocationBuilder {
        SourceLocationBuilder type(IdentifierType type);

        SourceLocationBuilder identifier(String name);

        SourceLocationBuilder compilation(String compilation);

        SourceLocationBuilder className(String className);

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
