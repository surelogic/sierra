package com.surelogic.sierra.client.eclipse.jobs;

import com.surelogic.common.eclipse.jobs.DatabaseJob;

public abstract class AbstractSierraDatabaseJob extends DatabaseJob {
	public AbstractSierraDatabaseJob(String name) {
		super(name, JobConstants.ACCESS_KEY);
	}
	
	public AbstractSierraDatabaseJob(String name, int priority) {
		super(name, priority, JobConstants.ACCESS_KEY);
	}
	
	public AbstractSierraDatabaseJob(Object family, String name, int priority) {
		super(family, name, priority, JobConstants.ACCESS_KEY);
	}
}
