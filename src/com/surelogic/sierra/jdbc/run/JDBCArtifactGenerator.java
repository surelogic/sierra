package com.surelogic.sierra.jdbc.run;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.record.ArtifactRecord;
import com.surelogic.sierra.jdbc.record.ArtifactSourceRecord;
import com.surelogic.sierra.jdbc.record.ClassMetricRecord;
import com.surelogic.sierra.jdbc.record.CompilationUnitRecord;
import com.surelogic.sierra.jdbc.record.Record;
import com.surelogic.sierra.jdbc.record.RecordRelationRecord;
import com.surelogic.sierra.jdbc.record.RelationRecord;
import com.surelogic.sierra.jdbc.record.RunRecord;
import com.surelogic.sierra.jdbc.record.SourceRecord;
import com.surelogic.sierra.jdbc.tool.FindingTypeManager;
import com.surelogic.sierra.jdbc.tool.MessageFilter;
import com.surelogic.sierra.tool.analyzer.ArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.MetricBuilder;
import com.surelogic.sierra.tool.message.IdentifierType;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

public class JDBCArtifactGenerator implements ArtifactGenerator {

	private static final Logger log = SLLogger
			.getLoggerFor(JDBCArtifactGenerator.class);

	private static final String TOOL_ID_SELECT = "SELECT FT.ID FROM TOOL T, FINDING_TYPE FT WHERE T.NAME = ? AND T.VERSION = ? AND FT.TOOL_ID = T.ID AND FT.MNEMONIC = ?";

	private static final int COMMIT_SIZE = 700;

	private final Connection conn;

	private final FindingTypeManager ftMan;

	private final RunRecordFactory factory;
	private final Runnable callback;

	private final PreparedStatement toolIdSelect;

	private final List<ArtifactRecord> artifacts;
	private final List<ClassMetricRecord> classMetrics;
	private final Map<SourceRecord, SourceRecord> sources;
	private final Map<CompilationUnitRecord, CompilationUnitRecord> compUnits;
	private final Set<ArtifactSourceRecord> relations;

	private final MessageFilter filter;

	private final ArtifactBuilder aBuilder;
	private final MetricBuilder mBuilder;

	public JDBCArtifactGenerator(Connection conn, RunRecordFactory factory,
			RunRecord run, MessageFilter filter, Runnable callback)
			throws SQLException {
		log.info("Now persisting artifacts to database for run " + run.getId());
		this.conn = conn;
		this.factory = factory;
		this.callback = callback;
		toolIdSelect = conn.prepareStatement(TOOL_ID_SELECT);

		this.filter = filter;
		this.ftMan = FindingTypeManager.getInstance(conn);
		this.artifacts = new ArrayList<ArtifactRecord>(COMMIT_SIZE);
		this.classMetrics = new ArrayList<ClassMetricRecord>(COMMIT_SIZE);
		this.sources = new HashMap<SourceRecord, SourceRecord>(COMMIT_SIZE * 3);
		this.compUnits = new HashMap<CompilationUnitRecord, CompilationUnitRecord>(
				COMMIT_SIZE * 3);
		this.relations = new HashSet<ArtifactSourceRecord>(COMMIT_SIZE * 2);
		this.aBuilder = new JDBCArtifactBuilder(run);
		this.mBuilder = new JDBCMetricBuilder(run);
	}

	public void finished() {
		try {
			persist();
			toolIdSelect.close();
			callback.run();
		} catch (SQLException e) {
			throw new RunPersistenceException(e);
		}
	}

	private void persist() throws SQLException {
		lookupOrInsert(compUnits.values());
		compUnits.clear();
		lookupOrInsert(sources.values());
		sources.clear();
		insert(artifacts);
		artifacts.clear();
		insert(relations);
		relations.clear();
		insert(classMetrics);
		classMetrics.clear();
		conn.commit();
	}

	private void lookupOrInsert(Collection<? extends Record<?>> objects)
			throws SQLException {
		for (Record<?> ins : objects) {
			if (!ins.select()) {
				ins.insert();
			}
		}
	}

	private void insert(Collection<? extends Record<?>> objects)
			throws SQLException {
		for (Record<?> rec : objects) {
			rec.insert();
		}
	}

	public ArtifactBuilder artifact() {
		return aBuilder;
	}

	public ErrorBuilder error() {
		return new JDBCErrorBuilder();
	}

	public MetricBuilder metric() {
		return mBuilder;
	}

	private class JDBCMetricBuilder implements MetricBuilder {

		private final RunRecord run;
		private CompilationUnitRecord compUnit;
		private Integer linesOfCode;

		public JDBCMetricBuilder(RunRecord run) {
			this.run = run;
			clear();
		}

