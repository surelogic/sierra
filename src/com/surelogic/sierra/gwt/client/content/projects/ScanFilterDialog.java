package com.surelogic.sierra.gwt.client.content.projects;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.ListBox;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.cache.Cache;
import com.surelogic.sierra.gwt.client.data.cache.CacheListenerAdapter;
import com.surelogic.sierra.gwt.client.data.cache.ScanFilterCache;
import com.surelogic.sierra.gwt.client.ui.dialog.FormDialog;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ScanFilterDialog extends FormDialog {
	private ListBox filters;

	public ScanFilterDialog() {
		super("Select Scan Filter", "200px");
	}

	@Override
	protected void doInitialize(final FlexTable contentTable) {
		filters = new ListBox(false);
		filters.setWidth("100%");
		contentTable.setWidget(0, 0, filters);
	}

	@Override
	protected HasFocus getInitialFocus() {
		return null;
	}

	public void update(final ScanFilter selectedFilter) {
		ScanFilterCache.getInstance().refresh(false,
				new CacheListenerAdapter<ScanFilter>() {
					@Override
					public void onStartRefresh(final Cache<ScanFilter> cache) {
						filters.clear();
						filters.addItem("Retrieving filters");
						filters.setSelectedIndex(0);
					}

					@Override
					public void onRefresh(final Cache<ScanFilter> cache,
							final Throwable failure) {
						filters.clear();
						for (final ScanFilter filter : cache) {
							filters.addItem(filter.getName(), filter.getUuid());
							if (LangUtil.equals(filter, selectedFilter)) {
								filters
										.setSelectedIndex(filters
												.getItemCount() - 1);
							}
						}

						if (filters.getItemCount() == 0) {
							filters.addItem("No scan filters to add");
							setOkEnabled(false);
						} else {
							setOkEnabled(true);
						}
					}

				});
	}

	public ScanFilter getSelectedFilter() {
		final int selectedIndex = filters.getSelectedIndex();
		if (selectedIndex >= 0) {
			final String uuid = filters.getValue(selectedIndex);
			return ScanFilterCache.getInstance().getItem(uuid);
		}
		return null;
	}

}
