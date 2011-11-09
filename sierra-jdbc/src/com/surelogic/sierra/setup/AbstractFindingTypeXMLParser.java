/**
 * 
 */
package com.surelogic.sierra.setup;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.sierra.setup.FindingTypeInfo;

public class AbstractFindingTypeXMLParser extends DefaultHandler {
	private final String FINDING;
	private final String NAME;
	private final String SHORT_DESC;
	private final String LONG_DESC;
	private final String INFO;

	private final Map<String, FindingTypeInfo> infoMap = new HashMap<String, FindingTypeInfo>();

	private String name;

	private String activeTag;
	private StringBuilder activeBuffer;

	protected final StringBuilder info = new StringBuilder();
	protected final StringBuilder longD = new StringBuilder();
	protected final StringBuilder shortD = new StringBuilder();

	/**
	 * Parameters are the XML tag names to be matched
	 */
	protected AbstractFindingTypeXMLParser(String type, String name, 
			                               String shortD, String longD, String info) {
		FINDING = type;
		NAME = name;
		SHORT_DESC = shortD;
		LONG_DESC = longD;
		INFO = info;
	}
	
	protected FindingTypeInfo newInfo(String name) {
		return new FindingTypeInfo(null, shortD.toString(), longD.toString(), info.toString(), null);
	}
	
	@Override
	public final void characters(char[] ch, int start, int length) throws SAXException {
		if (activeBuffer == null) {
			return;			
		}
		activeBuffer.append(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (FINDING.equals(localName)) {
			infoMap.put(name, newInfo(name));
			name = null;
		} else if (activeTag != null && activeTag.equals(localName)) {
			activeTag = null;
			activeBuffer = null;
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (FINDING.equals(localName)) {
			name = attributes.getValue(NAME);
			processAttributes(attributes);
		} else if (name != null) {
			if (SHORT_DESC != null && SHORT_DESC.equals(localName)) {							
				activeBuffer = shortD;
			}
			else if (LONG_DESC != null && LONG_DESC.equals(localName)) {
				activeBuffer = longD;
			}
			else if (INFO != null && INFO.equals(localName)) {
				activeBuffer = info;
			}
			else {
				activeBuffer = findBuffer(localName);
			}
			if (activeBuffer != null) {
				activeTag = localName;
				activeBuffer.setLength(0);
			}
		}
	}
	
	protected void processAttributes(Attributes attributes) {
		// Nothing to do right now
	}

	protected StringBuilder findBuffer(String name) {
		return null;
	}

	public final Map<String, FindingTypeInfo> getInfoMap() {
		return infoMap;
	}
}
