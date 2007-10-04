package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.widgets.Composite;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.RadioArrowMenu;
import com.surelogic.common.eclipse.RadioArrowMenu.IRadioMenuObserver;
import com.surelogic.sierra.client.eclipse.model.selection.AbstractFilterObserver;
import com.surelogic.sierra.client.eclipse.model.selection.Filter;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionFilterFactory;

public final class FilterSelectionMenu extends AbstractFilterObserver implements
		IRadioMenuObserver {

	private final CascadingList f_finder;

	private RadioArrowMenu f_menu = null;

	FilterSelectionMenu(CascadingList finder, final Filter input,
			final IRadioMenuObserver menuObserver) {
		assert finder != null;
		f_finder = finder;
		final CascadingList.IColumn m = new CascadingList.IColumn() {
			public void createContents(Composite panel) {
				f_menu = new RadioArrowMenu(panel);
				for (ISelectionFilterFactory f : input.getSelection()
						.getAvailableFilters()) {
					f_menu.addChoice(f, null);
				}
				f_menu.addSeparator();
				f_menu.addChoice("Show", null);
				f_menu.addChoice("Graph", null);
				f_menu.addObserver(menuObserver);
			}
		};
		f_finder.addColumn(m);
	}

	public void selected(Object choice, RadioArrowMenu menu) {
		// TODO Auto-generated method stub

	}
}
