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

}
