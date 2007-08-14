package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class TrailObsoletion {
	private String trail;
	private String obsoletedTrail;

	public TrailObsoletion() {
		// Do Nothing
	}

	public TrailObsoletion(String obsoletedTrail, String trail) {
		this.trail = trail;
	}

	public String getTrail() {
		return trail;
	}

	public void setTrail(String trail) {
		this.trail = trail;
	}

	public String getObsoletedTrail() {
		return obsoletedTrail;
	}

	public void setObsoletedTrail(String obsoletedTrail) {
		this.obsoletedTrail = obsoletedTrail;
	}

}
