package com.surelogic.sierra.gwt.client.content.categories;

import java.util.HashSet;
import java.util.Set;

import com.surelogic.sierra.gwt.client.content.common.FindingSelectionDialog;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FindingTypeFilter;
import com.surelogic.sierra.gwt.client.data.cache.CategoryCache;

public class AddCategoriesDialog extends FindingSelectionDialog {

	public AddCategoriesDialog() {
		super("Select Categories and/or Findings");
	}

	public void update(final CategoryCache categories,
			final Category currentCategory) {
		clearFindings();

		final Set<FindingTypeFilter> excludeFindings = currentCategory
				.getIncludedEntries();
		final Set<String> excludedFindingUuids = new HashSet<String>(
				excludeFindings.size());
		for (final FindingTypeFilter excludedFinding : excludeFindings) {
			excludedFindingUuids.add(excludedFinding.getUuid());
		}

		for (final Category cat : categories) {
			addCategory(cat, excludedFindingUuids);
		}
	}

}
