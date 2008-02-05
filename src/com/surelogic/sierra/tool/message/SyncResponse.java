package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
@XmlRootElement
public class SyncResponse {

	private List<SyncTrailResponse> trails;

	public List<SyncTrailResponse> getTrails() {
		if (trails == null) {
			trails = new ArrayList<SyncTrailResponse>();
		}
		return trails;
	}

	public void setTrails(List<SyncTrailResponse> trails) {
		this.trails = trails;
	}

}
