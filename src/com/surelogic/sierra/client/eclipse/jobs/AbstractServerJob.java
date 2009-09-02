package com.surelogic.sierra.client.eclipse.jobs;

import org.eclipse.core.runtime.jobs.Job;

import com.surelogic.sierra.client.eclipse.actions.TroubleshootConnection;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootNoSuchServer;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootWrongAuthentication;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootWrongServerVersion;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.tool.message.InvalidLoginException;
import com.surelogic.sierra.tool.message.InvalidVersionException;
import com.surelogic.sierra.tool.message.ServerLocation;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

public abstract class AbstractServerJob extends Job {

	public AbstractServerJob(final String name) {
		super(name);

		// Lower priority below UI jobs
		this.setPriority(BUILD);
	}

	public static final TroubleshootConnection getTroubleshootConnection(
			final ServerFailureReport strategy, final ServerLocation location,
			final SierraServiceClientException e) {
		if (e instanceof InvalidLoginException) {
			return new TroubleshootWrongAuthentication(strategy, location);
		} else if (e instanceof InvalidVersionException) {
			return new TroubleshootWrongServerVersion(strategy, location);
		} else {
			return new TroubleshootNoSuchServer(strategy, location);
		}
	}

}
