package com.surelogic.sierra.message.srpc;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * SRPC Services are defined by writing an interface extending {@link Service}
 * and defining one or more methods. The parameters and return type of each
 * method should be annotated w/ {@link XmlRootElement}.
 * 
 * @author nathan
 * 
 */
//TODO this should be an annotation, not an interface.
public interface Service {

}
