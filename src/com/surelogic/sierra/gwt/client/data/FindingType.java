package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.gwt.client.data.cache.Cacheable;

public class FindingType implements Cacheable, Serializable {
	private static final long serialVersionUID = 1766277814214421247L;

	private String uuid;
	private String name;
	private String shortMessage;
	private String info;
	private List<String> reportedBy;
	private List<CategoryInfo> categoriesIncluding;
	private List<CategoryInfo> categoriesExcluding;
	private List<ScanFilterInfo> scanFiltersIncluding;
	private List<ArtifactTypeInfo> artifactTypes;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(final String uid) {
		uuid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getShortMessage() {
		return shortMessage;
	}

	public void setShortMessage(final String shortMessage) {
		this.shortMessage = shortMessage;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(final String info) {
		this.info = info;
	}

	public List<String> getReportedBy() {
		if (reportedBy == null) {
			reportedBy = new ArrayList<String>();
		}
		return reportedBy;
	}

	public List<CategoryInfo> getCategoriesIncluding() {
		if (categoriesIncluding == null) {
			categoriesIncluding = new ArrayList<CategoryInfo>();
		}
		return categoriesIncluding;
	}

	public List<CategoryInfo> getCategoriesExcluding() {
		if (categoriesExcluding == null) {
			categoriesExcluding = new ArrayList<CategoryInfo>();
		}
		return categoriesExcluding;
	}

	public List<ScanFilterInfo> getScanFiltersIncluding() {
		if (scanFiltersIncluding == null) {
			scanFiltersIncluding = new ArrayList<ScanFilterInfo>();
		}
		return scanFiltersIncluding;
	}

	public List<ArtifactTypeInfo> getArtifactTypes() {
		if (artifactTypes == null) {
			artifactTypes = new ArrayList<ArtifactTypeInfo>();
		}
		return artifactTypes;
	}

	/**
	 * Describes a scan filter. Equality is based on uuid.
	 * 
	 * @author nathan
	 * 
	 */
	public static class ScanFilterInfo implements Serializable, Cacheable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5289740701941214936L;
		private String uuid;
		private String name;

		public ScanFilterInfo(final String uuid, final String name) {
			this.uuid = uuid;
			this.name = name;
		}

		public ScanFilterInfo() {
			// Do nothing
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(final String uuid) {
			this.uuid = uuid;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
			final ScanFilterInfo other = (ScanFilterInfo) obj;
			if (uuid == null) {
				if (other.uuid != null) {
					return false;
				}
			} else if (!uuid.equals(other.uuid)) {
				return false;
			}
			return true;
		}

	}

	/**
	 * Describes an artifact type. Equality is based on the tool and artifact
	 * type properties.
	 * 
	 * @author nathan
	 * 
	 */
	public static class ArtifactTypeInfo implements Serializable {

		private String tool;
		private String artifactType;
		private List<String> versions;

		public ArtifactTypeInfo() {
			// Do nothing
		}

		public ArtifactTypeInfo(final String tool, final String artifactType) {
			this.tool = tool;
			this.artifactType = artifactType;
		}

		public String getTool() {
			return tool;
		}

		public void setTool(final String tool) {
			this.tool = tool;
		}

		public String getArtifactType() {
			return artifactType;
		}

		public void setArtifactType(final String artifactType) {
			this.artifactType = artifactType;
		}

		public List<String> getVersions() {
			if (versions == null) {
				versions = new ArrayList<String>();
			}
			return versions;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((artifactType == null) ? 0 : artifactType.hashCode());
			result = prime * result + ((tool == null) ? 0 : tool.hashCode());
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
			final ArtifactTypeInfo other = (ArtifactTypeInfo) obj;
			if (artifactType == null) {
				if (other.artifactType != null) {
					return false;
				}
			} else if (!artifactType.equals(other.artifactType)) {
				return false;
			}
			if (tool == null) {
				if (other.tool != null) {
					return false;
				}
			} else if (!tool.equals(other.tool)) {
				return false;
			}
			return true;
		}

	}

	/**
	 * Describes a category. Equality is based on uuid.
	 * 
	 * @author nathan
	 * 
	 */
	public static class CategoryInfo implements Serializable, Cacheable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -9024044811648450240L;
		private String uuid;
		private String name;
		private String description;

		public CategoryInfo() {
			// Do nothing
		}

		public CategoryInfo(final String uuid, final String name,
				final String description) {
			this.uuid = uuid;
			this.name = name;
			this.description = description;
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(final String uuid) {
			this.uuid = uuid;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(final String description) {
			this.description = description;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
			final CategoryInfo other = (CategoryInfo) obj;
			if (uuid == null) {
				if (other.uuid != null) {
					return false;
				}
			} else if (!uuid.equals(other.uuid)) {
				return false;
			}
			return true;
		}

	}

}
