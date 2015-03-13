package com.surelogic.sierra.findbugs3_0_1;

import java.io.InputStream;
import java.sql.Connection;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;

import com.surelogic.common.xml.XMLUtil;
import com.surelogic.sierra.findbugs.AbstractFBToolInfoGenerator;
import com.surelogic.sierra.jdbc.tool.ToolBuilder.ArtifactTypeBuilder;

public class FindBugs3_0_1ToolInfoGenerator extends AbstractFBToolInfoGenerator {
  private static final Logger log = Logger
      .getLogger(FindBugs3_0_1ToolInfoGenerator.class.getName());

  /**
   * This needs to be in this class to load the right resources
   */
  @Override
  protected InputStream getStream(String name) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
  }
  
  /**
   * Load the rulesets and persist them to the embedded database.
   * 
   */
  public static void generateTool(Connection conn) {
    FindBugs3_0_1ToolInfoGenerator gen = new FindBugs3_0_1ToolInfoGenerator(); 
    SAXParser sp = XMLUtil.createSAXParser();
    // NEED to change these
    ArtifactTypeBuilder t = createArtifactTypeBuilder(conn, "3.0.1");
    gen.parseResources(log, sp, t, "com/surelogic/sierra/findbugs3_0_1/messages.xml",
                                   "com/surelogic/sierra/findbugs3_0_1/findbugs.xml");
  }
}
