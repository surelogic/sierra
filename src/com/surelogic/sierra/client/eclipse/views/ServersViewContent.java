package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.views.SierraServersMediator.*;

class ServersViewContent implements IServerActionFilter {
	final ServersViewContent parent;
	private ServersViewContent[] children = SierraServersMediator.emptyChildren;
	private ChangeStatus changeStatus = ChangeStatus.NONE;
	private ServerStatus status = ServerStatus.OK;
	final Image image;
	private String text = "";
	private Object data = null;	
	
	ServersViewContent(ServersViewContent p, Image i) {
		parent = p;
		image = i;	
	}
	public void setText(String t) {
		text = t;
	}
	public void setData(Object o) {
		data = o;			
	}
	public void setChildren(ServersViewContent[] c) {
		children = c;
		for(ServersViewContent svc : c) {
			mergeStatus(svc);
		}
	}
	private void mergeStatus(ServersViewContent c) {
		status = status.merge(c.status);
		changeStatus = changeStatus.merge(c.changeStatus);
	}
	public void setChangeStatus(ChangeStatus s) {
		changeStatus = s;
	}
	public void setServerStatus(ServerStatus s) {
		status = s;
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
			//System.out.println("Checking if "+name+" = "+value+" for "+this);
			return status.toString().equals(value);
		}
		else if (SOURCE_ATTR.equals(name)) {
			if (WARNING_SRC.equals(value)) {
				return image.equals(SLImages.getWorkbenchImage(ISharedImages.IMG_OBJS_WARN_TSK));
			}
			return false;
		}
		return false;
	}
}
