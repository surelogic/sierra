package com.surelogic.sierra.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.surelogic.sierra.db.Data;
import com.surelogic.sierra.tool.analyzer.ArtifactGenerator;
import com.surelogic.sierra.tool.message.IdentifierType;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

public class JDBCArtifactGenerator implements ArtifactGenerator {

	private static final String TOOL_ID_SELECT = "SELECT FT.ID FROM TOOL T, FINDING_TYPE FT WHERE T.NAME = ? AND FT.TOOL_ID = T.ID AND FT.MNEMONIC = ?";

	private static final String COMPILATION_UNIT_SELECT = "SELECT ID FROM SIERRA.COMPILATION_UNIT CU WHERE CU.PATH = ? AND CU.CLASS_NAME = ? AND CU.PACKAGE_NAME = ?";
	private static final String COMPILATION_UNIT_INSERT = "INSERT INTO SIERRA.COMPILATION_UNIT (PATH,CLASS_NAME,PACKAGE_NAME) VALUES (?,?,?)";
	private static final String SOURCE_LOCATION_SELECT = "SELECT ID FROM SIERRA.SOURCE_LOCATION SL WHERE SL.COMPILATION_UNIT_ID = ? AND SL.HASH = ? AND SL.LINE_OF_CODE = ? AND SL.END_LINE_OF_CODE = ? AND SL.LOCATION_TYPE = ? AND SL.IDENTIFIER = ?";
	private static final String SOURCE_LOCATION_INSERT = "INSERT INTO SIERRA.SOURCE_LOCATION (COMPILATION_UNIT_ID,HASH,LINE_OF_CODE,END_LINE_OF_CODE,LOCATION_TYPE,IDENTIFIER) VALUES (?,?,?,?,?,?)";
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

	private final List<ArtifactIns> artifacts;
	private final Map<SourceIns, SourceIns> sources;
	private final Map<CompUnitIns, CompUnitIns> compUnits;
	private final Set<ArtSourceRel> relations;

	private final ArtifactBuilder builder;

	public JDBCArtifactGenerator(long runId) {

		try {
			this.conn = Data.getConnection();
			conn.setAutoCommit(false);
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
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}

		this.artifacts = new ArrayList<ArtifactIns>(COMMIT_SIZE);
		this.sources = new HashMap<SourceIns, SourceIns>(COMMIT_SIZE * 3);
		this.compUnits = new HashMap<CompUnitIns, CompUnitIns>(COMMIT_SIZE * 3);
		this.relations = new HashSet<ArtSourceRel>(COMMIT_SIZE * 2);

		this.builder = new JDBCArtifactBuilder(runId);
	}

	public void finish() {
		persist();
		try {
			conn.close();
		} catch (SQLException e) {
			sqlError(e);
		}
	}

	private void persist() {
		try {
			lookupOrGenerateIds(compUnitSelect, compUnitInsert, compUnits
					.values());
			compUnits.clear();
			lookupOrGenerateIds(sourceSelect, sourceInsert, sources.values());
			sources.clear();
			executeAndGenerateIds(artifactInsert, artifacts);
			artifacts.clear();
			executeAndGenerateIds(artifactSourceInsert, relations);
			relations.clear();
			conn.commit();
		} catch (SQLException e) {
			sqlError(e);
		}
	}

