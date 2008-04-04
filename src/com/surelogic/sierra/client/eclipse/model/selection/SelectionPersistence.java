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

import com.surelogic.adhoc.eclipse.Activator;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.xml.Entities;

public final class SelectionPersistence {

	public static final String VERSION = "version";
	public static final String TOP = "saved-finding-selections";
	public static final String SELECTION = "selection";
	public static final String NAME = "name";
	public static final String SHOWING = "showing-finding-set";
	public static final String FILTER = "filter";
	public static final String TYPE = "type";
	public static final String POROUS = "porous";
	public static final String VALUE = "value";
	
  public static final String COLUMN = "column";
  public static final String VISIBLE = "visible";
  public static final String WIDTH = "width";
  public static final String SORT = "sort";
  public static final String INDEX = "index";
	
	public static void save(final SelectionManager manager, final File saveFile) {
		save(manager, manager.getSavedSelectionNames(), saveFile);
	}

	public static void save(final SelectionManager manager,
			final List<String> selections, final File saveFile) {
		try {
			final PrintWriter pw = new PrintWriter(new FileWriter(saveFile));
			final StringBuilder b = new StringBuilder();
			outputXMLHeader(pw, b);
			for (String name : selections) {
				Selection s = manager.getSavedSelection(name);
				if (s == null)
					throw new IllegalStateException(
							"null selection returned for \"" + name + "\"");
				outputSelection(pw, b, name, s);
			}
			outputXMLFooter(pw, b);
			pw.close();

		} catch (IOException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					"Failure to persist selections to " + saveFile, e);
		}
	}

	/**
	 * Maintains the invariant that the buffer is cleared
	 * after each use
	 */
	private static void outputBuffer(PrintWriter pw, StringBuilder b) {
	  pw.println(b.toString());
	  b.setLength(0);
	}
	
	private static void outputXMLHeader(PrintWriter pw, StringBuilder b) {
		pw.println("<?xml version='1.0' encoding='" + Activator.XML_ENCODING
				+ "' standalone='yes'?>");
		// No need to clear, since just created
		// b.setLength(0);
		b.append("<").append(TOP);
		Entities.addAttribute(VERSION, "1.0", b);
		b.append(">"); // don't end this element
		outputBuffer(pw, b);
	}

	// private static final String[] INDENTS = { "", "  ", "    ", "      " };
	
	private static void outputSelection(PrintWriter pw, StringBuilder b,
	                                    String name, Selection s) {
		b.append("  <").append(SELECTION);
		Entities.addAttribute(NAME, name, b);
		if (s.isShowingFindings())
			Entities.addAttribute(SHOWING, "Y", b);
		b.append(">");
    outputBuffer(pw, b);
    
		for (Filter f : s.getFilters()) {
			outputFilter(pw, b, f);
		}
	  for (Column c : s.getColumns()) {
		  outputColumn(pw, b, c);
		}
    end(pw, b, SELECTION);
	}

	private static void outputFilter(PrintWriter pw, StringBuilder b, Filter f) {
		b.append("    <").append(FILTER);
		Entities.addAttribute(TYPE, f.getFactory().getFilterLabel(), b);
		b.append(">");
		outputBuffer(pw, b);
		for (String p : f.getPorousValues()) {
			b.append("      <");
			b.append(POROUS);
			Entities.addAttribute(VALUE, p, b);
			b.append("/>");
	    outputBuffer(pw, b);
		}
		end(pw, b, FILTER);
	}
	
  private static void outputColumn(PrintWriter pw, StringBuilder b, Column c) {
    b.append("    <").append(COLUMN);
    Entities.addAttribute(NAME, c.getName(), b);
    if (c.isVisible()) {
      Entities.addAttribute(VISIBLE, "true", b);
    }
    Entities.addAttribute(WIDTH, c.getWidth(), b);
    Entities.addAttribute(SORT, c.getSort().toString(), b);
    Entities.addAttribute(INDEX, c.getIndex(), b);
    b.append(">");
    outputBuffer(pw, b);
    end(pw, b, COLUMN);
  }	

  private static void end(PrintWriter pw, final StringBuilder b, String tag) {
		b.append("    </").append(tag).append(">");
    outputBuffer(pw, b);
  }	
	
	private static void outputXMLFooter(PrintWriter pw, StringBuilder b) {
	  end(pw, b, TOP);
	}

	public static void load(final SelectionManager manager, final File saveFile)
			throws Exception {
		if (saveFile.exists()) {
			try {
				InputStream stream = new FileInputStream(saveFile);
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SaveFileReader handler = new SaveFileReader(manager);
				try {
					// Parse the input
					SAXParser saxParser = factory.newSAXParser();
					saxParser.parse(stream, handler);
				} catch (SAXException e) {
					SLLogger.getLogger().log(Level.SEVERE,
							"Problem parsing selections from " + saveFile, e);
				} finally {
					stream.close();
				}
			} catch (Exception e) {
				SLLogger.getLogger().log(Level.SEVERE,
						"Problem reading selections from " + saveFile, e);
			}
		}
	}

	/**
	 * SAX reader for the query save file.
	 */
	static class SaveFileReader extends DefaultHandler {

		private final SelectionManager f_manager;

		SaveFileReader(SelectionManager manager) {
			f_manager = manager;
		}

		private Selection f_workingSelection = null;
		private String f_selectionName = null;
		private Filter f_filter = null;

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			if (name.equals(SELECTION)) {
				f_selectionName = attributes.getValue(NAME);
				final boolean showing = attributes.getValue(SHOWING) != null;
				f_workingSelection = f_manager.construct();
				f_workingSelection.setShowingFindings(showing);
			} else if (name.equals(FILTER)) {
				final String type = attributes.getValue(TYPE);
				if (f_workingSelection != null) {
					boolean found = false;
					for (ISelectionFilterFactory ff : f_workingSelection
							.getAvailableFilters()) {
						if (ff.getFilterLabel().equals(type)) {
							found = true;
							f_filter = f_workingSelection.construct(ff, null);
						}
					}
					if (!found) {
						SLLogger.getLogger().log(
								Level.SEVERE,
								FILTER + " found in XML but type=" + type
										+ " is not a filter type");
					}
				} else {
					SLLogger.getLogger().log(
							Level.SEVERE,
							FILTER + " found in XML but name="
									+ f_selectionName + " and selection="
									+ f_workingSelection);
				}
			} else if (name.equals(POROUS)) {
				final String value = attributes.getValue(VALUE);
				if (f_filter != null) {
					f_filter.setPorousOnLoad(value, true);
				} else {
					SLLogger.getLogger().log(Level.SEVERE,
							POROUS + " found in XML but filter is null");
				}
      } else if (name.equals(COLUMN)) {
        final String id = attributes.getValue(NAME);      
        for (Column c : f_workingSelection.getColumns()) {
          if (c.getName().equals(id)) {
            final String viz   = attributes.getValue(VISIBLE);
            final String width = attributes.getValue(WIDTH);
            final String sort  = attributes.getValue(SORT);
            final String index = attributes.getValue(INDEX);
            c.configure(viz != null, Integer.parseInt(width), 
                        ColumnSort.valueOf(sort), Integer.parseInt(index));
          }
        }
			}
		}

		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException {
			if (name.equals(SELECTION)) {
				if (f_selectionName != null && f_workingSelection != null) {
					f_manager
							.saveSelection(f_selectionName, f_workingSelection);
					f_selectionName = null;
					f_workingSelection = null;
					f_filter = null; // critical to null (used as previous)
				} else {
					SLLogger.getLogger().log(
							Level.SEVERE,
							SELECTION + " ended in XML but name="
									+ f_selectionName + " and selection="
									+ f_workingSelection);
				}
			}
		}
	}
}
