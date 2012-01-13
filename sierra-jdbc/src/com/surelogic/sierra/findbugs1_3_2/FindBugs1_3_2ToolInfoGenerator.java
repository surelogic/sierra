package com.surelogic.sierra.findbugs1_3_2;

import java.io.InputStream;
import java.sql.Connection;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;

import com.surelogic.common.xml.XMLUtil;
import com.surelogic.sierra.findbugs.AbstractFBToolInfoGenerator;
import com.surelogic.sierra.jdbc.tool.ToolBuilder.ArtifactTypeBuilder;

public class FindBugs1_3_2ToolInfoGenerator extends AbstractFBToolInfoGenerator {
  private static final Logger log = Logger
      .getLogger(FindBugs1_3_2ToolInfoGenerator.class.getName());

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
    FindBugs1_3_2ToolInfoGenerator gen = new FindBugs1_3_2ToolInfoGenerator(); 
    SAXParser sp = XMLUtil.createSAXParser();
    ArtifactTypeBuilder t = createArtifactTypeBuilder(conn, "1.3.2");
    gen.parseResources(log, sp, t, "com/surelogic/sierra/findbugs1_3_2/messages.xml",
                                   "com/surelogic/sierra/findbugs1_3_2/findbugs.xml");
  }
}