package com.surelogic.sierra.client.eclipse.jobs;

import com.surelogic.common.core.jobs.EclipseAccessKeysJob;

public abstract class AbstractSierraDatabaseJob extends EclipseAccessKeysJob {
  public AbstractSierraDatabaseJob(String name) {
    super(name, JobConstants.ACCESS_KEY);
  }
}
