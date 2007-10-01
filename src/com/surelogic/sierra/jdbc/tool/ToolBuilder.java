package com.surelogic.sierra.jdbc.tool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToolBuilder {

	private static final String INSERT_TOOL = "INSERT INTO TOOL (NAME,VERSION) VALUES (?,?)";

	private static final String INSERT_ARTIFACT_TYPE = "INSERT INTO ARTIFACT_TYPE (TOOL_ID, MNEMONIC, MNEMONIC_DISPLAY, CATEGORY, LINK, INFO) VALUES (?,?,?,?,?,?)";

	private final PreparedStatement insertTool;
	private final PreparedStatement insertFindingType;

	private ToolBuilder(Connection conn) {
		try {
			this.insertTool = conn.prepareStatement(INSERT_TOOL,
					Statement.RETURN_GENERATED_KEYS);
			this.insertFindingType = conn
					.prepareStatement(INSERT_ARTIFACT_TYPE);
		} catch (SQLException e) {
			throw new IllegalStateException(
					"Could not persist tool into database", e);
		}
	}

	public ArtifactTypeBuilder build(String name, String version)
			throws SQLException {
		insertTool.setString(1, name);
		insertTool.setString(2, version);
		insertTool.executeUpdate();
		ResultSet set = insertTool.getGeneratedKeys();
		try {
			set.next();
			return new ArtifactTypeBuilder(set.getLong(1));
		} finally {
			set.close();
		}
	}

	public class ArtifactTypeBuilder {
		private final long toolId;
		private String mnemonic;
		private String mnemonicDisplay;
		private String category;
		private String link;
		private String info;

		public ArtifactTypeBuilder mnemonic(String mnemonic) {
			this.mnemonic = mnemonic;
			this.mnemonicDisplay = prettyPrint(mnemonic);
			return this;
		}

		public ArtifactTypeBuilder category(String category) {
			this.category = prettyPrint(category);
			return this;
		}

		public ArtifactTypeBuilder link(String link) {
			this.link = link;
			return this;
		}

		public ArtifactTypeBuilder info(String info) {
			this.info = info;
			return this;
		}

		public void build() throws SQLException {
			int i = 1;
			insertFindingType.setLong(i++, toolId);
			insertFindingType.setString(i++, mnemonic);
			insertFindingType.setString(i++, mnemonicDisplay);
			insertFindingType.setString(i++, category);
			insertFindingType.setString(i++, link);
			insertFindingType.setString(i++, info);
			insertFindingType.executeUpdate();
			clear();
		}

		private void clear() {
			this.mnemonic = null;
			this.mnemonicDisplay = null;
			this.category = null;
			this.link = null;
			this.info = null;
		}

		private final Pattern underscoresToSpaces = Pattern.compile("_");
		private final Pattern breakUpWords = Pattern
				.compile("([A-Z][a-z]+)(?=[A-Z])");
		private final Pattern allButFirstLetter = Pattern
				.compile("(?<=\\b[A-Z])([A-Z]+)(?=\\b)");
		private final StringBuffer sb = new StringBuffer();

		private String prettyPrint(String s) {
			s = underscoresToSpaces.matcher(s).replaceAll(" ");
			s = breakUpWords.matcher(s).replaceAll("$1 ");
			Matcher m = allButFirstLetter.matcher(s);
			while (m.find()) {
				String replacement = m.group().toLowerCase();
				m.appendReplacement(sb, replacement);
			}
			m.appendTail(sb);
			s = sb.toString();
			sb.setLength(0);
			return s;
		}

		private ArtifactTypeBuilder(long toolId) {
			this.toolId = toolId;
		}

	}

	public static ToolBuilder getBuilder(Connection conn) {
		return new ToolBuilder(conn);
	}

}
