package com.surelogic.sierra.jsure;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.tool.*;
import com.surelogic.sierra.jdbc.tool.ToolBuilder.ArtifactTypeBuilder;

public class JSure0_9ToolInfoGenerator {
	/**
	 * Load the rulesets and persist them to the embedded database.
	 * 
	 */
	public static void generateTool(Connection conn) {
		try {
			ArtifactTypeBuilder atb = ToolBuilder.getBuilder(conn).build("JSure", "0.9");
			atb.category("JSure");
			atb.info("Something related to JSure");
			atb.link("");
			atb.mnemonic("JSure");
			atb.build();
		} catch (SQLException e) {
			throw new IllegalStateException("Problem with JSure tool info", e);
		}
	}
}
