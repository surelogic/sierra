package com.surelogic.sierra.cpd;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.tool.ToolBuilder;
import com.surelogic.sierra.jdbc.tool.ToolBuilder.ArtifactTypeBuilder;

public class CPD5_0_5ToolInfoGenerator {
  public static void generateTool(Connection conn) {
    
    try {
      ArtifactTypeBuilder rule = ToolBuilder.getBuilder(conn).build("CPD", "5.0.5");
      rule.mnemonic("DuplicatedCode");
      rule.category("Code Duplication");
      rule.info("Found duplicated code");
      rule.link("http://pmd.sourceforge.net/cpd.html");
      rule.build();
    } catch (SQLException e) {
      throw new IllegalStateException("Could not build CPD tool info for v.5.0.5", e);      
    }
  }
}
