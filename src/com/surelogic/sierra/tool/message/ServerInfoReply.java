package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ServerInfoReply {

	private List<Services> services;

	public List<Services> getServices() {
		if (services == null) {
			services = new ArrayList<Services>();
		}
		return services;
	}

}
