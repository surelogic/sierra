package com.surelogic.sierra.pmd4_0;

import java.sql.Connection;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;

import com.surelogic.common.xml.XMLUtil;
import com.surelogic.sierra.pmd.PMD1_0_0ToolInfoGenerator;

/**
 * Generates the tool and finding type information for pmd. The rulesets were
 * taken from CVS HEAD on 06/18/2007.
 *
 * @author nathan
 *
 */
public final class PMD4_0ToolInfoGenerator extends PMD1_0_0ToolInfoGenerator {
    private static final Logger log = Logger
            .getLogger(PMD4_0ToolInfoGenerator.class.getName());

    /**
     * Load the rulesets and persist them to the embedded database.
     * 
     */
    public static void generateTool(Connection conn) {
        PMD4_0ToolInfoGenerator handler = new PMD4_0ToolInfoGenerator(conn);
        SAXParser sp = XMLUtil.createSAXParser();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String rulesetFilenames = getRulesetNames(
                log,
                cl.getResourceAsStream("com/surelogic/sierra/pmd4_0/rulesets.properties"));

        for (StringTokenizer st = new StringTokenizer(rulesetFilenames, ","); st
                .hasMoreTokens();) {
            String fileName = st.nextToken();
            XMLUtil.parseResource(
                    log,
                    sp,
                    cl.getResourceAsStream("com/surelogic/sierra/pmd4_0/"
                            + fileName), handler,
                    "Could not parse a PMD ruleset");
        }
    }

    private PMD4_0ToolInfoGenerator(Connection conn) {
        super(conn, "4.0");
    }
}
