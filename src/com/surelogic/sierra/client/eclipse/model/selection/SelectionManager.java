package com.surelogic.sierra.client.eclipse.model.selection;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class SelectionManager {

	private static final SelectionManager INSTANCE = new SelectionManager();

	public static SelectionManager getInstance() {
		return INSTANCE;
	}

	private final Executor f_executor = Executors.newSingleThreadExecutor();

	public Executor getExecutor() {
		return f_executor;
	}

	private SelectionManager() {
		// singleton
	}

	public Selection construct() {
		final Selection result = new Selection(this, f_executor);
		result.init();
		return result;
	}

	public void save(File file) {
		// TODO
	}

	public void load(File file) throws Exception {
		// TODO
	}
}
