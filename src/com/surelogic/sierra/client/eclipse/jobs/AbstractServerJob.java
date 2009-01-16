package com.surelogic.sierra.client.eclipse.jobs;

import org.eclipse.core.runtime.jobs.Job;

import com.surelogic.sierra.client.eclipse.actions.TroubleshootConnection;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootNoSuchServer;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootWrongAuthentication;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.tool.message.InvalidLoginException;
import com.surelogic.sierra.tool.message.ServerLocation;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

public abstract class AbstractServerJob extends Job {

	public AbstractServerJob(String name) {
		super(name);
	}

	public static final TroubleshootConnection getTroubleshootConnection(
			final ServerFailureReport method, final ServerLocation location,
			final SierraServiceClientException e) {
		if (e instanceof InvalidLoginException) {
			return new TroubleshootWrongAuthentication(method, location);
		} else {
			return new TroubleshootNoSuchServer(method, location);
		}
	}
	
}
