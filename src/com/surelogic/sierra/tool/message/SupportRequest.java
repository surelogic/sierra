package com.surelogic.sierra.tool.message;

import java.util.*;

import javax.xml.bind.annotation.*;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
@XmlRootElement
public class SupportRequest {
	public enum RequestType {
		REGISTER, UPDATE, USAGE, ERROR
	}
	private RequestType type = RequestType.ERROR;
	private Map<String,String> pairs;
		
	public SupportRequest() {
		this(RequestType.ERROR);
	}
	
	public SupportRequest(RequestType t) {
		type = t;
	}

	public RequestType getType() {
		return type;
	}
	
	public void setType(RequestType t) {
		type = t;
	}
	
	public Map<String,String> getPairs() {
		return pairs;
	}

	public void setPairs(Map<String,String> pairs) {
		this.pairs = pairs;
	}
}
