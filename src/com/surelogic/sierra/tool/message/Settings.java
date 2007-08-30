package com.surelogic.sierra.tool.message;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
@XmlRootElement
public class Settings {

	private Collection<FindingTypeFilter> ruleFilter;

	public Collection<FindingTypeFilter> getRuleFilter() {
		return ruleFilter;
	}

	public void setRuleFilter(Collection<FindingTypeFilter> ruleFilter) {
		this.ruleFilter = ruleFilter;
	}

}
