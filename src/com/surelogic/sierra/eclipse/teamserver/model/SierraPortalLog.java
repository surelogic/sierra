package com.surelogic.sierra.eclipse.teamserver.model;

import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.ScheduledExecutorService;

public final class SierraPortalLog extends FileBasedServerLog {

	final FilenameFilter f_filenameFilter;

	@Override
	protected FilenameFilter getFilenameFilter() {
		return f_filenameFilter;
	}

	public SierraPortalLog(ScheduledExecutorService executor) {
		super(executor);
		f_filenameFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.startsWith("log-portal") && name.endsWith("txt"));
			}
		};
	}
}