	private void lookupOrGenerateIds(PreparedStatement lookup,
			PreparedStatement generate, Collection<? extends Ins> objects)
			throws SQLException {
		int idx = 1;
		for (Ins ins : objects) {
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
			Collection<? extends Ins> objects) throws SQLException {
		int idx = 1;
		for (Ins i : objects) {
			i.fill(st, idx);
			st.executeUpdate();
			ResultSet set = st.getGeneratedKeys();
			set.next();
			i.setId(set.getLong(1));
		}
	}

	private static interface Ins {

		long getId();

		void setId(long id);

		int fill(PreparedStatement st, int idx) throws SQLException;
	}

	private static class ArtifactIns implements Ins {
		private long id;

		private long runId;
		private long findingTypeId;
		private Priority priority;
		private Severity severity;
		private String message;
		private SourceIns primary;

		ArtifactIns() {
		}

		public long getRunId() {
			return runId;
		}

		public void setRunId(long runId) {
			this.runId = runId;
		}

		public long getFindingTypeId() {
			return findingTypeId;
		}

		public void setFindingTypeId(long findingTypeId) {
			this.findingTypeId = findingTypeId;
		}

		public Priority getPriority() {
			return priority;
		}

		public void setPriority(Priority priority) {
			this.priority = priority;
		}

		public Severity getSeverity() {
			return severity;
		}

		public void setSeverity(Severity severity) {
			this.severity = severity;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public SourceIns getPrimary() {
			return primary;
		}

		public void setPrimary(SourceIns primary) {
			this.primary = primary;
		}

		public int fill(PreparedStatement st, int idx) throws SQLException {
			st.setLong(idx++, runId);
			st.setLong(idx++, findingTypeId);
			st.setLong(idx++, primary.getId());
			st.setInt(idx++, priority.ordinal());
			st.setInt(idx++, severity.ordinal());
			setNullableString(idx++, st, message);
			return idx;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ (int) (findingTypeId ^ (findingTypeId >>> 32));
			result = prime * result
					+ ((message == null) ? 0 : message.hashCode());
			result = prime * result
					+ ((primary == null) ? 0 : primary.hashCode());
			result = prime * result
					+ ((priority == null) ? 0 : priority.hashCode());
			result = prime * result + (int) (runId ^ (runId >>> 32));
			result = prime * result
					+ ((severity == null) ? 0 : severity.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final ArtifactIns other = (ArtifactIns) obj;
			if (findingTypeId != other.findingTypeId)
				return false;
			if (message == null) {
				if (other.message != null)
					return false;
			} else if (!message.equals(other.message))
				return false;
			if (primary == null) {
				if (other.primary != null)
					return false;
			} else if (!primary.equals(other.primary))
				return false;
			if (priority == null) {
				if (other.priority != null)
					return false;
			} else if (!priority.equals(other.priority))
				return false;
			if (runId != other.runId)
				return false;
			if (severity == null) {
				if (other.severity != null)
					return false;
			} else if (!severity.equals(other.severity))
				return false;
			return true;
		}

	}

	private static class SourceIns implements Ins {
		private long id;
		private Long hash;
		private int lineOfCode;
		private int endLineOfCode;
		private IdentifierType type;
		private String identifier;

		private CompUnitIns compUnit;

		SourceIns() {
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public Long getHash() {
			return hash;
		}

		public void setHash(Long hash) {
			this.hash = hash;
		}

		public int getLineOfCode() {
			return lineOfCode;
		}

		public void setLineOfCode(int lineOfCode) {
			this.lineOfCode = lineOfCode;
		}

		public int getEndLineOfCode() {
			return endLineOfCode;
		}

		public void setEndLineOfCode(int endLineOfCode) {
			this.endLineOfCode = endLineOfCode;
		}

		public IdentifierType getType() {
			return type;
		}

		public void setType(IdentifierType type) {
			this.type = type;
		}

		public String getIdentifier() {
			return identifier;
		}

		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}

		public CompUnitIns getCompUnit() {
			return compUnit;
		}

		public void setCompUnit(CompUnitIns compUnit) {
			this.compUnit = compUnit;
		}

		// COMPILATION_UNIT_ID,HASH,LINE_OF_CODE,END_LINE_OF_CODE,LOCATION_TYPE,IDENTIFIER
		public int fill(PreparedStatement st, int idx) throws SQLException {
			st.setLong(idx++, compUnit.getId());
			setNullableLong(idx++, st, hash);
			st.setInt(idx++, lineOfCode);
			st.setInt(idx++, endLineOfCode);
			if (type != null) {
				st.setString(idx++, type.name());
			} else {
				st.setNull(idx++, Types.VARCHAR);
			}
			setNullableString(idx++, st, identifier);
			return idx;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((compUnit == null) ? 0 : compUnit.hashCode());
			result = prime * result + endLineOfCode;
			result = prime * result + ((hash == null) ? 0 : hash.hashCode());
			result = prime * result
					+ ((identifier == null) ? 0 : identifier.hashCode());
			result = prime * result + lineOfCode;
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final SourceIns other = (SourceIns) obj;
			if (compUnit == null) {
				if (other.compUnit != null)
					return false;
			} else if (!compUnit.equals(other.compUnit))
				return false;
			if (endLineOfCode != other.endLineOfCode)
				return false;
			if (hash == null) {
				if (other.hash != null)
					return false;
			} else if (!hash.equals(other.hash))
				return false;
			if (identifier == null) {
				if (other.identifier != null)
					return false;
			} else if (!identifier.equals(other.identifier))
				return false;
			if (lineOfCode != other.lineOfCode)
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}

	}

	private static class CompUnitIns implements Ins {
		private long id;
		private String path;
		private String className;
		private String packageName;

		CompUnitIns() {
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		public String getPackageName() {
			return packageName;
		}

		public void setPackageName(String packageName) {
			this.packageName = packageName;
		}

		// PATH,CLASS_NAME,PACKAGE_NAME
		public int fill(PreparedStatement st, int idx) throws SQLException {
			setNullableString(idx++, st, path);
			assert className != null;
			assert packageName != null;
			st.setString(idx++, className);
			st.setString(idx++, packageName);
			return idx;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((className == null) ? 0 : className.hashCode());
			result = prime * result
					+ ((packageName == null) ? 0 : packageName.hashCode());
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final CompUnitIns other = (CompUnitIns) obj;
			if (className == null) {
				if (other.className != null)
					return false;
			} else if (!className.equals(other.className))
				return false;
			if (packageName == null) {
				if (other.packageName != null)
					return false;
			} else if (!packageName.equals(other.packageName))
				return false;
			if (path == null) {
				if (other.path != null)
					return false;
			} else if (!path.equals(other.path))
				return false;
			return true;
		}

	}

	private static class ArtSourceRel implements Ins {
		private ArtifactIns artifact;
		private SourceIns source;

		ArtSourceRel(ArtifactIns artifact, SourceIns source) {
			this.artifact = artifact;
			this.source = source;
		}

		// ARTIFACT_ID,SOURCE_LOCATION_ID
		public int fill(PreparedStatement st, int idx) throws SQLException {
			st.setLong(idx++, artifact.getId());
			st.setLong(idx++, source.getId());
			return idx;
		}

		public long getId() {
			// No-op, since we don't need this id
			return 0;
		}

		public void setId(long id) {
			// No-op, since we don't need this id
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((artifact == null) ? 0 : artifact.hashCode());
			result = prime * result
					+ ((source == null) ? 0 : source.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final ArtSourceRel other = (ArtSourceRel) obj;
			if (artifact == null) {
				if (other.artifact != null)
					return false;
			} else if (!artifact.equals(other.artifact))
				return false;
			if (source == null) {
				if (other.source != null)
					return false;
			} else if (!source.equals(other.source))
				return false;
			return true;
		}
	}

	private static class ToolIns implements Ins {
		private long id;
		private final String tool;
		private final String mnemonic;

		ToolIns(String tool, String mnemonic) {
			this.tool = tool;
			this.mnemonic = mnemonic;
		}

		public int fill(PreparedStatement st, int idx) throws SQLException {
			st.setString(idx++, tool);
			st.setString(idx++, mnemonic);
			return idx;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((mnemonic == null) ? 0 : mnemonic.hashCode());
			result = prime * result + ((tool == null) ? 0 : tool.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final ToolIns other = (ToolIns) obj;
			if (mnemonic == null) {
				if (other.mnemonic != null)
					return false;
			} else if (!mnemonic.equals(other.mnemonic))
				return false;
			if (tool == null) {
				if (other.tool != null)
					return false;
			} else if (!tool.equals(other.tool))
				return false;
			return true;
		}

	}

	// TODO We may be able to speed up here by keeping a map of recent ids
	private long getFindingTypeId(String tool, String mnemonic) {
		ToolIns ins = new ToolIns(tool, mnemonic);
		try {
			ins.fill(toolIdSelect, 1);
			ResultSet set = toolIdSelect.executeQuery();
			set.next();
			return set.getLong(1);
		} catch (SQLException e) {
			sqlError(e);
		}
		// We should never reach this line
		throw new RuntimeException();
	}

	private static void sqlError(SQLException e) {
		throw new RunPersistenceException(
				"Exception raised while persisting artifacts to database.", e);
	}

	private static void setNullableString(int idx, PreparedStatement st,
			String string) throws SQLException {
		if (string == null) {
			st.setNull(idx, Types.VARCHAR);
		} else {
			st.setString(idx, string);
		}
	}

	private static void setNullableLong(int idx, PreparedStatement st,
			Long longValue) throws SQLException {
		if (longValue == null) {
			st.setNull(idx, Types.VARCHAR);
		} else {
			st.setLong(idx, longValue);
		}
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

		private ArtifactIns artifact;
		private long runId;

		public JDBCArtifactBuilder(long runId) {
			this.runId = runId;
			clear();
		}

		public void build() {
			artifacts.add(artifact);
			if (artifacts.size() == COMMIT_SIZE) {
				persist();
			}
			clear();
		}

		public ArtifactBuilder findingType(String tool, String mnemonic) {
			artifact.setFindingTypeId(getFindingTypeId(tool, mnemonic));
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
			artifact = new ArtifactIns();
			artifact.setRunId(runId);
		}

		private class JDBCSourceLocationBuilder implements
				SourceLocationBuilder {

			private final SourceIns sourceIns;
			private final CompUnitIns compUnit;
			private final boolean primary;

			public JDBCSourceLocationBuilder(boolean primary) {
				sourceIns = new SourceIns();
				compUnit = new CompUnitIns();
				this.primary = primary;
			}

			public void build() {
				CompUnitIns currentComp = compUnits.get(compUnit);
				if (currentComp == null) {
					compUnits.put(compUnit, compUnit);
					currentComp = compUnit;
				}
				sourceIns.setCompUnit(currentComp);
				SourceIns currentSource = sources.get(sourceIns);
				if (currentSource == null) {
					sources.put(sourceIns, sourceIns);
					currentSource = sourceIns;
				}
				if (primary) {
					artifact.setPrimary(currentSource);
				} else {
					ArtSourceRel rel = new ArtSourceRel(artifact, currentSource);
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
