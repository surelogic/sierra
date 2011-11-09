package com.surelogic.sierra.client.eclipse.actions;

import com.surelogic.sierra.client.eclipse.model.ServerSyncType;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;

public class SynchronizeBugLinkServersAction 
extends	SynchronizeAllProjectsAction {
    public SynchronizeBugLinkServersAction() {
    	super(ServerSyncType.BUGLINK, ServerFailureReport.SHOW_DIALOG, true, 0);
    }
}
