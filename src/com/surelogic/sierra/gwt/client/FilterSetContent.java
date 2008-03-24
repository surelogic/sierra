package com.surelogic.sierra.gwt.client;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.FilterEntry;
import com.surelogic.sierra.gwt.client.data.FilterSet;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class FilterSetContent extends ContentComposite {

	private static final FilterSetContent instance = new FilterSetContent();

	private final Tree tree = new Tree();

	public String getContentName() {
		return "FilterSets";
	}

	protected void onActivate(Context context) {
		ServiceHelper.getSettingsService().getFilterSets(new AsyncCallback() {

			public void onFailure(Throwable caught) {
				// TODO
			}

			public void onSuccess(Object result) {
				generateTree((List) result);
			}
		});

	}

	protected boolean onDeactivate() {
		return true;
	}

	protected void onInitialize(DockPanel rootPanel) {
		VerticalPanel panel = new VerticalPanel();
		panel.add(tree);
		getRootPanel().add(panel, DockPanel.CENTER);
	}

	private void generateTree(List sets) {
		tree.clear();
		for (Iterator i = sets.iterator(); i.hasNext();) {
			final FilterSet set = (FilterSet) i.next();
			final Set filters = new HashSet();
			for (Iterator pI = set.getParents().iterator(); pI.hasNext();) {
				final FilterSet parent = (FilterSet) pI.next();
				for (Iterator fI = parent.getEntries().iterator(); fI.hasNext();) {
					final FilterEntry entry = (FilterEntry) fI.next();
					if (entry.isFiltered()) {
						filters.remove(entry);
					} else {
						filters.add(entry);
					}
				}
			}
			for (Iterator fI = set.getEntries().iterator(); fI.hasNext();) {
				final FilterEntry entry = (FilterEntry) fI.next();
				if (entry.isFiltered()) {
					filters.remove(entry);
				} else {
					filters.add(entry);
				}
			}
			final TreeItem treeSet = new TreeItem(set.getName());
			treeSet.setUserObject(set);
			for (Iterator fI = filters.iterator(); fI.hasNext();) {
				final FilterEntry entry = (FilterEntry) fI.next();
				final TreeItem treeFilter = new TreeItem(entry.getName());
				treeFilter.setUserObject(entry);
				treeSet.addItem(treeFilter);
			}
			tree.addItem(treeSet);
		}
	}

	public static FilterSetContent getInstance() {
		return instance;
	}

}
