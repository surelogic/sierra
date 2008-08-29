package com.surelogic.sierra.tool.message;

import java.util.*;

import javax.xml.bind.annotation.*;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
@XmlRootElement
public class SupportRequest {
	public enum Type {
		REGISTER, UPDATE, USAGE, ERROR
	}
	private Type type;
	private Map<String,String> pairs;
		
	public Type getType() {
		return type;
	}
	
	public void setType(Type t) {
		type = t;
	}
	
	public Map<String,String> getPairs() {
		return pairs;
	}

	public void setPairs(Map<String,String> pairs) {
		this.pairs = pairs;
	}
}
