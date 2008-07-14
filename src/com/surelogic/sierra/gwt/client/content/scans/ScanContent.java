package com.surelogic.sierra.gwt.client.content.scans;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.ScanDetail;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.ImportanceChoice;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;
import com.surelogic.sierra.gwt.client.ui.StatusBox;

public class ScanContent extends ContentComposite {

	ScanView view = new ScanView();

	@Override
	protected void onDeactivate() {
		// Do nothing
		view.onDeactivate();
	}

	@Override
	protected void onInitialize(final DockPanel rootPanel) {
		rootPanel.add(view, DockPanel.CENTER);
	}

	@Override
	protected void onUpdate(final Context context) {
		view.update(context);
	}

	private static class ScanView extends SectionPanel {

		private VerticalPanel panel;

		@Override
		protected void onDeactivate() {
			setTitle("No Scan Selected");
			panel.clear();
		}

		@Override
		protected void onInitialize(final VerticalPanel contentPanel) {
			setTitle("No Scan Selected");
			panel = new VerticalPanel();
			contentPanel.add(panel);
		}

		@Override
		protected void onUpdate(final Context context) {
			final String uuid = context.getUuid();
			if ((uuid != null) && !(uuid.length() == 0)) {
				setScan(uuid);
			}
		}

		private void setScan(final String uuid) {
			panel.clear();
			ServiceHelper.getFindingService().getScanDetail(uuid,
					new AsyncCallback<ScanDetail>() {
						public void onFailure(final Throwable caught) {
							panel.add(new StatusBox(Status.failure(caught
									.getMessage())));
						}

						public void onSuccess(final ScanDetail result) {
							setTitle(uuid);
							final ListBox lb = new ListBox();
							final ImportanceChoice imp = new ImportanceChoice(
									true);
							lb.setMultipleSelect(true);
							for (final String pakkage : result
									.getCompilations().keySet()) {
								lb.addItem(pakkage);
							}
							panel.add(lb);
							panel.add(imp);
						}
					});
		}
	}

	private ScanContent() {
		// Do nothing
	}

	private static final ScanContent INSTANCE = new ScanContent();

	public static ScanContent getInstance() {
		return INSTANCE;
	}
}
