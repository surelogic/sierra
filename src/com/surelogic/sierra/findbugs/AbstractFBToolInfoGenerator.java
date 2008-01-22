package com.surelogic.sierra.findbugs;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.sierra.jdbc.tool.ToolBuilder;
import com.surelogic.sierra.jdbc.tool.ToolBuilder.ArtifactTypeBuilder;

public abstract class AbstractFBToolInfoGenerator {
  protected static SAXParser createSAXParser() {
    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setValidating(true);
    spf.setNamespaceAware(true);
    SAXParser sp;
    try {
      sp = spf.newSAXParser();
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException("Could not create SAX parser.", e);
    } catch (SAXException e) {
      throw new IllegalStateException("Could not create SAX parser.", e);
    }
    return sp;
  }
  
  protected static ArtifactTypeBuilder createArtifactTypeBuilder(Connection conn, String version) {
    ArtifactTypeBuilder t;
    try {
      t = ToolBuilder.getBuilder(conn).build("FindBugs", version);
    } catch (SQLException e) {
      throw new IllegalStateException("Could not build FindBugs tool.", e);
    }
    return t;
  }
  
  private static void parseResource(Logger log, SAXParser sp, String resource,
      DefaultHandler handler) {
    try {
      sp.parse(Thread.currentThread().getContextClassLoader()
          .getResourceAsStream(resource), handler);
    } catch (SAXException e) {
      log.log(Level.WARNING, "Could not parse a FindBugs pattern", e);
    } catch (IOException e) {
      log.log(Level.WARNING, "Could not parse a FindBugs pattern", e);
    }
  }
  
  protected static void parseResources(Logger log, SAXParser sp, 
                                       ArtifactTypeBuilder t, 
                                       String messages, String fb) {
    FindBugsMessageParser messageParser = new FindBugsMessageParser();
    parseResource(log, sp, messages, messageParser);
    FindBugsParser findingParser = new FindBugsParser(t, messageParser
        .getInfoMap());
    parseResource(log, sp, fb, findingParser);
  }
}
