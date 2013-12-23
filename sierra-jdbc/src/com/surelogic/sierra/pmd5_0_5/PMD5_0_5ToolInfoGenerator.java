package com.surelogic.sierra.pmd5_0_5;

import java.sql.Connection;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;

import com.surelogic.common.xml.XMLUtil;
import com.surelogic.sierra.pmd.AbstractPMDToolInfoGenerator;

/**
 * Generates the tool and finding type information for pmd. 
 * 
 * @author nathan
 * 
 */
public final class PMD5_0_5ToolInfoGenerator extends AbstractPMDToolInfoGenerator {
	private static final Logger log = Logger
			.getLogger(PMD5_0_5ToolInfoGenerator.class.getName());

	/**
	 * Load the rulesets and persist them to the embedded database.
	 * 
	 */
	public static void generateTool(Connection conn) {
		PMD5_0_5ToolInfoGenerator handler = new PMD5_0_5ToolInfoGenerator(conn);
    SAXParser sp = XMLUtil.createSAXParser();
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    String rulesetFilenames = getRulesetNames(log, cl.getResourceAsStream(
                                                   "com/surelogic/sierra/pmd5_0_5/rulesets/java/rulesets.properties"));

		for (StringTokenizer st = new StringTokenizer(rulesetFilenames, ","); st
				.hasMoreTokens();) {
			String fileName = st.nextToken();
			XMLUtil.parseResource(log, sp, 
			                      cl.getResourceAsStream("com/surelogic/sierra/pmd5_0_5/" + fileName), 
			                      handler, "Could not parse a PMD ruleset");
		}
	}

	private PMD5_0_5ToolInfoGenerator(Connection conn) {
    super(conn, "5.0.5");
	}
}
