package com.surelogic.sierra.message.srpc;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
class Failure {

	protected String message;
	protected String trace;

	public Failure() {
		// Do nothing
	}

	public Failure(final Exception e) {
		this.message = e.getMessage();
		final StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer));
		this.trace = writer.toString();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public String getTrace() {
		return trace;
	}

	public void setTrace(final String trace) {
		this.trace = trace;
	}

}
