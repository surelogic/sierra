package com.surelogic.sierra.client.eclipse.model.selection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.common.SLUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.xml.Entities;

public class ColumnPersistence {

  public static final String TOP = "columns-for-findings-view";
  public static final String NAME = "name";
  public static final String COLUMN = "column";
  public static final String WIDTH = "width";
  public static final String SORT = "sort";
  public static final String INDEX = "index";
  public static final String VERSION = "version";

  public static void save(List<Column> columns, File saveFile) {
    try {
      final PrintWriter pw = new PrintWriter(new FileWriter(saveFile));
      final StringBuilder b = new StringBuilder();
      outputXMLHeader(pw, b);
      for (Column column : columns) {
        outputColumn(pw, b, column);
      }
      outputXMLFooter(pw, b);
      pw.close();

    } catch (IOException e) {
      SLLogger.getLogger().log(Level.SEVERE, "Failure to persist columns to " + saveFile, e);
    }
  }

  private static void outputXMLHeader(PrintWriter pw, StringBuilder b) {
    pw.println("<?xml version='1.0' encoding='" + SLUtility.ENCODING + "' standalone='yes'?>");
    // No need to clear, since just created
    // b.setLength(0);
    b.append("<").append(TOP);
    Entities.addAttribute(VERSION, "1.0", b);
    b.append(">"); // don't end this element
    outputBuffer(pw, b);
  }

  private static void outputColumn(PrintWriter pw, StringBuilder b, Column c) {
    b.append("    <").append(COLUMN);
    Entities.addAttribute(NAME, c.getTitle(), b);
    if (c.hasUserSetWidth())
      Entities.addAttribute(WIDTH, c.getUserSetWidth(), b);
    Entities.addAttribute(SORT, c.getSort().toString(), b);
    Entities.addAttribute(INDEX, c.getIndex(), b);
    b.append(">");
    outputBuffer(pw, b);
    end(pw, b, COLUMN);
  }

  /**
   * Maintains the invariant that the buffer is cleared after each use
   */
  private static void outputBuffer(PrintWriter pw, StringBuilder b) {
    pw.println(b.toString());
    b.setLength(0);
  }

  private static void end(PrintWriter pw, final StringBuilder b, String tag) {
    b.append("    </").append(tag).append(">");
    outputBuffer(pw, b);
  }

  private static void outputXMLFooter(PrintWriter pw, StringBuilder b) {
    end(pw, b, TOP);
  }

  /**
   * Loads in the saved column information from the passed file into the already
   * full list of column information. The passed list is not mutated, but its
   * elements are.
   * 
   * @param columns
   *          the populated list of table columns.
   * @param saveFile
   *          the input file.
   */
  public static void load(List<Column> columns, final File saveFile) {
    if (saveFile.exists() && saveFile.length() > 0) {
      try {
        InputStream stream = new FileInputStream(saveFile);
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SaveFileReader handler = new SaveFileReader(columns);
        try {
          // Parse the input
          SAXParser saxParser = factory.newSAXParser();
          saxParser.parse(stream, handler);
        } catch (SAXException e) {
          SLLogger.getLogger().log(Level.SEVERE, "Problem parsing columns from " + saveFile, e);
        } finally {
          stream.close();
        }
      } catch (Exception e) {
        SLLogger.getLogger().log(Level.SEVERE, "Problem reading columns from " + saveFile, e);
      }
    }
  }

  /**
   * SAX reader for the query save file.
   */
  static class SaveFileReader extends DefaultHandler {

    private final List<Column> f_populatedColumns;
    private Column f_workingColumn = null;

    SaveFileReader(List<Column> populatedColumns) {
      f_populatedColumns = populatedColumns;
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
      if (name.equals(COLUMN)) {
        final String id = attributes.getValue(NAME);
        for (Column c : f_populatedColumns) {
          if (c.getTitle().equals(id)) {
            f_workingColumn = c;
            break;
          }
        }
        if (f_workingColumn != null) {
          final String width = attributes.getValue(WIDTH);
          if (width != null) {
            f_workingColumn.setUserSetWidth(Integer.parseInt(width));
          }
          final String sort = attributes.getValue(SORT);
          if (sort != null) {
            f_workingColumn.setSort(ColumnSort.valueOf(sort));
          }
          final String index = attributes.getValue(INDEX);
          if (index != null) {
            final int i = Integer.parseInt(index);
            if (0 < i && i < f_populatedColumns.size()) {
              f_workingColumn.setIndex(i);
            }
          }
        }
      }
    }
  }
}
