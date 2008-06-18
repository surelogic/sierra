package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uid) {
		uuid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortMessage() {
		return shortMessage;
	}

	public void setShortMessage(String shortMessage) {
		this.shortMessage = shortMessage;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
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

	public static class ScanFilterInfo implements Serializable, Cacheable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5289740701941214936L;
		private String uuid;
		private String name;

		public ScanFilterInfo(String uuid, String name) {
			this.uuid = uuid;
			this.name = name;
		}

		public ScanFilterInfo() {

		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

	public static class CategoryInfo implements Serializable, Cacheable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -9024044811648450240L;
		private String uuid;
		private String name;
		private String description;

		public CategoryInfo() {

		}

		public CategoryInfo(String uuid, String name, String description) {
			this.uuid = uuid;
			this.name = name;
			this.description = description;
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

	}

}
