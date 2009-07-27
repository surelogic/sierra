package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for the extension complex type. An extension represents a set of
 * additional detectors built for a core tool, as well as the accompanying
 * metadata. A filter set is uniquely identified both by a name and by a
 * version. The metadata is comprised of the set of artifacts generated by these
 * detectors, as well as any new finding types that they are associated with. No
 * finding types need be listed if all of the artifacts map to built- in finding
 * types.
 * 
 * TODO An extension may also define one or more filter sets (categories).
 * 
 * @author nathan
 * 
 */
@XmlType(propOrder = { "name", "version", "artifact", "findingType" })
@XmlAccessorType(XmlAccessType.FIELD)
public class Extension {
	@XmlElement(required = true)
	protected String name;
	@XmlElement(required = true)
	protected String version;
	@XmlElement(required = true)
	protected String path;

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

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (version == null ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Extension other = (Extension) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		return true;
	}

}