		private void clear() {
			this.compUnit = factory.newCompilationUnit();
			linesOfCode = null;
		}

		public void build() {
			CompilationUnitRecord currentComp = compUnits.get(compUnit);
			if (currentComp == null) {
				compUnits.put(compUnit, compUnit);
				currentComp = compUnit;
			}
			ClassMetricRecord rec = factory.newClassMetric();
			rec.setId(new RelationRecord.PK<RunRecord, CompilationUnitRecord>(
					run, currentComp));
			rec.setLinesOfCode(linesOfCode);
			classMetrics.add(rec);
			if (classMetrics.size() == COMMIT_SIZE) {
				try {
					persist();
				} catch (SQLException e) {
					throw new RunPersistenceException(e);
				}
			}
			clear();
		}

		public MetricBuilder className(String name) {
			compUnit.setClassName(name);
			return this;
		}

		public MetricBuilder linesOfCode(int line) {
			this.linesOfCode = line;
			return this;
		}

		public MetricBuilder packageName(String name) {
			compUnit.setPackageName(name);
			return this;
		}

		public MetricBuilder path(String path) {
			compUnit.setPath(path);
			return this;
		}

	}

	private class JDBCErrorBuilder implements ErrorBuilder {

		public void build() {
			// TODO Auto-generated method stub

		}

		public ErrorBuilder message(String message) {
			// TODO Auto-generated method stub
			return null;
		}

		public ErrorBuilder tool(String tool) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	private class JDBCArtifactBuilder implements ArtifactBuilder {

		private ArtifactRecord artifact;
		private long runId;
		private final List<SourceRecord> aSources;
		private SourceRecord pSource;

		public JDBCArtifactBuilder(RunRecord run) {
			this.runId = run.getId();
			this.aSources = new ArrayList<SourceRecord>();
			clear();
		}

		private SourceRecord addSource(SourceRecord source) {
			CompilationUnitRecord compUnit = source.getCompUnit();
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

		public void build() {
			if (filter.accept(artifact.getFindingTypeId())) {
				artifact.setPrimary(addSource(pSource));
				for (SourceRecord source : aSources) {
					SourceRecord currentSource = addSource(source);
					ArtifactSourceRecord rel = factory
							.newArtifactSourceRelation();
					rel
							.setId(new RecordRelationRecord.PK<ArtifactRecord, SourceRecord>(
									artifact, currentSource));
					relations.add(rel);
				}

				artifacts.add(artifact);
				if (artifacts.size() == COMMIT_SIZE) {
					try {
						persist();
					} catch (SQLException e) {
						throw new RunPersistenceException(e);
					}
				}
			}
			clear();
		}

		public ArtifactBuilder findingType(String tool, String version,
				String mnemonic) {
			try {
				artifact.setFindingTypeId(ftMan.getFindingTypeId(tool, version,
						mnemonic));
			} catch (SQLException e) {
				throw new RunPersistenceException(e);
			}
			return this;
		}

		public ArtifactBuilder message(String message) {
			artifact.setMessage(message);
			return this;
		}

		public SourceLocationBuilder primarySourceLocation() {
			return new JDBCSourceLocationBuilder(true);
		}

		public ArtifactBuilder priority(Priority priority) {
			artifact.setPriority(priority);
			return this;
		}

		public ArtifactBuilder severity(Severity severity) {
			artifact.setSeverity(severity);
			return this;
		}

		public SourceLocationBuilder sourceLocation() {
			return new JDBCSourceLocationBuilder(false);
		}

		private void clear() {
			artifact = factory.newArtifact();
			artifact.setRunId(runId);
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

			public void build() {
				if (primary) {
					pSource = sourceIns;
				} else {
					aSources.add(sourceIns);
				}
			}

			public SourceLocationBuilder className(String className) {
				compUnit.setClassName(className);
				return this;
			}

			public SourceLocationBuilder endLine(int line) {
				sourceIns.setEndLineOfCode(line);
				return this;
			}

			public SourceLocationBuilder hash(Long hash) {
				sourceIns.setHash(hash);
				return this;
			}

			public SourceLocationBuilder identifier(String name) {
				sourceIns.setIdentifier(name);
				return this;
			}

			public SourceLocationBuilder lineOfCode(int line) {
				sourceIns.setLineOfCode(line);
				return this;
			}

			public SourceLocationBuilder packageName(String packageName) {
				compUnit.setPackageName(packageName);
				return this;
			}

			public SourceLocationBuilder path(String path) {
				compUnit.setPath(path);
				return this;
			}

			public SourceLocationBuilder type(IdentifierType type) {
				sourceIns.setType(type);
				return this;
			}

		}

	}
}
