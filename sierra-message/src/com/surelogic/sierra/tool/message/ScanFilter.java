package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class ScanFilter {

	@XmlElement(required = true)
	protected long revision;
	@XmlElement(required = true)
	protected String name;
	@XmlElement(required = true)
	protected String uid;
	@XmlElement(required = true)
	protected String owner;
	protected List<TypeFilter> typeFilter;
	protected List<CategoryFilter> categoryFilter;

	public long getRevision() {
		return revision;
	}

	public void setRevision(final long revision) {
		this.revision = revision;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(final String uid) {
		this.uid = uid;
	}

	public List<TypeFilter> getTypeFilter() {
		if (typeFilter == null) {
			typeFilter = new ArrayList<TypeFilter>();
		}
		return typeFilter;
	}

	public List<CategoryFilter> getCategoryFilter() {
		if (categoryFilter == null) {
			categoryFilter = new ArrayList<CategoryFilter>();
		}
		return categoryFilter;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(final String server) {
		owner = server;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((categoryFilter == null) ? 0 : categoryFilter.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result + (int) (revision ^ (revision >>> 32));
		result = prime * result
				+ ((typeFilter == null) ? 0 : typeFilter.hashCode());
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
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
		final ScanFilter other = (ScanFilter) obj;
		if (categoryFilter == null) {
			if (other.categoryFilter != null) {
				return false;
			}
		} else if (!categoryFilter.equals(other.categoryFilter)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (owner == null) {
			if (other.owner != null) {
				return false;
			}
		} else if (!owner.equals(other.owner)) {
			return false;
		}
		if (revision != other.revision) {
			return false;
		}
		if (typeFilter == null) {
			if (other.typeFilter != null) {
				return false;
			}
		} else if (!typeFilter.equals(other.typeFilter)) {
			return false;
		}
		if (uid == null) {
			if (other.uid != null) {
				return false;
			}
		} else if (!uid.equals(other.uid)) {
			return false;
		}
		return true;
	}

}
