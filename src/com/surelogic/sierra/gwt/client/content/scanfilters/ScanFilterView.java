package com.surelogic.sierra.gwt.client.content.scanfilters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestionEvent;
import com.google.gwt.user.client.ui.SuggestionHandler;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.ScanFilterEntry;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.cache.ScanFilterCache;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.ClickLabel;
import com.surelogic.sierra.gwt.client.ui.ImportanceChoice;
import com.surelogic.sierra.gwt.client.ui.StatusBox;
import com.surelogic.sierra.gwt.client.util.UI;

public class ScanFilterView extends BlockPanel {

	private VerticalPanel panel;
	private FilterEntries ftPanel;
	private FilterEntries cPanel;
	private ScanFilter filter;
	private StatusBox status;

	@Override
	protected void onInitialize(VerticalPanel panel) {
		this.panel = panel;
		status = new StatusBox();
		refresh();

	}

	public ScanFilter getSelection() {
		return filter;
	}

	public void setSelection(ScanFilter filter) {
		this.filter = filter;
		refresh();
	}

	public void setStatus(Status s) {
		this.status.setStatus(s);
	}

	private void refresh() {
		panel.clear();
		if (filter != null) {
			setSummary(filter.getName());
			panel
					.add(new Label(
							"A scan filter specifies the finding types that are included when a scan is loaded into the system.  You can add finding types individually, or you can add all of the finding types in a category at once. You can also set the importance that a particular finding type or category has."));
			panel.add(UI.h3("Categories"));
			cPanel = new FilterEntries(filter.getCategories());
			panel.add(addCategoryBox());
			panel.add(cPanel);
			panel.add(UI.h3("Finding Types"));
			ftPanel = new FilterEntries(filter.getTypes());
			panel.add(addFindingTypeBox());
			panel.add(ftPanel);
			final HorizontalPanel buttonPanel = new HorizontalPanel();
			panel.add(buttonPanel);
			buttonPanel.add(new Button("Update", new ClickListener() {
				public void onClick(Widget sender) {
					ScanFilterCache.getInstance().save(filter);
				}
			}));
			status = new StatusBox();
			panel.add(status);
		} else {
			panel.add(UI.h1("None selected"));
		}
	}

	private Widget addFindingTypeBox() {
		final VerticalPanel panel = new VerticalPanel();
		panel
				.add(new Label(
						"Begin typing to search for a finding type to add to this scan filter.  Use * to match any text."));
		final SuggestBox box = new SuggestBox(new FindingTypeSuggestOracle());
		box.addEventHandler(new SuggestionHandler() {
			public void onSuggestionSelected(SuggestionEvent event) {
				final CategorySuggestOracle.Suggestion s = (com.surelogic.sierra.gwt.client.content.scanfilters.CategorySuggestOracle.Suggestion) event
						.getSelectedSuggestion();
				cPanel.addEntry(s.getEntry());
			}
		});
		panel.add(box);
		return panel;
	}

	private Widget addCategoryBox() {
		final VerticalPanel panel = new VerticalPanel();
		panel
				.add(new Label(
						"Begin typing to search for a category to add to this scan filter.  Use * to match any text."));
		final SuggestBox box = new SuggestBox(new CategorySuggestOracle());
		box.addEventHandler(new SuggestionHandler() {
			public void onSuggestionSelected(SuggestionEvent event) {
				final CategorySuggestOracle.Suggestion s = (com.surelogic.sierra.gwt.client.content.scanfilters.CategorySuggestOracle.Suggestion) event
						.getSelectedSuggestion();
				cPanel.addEntry(s.getEntry());
			}
		});
		panel.add(box);
		return panel;
	}

	private static class FilterEntries extends Grid {

		private final Set<ScanFilterEntry> entries;

		public FilterEntries(Set<ScanFilterEntry> entries) {
			super();
			this.entries = entries;
			resize(entries.size() + 1, 3);
			setText(0, 0, "Name");
			setText(0, 1, "Importance");
			getCellFormatter().addStyleName(0, 0, "scan-filter-entry-title");
			getCellFormatter().addStyleName(0, 1, "scan-filter-entry-title");
			int row = 1;
			final List<ScanFilterEntry> sortedEntries = new ArrayList<ScanFilterEntry>(
					entries);
			Collections.sort(sortedEntries);
			for (final ScanFilterEntry entry : sortedEntries) {
				entry(row++, entry);
			}
		}

		public void addEntry(ScanFilterEntry e) {
			if (!entries.contains(e)) {
				entries.add(e);
				final int row = getRowCount();
				resize(row + 1, 3);
				entry(row, e);
			}
		}

		private void entry(int row, final ScanFilterEntry e) {
			final HTML h = new HTML(e.getName());
			h.setTitle(e.getShortMessage());
			final ImportanceChoice box = new ImportanceChoice();
			box.setSelectedImportance(e.getImportance());
			box.addChangeListener(new ChangeListener() {
				public void onChange(Widget sender) {
					e.setImportance(((ImportanceChoice) sender)
							.getSelectedImportance());
				}
			});
			final Label remove = new ClickLabel("remove", new ClickListener() {
				public void onClick(Widget sender) {
					entries.remove(e);
					resize(getRowCount() - 1, 3);
					int row = 1;
					for (final Object element2 : entries) {
						entry(row++, (ScanFilterEntry) element2);
					}
				}
			});
			setWidget(row, 0, h);
			setWidget(row, 1, box);
			setWidget(row, 2, remove);
		}
	}
}
