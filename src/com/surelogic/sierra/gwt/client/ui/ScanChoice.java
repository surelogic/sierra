package com.surelogic.sierra.gwt.client.ui;

import java.util.List;

import com.google.gwt.user.client.ui.ListBox;
import com.surelogic.sierra.gwt.client.data.Scan;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StandardCallback;

public class ScanChoice extends ListBox {

	public ScanChoice(final boolean allowMultiples) {
		super(false);
	}

	public void displayProjectScans(final String project) {
		ServiceHelper.getFindingService().getScans(project,
				new StandardCallback<List<Scan>>() {
					@Override
					protected void doSuccess(final List<Scan> result) {
						for (final Scan scan : result) {
							addItem(scan.getScanTimeDisplay(), scan.getUuid());
						}
					}
				});
	}

}
