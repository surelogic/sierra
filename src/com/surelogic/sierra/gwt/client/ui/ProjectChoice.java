package com.surelogic.sierra.gwt.client.ui;

import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.ui.ListBox;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StandardCallback;

public class ProjectChoice extends ListBox {

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
							}
						}
					}
				});
	}

	@SuppressWarnings("unchecked")
	public ProjectChoice(final boolean allowMultiples) {
		this(Collections.EMPTY_LIST, allowMultiples);
	}

}
