package com.surelogic.sierra.message.srpc;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlRootElement
class Failure {

	private String message;
	private String trace;

	public Failure() {
		// Do nothing
	}

	public Failure(Exception e) {
		this.message = e.getMessage();
		final StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer));
		this.trace = writer.toString();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTrace() {
		return trace;
	}

	public void setTrace(String trace) {
		this.trace = trace;
	}

}
