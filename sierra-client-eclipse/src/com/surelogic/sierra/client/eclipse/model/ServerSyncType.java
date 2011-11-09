package com.surelogic.sierra.client.eclipse.model;

public enum ServerSyncType {
  BUGLINK, BY_SERVER_SETTINGS, ALL;
  
  public boolean syncBugLink() {
	  return true;
  }
  
  /**
   * @return if supposed to sync project (ScanLink) data
   */
  public boolean syncProjects() {
	  return !this.equals(BUGLINK);
  }
  
  /**  
   * @return if supposed to synchronize, based on server settings
   */
  public boolean syncByServerSettings() {
	  return this.equals(BY_SERVER_SETTINGS);
  }
}
