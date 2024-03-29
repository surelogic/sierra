package com.surelogic.sierra.jdbc.scan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.jobs.NullSLProgressMonitor;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.record.ArtifactRecord;
import com.surelogic.sierra.jdbc.record.ArtifactSourceRecord;
import com.surelogic.sierra.jdbc.record.ClassMetricRecord;
import com.surelogic.sierra.jdbc.record.CompilationUnitRecord;
import com.surelogic.sierra.jdbc.record.Record;
import com.surelogic.sierra.jdbc.record.RecordRelationRecord;
import com.surelogic.sierra.jdbc.record.RelationRecord;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.record.SourceRecord;
import com.surelogic.sierra.jdbc.tool.FindingFilter;
import com.surelogic.sierra.jdbc.tool.FindingTypeManager;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.IdentifierType;
import com.surelogic.sierra.tool.message.MetricBuilder;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

public class JDBCArtifactGenerator implements ArtifactGenerator {

    private static final Logger log = SLLogger
            .getLoggerFor(JDBCArtifactGenerator.class);

    private static final int COMMIT_SIZE = 700;

    private final Connection conn;

    private final FindingTypeManager ftMan;

    private final ScanRecordFactory factory;
    private final ScanManager manager;

    private final List<ArtifactRecord> artifacts;
    private final List<ClassMetricRecord> classMetrics;
    private final List<ArtifactArtifactRelation> artifactRelations;
    private final Map<SourceRecord, SourceRecord> sources;
    private final Map<CompilationUnitRecord, CompilationUnitRecord> compUnits;
    private final Set<ArtifactSourceRecord> relations;

    private final FindingFilter filter;
    @SuppressWarnings("unused")
    private final String projectName;
    private final ScanRecord scan;

    private final ArtifactBuilder aBuilder;
    private final MetricBuilder mBuilder;

    private final PreparedStatement insertArtifactNumberRelation;

