package com.surelogic.sierra.pmd4_0;

import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.surelogic.common.xml.XMLUtil;
import com.surelogic.sierra.setup.AbstractFindingTypeGenerator;
import com.surelogic.sierra.tool.message.ArtifactType;
import com.surelogic.sierra.tool.message.Category;
import com.surelogic.sierra.tool.message.FindingType;

/**
 * Massages the data into the format we want
 * @author Edwin.Chan
 */
public class PMDFindingTypeGenerator extends AbstractFindingTypeGenerator {

	private static final Logger log = Logger.getAnonymousLogger();

	private static final String PMD_URI = "http://pmd.sf.net/ruleset/1.0.0";

	private static final String RULESET = "ruleset";

	private static final String RULE = "rule";

	private static final String NAME = "name";

	private static final String INFO = "description";

	private static final String EXAMPLE = "example";

	private StringBuilder buffer = new StringBuilder();

	private boolean isInfo;
	private boolean isExample;

	private Category category;
	private FindingType type;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new PMDFindingTypeGenerator().parse();
	}

	public void parse() {
		SAXParser sp = XMLUtil.createSAXParser();
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
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
	    XMLUtil.parseResource(log, sp, 
	                          cl.getResourceAsStream("com/surelogic/sierra/pmd4_0/" + fileName), 
	                          this, "Could not parse a PMD ruleset");
		}
		printFindingTypes();
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
				category.setId(category.getName());
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
}
