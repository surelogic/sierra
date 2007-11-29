package com.surelogic.sierra.client.eclipse.model.selection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class SelectionManager {

	private static final SelectionManager INSTANCE = new SelectionManager();

	public static SelectionManager getInstance() {
		return INSTANCE;
	}

	private SelectionManager() {
		// singleton
	}

	public Selection construct() {
		final Selection result = new Selection(this);
		return result;
	}

	private final Map<String, Selection> f_nameToSelection = new HashMap<String, Selection>();

	/**
	 * Saves a copy of the passed selection. If a previous saved selection with
	 * the passed name existed, it is overwritten.
	 * 
	 * @param name
	 *            a name for the saved selection.
	 * @param selection
	 *            the selection to save a copy or.
	 */
	public void saveSelection(String name, Selection selection) {
		Selection copy = new Selection(selection);
		f_nameToSelection.put(name, copy);
		notifySavedSelectionsChanged();
	}

	public void removeSavedSelection(String name) {
		if (f_nameToSelection.remove(name) != null) {
			notifySavedSelectionsChanged();
		}
	}

	/**
	 * Returns a copy of the saved selection with the passed name, or
	 * <code>null</code> if no such saved selection exists.
	 * 
	 * @param name
	 *            a saved selection name.
	 * @return a copy of the saved selection with the passed name, or
	 *         <code>null</code> if no such saved selection exists.
	 */
	public Selection getSavedSelection(String name) {
		Selection result = f_nameToSelection.get(name);
		if (result != null)
			result = new Selection(result);
		return result;
	}

	public List<String> getSavedSelectionNames() {
		List<String> result = new ArrayList<String>(f_nameToSelection.keySet());
		Collections.sort(result);
		return result;
	}

	public boolean isEmpty() {
		return f_nameToSelection.isEmpty();
	}

	private final Set<ISelectionManagerObserver> f_observers = new CopyOnWriteArraySet<ISelectionManagerObserver>();

	public void addObserver(ISelectionManagerObserver o) {
		if (o == null)
			return;
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
		SelectionPersistence.save(this, file);
	}

	public void load(File file) throws Exception {
		SelectionPersistence.load(this, file);
		notifySavedSelectionsChanged();
	}
}
