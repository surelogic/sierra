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
			if (clazz != null) {
				// TODO get the message information in here
				return (Exception) clazz.newInstance();
			} else {
				return new IllegalArgumentException("Unknown exception type:"
						+ clazz);
			}
		} catch (Exception e) {
			// TODO is this the right exception?
			throw new ServiceInvocationException(e);
		}
	}
}
