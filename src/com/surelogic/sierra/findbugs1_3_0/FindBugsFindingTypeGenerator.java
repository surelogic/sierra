package com.surelogic.sierra.findbugs1_3_0;

import com.surelogic.sierra.findbugs.AbstractFBFindingTypeGenerator;

public class FindBugsFindingTypeGenerator extends AbstractFBFindingTypeGenerator {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new FindBugsFindingTypeGenerator().parse();
	}

	public void parse() {
	  parse("com/surelogic/sierra/findbugs1_3_0/messages.xml",
	        "com/surelogic/sierra/findbugs1_3_0/findbugs.xml");
	}
}
