package com.surelogic.sierra.gwt.client.content.projects;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.cache.Cache;
import com.surelogic.sierra.gwt.client.data.cache.CacheListener;
import com.surelogic.sierra.gwt.client.data.cache.CacheListenerAdapter;
import com.surelogic.sierra.gwt.client.data.cache.ScanFilterCache;
import com.surelogic.sierra.gwt.client.ui.FormDialog;
import com.surelogic.sierra.gwt.client.ui.ItalicLabel;
import com.surelogic.sierra.gwt.client.ui.ItemLabel;
import com.surelogic.sierra.gwt.client.ui.SelectionTracker;
import com.surelogic.sierra.gwt.client.util.ImageHelper;

public class ScanFilterDialog extends FormDialog {
	private final VerticalPanel scanFilterPanel = new VerticalPanel();
	private final SelectionTracker<ItemLabel<ScanFilter>> selectionTracker = new SelectionTracker<ItemLabel<ScanFilter>>();

	@Override
	protected void doInitialize(FlexTable contentTable) {
		setText("Select Scan Filter");
		setWidth("500px");

		scanFilterPanel.setWidth("100%");

		final ScrollPanel scanFilterScroller = new ScrollPanel(scanFilterPanel);
		scanFilterScroller.setWidth("100%");
		scanFilterScroller.setAlwaysShowScrollBars(true);
		scanFilterScroller.setHeight("425px");

		contentTable.setWidget(0, 0, scanFilterScroller);
	}

	@Override
	protected HasFocus getInitialFocus() {
		return null;
	}

	public void setScanFilter(final ScanFilter selectedFilter) {
		final ScanFilterCache scanFilters = ScanFilterCache.getInstance();

		final CacheListener<ScanFilter> cacheListener = new CacheListenerAdapter<ScanFilter>() {
			@Override
			public void onStartRefresh(Cache<ScanFilter> cache) {
				scanFilterPanel.add(ImageHelper.getWaitImage(16));
			}

			@Override
			public void onRefresh(Cache<ScanFilter> cache, Throwable failure) {
				scanFilters.removeListener(this);

				scanFilterPanel.clear();
				for (final ScanFilter filter : scanFilters) {
					final ItemLabel<ScanFilter> filterCheck = new ItemLabel<ScanFilter>(
							filter.getName(), filter, selectionTracker, null);
					scanFilterPanel.add(filterCheck);
					filterCheck.setSelected(filter.equals(selectedFilter));
				}

				if (scanFilterPanel.getWidgetCount() == 0) {
					scanFilterPanel.add(new ItalicLabel(
							"No scan filters to add"));
					setOkEnabled(false);
				} else {
					setOkEnabled(true);
				}
			}

		};

		scanFilters.addListener(cacheListener);
		scanFilters.refresh();
	}

	public ScanFilter getSelectedFilter() {
		final ItemLabel<ScanFilter> selection = selectionTracker.getSelected();
		return selection == null ? null : selection.getItem();
	}

	@Override
	protected void doOkClick() {
		setStatus(Status.success());
		hide();
	}

}
