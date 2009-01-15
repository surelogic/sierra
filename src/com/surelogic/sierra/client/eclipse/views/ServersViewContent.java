package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.graphics.Image;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.images.CommonImages;
import com.surelogic.sierra.client.eclipse.views.SierraServersMediator.ChangeStatus;
import com.surelogic.sierra.client.eclipse.views.SierraServersMediator.ServerStatus;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;

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
		for (ServersViewContent svc : c) {
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

	public Object getData() {
		return data;
	}

	public ServersViewContent getParent() {
		return parent;
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
			// System.out.println("Checking if "+name+" = "+value+" for "+this);
			return status.toString().equals(value);
		} else if (SOURCE_ATTR.equals(name)) {
			if (WARNING_SRC.equals(value)) {
				return image
						.equals(SLImages.getImage(CommonImages.IMG_WARNING));
			}
		} else if (SERVER_TYPE_ATTR.equals(name)) {
			if (data instanceof ConnectedServer) {
				ConnectedServer server = (ConnectedServer) data;
				if (BUGLINK_TYPE.equals(value)) {
					return true; 
				} else if (TEAM_SERVER_TYPE.equals(value)) {
					return server.isTeamServer();
				} else if (AUTO_SYNC.equals(value)) {
					return server.getLocation().isAutoSync();
				}
			} else if (BUGLINK_TYPE.equals(value)) {
				return text.endsWith(SierraServersMediator.SCAN_FILTERS)
						|| text.endsWith(SierraServersMediator.CATEGORIES);
			} else if (TEAM_SERVER_TYPE.equals(value)) {
				return text.endsWith(SierraServersMediator.CONNECTED_PROJECTS);
			}
		}		
		return false;
	}
}
