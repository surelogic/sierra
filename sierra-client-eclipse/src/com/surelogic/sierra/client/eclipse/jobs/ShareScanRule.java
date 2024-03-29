package com.surelogic.sierra.client.eclipse.jobs;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class ShareScanRule implements ISchedulingRule {

  private ShareScanRule() {
    // singleton
  }

  private static final ISchedulingRule INSTANCE = new ShareScanRule();

  public static ISchedulingRule getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean contains(final ISchedulingRule rule) {
    return rule == this;
  }

  @Override
  public boolean isConflicting(final ISchedulingRule rule) {
    return rule == this;
  }
}
