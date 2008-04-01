package com.surelogic.sierra.tool.registration;

import javax.xml.bind.annotation.*;

/**
 * Information to register a given SureLogic product 
 * 
 * @author Edwin Chan
 * 
 */
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
@XmlRootElement
public class ProductRegistrationInfo extends ProductInfo {
	String last, first, email;

	public String getFirstName() {
		return first;
	}
	
	public String getLastName() {
		return last;
	}
	
	public void setFirstName(String name) {
		first = name;
	}
	
	public void setLastName(String name) {
		last = name;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String addr) {
		email = addr;
	}
}
