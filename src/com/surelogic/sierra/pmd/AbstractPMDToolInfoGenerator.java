package com.surelogic.sierra.pmd;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.sierra.jdbc.tool.ToolBuilder;
import com.surelogic.sierra.jdbc.tool.ToolBuilder.ArtifactTypeBuilder;
import com.surelogic.sierra.setup.FindingTypeInfo;

public class AbstractPMDToolInfoGenerator extends PMDRulesXMLReader {
  private ArtifactTypeBuilder rule;

  protected AbstractPMDToolInfoGenerator(Connection conn, String version) {
    try {
      rule = ToolBuilder.getBuilder(conn).build("PMD", version);
    } catch (SQLException e) {
      throw new IllegalStateException("Could not build PMD tool info for v."+version, e);
    }
  }

  public static String getRulesetNames(Logger log, InputStream in) {
    Properties props = new Properties();
    try {
      props.load(in);
    } catch (IOException e) {
      // TODO we probably want to throw an exception here
      log
          .log(Level.SEVERE,
              "Could not load ruleset filenames for PMD", e);
    }
    return props.getProperty("rulesets.filenames");
  }
  
  @Override 
  protected FindingTypeInfo newInfo(String name) {
	  FindingTypeInfo info = super.newInfo(name);
	  // Build artifact
	  rule.mnemonic(name).link(info.link).category(info.category);
      try {
    	  rule.info(info.details).build();
      } catch (SQLException e) {
          throw new IllegalStateException(
              "Could not build PMD tool info.", e);
      }
	  return info;
  }
}

