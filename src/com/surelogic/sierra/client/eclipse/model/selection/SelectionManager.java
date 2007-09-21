package com.surelogic.sierra.client.eclipse.model.selection;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class SelectionManager {

	public static final SelectionManager INSTANCE = new SelectionManager();

	public static SelectionManager getInstance() {
		return INSTANCE;
	}

	private final Executor f_executor = Executors.newSingleThreadExecutor();

	private SelectionManager() {
		// singleton
	}

	public Selection construct() {
		return new Selection(this, f_executor);
	}

	public void save(File file) {
		// TODO
	}

	public void load(File file) throws Exception {
		// TODO
	}
}
