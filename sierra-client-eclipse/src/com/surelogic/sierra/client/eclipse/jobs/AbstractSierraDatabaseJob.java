package com.surelogic.sierra.client.eclipse.jobs;

import com.surelogic.common.core.jobs.AbstractEclipseAccessKeysJob;

public abstract class AbstractSierraDatabaseJob extends AbstractEclipseAccessKeysJob {
  public AbstractSierraDatabaseJob(String name) {
    super(name, JobConstants.ACCESS_KEY);
  }
}
