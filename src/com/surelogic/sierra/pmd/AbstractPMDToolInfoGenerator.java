package com.surelogic.sierra.pmd;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.sierra.jdbc.tool.ToolBuilder;
import com.surelogic.sierra.jdbc.tool.ToolBuilder.ArtifactTypeBuilder;

public class AbstractPMDToolInfoGenerator extends DefaultHandler {
  private static final String PMD_URI = "http://pmd.sf.net/ruleset/1.0.0";

  private static final String RULESET = "ruleset";

  private static final String RULE = "rule";

  private static final String NAME = "name";

  private static final String LINK = "externalInfoUrl";

  private static final String INFO = "description";

  private String ruleset;

  private ArtifactTypeBuilder rule;
  
  // Indicates that we are currently parsing the description attribute.
  private boolean isInfo;

  private final StringBuilder info = new StringBuilder();

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
  public void characters(char[] ch, int start, int length)
      throws SAXException {
    if (isInfo) {
      info.append(ch, start, length);
    }
  }
  
  @Override
  public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws SAXException {
    if (PMD_URI.equals(uri)) {
      if (RULESET.equals(localName)) {
        ruleset = attributes.getValue(NAME);
      } else if (RULE.equals(localName)) {
        rule.mnemonic(attributes.getValue(NAME)).link(
            attributes.getValue(LINK)).category(ruleset);
      } else if (INFO.equals(localName)) {
        isInfo = rule != null;
        if (isInfo) {
          info.setLength(0);
        }
      }
    }
  }
  
  @Override
  public void endElement(String uri, String localName, String qName)
      throws SAXException {
    if (PMD_URI.equals(uri)) {
      if (RULESET.equals(localName)) {
        ruleset = null;
      } else if (RULE.equals(localName)) {
        try {
          rule.build();
        } catch (SQLException e) {
          throw new IllegalStateException(
              "Could not build PMD tool info.", e);
        }
      } else if (INFO.equals(localName)) {
        if (isInfo) {
          rule.info(info.toString());
          isInfo = false;
        }
      }
    }
  }
}

