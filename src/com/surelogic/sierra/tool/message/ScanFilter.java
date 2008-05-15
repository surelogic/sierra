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
	protected List<String> project;

	public long getRevision() {
		return revision;
	}

	public void setRevision(long revision) {
		this.revision = revision;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
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

	public List<String> getProject() {
		if (project == null) {
			project = new ArrayList<String>();
		}
		return project;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String server) {
		owner = server;
	}

}
