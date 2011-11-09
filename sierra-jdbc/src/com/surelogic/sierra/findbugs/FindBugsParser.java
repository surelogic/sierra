/**
 * 
 */
package com.surelogic.sierra.findbugs;

import java.sql.SQLException;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.sierra.jdbc.tool.ToolBuilder.ArtifactTypeBuilder;
import com.surelogic.sierra.setup.FindingTypeInfo;

/**
 * @author nathan
 * 
 */
class FindBugsParser extends DefaultHandler {

	static final String BUG_PATTERN = "BugPattern";

	static final String TYPE = "type";

	static final String CATEGORY = "category";

	private final Map<String, FindingTypeInfo> infoMap;

	private final ArtifactTypeBuilder builder;

	public FindBugsParser(ArtifactTypeBuilder findingTypeBuilder,
			Map<String, FindingTypeInfo> infoMap) {
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
						mnemonic).info(infoMap.get(mnemonic).details).build();
			} catch (SQLException e) {
				throw new IllegalStateException(
						"SQL exception encountered while writing out Findbugs finding types.",
						e);
			}
		}
	}

}
