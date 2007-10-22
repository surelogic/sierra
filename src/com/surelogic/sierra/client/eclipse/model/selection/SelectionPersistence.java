package com.surelogic.sierra.client.eclipse.model.selection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;

import com.surelogic.adhoc.Activator;
import com.surelogic.common.Entities;
import com.surelogic.common.logging.SLLogger;

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
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(saveFile));
			outputXMLHeader(pw);
			for (String name : manager.getSavedSelectionNames()) {
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
		Entities.addAttribute(SHOWING, s.showingSelection() ? "Y" : "N", b);
		b.append(">");
		pw.println(b.toString());
		b = new StringBuilder();
		for (Filter f : s.getFilters()) {
			outputFilter(pw, f);
		}
		b = new StringBuilder();
		b.append("  </").append(SELECTION).append(">");
		pw.println(b.toString());
	}

	private static void outputFilter(PrintWriter pw, Filter f) {
		StringBuilder b = new StringBuilder();
		b.append("    <").append(FILTER);
		Entities.addAttribute(TYPE, f.getFactory().getFilterLabel(), b);
		b.append(">");
		pw.println(b.toString());
		b = new StringBuilder();
		for (String p : f.getPouousValues()) {
			b.append("      <");
			b.append(POROUS);
			Entities.addAttribute(VALUE, p, b);
			b.append("/>");
			pw.println(b.toString());
			b = new StringBuilder();
		}
		b = new StringBuilder();
		b.append("    </").append(FILTER).append(">");
		pw.println(b.toString());
	}

	private static void outputXMLFooter(PrintWriter pw) {
		pw.println("</" + TOP + ">");
	}

	public static void load(final SelectionManager manager, final File saveFile) {
		// TODO
	}

}
