package com.surelogic.sierra.setup;

public class FindingTypeInfo {
	public final String longDesc;
	public final String shortDesc;
	public final String details;
	public final String link;
	public final String category;
	
	public FindingTypeInfo(String cat, String shortD, String longD, String more, String url) {
		category  = cat;
		shortDesc = shortD;
		longDesc  = longD;
		details   = more;
		link      = url;
	}
}
