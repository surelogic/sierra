package com.surelogic.sierra.gwt.client;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosureEvent;
import com.google.gwt.user.client.ui.DisclosureHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.FilterEntry;
import com.surelogic.sierra.gwt.client.data.FilterSet;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class FilterSetContent extends ContentComposite {

	private static final FilterSetContent instance = new FilterSetContent();

	private final TextBox filterBox = new TextBox();
	private final Button createFilter = new Button("Create");

	private final VerticalPanel sets = new VerticalPanel();

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
		final VerticalPanel panel = new VerticalPanel();
		final HorizontalPanel head = new HorizontalPanel();
		head.add(filterBox);
		head.add(createFilter);
		panel.add(new HTML("<h2>New Category: </h2>"));
		panel.add(head);
		panel.add(sets);
		getRootPanel().add(panel, DockPanel.CENTER);
	}

	private void generateTree(List filterSets) {
		sets.clear();
		sets.add(new HTML("<h3>Categories</h3>"));
		for (final Iterator i = filterSets.iterator(); i.hasNext();) {
			sets.add(new FilterSetComposite((FilterSet) i.next()));
		}
	}

	public static FilterSetContent getInstance() {
		return instance;
	}

	private static class FilterSetComposite extends Composite {
		private final FilterSet set;
		private final DisclosurePanel panel;
		private final VerticalPanel entries;

		FilterSetComposite(FilterSet set) {
			this.set = set;
			panel = new DisclosurePanel();
			entries = new VerticalPanel();
			final HTML name = new HTML(set.getName());
			name.addStyleName("filter-set-name");
			panel.setHeader(name);
			panel.setContent(entries);
			panel.addEventHandler(new DisclosureHandler() {

				public void onClose(DisclosureEvent event) {
					entries.clear();
				}

				public void onOpen(DisclosureEvent event) {
					updatePanel();
				}
			});
			initWidget(panel);
		}

		private void updatePanel() {
			final Set filters = new HashSet();
			for (final Iterator pI = set.getParents().iterator(); pI.hasNext();) {
				final FilterSet parent = (FilterSet) pI.next();
				for (final Iterator fI = parent.getEntries().iterator(); fI
						.hasNext();) {
					final FilterEntry entry = (FilterEntry) fI.next();
					if (entry.isFiltered()) {
						filters.remove(entry);
					} else {
						filters.add(entry);
					}
				}
			}
			for (final Iterator fI = set.getEntries().iterator(); fI.hasNext();) {
				final FilterEntry entry = (FilterEntry) fI.next();
				if (entry.isFiltered()) {
					filters.remove(entry);
				} else {
					filters.add(entry);
				}
			}
			final TreeItem treeSet = new TreeItem(set.getName());
			treeSet.setUserObject(set);
			for (final Iterator fI = filters.iterator(); fI.hasNext();) {
				entries.add(new FilterEntryComposite((FilterEntry) fI.next()));
			}
		}
	}

	private static class FilterEntryComposite extends Composite {
		private final FilterEntry entry;
		private final HorizontalPanel panel;
		private final CheckBox box;

		FilterEntryComposite(FilterEntry entry) {
			panel = new HorizontalPanel();
			box = new CheckBox();
			this.entry = entry;
			box.setChecked(!entry.isFiltered());
			final HTML name = new HTML(entry.getName() + ".....");
			name.addStyleName("filter-entry-name");
			panel.add(name);
			panel.add(box);
			panel.setCellHorizontalAlignment(name,
					HasHorizontalAlignment.ALIGN_LEFT);
			panel.setCellHorizontalAlignment(box,
					HasHorizontalAlignment.ALIGN_RIGHT);
			panel.addStyleName("filter-entry-widget");
			initWidget(panel);
		}

		public FilterEntry getEntry() {
			return entry;
		}

		public boolean isFiltered() {
			return !box.isChecked();
		}
	}
}
