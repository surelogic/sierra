package com.surelogic.sierra.gwt.client.content.scans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.ListBox;

public class PackageChoice extends ListBox {

	private final List<String> packageList;

	public PackageChoice(final Set<String> packages) {
		this(packages, false);
	}

	public PackageChoice(final Set<String> packages,
			final boolean allowMultiples) {
		super(allowMultiples);
		addItem("*all packages*");
		setSelectedIndex(0);
		packageList = new ArrayList<String>();
		packageList.addAll(packages);
		Collections.sort(packageList);
		for (final String pakkage : packageList) {
			addItem(pakkage);
		}
	}

	/**
	 * Returns all of the selected importance values. If 'Default' is selected,
	 * it will return all importance values that belong to the default set.
	 * 
	 * @return
	 */
	public Set<String> getSelectedPackages() {
		final Set<String> selected = new HashSet<String>();
		if (isItemSelected(0)) {
			selected.addAll(packageList);
		} else {
			for (int i = 1; i <= packageList.size(); i++) {
				if (isItemSelected(i)) {
					selected.add(getItemText(i));
				}
			}
		}
		return selected;
	}

	/**
	 * Return a single selected importance, or <code>null</code> if 'Default' is
	 * selected.
	 * 
	 * @return
	 */
	public String getSelectedPackage() {
		final String selected = getItemText(getSelectedIndex());
		if ("*all packages*".equals(selected)) {
			return null;
		}
		return selected;
	}

}
