/**
 * 
 */
package com.surelogic.sierra.findbugs;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses a findbugs message file, and gets the information for all findbugs
 * finding types.
 * 
 * @author nathan
 * 
 */
class FindBugsMessageParser extends DefaultHandler {

	private static final String BUG_PATTERN = "BugPattern";

	private static final String NAME = "type";

	private static final String INFO = "Details";

	private Map<String, String> infoMap = new HashMap<String, String>();

	private String name;

	private boolean isInfo;

	private StringBuilder info = new StringBuilder();

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (isInfo) {
			info.append(ch, start, length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (BUG_PATTERN.equals(localName)) {
			infoMap.put(name, info.toString());
			name = null;
		} else if (INFO.equals(localName)) {
			isInfo = false;
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (BUG_PATTERN.equals(localName)) {
			name = attributes.getValue(NAME);
		} else if (INFO.equals(localName)) {
			isInfo = name != null;
			if (isInfo) {
				info.setLength(0);
			}
		}
	}

	public Map<String, String> getInfoMap() {
		return infoMap;
	}

}
