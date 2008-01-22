package com.surelogic.sierra.findbugs1_3_0;

import java.sql.Connection;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;

import com.surelogic.sierra.findbugs.*;
import com.surelogic.sierra.jdbc.tool.ToolBuilder.ArtifactTypeBuilder;

public class FindBugs1_3_0ToolInfoGenerator extends AbstractFBToolInfoGenerator {

	private static final Logger log = Logger
			.getLogger(FindBugs1_3_0ToolInfoGenerator.class.getName());

	/**
	 * Load the rulesets and persist them to the embedded database.
	 * 
	 */
	public static void generateTool(Connection conn) {
    SAXParser sp = createSAXParser();
		ArtifactTypeBuilder t = createArtifactTypeBuilder(conn, "1.3.0");
    parseResources(log, sp, t, "com/surelogic/sierra/findbugs1_3_0/messages.xml",
                               "com/surelogic/sierra/findbugs1_3_0/findbugs.xml");
	}
}
