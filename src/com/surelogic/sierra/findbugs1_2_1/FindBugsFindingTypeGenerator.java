package com.surelogic.sierra.findbugs1_2_1;

import com.surelogic.sierra.findbugs.AbstractFBFindingTypeGenerator;

/**
 * Massages the data into the format we want
 * @author Edwin.Chan
 */
public class FindBugsFindingTypeGenerator extends AbstractFBFindingTypeGenerator {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new FindBugsFindingTypeGenerator().parse();
	}

	public void parse() {
    parse("com/surelogic/sierra/findbugs1_2_1/messages.xml",
          "com/surelogic/sierra/findbugs1_2_1/findbugs.xml");
	}
}
