package com.surelogic.sierra.findbugs1_2_1;

import java.sql.Connection;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;

import com.surelogic.common.xml.XMLUtil;
import com.surelogic.sierra.findbugs.*;
import com.surelogic.sierra.jdbc.tool.ToolBuilder.ArtifactTypeBuilder;

public class FindBugs1_2_1ToolInfoGenerator extends AbstractFBToolInfoGenerator {

	private static final Logger log = Logger
			.getLogger(FindBugs1_2_1ToolInfoGenerator.class.getName());

	/**
	 * Load the rulesets and persist them to the embedded database.
	 * 
	 */
	public static void generateTool(Connection conn) {
		SAXParser sp = XMLUtil.createSAXParser();
		ArtifactTypeBuilder t = createArtifactTypeBuilder(conn, "1.2.1");
		parseResources(log, sp, t, "com/surelogic/sierra/findbugs1_2_1/messages.xml",
		                           "com/surelogic/sierra/findbugs1_2_1/findbugs.xml");
	}
}
