package com.surelogic.sierra.setup;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.sierra.tool.message.Category;
import com.surelogic.sierra.tool.message.FindingType;
import com.surelogic.sierra.tool.message.FindingTypes;
import com.surelogic.sierra.tool.message.MessageWarehouse;

public class AbstractFindingTypeGenerator extends DefaultHandler {
  protected final List<FindingType> types = new ArrayList<FindingType>();
  protected final List<Category> categories = new ArrayList<Category>();
  
  protected void printFindingTypes() {
    FindingTypes ft = new FindingTypes();
    ft.getCategory().addAll(categories);
    ft.getFindingType().addAll(types);
    MessageWarehouse.getInstance().writeFindingTypes(ft, System.out);
  }
  
  private final Pattern underscoresToSpaces = Pattern.compile("_");
  private final Pattern breakUpWords = Pattern
      .compile("([A-Z][a-z]+)(?=[A-Z])");
  private final Pattern allButFirstLetter = Pattern
      .compile("(?<=\\b[A-Z])([A-Z]+)(?=\\b)");
  private final StringBuffer sb = new StringBuffer();
  
  protected String prettyPrint(String s) {
    s = underscoresToSpaces.matcher(s).replaceAll(" ");
    s = breakUpWords.matcher(s).replaceAll("$1 ");
    Matcher m = allButFirstLetter.matcher(s);
    while (m.find()) {
      String replacement = m.group().toLowerCase();
      m.appendReplacement(sb, replacement);
    }
    m.appendTail(sb);
    s = finishPrettyPrint(sb);
    sb.setLength(0);
    return s;
  }
  
  protected String finishPrettyPrint(StringBuffer sb) {
    return sb.toString();
  }
}
