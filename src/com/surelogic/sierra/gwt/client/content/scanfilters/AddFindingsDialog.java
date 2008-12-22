package com.surelogic.sierra.gwt.client.content.scanfilters;

import java.util.HashSet;
import java.util.Set;

import com.surelogic.sierra.gwt.client.content.common.FindingSelectionDialog;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.ScanFilterEntry;
import com.surelogic.sierra.gwt.client.data.cache.Cache;
import com.surelogic.sierra.gwt.client.data.cache.CacheListenerAdapter;
import com.surelogic.sierra.gwt.client.data.cache.CategoryCache;

public class AddFindingsDialog extends FindingSelectionDialog {

	public AddFindingsDialog() {
		super("Select Finding Types");
	}

	public void update(final Set<ScanFilterEntry> excludeCategories,
			final Set<ScanFilterEntry> excludeFindings) {
		clearFindings();

		final Set<String> excludedCategoryUuids = getUuids(excludeCategories);
		final Set<String> excludedFindingUuids = getUuids(excludeFindings);

		CategoryCache.getInstance().refresh(false,
				new CacheListenerAdapter<Category>() {
					@Override
					public void onStartRefresh(final Cache<Category> cache) {
						addWaitStatus();
					}

					@Override
					public void onRefresh(final Cache<Category> cache,
							final Throwable failure) {
						clearFindings();
						for (final Category cat : cache) {
							if (!isExcluded(cat.getUuid(),
									excludedCategoryUuids)) {
								addCategory(cat, excludedFindingUuids);
							}
						}
						refreshUI();
					}

				});
	}

	private Set<String> getUuids(final Set<ScanFilterEntry> filterEntries) {
		final Set<String> entryUuids = new HashSet<String>(filterEntries.size());
		for (final ScanFilterEntry filter : filterEntries) {
			entryUuids.add(filter.getUuid());
		}
		return entryUuids;
	}

}
