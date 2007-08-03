/**
 * 
 */
package com.surelogic.sierra.findbugs;

import java.sql.SQLException;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.sierra.jdbc.tool.ToolBuilder.FindingTypeBuilder;

/**
 * @author nathan
 * 
 */
class FindBugsParser extends DefaultHandler {

	private static final String BUG_PATTERN = "BugPattern";

	private static final String TYPE = "type";

	private static final String CATEGORY = "category";

	private final Map<String, String> infoMap;

	private final FindingTypeBuilder builder;

	public FindBugsParser(FindingTypeBuilder findingTypeBuilder,
			Map<String, String> infoMap) {
		builder = findingTypeBuilder;
		this.infoMap = infoMap;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (BUG_PATTERN.equals(localName)) {
			String mnemonic = attributes.getValue(TYPE);
			try {
				builder.category(attributes.getValue(CATEGORY)).mnemonic(
						mnemonic).info(infoMap.get(mnemonic)).build();
			} catch (SQLException e) {
				throw new IllegalStateException(
						"SQL exception encountered while writing out Findbugs finding types.",
						e);
			}
		}
	}

}
