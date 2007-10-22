package com.surelogic.sierra.client.eclipse.model.selection;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
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

	private final Map<String, Selection> f_nameToSelection = new HashMap<String, Selection>();

	public void saveSelection(String name, Selection selection) {
		f_nameToSelection.put(name, selection);
		notifySavedSelectionsChanged();
	}

	public void removeSavedSelection(String name) {
		if (f_nameToSelection.remove(name) != null) {
			notifySavedSelectionsChanged();
		}
	}

	public Selection getSavedSelection(String name) {
		return f_nameToSelection.get(name);
	}

	public Set<String> getSavedSelectionNames() {
		return new HashSet<String>(f_nameToSelection.keySet());
	}

	public boolean isEmpty() {
		return f_nameToSelection.isEmpty();
	}

	private final Set<ISelectionManagerObserver> f_observers = new CopyOnWriteArraySet<ISelectionManagerObserver>();

	public void addObserver(ISelectionManagerObserver o) {
		/*
		 * No lock needed because we are using a util.concurrent collection.
		 */
		f_observers.add(o);
	}

	public void removeObserver(ISelectionManagerObserver o) {
		/*
		 * No lock needed because we are using a util.concurrent collection.
		 */
		f_observers.remove(o);
	}

	/**
	 * Do not call this method holding a lock on <code>this</code>. Deadlock
	 * could occur as we are invoking an alien method.
	 */
	private void notifySavedSelectionsChanged() {
		for (ISelectionManagerObserver o : f_observers)
			o.savedSelectionsChanged(this);
	}

	public void save(File file) {
		// TODO
	}

	public void load(File file) throws Exception {
		// TODO
		notifySavedSelectionsChanged();
	}
}
