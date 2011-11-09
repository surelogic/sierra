package com.surelogic.sierra.tool.registration;

import javax.xml.bind.annotation.*;

/**
 * Information about a given SureLogic product 
 * 
 * @author Edwin Chan
 * 
 */
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
@XmlRootElement
public class ProductInfo {
	String name;
	String id;
	String version;
	
	public String getName() {
		return name;
	}

	public void setName(String id) {
		name = id;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String v) {
		version = v;
	}
}
