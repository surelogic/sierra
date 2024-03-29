package com.surelogic.sierra.findbugs;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.sierra.setup.AbstractFindingTypeGenerator;
import com.surelogic.sierra.tool.message.*;

/**
 * Common code used by FindBugs apps that help massage the data into the format
 * we want
 * 
 * @author Edwin.Chan
 */
public class AbstractFBFindingTypeGenerator extends AbstractFindingTypeGenerator {
  protected static final String TOOL = "FindBugs";

  protected final Map<String, Category> cMap = new HashMap<>();
  protected final Map<String, FindingType> fMap = new HashMap<>();

  public void parse(String messages, String findbugs) {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    try {
      SAXParser parser = factory.newSAXParser();
      parser.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(messages), new FindingTypeHandler());
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      SAXParser parser = factory.newSAXParser();
      parser.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(findbugs), new CategoryMapper());
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    printFindingTypes();
  }

  @Override
  protected String finishPrettyPrint(StringBuffer sb) {
    String s;
    int firstSpace = sb.indexOf(" ");
    if (firstSpace > 0) {
      s = sb.substring(++firstSpace);
    } else {
      s = sb.toString();
    }
    return s;
  }

  class FindingTypeHandler extends DefaultHandler {
    static final String FINDING_TYPE = "BugPattern";
    static final String MESSAGE = "ShortDescription";
    static final String INFO = "Details";
    static final String NAME = "type";
    static final String CATEGORY = "BugCategory";
    static final String CATEGORY_NAME = "Description";
    static final String CATEGORY_MNEMONIC = "category";
    static final String DESCRIPTION = "Details";
    Category category;
    boolean inCategory;
    FindingType type;
    final StringBuilder buffer = new StringBuilder();
    boolean inType;
    boolean isInfo;
    boolean isMessage;
    boolean isDescription;
    boolean isName;

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      if (isInfo || isMessage || isDescription || isName) {
        buffer.append(ch, start, length);
      }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
      if (inType) {
        if (name.equals(FINDING_TYPE)) {
          types.add(type);
          type = null;
          inType = false;
        } else if (MESSAGE.equals(name)) {
          type.setShortMessage(buffer.toString());
          buffer.setLength(0);
          isMessage = false;
        } else if (INFO.equals(name)) {
          type.setInfo(buffer.toString());
          buffer.setLength(0);
          isInfo = false;
        }
      } else if (inCategory) {
        if (CATEGORY.equals(name)) {
          categories.add(category);
          inCategory = false;
          category = null;
        } else if (DESCRIPTION.equals(name)) {
          category.setDescription(buffer.toString());
          buffer.setLength(0);
          isDescription = false;
        } else if (CATEGORY_NAME.equals(name)) {
          category.setName(buffer.toString());
          category.setId(buffer.toString());
          buffer.setLength(0);
          isName = false;
        }
      }
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
      if (name.equals(FINDING_TYPE)) {
        inType = true;
        type = new FindingType();
        for (int i = 0; i < attributes.getLength(); i++) {
          if (NAME.equals(attributes.getQName(i))) {
            String ftName = attributes.getValue(i);
            fMap.put(ftName, type);
            type.setId(ftName);
            type.setName(prettyPrint(ftName));
          }
        }
        ArtifactType at = new ArtifactType();
        at.setMnemonic(type.getId());
        at.setTool(TOOL);
        type.getArtifact().add(at);
      } else if (inType) {
        if (MESSAGE.equals(name)) {
          isMessage = true;
        } else if (INFO.equals(name)) {
          isInfo = true;
        }
      } else if (CATEGORY.equals(name)) {
        category = new Category();
        inCategory = true;
        for (int i = 0; i < attributes.getLength(); i++) {
          if (CATEGORY_MNEMONIC.equals(attributes.getQName(i))) {
            cMap.put(attributes.getValue(i), category);
          }
        }
      } else if (inCategory) {
        if (DESCRIPTION.equals(name)) {
          isDescription = true;
        }
        if (CATEGORY_NAME.equals(name)) {
          isName = true;
        }
      }
    }
  }

  class CategoryMapper extends DefaultHandler {
    private static final String BUG_PATTERN = "BugPattern";
    private static final String FINDING_TYPE = "type";
    private static final String CATEGORY = "category";

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
      if (BUG_PATTERN.equals(name)) {
        String type = null;
        String category = null;
        for (int i = 0; i < attributes.getLength(); i++) {
          if (FINDING_TYPE.equals(attributes.getQName(i))) {
            type = attributes.getValue(i);
          } else if (CATEGORY.equals(attributes.getQName(i))) {
            category = attributes.getValue(i);
          }
        }
        cMap.get(category).getFindingType().add(fMap.get(type).getId());
      }
    }
  }
}
