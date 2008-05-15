package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ServerInfoReply {

	private List<Services> services;

	private String uid;

	public List<Services> getServices() {
		if (services == null) {
			services = new ArrayList<Services>();
		}
		return services;
	}

	public void setServices(List<Services> s) {
		services = s;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

}
