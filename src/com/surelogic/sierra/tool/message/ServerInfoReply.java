package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ServerInfoReply {

	private List<ServerIdentity> servers;

	private List<Services> services;

	private String uid;

	private String name;

	public List<Services> getServices() {
		if (services == null) {
			services = new ArrayList<Services>();
		}
		return services;
	}

	public void setServices(final List<Services> s) {
		services = s;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(final String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public List<ServerIdentity> getServers() {
		if (servers == null) {
			servers = new ArrayList<ServerIdentity>();
		}
		return servers;
	}

	public void setServers(final List<ServerIdentity> servers) {
		this.servers = servers;
	}

}
