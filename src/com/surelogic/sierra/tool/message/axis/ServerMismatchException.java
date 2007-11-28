/**
 * ServerMismatchException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.3  Built on : Aug 10, 2007 (04:45:47 LKT)
 */
package com.surelogic.sierra.tool.message.axis;

@SuppressWarnings("serial")
public class ServerMismatchException extends java.lang.Exception {
    private com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.ServerMismatchFault faultMessage;

    public ServerMismatchException() {
        super("ServerMismatchException");
    }

    public ServerMismatchException(java.lang.String s) {
        super(s);
    }

    public ServerMismatchException(java.lang.String s, java.lang.Throwable ex) {
        super(s, ex);
    }

    public void setFaultMessage(
        com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.ServerMismatchFault msg) {
        faultMessage = msg;
    }

    public com.surelogic.sierra.tool.message.axis.SierraServiceBeanServiceStub.ServerMismatchFault getFaultMessage() {
        return faultMessage;
    }
}
