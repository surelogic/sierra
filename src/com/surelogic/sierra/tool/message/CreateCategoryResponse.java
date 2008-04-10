package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CreateCategoryResponse {

	protected FilterSet set;

	public FilterSet getSet() {
		return set;
	}

	public void setSet(FilterSet set) {
		this.set = set;
	}

}
