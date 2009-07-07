package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "name", "version", "artifact", "findingType" })
@XmlAccessorType(XmlAccessType.FIELD)
public class Extension {
	@XmlElement(required = true)
	protected String name;
	@XmlElement(required = true)
	protected String version;

	protected List<ExtensionArtifactType> artifact;

	protected List<ExtensionFindingType> findingType;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public List<ExtensionArtifactType> getArtifacts() {
		if (artifact == null) {
			artifact = new ArrayList<ExtensionArtifactType>();
		}
		return artifact;
	}

	public void setArtifacts(final List<ExtensionArtifactType> artifact) {
		this.artifact = artifact;
	}

	public List<ExtensionFindingType> getFindingTypes() {
		if (findingType == null) {
			findingType = new ArrayList<ExtensionFindingType>();
		}
		return findingType;
	}

	public void setFindingTypes(final List<ExtensionFindingType> findingType) {
		this.findingType = findingType;
	}

}
