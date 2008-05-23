package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ScanFilter implements Serializable, Cacheable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5806872701106755880L;

	private String uuid;

	private String name;

	private long revision;

	private Set<ScanFilterEntry> categories;

	private Set<ScanFilterEntry> types;

	private Set<String> projects;

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

	public long getRevision() {
		return revision;
	}

	public void setRevision(long revision) {
		this.revision = revision;
	}

	public Set<String> getProjects() {
		if (projects == null) {
			projects = new HashSet<String>();
		}
		return projects;
	}

	/**
	 * Return the categories defined in this scan filter
	 * 
	 * @return
	 */
	public Set<ScanFilterEntry> getCategories() {
		if (categories == null) {
			categories = new HashSet<ScanFilterEntry>();
		}
		return categories;
	}

	/**
	 * Return the finding types referenced in this scan filter
	 * 
	 * @return
	 */
	public Set<ScanFilterEntry> getTypes() {
		if (types == null) {
			types = new HashSet<ScanFilterEntry>();
		}
		return types;
	}

	public ScanFilter copy() {
		final ScanFilter copy = new ScanFilter();
		copy.uuid = uuid;
		copy.name = name;
		copy.revision = revision;
		copy.categories = new HashSet<ScanFilterEntry>(getCategories().size());
		for (ScanFilterEntry entry : getCategories()) {
			copy.categories.add(entry);
		}
		copy.types = new HashSet<ScanFilterEntry>(getTypes().size());
		for (ScanFilterEntry entry : getTypes()) {
			copy.types.add(entry);
		}
		copy.projects = new HashSet<String>(getProjects().size());
		for (String project : getProjects()) {
			copy.projects.add(project);
		}
		return copy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && obj instanceof ScanFilter) {
			return LangUtil.equals(uuid, ((ScanFilter) obj).uuid);
		}
		return false;
	}

}
