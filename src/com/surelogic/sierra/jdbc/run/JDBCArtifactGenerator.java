package com.surelogic.sierra.jdbc.run;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.surelogic.sierra.jdbc.Record;
import com.surelogic.sierra.jdbc.tool.FindingTypeKey;
import com.surelogic.sierra.tool.analyzer.ArtifactGenerator;
import com.surelogic.sierra.tool.message.IdentifierType;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;
import static com.surelogic.sierra.jdbc.JDBCUtils.*;

public class JDBCArtifactGenerator implements ArtifactGenerator {

	private static final String TOOL_ID_SELECT = "SELECT FT.ID FROM TOOL T, FINDING_TYPE FT WHERE T.NAME = ? AND FT.TOOL_ID = T.ID AND FT.MNEMONIC = ?";
	private static final String COMPILATION_UNIT_INSERT = "INSERT INTO SIERRA.COMPILATION_UNIT (PATH,CLASS_NAME,PACKAGE_NAME) VALUES (?,?,?)";
	private static final String COMPILATION_UNIT_SELECT = "SELECT ID FROM SIERRA.COMPILATION_UNIT CU WHERE CU.PATH = ? AND CU.CLASS_NAME = ? AND CU.PACKAGE_NAME = ?";
	private static final String SOURCE_LOCATION_INSERT = "INSERT INTO SIERRA.SOURCE_LOCATION (COMPILATION_UNIT_ID,HASH,LINE_OF_CODE,END_LINE_OF_CODE,LOCATION_TYPE,IDENTIFIER) VALUES (?,?,?,?,?,?)";
	private static final String SOURCE_LOCATION_SELECT = "SELECT ID FROM SIERRA.SOURCE_LOCATION SL WHERE SL.COMPILATION_UNIT_ID = ? AND SL.HASH = ? AND SL.LINE_OF_CODE = ? AND SL.END_LINE_OF_CODE = ? AND SL.LOCATION_TYPE = ? AND SL.IDENTIFIER = ?";
	private static final String ARTIFACT_INSERT = "INSERT INTO SIERRA.ARTIFACT (RUN_ID,FINDING_TYPE_ID,PRIMARY_SOURCE_LOCATION_ID,PRIORITY,SEVERITY,MESSAGE) VALUES (?,?,?,?,?,?)";
	private static final String ARTIFACT_SOURCE_RELATION_INSERT = "INSERT INTO SIERRA.ARTIFACT_SOURCE_LOCATION_RELTN (ARTIFACT_ID,SOURCE_LOCATION_ID) VALUES (?,?)";

	private static final int COMMIT_SIZE = 700;

	private final Connection conn;

	private final PreparedStatement toolIdSelect;
	private final PreparedStatement compUnitInsert;
	private final PreparedStatement compUnitSelect;
	private final PreparedStatement sourceSelect;
	private final PreparedStatement sourceInsert;
	private final PreparedStatement artifactInsert;
	private final PreparedStatement artifactSourceInsert;

	private final List<ArtifactRecord> artifacts;
	private final Map<SourceRecord, SourceRecord> sources;
	private final Map<CompilationUnitRecord, CompilationUnitRecord> compUnits;
	private final Set<ArtifactSourceRecord> relations;

	private final ArtifactBuilder builder;

	public JDBCArtifactGenerator(Connection conn, Long runId)
			throws SQLException {
		this.conn = conn;
		toolIdSelect = conn.prepareStatement(TOOL_ID_SELECT);
		compUnitSelect = conn.prepareStatement(COMPILATION_UNIT_SELECT);
		compUnitInsert = conn.prepareStatement(COMPILATION_UNIT_INSERT,
				Statement.RETURN_GENERATED_KEYS);
		sourceSelect = conn.prepareStatement(SOURCE_LOCATION_SELECT);
		sourceInsert = conn.prepareStatement(SOURCE_LOCATION_INSERT,
				Statement.RETURN_GENERATED_KEYS);
		artifactInsert = conn.prepareStatement(ARTIFACT_INSERT,
				Statement.RETURN_GENERATED_KEYS);
		artifactSourceInsert = conn.prepareStatement(
				ARTIFACT_SOURCE_RELATION_INSERT,
				Statement.RETURN_GENERATED_KEYS);
		this.artifacts = new ArrayList<ArtifactRecord>(COMMIT_SIZE);
		this.sources = new HashMap<SourceRecord, SourceRecord>(COMMIT_SIZE * 3);
		this.compUnits = new HashMap<CompilationUnitRecord, CompilationUnitRecord>(
				COMMIT_SIZE * 3);
		this.relations = new HashSet<ArtifactSourceRecord>(COMMIT_SIZE * 2);

		this.builder = new JDBCArtifactBuilder(runId);
	}

	public void finish() throws SQLException {
		persist();
		toolIdSelect.close();
		compUnitInsert.close();
		compUnitSelect.close();
		sourceSelect.close();
		sourceInsert.close();
		artifactInsert.close();
		artifactSourceInsert.close();
	}

	private void persist() throws SQLException {
		lookupOrGenerateIds(compUnitSelect, compUnitInsert, compUnits.values());
		compUnits.clear();
		lookupOrGenerateIds(sourceSelect, sourceInsert, sources.values());
		sources.clear();
		executeAndGenerateIds(artifactInsert, artifacts);
		artifacts.clear();
		for (ArtifactSourceRecord rec : relations) {
			rec.fill(artifactSourceInsert, 1);
			artifactSourceInsert.executeUpdate();
		}
		relations.clear();
		conn.commit();
	}

	private void lookupOrGenerateIds(PreparedStatement lookup,
			PreparedStatement generate,
			Collection<? extends Record<Long>> objects) throws SQLException {
		int idx = 1;
		for (Record<Long> ins : objects) {
			ins.fill(lookup, idx);
			ResultSet set = lookup.executeQuery();
			if (set.next()) {
				ins.setId(set.getLong(1));
			} else {
				ins.fill(generate, idx);
				generate.executeUpdate();
				set = generate.getGeneratedKeys();
				set.next();
				ins.setId(set.getLong(1));
			}
		}
	}

	private void executeAndGenerateIds(PreparedStatement st,
			Collection<? extends Record<Long>> objects) throws SQLException {
		for (Record<?> rec : objects) {
			insert(st, rec);
		}
	}

	// TODO We may be able to speed up here by keeping a map of recent ids
	private long getFindingTypeId(String tool, String mnemonic)
			throws SQLException {
		FindingTypeKey ins = new FindingTypeKey(tool, mnemonic);
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

		public ArtifactBuilder findingType(String tool, String mnemonic) {
			try {
				artifact.setFindingTypeId(getFindingTypeId(tool, mnemonic));
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
			artifact = new ArtifactRecord();
			artifact.setRunId(runId);
		}

		private class JDBCSourceLocationBuilder implements
				SourceLocationBuilder {

			private final SourceRecord sourceIns;
			private final CompilationUnitRecord compUnit;
			private final boolean primary;

			public JDBCSourceLocationBuilder(boolean primary) {
				sourceIns = new SourceRecord();
				compUnit = new CompilationUnitRecord();
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
					ArtifactSourceRecord rel = new ArtifactSourceRecord(
							artifact, currentSource);
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

}
