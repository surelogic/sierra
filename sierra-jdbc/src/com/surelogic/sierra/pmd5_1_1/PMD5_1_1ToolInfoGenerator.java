package com.surelogic.sierra.pmd5_1_1;

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
public final class PMD5_1_1ToolInfoGenerator extends PMD2_0_0ToolInfoGenerator {
    private static final Logger log = Logger
            .getLogger(PMD5_1_1ToolInfoGenerator.class.getName());

    /**
     * Load the rulesets and persist them to the embedded database.
     * 
     */
    public static void generateTool(Connection conn) {
        PMD5_1_1ToolInfoGenerator handler = new PMD5_1_1ToolInfoGenerator(conn);
        SAXParser sp = XMLUtil.createSAXParser();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String rulesetFilenames = getRulesetNames(
                log,
                cl.getResourceAsStream("com/surelogic/sierra/pmd5_1_1/rulesets/java/rulesets.properties"));

        for (StringTokenizer st = new StringTokenizer(rulesetFilenames, ","); st
                .hasMoreTokens();) {
            String fileName = st.nextToken();
            XMLUtil.parseResource(
                    log,
                    sp,
                    cl.getResourceAsStream("com/surelogic/sierra/pmd5_1_1/"
                            + fileName), handler,
                    "Could not parse a PMD ruleset");
        }
    }

    private PMD5_1_1ToolInfoGenerator(Connection conn) {
        super(conn, "5.1.1");
    }
}
