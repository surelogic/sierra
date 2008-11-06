/**
 * 
 */
package com.surelogic.sierra.findbugs;

import com.surelogic.sierra.setup.AbstractFindingTypeXMLParser;

/**
 * Parses a findbugs message file, and gets the information for all findbugs
 * finding types.
 * 
 * @author nathan
 * 
 */
class FindBugsMessageParser extends AbstractFindingTypeXMLParser {
	private static final String BUG_PATTERN = "BugPattern";

	private static final String NAME = "type";

	private static final String SHORT_DESC = "ShortDescription";

	private static final String LONG_DESC = "LongDescription";
    
	private static final String INFO = "Details";

	FindBugsMessageParser() {
		super(BUG_PATTERN, NAME, SHORT_DESC, LONG_DESC, INFO);
	}
}
