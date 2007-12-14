package com.surelogic.sierra.tool.message.jaxws;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ServerMismatchException")
@XmlRootElement(name = "ServerMismatchFault", namespace = "http://message.tool.sierra.surelogic.com/")
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class ServerMismatchExceptionBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4600136609529425024L;
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
