package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@XmlType(propOrder = { "uid", "toolOutput", "config" })
public class Scan {

	@XmlAttribute
	protected String version;

	@XmlElement(required = true)
	protected String uid;
	@XmlElement(required = true)
	protected Config config;
	@XmlElement(required = true)
	protected ToolOutput toolOutput;

	public Scan() {
		// Nothing to do
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	public ToolOutput getToolOutput() {
		return toolOutput;
	}

	public void setToolOutput(ToolOutput toolOutput) {
		this.toolOutput = toolOutput;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getVersion() {
		if (version == null)
			return "";
		else
			return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((config == null) ? 0 : config.hashCode());
		result = prime * result
				+ ((toolOutput == null) ? 0 : toolOutput.hashCode());
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Scan other = (Scan) obj;
		if (config == null) {
			if (other.config != null)
				return false;
		} else if (!config.equals(other.config))
			return false;
		if (toolOutput == null) {
			if (other.toolOutput != null)
				return false;
		} else if (!toolOutput.equals(other.toolOutput))
			return false;
		if (uid == null) {
			if (other.uid != null)
				return false;
		} else if (!uid.equals(other.uid))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

}
