package com.surelogic.sierra.gwt.client.ui;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ListBox;
import com.surelogic.sierra.gwt.client.data.Scan;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StandardCallback;

public class ScanChoice extends ListBox {
	private final List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();

	public ScanChoice(final boolean allowMultiples) {
		super(false);
	}

	public void displayProjectScans(final String project) {
		ServiceHelper.getFindingService().getScans(project,
				new StandardCallback<List<Scan>>() {
					@Override
					protected void doSuccess(final List<Scan> result) {
						clear();
						for (final Scan scan : result) {
							addItem(scan.getScanTimeDisplay(), scan.getUuid());
						}
						for (final ChangeListener listener : changeListeners) {
							listener.onChange(ScanChoice.this);
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
}
