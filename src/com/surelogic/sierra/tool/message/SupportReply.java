package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.*;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
@XmlRootElement
public class SupportReply {
	String message;
	
	public SupportReply() {
		this(null);
	}
	
	public SupportReply(String msg) {
		message = msg;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String msg) {
		this.message = msg;
	}
}
