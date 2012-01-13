package com.surelogic.sierra.findbugs2_0_0;

import java.io.InputStream;
import java.sql.Connection;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;

import com.surelogic.common.xml.XMLUtil;
import com.surelogic.sierra.findbugs.AbstractFBToolInfoGenerator;
import com.surelogic.sierra.jdbc.tool.ToolBuilder.ArtifactTypeBuilder;

public class FindBugs2_0_0ToolInfoGenerator extends AbstractFBToolInfoGenerator {
  private static final Logger log = Logger
      .getLogger(FindBugs2_0_0ToolInfoGenerator.class.getName());

  /**
   * This needs to be in this class to load the right resources
   */
  protected InputStream getStream(String name) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
  }
  
  /**
   * Load the rulesets and persist them to the embedded database.
   * 
   */
  public static void generateTool(Connection conn) {
    FindBugs2_0_0ToolInfoGenerator gen = new FindBugs2_0_0ToolInfoGenerator(); 
    SAXParser sp = XMLUtil.createSAXParser();
    // NEED to change these
    ArtifactTypeBuilder t = createArtifactTypeBuilder(conn, "2.0.0");
    gen.parseResources(log, sp, t, "com/surelogic/sierra/findbugs2_0_0/messages.xml",
                                   "com/surelogic/sierra/findbugs2_0_0/findbugs.xml");
  }
}
