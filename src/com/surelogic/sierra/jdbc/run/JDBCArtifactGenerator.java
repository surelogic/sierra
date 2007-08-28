package com.surelogic.sierra.jdbc.run;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import com.surelogic.sierra.jdbc.record.CompilationUnitRecord;
import com.surelogic.sierra.jdbc.record.Record;
import com.surelogic.sierra.jdbc.record.RecordRelationRecord;
import com.surelogic.sierra.jdbc.record.RunRecord;
import com.surelogic.sierra.jdbc.record.SourceRecord;
import com.surelogic.sierra.jdbc.tool.FindingTypeKey;
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

	private final RunRecordFactory factory;
	private final Runnable callback;

	private final PreparedStatement toolIdSelect;

	private final List<ArtifactRecord> artifacts;
	private final Map<SourceRecord, SourceRecord> sources;
	private final Map<CompilationUnitRecord, CompilationUnitRecord> compUnits;
	private final Set<ArtifactSourceRecord> relations;

	private final ArtifactBuilder builder;

	public JDBCArtifactGenerator(Connection conn, RunRecordFactory factory,
			RunRecord run, Runnable callback) throws SQLException {
		log.info("Now persisting artifacts to database for run " + run.getId());
		this.conn = conn;
		this.factory = factory;
		this.callback = callback;
		toolIdSelect = conn.prepareStatement(TOOL_ID_SELECT);

		this.artifacts = new ArrayList<ArtifactRecord>(COMMIT_SIZE);
		this.sources = new HashMap<SourceRecord, SourceRecord>(COMMIT_SIZE * 3);
		this.compUnits = new HashMap<CompilationUnitRecord, CompilationUnitRecord>(
				COMMIT_SIZE * 3);
		this.relations = new HashSet<ArtifactSourceRecord>(COMMIT_SIZE * 2);
		this.builder = new JDBCArtifactBuilder(run.getId());
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

	// TODO We may be able to speed up here by keeping a map of recent ids
	private long getFindingTypeId(String tool, String version, String mnemonic)
			throws SQLException {
		FindingTypeKey ins = new FindingTypeKey(tool, version, mnemonic);
		ins.fill(toolIdSelect, 1);
		ResultSet set = toolIdSelect.executeQuery();
		set.next();
		return set.getLong(1);
	}

	public ArtifactBuilder artifact() {
		return builder;
	}

	public ErrorBuilder error() {
		return new JDBCErrorBuilder();
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

		public JDBCArtifactBuilder(long runId) {
			this.runId = runId;
			clear();
		}

		public void build() {
			artifacts.add(artifact);
			if (artifacts.size() == COMMIT_SIZE) {
				try {
					persist();
				} catch (SQLException e) {
					throw new RunPersistenceException(e);
				}
			}
			clear();
		}

		public ArtifactBuilder findingType(String tool, String version,
				String mnemonic) {
			try {
				artifact.setFindingTypeId(getFindingTypeId(tool, version,
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
		}

		private class JDBCSourceLocationBuilder implements
				SourceLocationBuilder {

			private final SourceRecord sourceIns;
			private final CompilationUnitRecord compUnit;
			private final boolean primary;

			public JDBCSourceLocationBuilder(boolean primary) {
				sourceIns = factory.newSource();
				compUnit = factory.newCompilationUnit();
				this.primary = primary;
			}

			public void build() {
				CompilationUnitRecord currentComp = compUnits.get(compUnit);
				if (currentComp == null) {
					compUnits.put(compUnit, compUnit);
					currentComp = compUnit;
				}
				sourceIns.setCompUnit(currentComp);
				SourceRecord currentSource = sources.get(sourceIns);
				if (currentSource == null) {
					sources.put(sourceIns, sourceIns);
					currentSource = sourceIns;
				}
				if (primary) {
					artifact.setPrimary(currentSource);
				} else {
					ArtifactSourceRecord rel = factory
							.newArtifactSourceRelation();
					rel
							.setId(new RecordRelationRecord.PK<ArtifactRecord, SourceRecord>(
									artifact, currentSource));
					relations.add(rel);
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

	public MetricBuilder metric() {
		// TODO Auto-generated method stub
		return null;
	}

}
