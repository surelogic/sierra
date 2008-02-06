package com.surelogic.sierra.cpd;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.tool.ToolBuilder;
import com.surelogic.sierra.jdbc.tool.ToolBuilder.ArtifactTypeBuilder;

public class CPD4_1ToolInfoGenerator {
  public static void generateTool(Connection conn) {
    
    try {
      ArtifactTypeBuilder rule = ToolBuilder.getBuilder(conn).build("CPD", "4.1");
      rule.mnemonic("DuplicatedCode");
      rule.category("Code Duplication");
      rule.info("Found duplicated code");
      rule.link("http://pmd.sourceforge.net/cpd.html");
      rule.build();
    } catch (SQLException e) {
      throw new IllegalStateException("Could not build CPD tool info for v.4.1", e);      
    }
  }
}
