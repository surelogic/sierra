package com.surelogic.sierra.gwt.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
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
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.FilterEntry;
import com.surelogic.sierra.gwt.client.data.FilterSet;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.StatusBox;

public class FilterSetContent extends ContentComposite {

	private static final FilterSetContent instance = new FilterSetContent();

	private final TextBox filterBox = new TextBox();
	private final Button createFilter = new Button("Create");
	private final StatusBox status = new StatusBox();
	private final VerticalPanel sets = new VerticalPanel();

	protected void onActivate(Context context) {
		refreshFilterList();
	}

	protected boolean onDeactivate() {
		return true;
	}

	protected void onInitialize(DockPanel rootPanel) {
		final VerticalPanel panel = new VerticalPanel();
		final HorizontalPanel head = new HorizontalPanel();
		head.add(filterBox);
		head.add(createFilter);
		head.add(status);
		panel.add(new HTML("<h2>New Category: </h2>"));
		panel.add(head);
		panel.add(sets);
		sets.add(new HTML("Loading..."));
		getRootPanel().add(panel, DockPanel.CENTER);
		createFilter.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				createFilter();
			}
		});
	}

	private void refreshFilterList() {
		ServiceHelper.getSettingsService().getFilterSets(new AsyncCallback() {

			public void onFailure(Throwable caught) {
				// TODO
			}

			public void onSuccess(Object result) {
				sets.clear();
				sets.add(new HTML("<h3>Categories</h3>"));
				for (final Iterator i = ((List) result).iterator(); i.hasNext();) {
					sets.add(new FilterSetComposite((FilterSet) i.next()));
				}
			}
		});

	}

	private void createFilter() {
		final String name = filterBox.getText();
		if ((name == null) || (name.length() == 0)) {
			status.setStatus(Status
					.failure("You must enter a name for your filter set."));
			return;
		}
		final List parents = new ArrayList();
		final List entries = new ArrayList();
		// Iterate over sets, but account for header tag
		for (int i = 1; i < sets.getWidgetCount(); i++) {
			final FilterSetComposite fs = (FilterSetComposite) sets
					.getWidget(i);
			if (fs.isSelected()) {
				parents.add(fs.getUuid());
				entries.addAll(fs.filteredEntries());
			}
		}
		ServiceHelper.getSettingsService().createFilterSet(name, entries,
				parents, new AsyncCallback() {

					public void onFailure(Throwable caught) {
						// TODO
					}

					public void onSuccess(Object result) {
						filterBox.setText("");
						refreshFilterList();
						status.setStatus((Status) result);
					}
				});
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

		private void updateFilters(Set filters, FilterSet set) {
			for (final Iterator i = set.getParents().iterator(); i.hasNext();) {
				final FilterSet parent = (FilterSet) i.next();
				updateFilters(filters, parent);
			}
			for (final Iterator i = set.getEntries().iterator(); i.hasNext();) {
				final FilterEntry entry = (FilterEntry) i.next();
				if (entry.isFiltered()) {
					filters.remove(entry);
				} else {
					filters.add(entry);
				}
			}
		}

		private void updatePanel() {
			final Set filters = new HashSet();
			updateFilters(filters, set);
			final TreeItem treeSet = new TreeItem(set.getName());
			treeSet.setUserObject(set);
			for (final Iterator fI = filters.iterator(); fI.hasNext();) {
				entries.add(new FilterEntryComposite((FilterEntry) fI.next()));
			}
		}

		public boolean isSelected() {
			return panel.isOpen();
		}

		public String getUuid() {
			return set.getUuid();
		}

		public List filteredEntries() {
			final List list = new ArrayList();
			final int count = entries.getWidgetCount();
			for (int i = 0; i < count; i++) {
				final FilterEntryComposite c = (FilterEntryComposite) entries
						.getWidget(i);
				if (c.isFiltered()) {
					list.add(c.getEntry());
				}
			}
			return list;
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
			entry.setFiltered(!box.isChecked());
			return entry;
		}

		public boolean isFiltered() {
			return !box.isChecked();
		}
	}
}
