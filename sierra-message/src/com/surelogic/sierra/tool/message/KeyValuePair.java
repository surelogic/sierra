package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.*;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
@XmlRootElement
public class KeyValuePair {
	private String key;
	private String value;
	
	@XmlElement
	public String getKey() {
		return key;
	}
	
	public void setKey(String k) {
		key = k;
	}
	
	@XmlElement
	public String getValue() {
		return value;
	}
	
	public void setValue(String v) {
		value = v;
	}
	
	public String toString() {
		return key+" => "+value;
	}
}
