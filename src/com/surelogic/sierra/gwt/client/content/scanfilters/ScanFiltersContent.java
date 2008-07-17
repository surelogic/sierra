package com.surelogic.sierra.gwt.client.content.scanfilters;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.content.ListContentComposite;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.cache.ScanFilterCache;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.FormButton;
import com.surelogic.sierra.gwt.client.util.ExceptionUtil;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ScanFiltersContent extends
		ListContentComposite<ScanFilter, ScanFilterCache> {
	private final ScanFilterView sf = new ScanFilterView();

	@Override
	protected void onInitialize(DockPanel rootPanel,
			VerticalPanel selectionPanel) {
		setCaption("Scan Filters");

		addAction(new CreateScanFilterForm());

		sf.initialize();
		sf.addAction("Delete", new ClickListener() {

			public void onClick(Widget sender) {
				final ScanFilter filter = sf.getSelection();
				if (filter != null) {
					ServiceHelper.getSettingsService().deleteScanFilter(
							filter.getUuid(), new AsyncCallback<Status>() {

								public void onFailure(Throwable caught) {
									ExceptionUtil.handle(caught);
								}

								public void onSuccess(Status result) {
									getCache().refresh();
								}
							});
				}
			}
		});
		selectionPanel.add(sf);
	}

	@Override
	protected String getItemText(ScanFilter item) {
		return item.getName();
	}

	@Override
	protected boolean isMatch(ScanFilter item, String query) {
		return LangUtil.containsIgnoreCase(item.getName(), query);
	}

	@Override
	protected void onSelectionChanged(ScanFilter item) {
		sf.setSelection(item);
	}

	private class CreateScanFilterForm extends FormButton {
		private final TextBox sfName = new TextBox();

		public CreateScanFilterForm() {
			super("Create a Scan Filter", "Create");
			getForm().addField("Name:", sfName);
		}

		@Override
		protected void onOpen() {
			sfName.setText("");
		}

		@Override
		protected void doOkClick() {
			final String name = sfName.getText();

			if (LangUtil.notEmpty(name)) {
				setWaitStatus();

				ServiceHelper.getSettingsService().createScanFilter(name,
						new AsyncCallback<ScanFilter>() {

							public void onFailure(Throwable caught) {
								clearWaitStatus();

								Window.alert("Scan Filter creation failed: "
										+ caught.getMessage());
							}

							public void onSuccess(ScanFilter result) {
								clearWaitStatus();
								setOpen(false);

								getCache().refresh();
								Context.createWithUuid(result).submit();
							}
						});
			}
		}
	}

	// Singleton
	private ScanFiltersContent() {
		super(ScanFilterCache.getInstance());
	}

	private static final ScanFiltersContent instance = new ScanFiltersContent();

	public static ScanFiltersContent getInstance() {
		return instance;
	}

}
