package com.surelogic.sierra.pmd4_0;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.sierra.tool.message.ArtifactType;
import com.surelogic.sierra.tool.message.Category;
import com.surelogic.sierra.tool.message.FindingType;
import com.surelogic.sierra.tool.message.FindingTypes;
import com.surelogic.sierra.tool.message.MessageWarehouse;

public class PMDFindingTypeGenerator extends DefaultHandler {

	private static final Logger log = Logger.getAnonymousLogger();

	private static final String PMD_URI = "http://pmd.sf.net/ruleset/1.0.0";

	private static final String RULESET = "ruleset";

	private static final String RULE = "rule";

	private static final String NAME = "name";

	private static final String LINK = "externalInfoUrl";

	private static final String INFO = "description";

	private static final String EXAMPLE = "example";

	private StringBuilder buffer = new StringBuilder();

	private boolean isInfo;
	private boolean isExample;
	private final List<Category> categories = new ArrayList<Category>();
	private final List<FindingType> types = new ArrayList<FindingType>();

	private Category category;
	private FindingType type;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new PMDFindingTypeGenerator().parse();
	}

	public void parse() {
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
						this);
			} catch (SAXException e) {
				log.log(Level.WARNING, "Could not parse a PMD ruleset", e);
			} catch (IOException e) {
				log.log(Level.WARNING, "Could not parse a PMD ruleset", e);
			}
		}
		FindingTypes ft = new FindingTypes();
		ft.getCategory().addAll(categories);
		ft.getFindingType().addAll(types);
		MessageWarehouse.getInstance().writeFindingTypes(ft, System.out);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (isInfo || isExample) {
			buffer.append(ch, start, length);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (PMD_URI.equals(uri)) {
			if (RULESET.equals(localName)) {
				category = new Category();
				category.setName(attributes.getValue(NAME));
				categories.add(category);
			} else if (RULE.equals(localName)) {
				type = new FindingType();
				String name = attributes.getValue(NAME);
				type.setId(name);
				type.setName(prettyPrint(name));
				ArtifactType at = new ArtifactType();
				at.setMnemonic(name);
				at.setTool("PMD");
				type.getArtifact().add(at);
				category.getFindingType().add(type.getId());
			} else if (INFO.equals(localName)) {
				isInfo = true;
			} else if (EXAMPLE.equals(localName)) {
				isExample = true;
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (PMD_URI.equals(uri)) {
			if (RULESET.equals(localName)) {
				category = null;
			} else if (RULE.equals(localName)) {
				types.add(type);
				type = null;
			} else if (INFO.equals(localName)) {
				String description = buffer.toString();
				description = description.trim();
				buffer.setLength(0);
				if (type != null) {
					type.setInfo(description);
					type.setShortMessage(description);
				} else {
					category.setDescription(description);
				}
				isInfo = false;
			} else if (EXAMPLE.equals(localName)) {
				String example = buffer.toString();
				example = example.trim();
				buffer.setLength(0);
				// type.setInfo(type.getInfo() + "\n\nExample:\n" + example);
			}
		}
	}

	private final Pattern underscoresToSpaces = Pattern.compile("_");
	private final Pattern breakUpWords = Pattern
			.compile("([A-Z][a-z]+)(?=[A-Z])");
	private final Pattern allButFirstLetter = Pattern
			.compile("(?<=\\b[A-Z])([A-Z]+)(?=\\b)");
	private final StringBuffer sb = new StringBuffer();

	private String prettyPrint(String s) {
		s = underscoresToSpaces.matcher(s).replaceAll(" ");
		s = breakUpWords.matcher(s).replaceAll("$1 ");
		Matcher m = allButFirstLetter.matcher(s);
		while (m.find()) {
			String replacement = m.group().toLowerCase();
			m.appendReplacement(sb, replacement);
		}
		m.appendTail(sb);
		s = sb.toString();
		sb.setLength(0);
		return s;
	}
}
