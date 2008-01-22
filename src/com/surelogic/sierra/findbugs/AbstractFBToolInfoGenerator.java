package com.surelogic.sierra.findbugs;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;

import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.common.xml.XMLUtil;
import com.surelogic.sierra.jdbc.tool.ToolBuilder;
import com.surelogic.sierra.jdbc.tool.ToolBuilder.ArtifactTypeBuilder;

public abstract class AbstractFBToolInfoGenerator {
  protected static ArtifactTypeBuilder createArtifactTypeBuilder(Connection conn, String version) {
    ArtifactTypeBuilder t;
    try {
      t = ToolBuilder.getBuilder(conn).build("FindBugs", version);
    } catch (SQLException e) {
      throw new IllegalStateException("Could not build FindBugs tool.", e);
    }
    return t;
  }
  
  /**
   * This needs to be in the package that has the resources
   */
  protected abstract InputStream getStream(String resource);
  
  private void parseResource(Logger log, SAXParser sp, String resource,
      DefaultHandler handler) {
    XMLUtil.parseResource(log, sp, getStream(resource), handler, 
                          "Could not parse a FindBugs pattern");
  }
  
  protected void parseResources(Logger log, SAXParser sp, 
                                       ArtifactTypeBuilder t, 
                                       String messages, String fb) {
    FindBugsMessageParser messageParser = new FindBugsMessageParser();
    parseResource(log, sp, messages, messageParser);
    FindBugsParser findingParser = new FindBugsParser(t, messageParser
        .getInfoMap());
    parseResource(log, sp, fb, findingParser);
  }
}
