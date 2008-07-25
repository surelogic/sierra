package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

import com.surelogic.sierra.gwt.client.data.cache.Cacheable;

public class FindingTypeFilter implements Serializable, Cacheable,
		Comparable<FindingTypeFilter> {
	private static final long serialVersionUID = 47046359782973349L;

	private String name;

	private boolean filtered;
	private String uuid;
	private String shortMessage;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isFiltered() {
		return filtered;
	}

	public void setFiltered(boolean filtered) {
		this.filtered = filtered;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String id) {
		uuid = id;
	}

	public String getShortMessage() {
		return shortMessage;
	}

	public void setShortMessage(String shortMessage) {
		this.shortMessage = shortMessage;
	}

	public FindingTypeFilter copy() {
		final FindingTypeFilter copy = new FindingTypeFilter();
		copy.name = name;
		copy.filtered = filtered;
		copy.uuid = uuid;
		copy.shortMessage = shortMessage;
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
		if (obj == null) {
			return false;
		}
		final FindingTypeFilter other = (FindingTypeFilter) obj;
		if (uuid == null) {
			if (other.uuid != null) {
				return false;
			}
		} else if (!uuid.equals(other.uuid)) {
			return false;
		}
		return true;
	}

	public int compareTo(FindingTypeFilter o) {
		return getName().compareTo(o.getName());
	}
}
