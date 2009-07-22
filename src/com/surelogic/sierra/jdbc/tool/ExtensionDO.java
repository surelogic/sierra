package com.surelogic.sierra.jdbc.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtensionDO {
	private String name;
	private String version;
	private final Map<String, List<ArtifactTypeDO>> artifactMap = new HashMap<String, List<ArtifactTypeDO>>();
	private final List<FindingTypeDO> newFindingTypes = new ArrayList<FindingTypeDO>();
	private final Map<String, List<String>> categoryMap = new HashMap<String, List<String>>();

	public ExtensionDO() {

	}

	public ExtensionDO(final String name, final String version) {
		this.name = name;
		this.version = version;
	}

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

	public void addType(final String findingType, final ArtifactTypeDO type) {
		List<ArtifactTypeDO> types = null;
		if ((types = artifactMap.get(findingType)) == null) {
			types = new ArrayList<ArtifactTypeDO>();
			artifactMap.put(findingType, types);
		}
		types.add(type);
	}

	public List<ArtifactTypeDO> putTypes(final String findingType,
			final List<ArtifactTypeDO> value) {
		return artifactMap.put(findingType, value);
	}

	public boolean addFindingType(final FindingTypeDO e, final String category) {
		List<String> catTypes = categoryMap.get(category);
		if (catTypes == null) {
			catTypes = new ArrayList<String>();
			categoryMap.put(category, catTypes);
		}
		catTypes.add(e.getUid());
		return newFindingTypes.add(e);
	}

	public boolean addFindingType(final FindingTypeDO e) {
		return newFindingTypes.add(e);
	}

	public List<ArtifactTypeDO> getTypes(final String key) {
		return artifactMap.get(key);
	}

	public Map<String, List<ArtifactTypeDO>> getArtifactMap() {
		return artifactMap;
	}

	public List<FindingTypeDO> getNewFindingTypes() {
		return newFindingTypes;
	}

	public Map<String, List<String>> getCategoryMap() {
		return categoryMap;
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
		final ExtensionDO other = (ExtensionDO) obj;
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
