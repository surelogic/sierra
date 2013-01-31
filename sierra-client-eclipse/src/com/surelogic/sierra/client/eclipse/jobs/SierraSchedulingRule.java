package com.surelogic.sierra.client.eclipse.jobs;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Scheduling rule for Sierra scans.
 * 
 * Currently only one job is allowed to be scheduled at a time.
 * 
 * @author Tanmay.Sinha
 * 
 */
public final class SierraSchedulingRule implements ISchedulingRule {

	private SierraSchedulingRule() {
		// singleton
	}

	private static final ISchedulingRule INSTANCE = new SierraSchedulingRule();

	public static ISchedulingRule getInstance() {
		return INSTANCE;
	}

	@Override
  public boolean contains(ISchedulingRule rule) {
		return rule == this;
	}

	@Override
  public boolean isConflicting(ISchedulingRule rule) {
		return rule == this;
	}
}
