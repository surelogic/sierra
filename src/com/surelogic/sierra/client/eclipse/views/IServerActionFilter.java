package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.ui.IActionFilter;

public interface IServerActionFilter extends IActionFilter {
  String STATUS_ATTR = "Status";
  String ERROR_STATUS = "ERROR";
  String WARNING_STATUS = "WARNING";
  
  String SOURCE_ATTR = "Source";
  String WARNING_SRC = WARNING_STATUS;
  
  String SERVER_TYPE_ATTR = "ServerType";
  String BUGLINK_TYPE = "BUGLINK";
  String TEAM_SERVER_TYPE = "TEAM_SERVER";
  String AUTO_SYNC = "AUTO_SYNC";
}
