package com.surelogic.sierra.tool;

import java.util.jar.*;

/**
 * A descriptor for an artifact that can be generated by a tool
 * 
 * @author edwin
 */
public class ArtifactType implements Comparable<ArtifactType> {
	public final String tool;
	public final String version;
	public final String plugin;
	public final String type;
	public final String category;
	private String findingType = null;
	
	private ArtifactType(String tool, String version, String plugin, String type, String category) {
		this.tool = tool;
		this.version = version;
		this.plugin = plugin;
		this.type = type;
		this.category = category;
	}
	
	public static ArtifactType create(IToolFactory factory, Manifest manifest,
			                          String plugin, String type, String category) {
		return create(factory.getId(), factory.getVersion(), manifest, plugin, type, category);
	}
	
	public static ArtifactType create(String tool, String version, Manifest manifest,
			                          final String plugin, final String type, String category) {
		final Attributes findingTypeMap, categoryMap;
		if (manifest != null) {
			findingTypeMap = manifest.getAttributes(ToolUtil.FINDING_TYPE_MAPPING_KEY);
			categoryMap = manifest.getAttributes(ToolUtil.CATEGORY_MAPPING_KEY);
		} else {
			findingTypeMap = categoryMap = null;
		}
		if (categoryMap != null) {
			String tmpCategory = categoryMap.getValue(type);
			if (tmpCategory != null) {
				category = tmpCategory;				
			}
		}		
		ArtifactType t = new ArtifactType(tool, version, plugin, type, category);
		if (findingTypeMap != null) {
			String findingType = findingTypeMap.getValue(type);
			if (findingType != null) {
				t.setFindingType(findingType);
			}
		}
		return t;
	}
	
	public void setFindingType(String type) {
		findingType = type;		
	}
	
	public String getFindingType() {
		return findingType;
	}
	
	@Override
	public int hashCode() {
		return tool.hashCode() + version.hashCode() + type.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ArtifactType) {
			ArtifactType a = (ArtifactType) o;
			return tool.equals(a.tool) && version.equals(a.version) && type.equals(a.type);
		}
		return false;
	}

	public int compareTo(ArtifactType a) {
		int rv = tool.compareTo(a.tool);
		if (rv == 0) {
			rv = version.compareTo(a.version);
			if (rv == 0) {
				rv = type.compareTo(a.type);
			}
		}
		return rv;
	}
}
