package com.surelogic.sierra.gwt.client.content.scanfilters;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.content.ListContentComposite;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.cache.Cache;
import com.surelogic.sierra.gwt.client.data.cache.ScanFilterCache;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StandardCallback;
import com.surelogic.sierra.gwt.client.service.callback.StatusCallback;
import com.surelogic.sierra.gwt.client.ui.FormButton;
import com.surelogic.sierra.gwt.client.ui.StyleHelper;
import com.surelogic.sierra.gwt.client.ui.StyleHelper.Style;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ScanFiltersContent extends
		ListContentComposite<ScanFilter, ScanFilterCache> {
	private final ScanFilterView viewer = new ScanFilterView();
	private final ScanFilterEditor editor = new ScanFilterEditor();
	private Label setDefaultAction;
	private Label editAction;
	private Label deleteAction;

	private ScanFilter defaultScanFilter;

	@Override
	protected void onInitialize(final DockPanel rootPanel,
			final VerticalPanel selectionPanel) {
		setCaption("Scan Filters");

		addAction(new CreateScanFilterForm());

		viewer.initialize();

		setDefaultAction = StyleHelper.add(new Label("Set as Default", false),
				Style.CLICKABLE);
		setDefaultAction.addClickListener(new ClickListener() {

			public void onClick(final Widget sender) {
				ServiceHelper.getSettingsService().setDefaultScanFilter(
						viewer.getSelection(), new StatusCallback() {

							@Override
							protected void doStatus(final Status status) {
								if (!status.isSuccess()) {
									Window
											.alert("Unable to save default scan filter: "
													+ status.getMessage());
								} else {
									ScanFilterCache.getInstance().refresh(true);
								}
							}
						});
			}
		});
		viewer.addAction(setDefaultAction);
		viewer.setActionVisible(setDefaultAction, false);

		final Label expandCategoriesAction = StyleHelper.add(new Label(
				"Expand Categories", false), Style.CLICKABLE);
		expandCategoriesAction.addClickListener(new ClickListener() {

			public void onClick(final Widget sender) {
				viewer.toggleCategories();
				if (viewer.isShowingCategories()) {
					expandCategoriesAction.setText("Expand Categories");
				} else {
					expandCategoriesAction.setText("View Categories");
				}
			}
		});
		viewer.addAction(expandCategoriesAction);

		editAction = StyleHelper.add(new Label("Edit", false), Style.CLICKABLE);
		editAction.addClickListener(new ClickListener() {

			public void onClick(final Widget sender) {
				setScanFilter(viewer.getSelection(), true);
			}
		});
		viewer.addAction(editAction);
		viewer.setActionVisible(editAction, false);

		deleteAction = StyleHelper.add(new Label("Delete", false),
				Style.CLICKABLE);
		deleteAction.addClickListener(new ClickListener() {

			public void onClick(final Widget sender) {
				deleteScanFilter(viewer.getSelection());
			}
		});
		viewer.addAction(deleteAction);
		viewer.setActionVisible(deleteAction, false);

		selectionPanel.add(viewer);

		editor.initialize();
		final ScanFilterCache cache = getCache();
		editor.addAction("Save", new ClickListener() {

			public void onClick(final Widget sender) {
				cache.save(editor.getUpdatedScanFilter());
			}

		});
		editor.addAction("Cancel", new ClickListener() {

			public void onClick(final Widget sender) {
				setScanFilter(cache.getItem(editor.getSelection().getUuid()),
						false);
			}
		});

	}

	@Override
	protected String getItemText(final ScanFilter item) {
		return item.getName();
	}

	@Override
	protected Widget getItemDecorator(final ScanFilter item) {
		if (item != null && item.equals(defaultScanFilter)) {
			final Widget w = StyleHelper.add(new Label("(Default)", false),
					Style.ITALICS);
			w
					.setTitle(item.getName()
							+ " is used by all projects without a manually configured Scan Filter.");
			return w;
		}
		return null;
	}

	@Override
	protected boolean isItemVisible(final ScanFilter item, final String query) {
		return LangUtil.containsIgnoreCase(item.getName(), query);
	}

	@Override
	protected void onCacheRefresh(final Cache<ScanFilter> cache,
			final Throwable failure) {
		ServiceHelper.getSettingsService().getDefaultScanFilter(
				new StandardCallback<ScanFilter>() {

					@Override
					protected void doSuccess(final ScanFilter result) {
						if (!LangUtil.equals(defaultScanFilter, result)) {
							defaultScanFilter = result;
							cache.refresh();
						}
					}
				});
	}

	@Override
	protected void onSelectionChanged(final ScanFilter item) {
		setScanFilter(item, false);
	}

	private void setScanFilter(final ScanFilter filter, final boolean edit) {
		final VerticalPanel selectionPanel = getSelectionPanel();
		if (edit && filter != null) {
			editor.setSelection(filter);
			editor.setActionsVisible(true);
			if (selectionPanel.getWidgetIndex(editor) == -1) {
				selectionPanel.clear();
				selectionPanel.add(editor);
			}
		} else {
			viewer.setSelection(filter);
			viewer.setActionVisible(setDefaultAction,
					defaultScanFilter == null ? false : !defaultScanFilter
							.equals(filter));
			final boolean localFilter = filter != null && filter.isLocal();
			viewer.setActionVisible(editAction, localFilter);
			viewer.setActionVisible(deleteAction, localFilter);
			if (selectionPanel.getWidgetIndex(viewer) == -1) {
				selectionPanel.clear();
				selectionPanel.add(viewer);
			}
		}
	}

	private void deleteScanFilter(final ScanFilter filter) {
		if (filter != null) {
			ServiceHelper.getSettingsService().deleteScanFilter(
					filter.getUuid(), new StatusCallback() {

						@Override
						protected void doStatus(final Status result) {
							getCache().refresh();
						}
					});
		}
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
						new StandardCallback<ScanFilter>() {

							@Override
							protected void doSuccess(final ScanFilter result) {
								clearWaitStatus();
								setOpen(false);

								getCache().refresh();
								Context.current().setUuid(result).submit();
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
