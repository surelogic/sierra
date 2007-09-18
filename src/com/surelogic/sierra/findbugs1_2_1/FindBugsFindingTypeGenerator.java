package com.surelogic.sierra.findbugs1_2_1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.media.sound.HsbParser;
import com.surelogic.sierra.tool.message.ArtifactType;
import com.surelogic.sierra.tool.message.Category;
import com.surelogic.sierra.tool.message.FindingType;
import com.surelogic.sierra.tool.message.FindingTypes;
import com.surelogic.sierra.tool.message.MessageWarehouse;

public class FindBugsFindingTypeGenerator {

	private static final String TOOL = "FindBugs";

	private List<FindingType> types = new ArrayList<FindingType>();
	private List<Category> categories = new ArrayList<Category>();
	private HashMap<String, Category> cMap = new HashMap<String, Category>();
	private HashMap<String, FindingType> fMap = new HashMap<String, FindingType>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new FindBugsFindingTypeGenerator().parse();
	}

	public void parse() {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			parser.parse(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(
							"com/surelogic/sierra/findbugs1_2_1/messages.xml"),
					new FindingTypeHandler());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FindingTypes ft = new FindingTypes();
		ft.getFindingType().addAll(types);
		ft.getCategory().addAll(categories);
		try {
			SAXParser parser = factory.newSAXParser();
			parser.parse(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(
							"com/surelogic/sierra/findbugs1_2_1/findbugs.xml"),
					new CategoryMapper());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		MessageWarehouse.getInstance().writeFindingTypes(ft, System.out);
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
		int firstSpace = sb.indexOf(" ");
		if (firstSpace > 0) {
			s = sb.substring(++firstSpace);
		} else {
			s = sb.toString();
		}
		sb.setLength(0);
		return s;
	}

	private class FindingTypeHandler extends DefaultHandler {
		private static final String FINDING_TYPE = "BugPattern";
		private static final String MESSAGE = "ShortDescription";
		private static final String INFO = "Details";
		private static final String NAME = "type";
		private static final String CATEGORY = "BugCategory";
		private static final String CATEGORY_NAME = "Description";
		private static final String CATEGORY_MNEMONIC = "category";
		private static final String DESCRIPTION = "Details";
		private Category category;
		private boolean inCategory;
		private FindingType type;
		private StringBuilder buffer = new StringBuilder();
		private boolean inType;
		private boolean isInfo;
		private boolean isMessage;
		private boolean isDescription;
		private boolean isName;

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (isInfo || isMessage || isDescription || isName) {
				buffer.append(ch, start, length);
			}
		}

		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException {
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
					buffer.setLength(0);
					isName = false;
				}
			}
		}

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			if (name.equals(FINDING_TYPE)) {
				inType = true;
				type = new FindingType();
				for (int i = 0; i < attributes.getLength(); i++) {
					if (NAME.equals(attributes.getQName(i))) {
						String ftName = attributes.getValue(i);
						fMap.put(ftName, type);
						type.setName(prettyPrint(ftName));
					}
				}
				ArtifactType at = new ArtifactType();
				at.setMnemonic(type.getName());
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

	private class CategoryMapper extends DefaultHandler {
		private static final String BUG_PATTERN = "BugPattern";
		private static final String FINDING_TYPE = "type";
		private static final String CATEGORY = "category";

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
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
				cMap.get(category).getFindingType().add(
						fMap.get(type).getName());
			}
		}

	}
}
