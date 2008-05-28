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

	protected List<String> deletions;

	public List<ScanFilter> getScanFilter() {
		if (scanFilter == null) {
			scanFilter = new ArrayList<ScanFilter>();
		}
		return scanFilter;
	}

	public List<String> getDeletions() {
		if (deletions == null) {
			deletions = new ArrayList<String>();
		}
		return deletions;
	}

}