    public JDBCArtifactGenerator(Connection conn, ScanRecordFactory factory,
            ScanManager manager, String projectName, ScanRecord scan,
            FindingFilter filter) throws SQLException {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Now persisting artifacts to database for scan "
                    + scan.getUid() + " in project " + projectName + ".");
        }
        this.conn = conn;
        this.factory = factory;
        this.manager = manager;
        this.filter = filter;
        ftMan = FindingTypeManager.getInstance(conn);
        artifacts = new ArrayList<ArtifactRecord>(COMMIT_SIZE);
        classMetrics = new ArrayList<ClassMetricRecord>(COMMIT_SIZE);
        sources = new HashMap<SourceRecord, SourceRecord>(COMMIT_SIZE * 3);
        compUnits = new HashMap<CompilationUnitRecord, CompilationUnitRecord>(
                COMMIT_SIZE * 3);
        relations = new HashSet<ArtifactSourceRecord>(COMMIT_SIZE * 2);
        artifactRelations = new ArrayList<ArtifactArtifactRelation>();
        this.projectName = projectName;
        this.scan = scan;
        aBuilder = new JDBCArtifactBuilder();
        mBuilder = new JDBCMetricBuilder();
        insertArtifactNumberRelation = conn
                .prepareStatement("INSERT INTO ARTIFACT_NUMBER_RELTN (SCAN_ID,PARENT_NUMBER,CHILD_NUMBER,RELATION_TYPE) VALUES (?,?,?,?)");
    }

    /*
     * Must be called by JDBCScanGenerator to ensure all artifacts and error are
     * actually persisted.
     */
    @Override
    public void finished(SLProgressMonitor monitor) {
        monitor.begin(100);
        persist(monitor);
    }

    private void persist(SLProgressMonitor monitor) {
        try {
            monitor.subTask("Persisting comp units");
            lookupOrInsert(compUnits.values());
            monitor.worked(1);
            compUnits.clear();

            monitor.subTask("Persisting sources");
            lookupOrInsert(sources.values());
            monitor.worked(1);
            sources.clear();

            monitor.subTask("Persisting artifacts");
            insert(artifacts);
            for (final ArtifactArtifactRelation relation : artifactRelations) {
                insertArtifactNumberRelation.setLong(1, scan.getId());
                insertArtifactNumberRelation.setInt(2, relation.parentNumber);
                insertArtifactNumberRelation.setInt(3, relation.childNumber);
                insertArtifactNumberRelation.setString(4, relation.type);
                insertArtifactNumberRelation.execute();
            }
            monitor.worked(1);
            artifacts.clear();
            artifactRelations.clear();
            monitor.subTask("Persisting relations");
            insert(relations);
            monitor.worked(1);
            relations.clear();

            monitor.subTask("Persisting metrics");
            insert(classMetrics);
            monitor.worked(1);
            classMetrics.clear();

            monitor.subTask("Committing ...");
            conn.commit();
            monitor.worked(1);
        } catch (final SQLException e) {
            // dumpTables();

            monitor.subTask("Rolling back ...");
            quietlyRollback();
            monitor.worked(1);
            throw new ScanPersistenceException(e);
        }
    }

    private void dumpTables() {
        try {
            final Statement st = conn.createStatement();
            dumpTable(st, "select * from metric_cu");
            dumpTable(st, "select * from compilation_unit");
            System.out.println();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void dumpTable(Statement st, String q) throws SQLException {
        System.out.println(q);
        final ResultSet rs = st.executeQuery(q);
        while (rs.next()) {
            StringBuilder sb = new StringBuilder("\t");
            final int num = rs.getMetaData().getColumnCount();
            for (int i = 1; i <= num; i++) {
                try {
                    sb.append(rs.getString(i)).append(", ");
                } catch (SQLException e) {
                    sb.append(rs.getLong(i)).append(", ");
                }
            }
            System.out.println(sb);
        }
    }

    private void lookupOrInsert(Collection<? extends Record<?>> objects)
            throws SQLException {
        final Iterator<? extends Record<?>> i = objects.iterator();
        while (i.hasNext()) {
            final Record<?> ins = i.next();
            if (ins.select()) {
                i.remove();
            }
        }
        for (final Record<?> ins : objects) {
            ins.insert();
        }
    }

    private void insert(Collection<? extends Record<?>> objects)
            throws SQLException {
        for (final Record<?> rec : objects) {
        	try {
        		rec.insert();
        	} catch (SQLException e) {
        		throw e;
        	}
        }
    }

    @Override
    public ArtifactBuilder artifact() {
        return aBuilder;
    }

    @Override
    public ErrorBuilder error() {
        return new JDBCErrorBuilder();
    }

    @Override
    public MetricBuilder metric() {
        return mBuilder;
    }

    private class JDBCMetricBuilder implements MetricBuilder {

        private CompilationUnitRecord compUnit;
        private Integer linesOfCode;

        public JDBCMetricBuilder() {
            clear();
        }

        private void clear() {
            compUnit = factory.newCompilationUnit();
            linesOfCode = null;
        }

        @Override
        public void build() {
            CompilationUnitRecord currentComp = compUnits.get(compUnit);
            if (currentComp == null) {
                compUnits.put(compUnit, compUnit);
                currentComp = compUnit;
            }
            final ClassMetricRecord rec = factory.newClassMetric();
            rec.setId(new RelationRecord.PK<ScanRecord, CompilationUnitRecord>(
                    scan, currentComp));
            rec.setLinesOfCode(linesOfCode);
            classMetrics.add(rec);
            if (classMetrics.size() == COMMIT_SIZE) {
                persist(new NullSLProgressMonitor());
            }
            clear();
        }

        @Override
        public MetricBuilder compilation(String name) {
            compUnit.setCompilation(name);
            return this;
        }

        @Override
        public MetricBuilder linesOfCode(int line) {
            linesOfCode = line;
            return this;
        }

        @Override
        public MetricBuilder packageName(String name) {
            compUnit.setPackageName(name);
            return this;
        }

    }

    private static class JDBCErrorBuilder implements ErrorBuilder {

        @Override
        public void build() {
            // TODO Auto-generated method stub

        }

        @Override
        public ErrorBuilder message(String message) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ErrorBuilder tool(String tool) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    private class JDBCArtifactBuilder implements ArtifactBuilder {

        private ArtifactRecord artifact;
        private final long scanId;
        private final List<SourceRecord> aSources;
        private SourceRecord pSource;

        public JDBCArtifactBuilder() {
            scanId = scan.getId();
            aSources = new ArrayList<SourceRecord>();
            clear();
        }

        private SourceRecord addSource(SourceRecord source) {
            final CompilationUnitRecord compUnit = source.getCompUnit();
            CompilationUnitRecord currentComp = compUnits.get(compUnit);
            if (currentComp == null) {
                compUnits.put(compUnit, compUnit);
                currentComp = compUnit;
            }
            source.setCompUnit(currentComp);
            SourceRecord currentSource = sources.get(source);
            if (currentSource == null) {
                sources.put(source, source);
                currentSource = source;
            }
            return currentSource;
        }

        @Override
        public void build() {
            if (filter.accept(artifact.getArtifactTypeId())) {
                artifact.setPrimary(addSource(pSource));
                for (final SourceRecord source : aSources) {
                    final SourceRecord currentSource = addSource(source);
                    final ArtifactSourceRecord rel = factory
                            .newArtifactSourceRelation();
                    rel.setId(new RecordRelationRecord.PK<ArtifactRecord, SourceRecord>(
                            artifact, currentSource));
                    relations.add(rel);
                }
                artifacts.add(artifact);
                if (artifacts.size() == COMMIT_SIZE) {
                    persist(new NullSLProgressMonitor());
                }
            }
            clear();
        }

        @Override
        public ArtifactBuilder findingType(String tool, String version,
                String mnemonic) {
            try {
                artifact.setFindingTypeId(ftMan.getArtifactTypeId(tool,
                        version, mnemonic));
            } catch (final SQLException e) {
                quietlyRollback();
                throw new ScanPersistenceException(e);
            }
            return this;
        }

        @Override
        public ArtifactBuilder message(String message) {
            artifact.setMessage(message);
            return this;
        }

        @Override
        public SourceLocationBuilder primarySourceLocation() {
            return new JDBCSourceLocationBuilder(true);
        }

        @Override
        public ArtifactBuilder priority(Priority priority) {
            artifact.setPriority(priority);
            return this;
        }

        @Override
        public ArtifactBuilder severity(Severity severity) {
            artifact.setSeverity(severity);
            return this;
        }

        @Override
        public ArtifactBuilder scanNumber(Integer number) {
            artifact.setScanNumber(number);
            return this;
        }

        @Override
        public SourceLocationBuilder sourceLocation() {
            return new JDBCSourceLocationBuilder(false);
        }

        private void clear() {
            artifact = factory.newArtifact();
            artifact.setScanId(scanId);
            aSources.clear();
        }

        private class JDBCSourceLocationBuilder implements
                SourceLocationBuilder {

            private final SourceRecord sourceIns;
            private final CompilationUnitRecord compUnit;
            private final boolean primary;

            public JDBCSourceLocationBuilder(boolean primary) {
                sourceIns = factory.newSource();
                compUnit = factory.newCompilationUnit();
                sourceIns.setCompUnit(compUnit);
                this.primary = primary;
            }

            @Override
            public void build() {
                if (primary) {
                    pSource = sourceIns;
                } else {
                    aSources.add(sourceIns);
                }
            }

            @Override
            public SourceLocationBuilder className(String className) {
                sourceIns.setClassName(className);
                return this;
            }

            @Override
            public SourceLocationBuilder endLine(int line) {
                sourceIns.setEndLineOfCode(line);
                return this;
            }

            @Override
            public SourceLocationBuilder hash(Long hash) {
                sourceIns.setHash(hash);
                return this;
            }

            @Override
            public SourceLocationBuilder identifier(String name) {
                sourceIns.setIdentifier(name);
                return this;
            }

            @Override
            public SourceLocationBuilder lineOfCode(int line) {
                sourceIns.setLineOfCode(line);
                return this;
            }

            @Override
            public SourceLocationBuilder packageName(String packageName) {
                compUnit.setPackageName(packageName);
                return this;
            }

            @Override
            public SourceLocationBuilder type(IdentifierType type) {
                sourceIns.setType(type);
                return this;
            }

            @Override
            public SourceLocationBuilder compilation(String compilation) {
                compUnit.setCompilation(compilation);
                return this;
            }

        }

    }

    private void quietlyRollback() {
        try {
            rollback();
        } catch (final Exception e) {
            // Do nothing
        }
    }

    @Override
    public void rollback() {
        try {
            conn.rollback();
            manager.deleteScan(scan.getUid(), null);
            conn.commit();
        } catch (final SQLException e) {
            throw new ScanPersistenceException(e);
        }
    }

    @Override
    public void relation(int parentNumber, int childNumber, String type) {
        artifactRelations.add(new ArtifactArtifactRelation(parentNumber,
                childNumber, type));

    }

    private static class ArtifactArtifactRelation {
        final int childNumber;
        final int parentNumber;
        final String type;

        ArtifactArtifactRelation(int parentNumber, int childNumber, String type) {
            this.parentNumber = parentNumber;
            this.childNumber = childNumber;
            this.type = type;
        }
    }

}
