package com.surelogic.sierra.gwt.client.ui.choice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ListBox;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StandardCallback;

public class ProjectChoice extends ListBox {

	private final List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();

	public ProjectChoice() {
		this(false);
	}

	public ProjectChoice(final List<String> projects,
			final boolean allowMultiples) {
		super(allowMultiples);
		ServiceHelper.getSettingsService().searchProjects("*", -1,
				new StandardCallback<List<String>>() {
					@Override
					protected void doSuccess(final List<String> result) {
						if (result.isEmpty()) {
							setEnabled(false);
							addItem("No Projects");
						} else {
							setEnabled(true);
							for (final String project : result) {
								addItem(project);
								if (projects.contains(project)) {
									setItemSelected(getItemCount() - 1, true);
								}
							}
							for (final ChangeListener listener : changeListeners) {
								listener.onChange(ProjectChoice.this);
							}
						}
					}
				});
	}

	@Override
	public void addChangeListener(final ChangeListener listener) {
		super.addChangeListener(listener);
		changeListeners.add(listener);
	}

	@Override
	public void removeChangeListener(final ChangeListener listener) {
		super.removeChangeListener(listener);
		changeListeners.remove(listener);
	}

	@SuppressWarnings("unchecked")
	public ProjectChoice(final boolean allowMultiples) {
		this(Collections.EMPTY_LIST, allowMultiples);
	}

}
