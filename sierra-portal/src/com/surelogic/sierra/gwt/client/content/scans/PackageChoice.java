package com.surelogic.sierra.gwt.client.content.scans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.ListBox;
import com.surelogic.sierra.gwt.client.data.ScanDetail;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StandardCallback;

public class PackageChoice extends ListBox {

	private final List<String> packageList;

	public PackageChoice() {
		this(true);
	}

	@SuppressWarnings("unchecked")
	public PackageChoice(final boolean allowMultiples) {
		this(Collections.EMPTY_SET, allowMultiples);
	}

	/**
	 * Create a package choice box w/ the specified packages as options.
	 * 
	 * @param packages
	 */
	public PackageChoice(final Set<String> packages) {
		this(packages, false);
	}

	/**
	 * Create a package choice box w/ the specified packages as options.
	 * 
	 * @param packages
	 * @param allowMultiples
	 */
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

	/**
	 * Display the packages available for this particular scan as choices.
	 * 
	 * @param scanUuid
	 */
	public void displayScanPackages(final String scanUuid) {
		ServiceHelper.getFindingService().getScanDetail(scanUuid,
				new StandardCallback<ScanDetail>() {
					@Override
					protected void doSuccess(final ScanDetail result) {
						final Set<String> packages = result.getCompilations()
								.keySet();
						clear();
						packageList.clear();
						addItem("*all packages*");
						setSelectedIndex(0);
						packageList.addAll(packages);
						Collections.sort(packageList);
						for (final String pakkage : packageList) {
							addItem(pakkage);
						}
					}
				});
	}
}
