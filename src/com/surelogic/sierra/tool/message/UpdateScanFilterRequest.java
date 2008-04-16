package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UpdateScanFilterRequest {
	protected ScanFilter filter;

	public ScanFilter getFilter() {
		return filter;
	}

	public void setFilter(ScanFilter filter) {
		this.filter = filter;
	}
}
