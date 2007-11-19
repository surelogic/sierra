package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
public class TrailObsoletion {
	private String trail;
	private String obsoletedTrail;
	private Long revision;

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

	public Long getRevision() {
		return revision;
	}

	public void setRevision(Long revision) {
		this.revision = revision;
	}

}
