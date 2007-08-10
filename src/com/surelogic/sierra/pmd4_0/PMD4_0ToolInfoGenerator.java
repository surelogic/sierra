package com.surelogic.sierra.pmd4_0;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.sierra.jdbc.tool.ToolBuilder;
import com.surelogic.sierra.jdbc.tool.ToolBuilder.FindingTypeBuilder;

/**
 * Generates the tool and finding type information for pmd. The rulesets were
 * taken from CVS HEAD on 06/18/2007.
 * 
 * @author nathan
 * 
 */
public class PMD4_0ToolInfoGenerator extends DefaultHandler {

	private static final String PMD_URI = "http://pmd.sf.net/ruleset/1.0.0";

	private static final String RULESET = "ruleset";

	private static final String RULE = "rule";

	private static final String NAME = "name";

	private static final String LINK = "externalInfoUrl";

	private static final String INFO = "description";

	private String ruleset;

	private FindingTypeBuilder rule;

	// Indicates that we are currently parsing the description attribute.
	private boolean isInfo;

	private StringBuilder info;

	private static final Logger log = Logger
			.getLogger(PMD4_0ToolInfoGenerator.class.getName());

	/**
	 * Load the rulesets and persist them to the embedded database.
	 * 
	 */
	public static void generateTool(Connection conn) {
		PMD4_0ToolInfoGenerator handler = new PMD4_0ToolInfoGenerator(conn);
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
		Properties props = new Properties();
		try {
			props.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(
							"com/surelogic/sierra/pmd4_0/rulesets.properties"));
		} catch (IOException e) {
			// TODO we probably want to throw an exception here
			log
					.log(Level.SEVERE,
							"Could not load ruleset filenames for PMD", e);
		}
		String rulesetFilenames = props.getProperty("rulesets.filenames");

		for (StringTokenizer st = new StringTokenizer(rulesetFilenames, ","); st
				.hasMoreTokens();) {
			String fileName = st.nextToken();
			try {
				sp.parse(Thread.currentThread().getContextClassLoader()
						.getResourceAsStream(
								"com/surelogic/sierra/pmd4_0/" + fileName),
						handler);
			} catch (SAXException e) {
				log.log(Level.WARNING, "Could not parse a PMD ruleset", e);
			} catch (IOException e) {
				log.log(Level.WARNING, "Could not parse a PMD ruleset", e);
			}
		}
	}

	private PMD4_0ToolInfoGenerator(Connection conn) {
		try {
			rule = ToolBuilder.getBuilder(conn).build("PMD", "4.0");
		} catch (SQLException e) {
			throw new IllegalStateException("Could not build PMD tool info.", e);
		}
		info = new StringBuilder();
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
