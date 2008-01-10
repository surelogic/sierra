package com.surelogic.sierra.message.srpc;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * SRPCException is thrown when an error occurs during the remote invocation of
 * an SRPC service method.
 * 
 * @author nathan
 * 
 */
@XmlType
@XmlRootElement
public class SRPCException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4619226105279296650L;

	public SRPCException() {
		super();
	}

	public SRPCException(String message) {
		super(message);
	}

	public SRPCException(Throwable cause) {
		super(cause);
	}

	public SRPCException(String message, Throwable cause) {
		super(message, cause);
	}

}
