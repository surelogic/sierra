package com.surelogic.sierra.message.srpc;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlRootElement
class RaisedException {

	private String exceptionClass;
	private String message;

	public RaisedException() {
		// Do Nothing
	}

	public RaisedException(Throwable e) {
		this.exceptionClass = e.getClass().getName();
		this.message = e.getMessage();
	}

	public String getExceptionClass() {
		return exceptionClass;
	}

	public void setExceptionClass(String exceptionClass) {
		this.exceptionClass = exceptionClass;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Exception regenerateException() {
		try {
			Class<?> clazz = Thread.currentThread().getContextClassLoader()
					.loadClass(exceptionClass);
			Exception e = (Exception) clazz.newInstance();
			// TODO get the message information in here
			return e;
		} catch (Exception e) {
			// TODO is this the right exception?
			throw new ServiceInvocationException(e);
		}
	}

}
