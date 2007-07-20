package com.surelogic.sierra.message;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType
public class Qualifiers {

	private List<String> qualifier;

	public List<String> getQualifier() {
		return qualifier;
	}

	public void setQualifier(List<String> qualifier) {
		this.qualifier = qualifier;
	}

}
