package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CreateCategoryRequest {
	private String name;
	private String description;
	private List<FilterEntry> filter;
	private List<String> parent;

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

	public List<FilterEntry> getFilter() {
		if (filter == null) {
			filter = new ArrayList<FilterEntry>();
		}
		return filter;
	}

	public List<String> getParent() {
		if (parent == null) {
			parent = new ArrayList<String>();
		}
		return parent;
	}

}
