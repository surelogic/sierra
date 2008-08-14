package com.surelogic.sierra.gwt.client.content.scans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.ListBox;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.cache.Cache;
import com.surelogic.sierra.gwt.client.data.cache.CacheListenerAdapter;
import com.surelogic.sierra.gwt.client.data.cache.CategoryCache;

public class CategoryChoice extends ListBox {

	private final List<String> uuids;

	public CategoryChoice() {
		super(true);
		uuids = new ArrayList<String>();
		CategoryCache.getInstance().refresh(false,
				new CacheListenerAdapter<Category>() {
					@Override
					public void onRefresh(final Cache<Category> cache,
							final Throwable failure) {
						for (final Category c : cache) {
							uuids.add(c.getUuid());
							addItem(c.getName());
						}
					}
				});
	}

	/**
	 * Returns all of the selected importance values. If 'Default' is selected,
	 * it will return all importance values that belong to the default set.
	 * 
	 * @return
	 */
	public Set<String> getSelectedCategories() {
		final Set<String> selected = new HashSet<String>();
		for (int i = 0; i < uuids.size(); i++) {
			if (isItemSelected(i)) {
				selected.add(uuids.get(i));
			}
		}
		return selected;
	}

}
