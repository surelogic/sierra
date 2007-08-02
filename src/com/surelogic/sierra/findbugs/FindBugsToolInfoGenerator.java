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

import com.surelogic.sierra.jdbc.ToolBuilder;
import com.surelogic.sierra.jdbc.ToolBuilder.FindingTypeBuilder;

public class FindBugsToolInfoGenerator {

	private static final Logger log = Logger
			.getLogger(FindBugsToolInfoGenerator.class.getName());

	/**
	 * Load the rulesets and persist them to the embedded database.
	 * 
	 */
	public static void generateTool(Connection conn) {
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
		FindingTypeBuilder t;
		try {
			t = ToolBuilder.getBuilder(conn).name("FindBugs");
		} catch (SQLException e) {
			throw new IllegalStateException("Could not build FindBugs tool.", e);
		}
		FindBugsMessageParser messageParser = new FindBugsMessageParser();
		parseResource(sp, "com/surelogic/sierra/findbugs/messages.xml",
				messageParser);
		FindBugsParser findingParser = new FindBugsParser(t, messageParser
				.getInfoMap());
		parseResource(sp, "com/surelogic/sierra/findbugs/findbugs.xml",
				findingParser);
	}

	private static void parseResource(SAXParser sp, String resource,
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

}
