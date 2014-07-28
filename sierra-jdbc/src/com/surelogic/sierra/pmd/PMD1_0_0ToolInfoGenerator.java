package com.surelogic.sierra.pmd;

import java.sql.Connection;

public class PMD1_0_0ToolInfoGenerator extends AbstractPMDToolInfoGenerator {

    protected PMD1_0_0ToolInfoGenerator(Connection conn, String version) {
        super(conn, version);
    }

    private static final String PMD_URI = "http://pmd.sf.net/ruleset/1.0.0";

    @Override
    protected String getPMDURI() {
        return PMD_URI;
    }

}
