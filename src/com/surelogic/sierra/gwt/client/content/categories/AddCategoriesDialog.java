package com.surelogic.sierra.gwt.client.content.categories;

import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.gwt.client.content.common.FindingSelectionDialog;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.cache.CategoryCache;

public class AddCategoriesDialog extends FindingSelectionDialog {

	public AddCategoriesDialog() {
		super("Select Categories and/or Findings");
	}

	public void setCategories(CategoryCache categories, Category currentCategory) {
		clearFindings();
		for (final Category cat : categories) {
			if (!hasCategory(currentCategory, cat, new ArrayList<Category>())) {
				addCategory(cat);
			}
		}
	}

	private boolean hasCategory(Category selectedCategory,
			Category testCategory, List<Category> checked) {
		// prevent infinite recursion
		if (checked.contains(selectedCategory)) {
			return false;
		} else {
			checked.add(selectedCategory);
		}

		if (selectedCategory.equals(testCategory)) {
			return true;
		}

		for (final Category child : selectedCategory.getParents()) {
			if (hasCategory(child, testCategory, checked)) {
				return true;
			}
		}
		return false;
	}

}
