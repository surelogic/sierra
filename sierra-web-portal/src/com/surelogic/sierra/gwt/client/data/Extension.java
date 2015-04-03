package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.gwt.client.data.cache.Cacheable;

public class Extension implements Serializable, Cacheable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6350261711540971624L;

	private String name;
	private String version;

	private boolean installed;

	private List<FindingType> findingTypes;
	private List<ArtifactType> artifactTypes;

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

	public boolean isInstalled() {
		return installed;
	}

	public void setInstalled(final boolean installed) {
		this.installed = installed;
	}

	public String getUuid() {
		return name + "___" + version;
	}

	public List<FindingType> getFindingTypes() {
		if (findingTypes == null) {
			findingTypes = new ArrayList<FindingType>();
		}
		return findingTypes;
	}

	public List<ArtifactType> getArtifactTypes() {
		if (artifactTypes == null) {
			artifactTypes = new ArrayList<ArtifactType>();
		}
		return artifactTypes;
	}

	public static class FindingType implements Serializable, Cacheable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5523545165017223146L;
		private String name;
		private String uuid;

		public FindingType() {
		}

		public FindingType(final String uuid, final String name) {
			this.uuid = uuid;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(final String uuid) {
			this.uuid = uuid;
		}

	}

	public static class ArtifactType implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2069657984977781121L;
		private String tool;
		private String name;

		public ArtifactType() {

		}

		public ArtifactType(final String tool, final String name) {
			this.tool = tool;
			this.name = name;
		}

		public String getTool() {
			return tool;
		}

		public void setTool(final String tool) {
			this.tool = tool;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public String getUuid() {
			return tool + "___" + name;
		}

	}

}
