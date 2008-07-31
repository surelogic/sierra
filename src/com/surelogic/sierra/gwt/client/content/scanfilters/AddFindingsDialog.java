package com.surelogic.sierra.gwt.client.content.scanfilters;

import java.util.Set;

import com.surelogic.sierra.gwt.client.content.common.FindingSelectionDialog;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.ScanFilterEntry;
import com.surelogic.sierra.gwt.client.data.cache.Cache;
import com.surelogic.sierra.gwt.client.data.cache.CacheListener;
import com.surelogic.sierra.gwt.client.data.cache.CacheListenerAdapter;
import com.surelogic.sierra.gwt.client.data.cache.CategoryCache;

public class AddFindingsDialog extends FindingSelectionDialog {

	public AddFindingsDialog() {
		super("Select Findings");
	}

	public void update(final Set<ScanFilterEntry> excludeCategories,
			final Set<ScanFilterEntry> excludeFindings) {
		clearFindings();

		final CategoryCache categories = CategoryCache.getInstance();

		final CacheListener<Category> cacheListener = new CacheListenerAdapter<Category>() {
			@Override
			public void onStartRefresh(Cache<Category> cache) {
				addWaitStatus();
			}

			@Override
			public void onRefresh(Cache<Category> cache, Throwable failure) {
				categories.removeListener(this);

				clearFindings();
				for (final Category cat : categories) {
					if (!isExcluded(cat.getUuid(), excludeCategories)) {
						addCategory(cat, excludeFindings);
					}
				}
			}

		};

		categories.addListener(cacheListener);
		categories.refresh();
	}

}
