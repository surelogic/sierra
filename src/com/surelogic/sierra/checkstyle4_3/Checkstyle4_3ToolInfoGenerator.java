package com.surelogic.sierra.checkstyle4_3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.surelogic.sierra.jdbc.tool.ToolBuilder;
import com.surelogic.sierra.jdbc.tool.ToolBuilder.ArtifactTypeBuilder;
import com.surelogic.sierra.tool.message.ArtifactType;
import com.surelogic.sierra.tool.message.Category;
import com.surelogic.sierra.tool.message.FindingType;
import com.surelogic.sierra.tool.message.FindingTypes;
import com.surelogic.sierra.tool.message.MessageWarehouse;

public class Checkstyle4_3ToolInfoGenerator {

	public static void generateTool(Connection conn) {
		try {
			ArtifactTypeBuilder t = ToolBuilder.getBuilder(conn).build(
					"Checkstyle", "4.3");
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(
							Thread
									.currentThread()
									.getContextClassLoader()
									.getResourceAsStream(
											"com/surelogic/sierra/checkstyle4_3/artifact_types")));
			StringBuffer artifactType = new StringBuffer();
			String line;
			String category = null;
			line = reader.readLine();
			while (line != null) {
				line = reader.readLine();
				category = line.trim();
				line = reader.readLine();
				while (line != null && !"".equals(line.trim())) {
					if (line.startsWith("\t")) {
						artifactType.append("\n");
						artifactType.append(line.substring(1));
					} else {
						if (artifactType.length() > 0) {
							String val = artifactType.toString();
							String mnemonic = val
									.substring(0, val.indexOf(" "));
							String message = val.substring(val.indexOf(" "))
									.trim();
							t.mnemonic(mnemonic + "Check").info(message).category(
									category).build();
							artifactType.setLength(0);
						}
						artifactType.append(line);
					}
					line = reader.readLine();
				}
			}
			if (artifactType.length() > 0) {
				String val = artifactType.toString();
				String mnemonic = val.substring(0, val.indexOf(" "));
				String message = val.substring(val.indexOf(" ")).trim();
				t.mnemonic(mnemonic + "Check").info(message).category(category).build();
				artifactType.setLength(0);
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Could not build FindBugs tool.", e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public static void generateFindingTypes() {
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(
							Thread
									.currentThread()
									.getContextClassLoader()
									.getResourceAsStream(
											"com/surelogic/sierra/checkstyle4_3/artifact_types")));
			FindingTypes parent = new FindingTypes();
			List<Category> categories = parent.getCategory();
			List<FindingType> types = parent.getFindingType();
			Category category = null;
			StringBuilder fullLine = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				line = reader.readLine();
				category = new Category();
				category.setName(line.trim());
				category.setId(line.trim());
				category.setDescription("");
				categories.add(category);
				line = reader.readLine();
				while (line != null && !"".equals(line.trim())) {
					if (line.startsWith("\t")) {
						fullLine.append("\n");
						fullLine.append(line.substring(1));
					} else {
						if (fullLine.length() > 0) {
							String val = fullLine.toString();
							String mnemonic = val.substring(0, val.indexOf(" "));
							String message = val.substring(val.indexOf(" ")).trim();
							ArtifactType art = new ArtifactType();
							art.setMnemonic(mnemonic + "Check");
							art.setTool("checkstyle");
							mnemonic = mnemonic.substring(mnemonic.lastIndexOf(".") + 1);
							FindingType type = new FindingType();
							type.setId(mnemonic);
							type.setName(mnemonic.replaceAll("([a-z])([A-Z])", "$1 $2"));
							type.getArtifact().add(art);
							type.setShortMessage(message);
							type.setInfo(message);
							category.getFindingType().add(type.getId());
							types.add(type);
							fullLine.setLength(0);
						}
						fullLine.append(line);
					}
					line = reader.readLine();
				}
			}
			if (fullLine.length() > 0) {
				String val = fullLine.toString();
				String mnemonic = val.substring(0, val.indexOf(" "));
				String message = val.substring(val.indexOf(" ")).trim();
				ArtifactType art = new ArtifactType();
				art.setMnemonic(mnemonic + "Check");
				art.setTool("checkstyle");
				mnemonic = mnemonic.substring(mnemonic.lastIndexOf(".") + 1);
				FindingType type = new FindingType();
				type.setId(mnemonic);
				type.setName(mnemonic.replaceAll("([a-z])([A-Z])", "$1 $2"));
				type.getArtifact().add(art);
				type.setShortMessage(message);
				type.setInfo(message);
				category.getFindingType().add(type.getId());
				types.add(type);
			}

			MessageWarehouse.getInstance()
					.writeFindingTypes(parent, System.out);
		} catch (IOException e) {
			// Do nothing
		}
	}

	public static void main(String[] args) {
		generateFindingTypes();
	}

}
