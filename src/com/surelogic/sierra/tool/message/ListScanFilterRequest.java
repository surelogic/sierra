package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlAccessorType(XmlAccessType.FIELD)
public class ListScanFilterRequest {

	protected List<ServerRevision> servers;

	public List<ServerRevision> getServerRevisions() {
		if (servers == null) {
			servers = new ArrayList<ServerRevision>();
		}
		return servers;
	}
}
