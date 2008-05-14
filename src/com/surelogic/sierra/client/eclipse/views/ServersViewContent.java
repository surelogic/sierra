package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.graphics.Image;

import com.surelogic.sierra.client.eclipse.views.SierraServersMediator.ChangeStatus;

class ServersViewContent implements IServerActionFilter {
	final ServersViewContent parent;
	private ServersViewContent[] children;
	private ChangeStatus changeStatus;
	//private Status status;
	final Image image;
	private String text;
	private Object data;	
	
	ServersViewContent(ServersViewContent p, Image i) {
		this(p, null, i, "", ChangeStatus.NONE);			
	}
	ServersViewContent(ServersViewContent p, ServersViewContent[] c, Image i, String t, ChangeStatus s) {
		parent = p;
		image = i;
		children = c;
		changeStatus = s;
		text = t;
	}
	public void setText(String t) {
		text = t;
	}
	public void setData(Object o) {
		data = o;			
	}
	public void setChildren(ServersViewContent[] c) {
		children = c;
	}
	public String getText() {
		return text;
	}
	public Image getImage() {
		return image;
	}
	public ServersViewContent[] getChildren() {
		return children;
	}
	public ChangeStatus getChangeStatus() {
		return changeStatus;
	}
	public boolean testAttribute(Object target, String name, String value) {
		if (target != this) {
			return false;
		}
		if (STATUS_ATTR.equals(name)) {			
			System.out.println("Checking if "+name+" = "+value+" for "+this);
			return true;
		}
		return false;
	}
}
