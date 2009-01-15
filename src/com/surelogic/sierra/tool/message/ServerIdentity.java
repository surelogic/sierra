package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class ServerIdentity {

	private String server;
	private String name;
	private long revision;

	public ServerIdentity() {
	};

	public ServerIdentity(final String server, final String name,
			final long revision) {
		this.server = server;
		this.name = name;
		this.revision = revision;
	}

	public String getServer() {
		return server;
	}

	public void setServer(final String server) {
		this.server = server;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public long getRevision() {
		return revision;
	}

	public void setRevision(final long revision) {
		this.revision = revision;
	}

}
