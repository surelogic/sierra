package com.surelogic.sierra.pmd;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.surelogic.sierra.setup.AbstractFindingTypeXMLParser;
import com.surelogic.sierra.setup.FindingTypeInfo;

class PMDRulesXMLReader extends AbstractFindingTypeXMLParser {	

  private static final String PMD_URI = "http://pmd.sf.net/ruleset/1.0.0";

  private static final String RULESET = "ruleset";

  private static final String RULE = "rule";

  private static final String NAME = "name";
  
  private static final String LINK = "externalInfoUrl";

  private static final String INFO = "description";

  private static final String MESSAGE = "message";
  
  private String ruleset;
  private String shortDesc;
  private String link;

  PMDRulesXMLReader() {
	  super(RULE, NAME, null, null, INFO);
  }
  
  @Override
  public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws SAXException {
    if (PMD_URI.equals(uri)) {
      if (RULESET.equals(localName)) {
        ruleset = attributes.getValue(NAME);
      } else {
    	super.startElement(uri, localName, qName, attributes);
      }
    }
  }
  
  @Override
  protected void processAttributes(Attributes attributes) {
	  shortDesc = attributes.getValue(MESSAGE);
	  link = attributes.getValue(LINK);
  }
  
  @Override
  protected FindingTypeInfo newInfo(String name) {
	final String info = this.info.toString();
	return new FindingTypeInfo(ruleset, shortDesc, info, info, link);
  }
  
  @Override
  public void endElement(String uri, String localName, String qName)
      throws SAXException {
    if (PMD_URI.equals(uri)) {
      if (RULESET.equals(localName)) {
        ruleset = null;
      } else {
    	super.endElement(uri, localName, qName);
      }
    }
  }
}

