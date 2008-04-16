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
public class CreateScanFilterRequest {
	@XmlElement(required = true)
	protected String name;
	protected List<TypeFilter> typeFilter;
	protected List<CategoryFilter> categoryFilter;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

}
