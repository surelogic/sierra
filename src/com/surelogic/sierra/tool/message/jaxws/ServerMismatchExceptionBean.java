package com.surelogic.sierra.tool.message.jaxws;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ServerMismatchException")
@XmlRootElement(name = "ServerMismatchFault",namespace="http://message.tool.sierra.surelogic.com/")
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class ServerMismatchExceptionBean {

	private String expected;
	private String actual;

	public String getExpected() {
		return expected;
	}

	public void setExpected(String expected) {
		this.expected = expected;
	}

	public String getActual() {
		return actual;
	}

	public void setActual(String actual) {
		this.actual = actual;
	}

}
