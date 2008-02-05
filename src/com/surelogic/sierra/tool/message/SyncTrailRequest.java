package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
public class SyncTrailRequest {

	private Merge merge;
	private List<Audit> audits;

	public Merge getMerge() {
		return merge;
	}

	public void setMerge(Merge merge) {
		this.merge = merge;
	}

	public List<Audit> getAudits() {
		if (audits == null) {
			audits = new ArrayList<Audit>();
		}
		return audits;
	}

	public void setAudits(List<Audit> audits) {
		this.audits = audits;
	}

}
