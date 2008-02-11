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

import com.surelogic.adhoc.Activator;
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

	public static void save(final SelectionManager manager, final File saveFile) {
		save(manager, manager.getSavedSelectionNames(), saveFile);
	}

	public static void save(final SelectionManager manager,
			final List<String> selections, final File saveFile) {
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(saveFile));
			outputXMLHeader(pw);
			for (String name : selections) {
				Selection s = manager.getSavedSelection(name);
				if (s == null)
					throw new IllegalStateException(
							"null selection returned for \"" + name + "\"");
				outputSelection(pw, name, s);
			}
			outputXMLFooter(pw);
			pw.close();

		} catch (IOException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					"Failure to persist selections to " + saveFile, e);
		}
	}

	private static void outputXMLHeader(PrintWriter pw) {
		pw.println("<?xml version='1.0' encoding='" + Activator.XML_ENCODING
				+ "' standalone='yes'?>");
		StringBuilder b = new StringBuilder();
		b.append("<").append(TOP);
		Entities.addAttribute(VERSION, "1.0", b);
		b.append(">"); // don't end this element
		pw.println(b.toString());
	}

	private static void outputSelection(PrintWriter pw, String name, Selection s) {
		StringBuilder b = new StringBuilder();
		b.append("  <").append(SELECTION);
		Entities.addAttribute(NAME, name, b);
		if (s.isShowingFindings())
			Entities.addAttribute(SHOWING, "Y", b);
		b.append(">");
		pw.println(b.toString());
		// UNUSED: b = new StringBuilder();
		for (Filter f : s.getFilters()) {
			outputFilter(pw, f);
		}
		b = new StringBuilder();
		b.append("  </").append(SELECTION).append(">");
		pw.println(b.toString());
	}

	private static void outputFilter(PrintWriter pw, Filter f) {
		final StringBuilder b = new StringBuilder();
		b.append("    <").append(FILTER);
		Entities.addAttribute(TYPE, f.getFactory().getFilterLabel(), b);
		b.append(">");
		pw.println(b.toString());
		b.setLength(0);
		for (String p : f.getPorousValues()) {
			b.append("      <");
			b.append(POROUS);
			Entities.addAttribute(VALUE, p, b);
			b.append("/>");
			pw.println(b.toString());
			b.setLength(0);
		}
		b.setLength(0);
		b.append("    </").append(FILTER).append(">");
		pw.println(b.toString());
	}

	private static void outputXMLFooter(PrintWriter pw) {
		pw.println("</" + TOP + ">");
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
