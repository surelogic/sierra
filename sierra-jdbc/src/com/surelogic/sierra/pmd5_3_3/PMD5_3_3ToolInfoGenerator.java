package com.surelogic.sierra.pmd5_3_3;

import java.sql.Connection;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;

import com.surelogic.common.xml.XMLUtil;
import com.surelogic.sierra.pmd.PMD2_0_0ToolInfoGenerator;

/**
 * Generates the tool and finding type information for pmd.
 *
 * @author nathan
 *
 */
public final class PMD5_3_3ToolInfoGenerator extends PMD2_0_0ToolInfoGenerator {
    private static final Logger log = Logger
            .getLogger(PMD5_3_3ToolInfoGenerator.class.getName());

    /**
     * Load the rulesets and persist them to the embedded database.
     * 
     */
    public static void generateTool(Connection conn) {
        PMD5_3_3ToolInfoGenerator handler = new PMD5_3_3ToolInfoGenerator(conn);
        SAXParser sp = XMLUtil.createSAXParser();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String rulesetFilenames = getRulesetNames(
                log,
                cl.getResourceAsStream("com/surelogic/sierra/pmd5_3_3/rulesets/java/rulesets.properties"));

        for (StringTokenizer st = new StringTokenizer(rulesetFilenames, ","); st
                .hasMoreTokens();) {
            final String fileName = st.nextToken();
            final boolean success = XMLUtil.parseResource(
                    log,
                    sp,
                    cl.getResourceAsStream("com/surelogic/sierra/pmd5_3_3/"
                            + fileName), handler,
                    "Could not parse a PMD ruleset");
            if (success) {
            	System.out.println("Parsed: "+fileName);
            }
        }
    }

    private PMD5_3_3ToolInfoGenerator(Connection conn) {
        super(conn, "5.3.3");
    }
}
