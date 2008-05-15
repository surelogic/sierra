package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ListScanFilterResponse {

	protected List<ScanFilter> scanFilter;

	public List<ScanFilter> getScanFilter() {
		if (scanFilter == null) {
			scanFilter = new ArrayList<ScanFilter>();
		}
		return scanFilter;
	}

	public void setScanFilter(List<ScanFilter> sf) {
		scanFilter = sf;
	}
}
