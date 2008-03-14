package com.surelogic.sierra.eclipse.teamserver.model;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

public final class JettyConsoleLog extends ServerLog {

	public JettyConsoleLog(ScheduledExecutorService executor) {
		super(executor);
	}

	@Override
	public void init() {
	}

	@Override
	public void dispose() {
	}

	public void changeToProcess(Process p) throws IOException {
		// TODO Auto-generated method stub
	}
}
