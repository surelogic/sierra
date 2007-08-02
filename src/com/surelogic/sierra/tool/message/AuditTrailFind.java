package com.surelogic.sierra.tool.message;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlRootElement
public class AuditTrailFind {
	List<Match> match;
	Qualifiers qualifiers;

	public List<Match> getMatch() {
		return match;
	}

	public void setMatch(List<Match> match) {
		this.match = match;
	}

	public Qualifiers getQualifiers() {
		return qualifiers;
	}

	public void setQualifiers(Qualifiers qualifiers) {
		this.qualifiers = qualifiers;
	}

}
