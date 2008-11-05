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
	static class Info {
		final String longDesc;
		final String shortDesc;
		final String details;
		
		Info(String shortD, String longD, String more) {
			shortDesc = shortD;
			longDesc  = longD;
			details   = more;
		}
	}	
	
	private static final String BUG_PATTERN = "BugPattern";

	private static final String NAME = "type";

	private static final String SHORT_DESC = "ShortDescription";

	private static final String LONG_DESC = "LongDescription";
    
	private static final String INFO = "Details";

	private final Map<String, Info> infoMap = new HashMap<String, Info>();

	private String name;

	private String activeTag;
	private StringBuilder activeBuffer;

	private final StringBuilder info = new StringBuilder();
	private final StringBuilder longD = new StringBuilder();
	private final StringBuilder shortD = new StringBuilder();

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (activeBuffer == null) {
			return;			
		}
		activeBuffer.append(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (BUG_PATTERN.equals(localName)) {
			infoMap.put(name, new Info(shortD.toString(), longD.toString(), info.toString()));
			name = null;
		} else if (activeTag != null && activeTag.equals(localName)) {
			activeTag = null;
			activeBuffer = null;
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (BUG_PATTERN.equals(localName)) {
			name = attributes.getValue(NAME);
		} else if (name != null) {
			if (SHORT_DESC.equals(localName)) {							
				activeBuffer = shortD;
			}
			else if (LONG_DESC.equals(localName)) {
				activeBuffer = longD;
			}
			else if (INFO.equals(localName)) {
				activeBuffer = info;
			}
			if (activeBuffer != null) {
				activeTag = localName;
				activeBuffer.setLength(0);
			}
		}
	}
	
	public Map<String, Info> getInfoMap() {
		return infoMap;
	}

}
