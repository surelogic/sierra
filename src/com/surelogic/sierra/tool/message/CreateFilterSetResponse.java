package com.surelogic.sierra.tool.message;

import com.sun.xml.internal.txw2.annotation.XmlElement;

@XmlElement
public class CreateFilterSetResponse {

	protected FilterSet set;

	public FilterSet getSet() {
		return set;
	}

	public void setSet(FilterSet set) {
		this.set = set;
	}

}
