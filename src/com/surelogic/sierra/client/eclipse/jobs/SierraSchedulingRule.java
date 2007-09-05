package com.surelogic.sierra.client.eclipse.jobs;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class SierraSchedulingRule implements ISchedulingRule {

	private SierraSchedulingRule() {
		// singleton
	}

	private static final ISchedulingRule INSTANCE = new SierraSchedulingRule();

	public static ISchedulingRule getInstance() {
		return INSTANCE;
	}

	public boolean contains(ISchedulingRule rule) {
		return rule == this;
	}

	public boolean isConflicting(ISchedulingRule rule) {
		return rule == this;
	}
}
