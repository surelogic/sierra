package com.surelogic.sierra.client.eclipse.model;

public enum ServerSyncType {
  BUGLINK, ALL;
  
  public boolean syncBugLink() {
	  return true;
  }
  
  public boolean syncProjects() {
	  return this.equals(ALL);
  }
}
