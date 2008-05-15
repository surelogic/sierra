package com.surelogic.sierra.gwt.client.rules;

import java.util.Iterator;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestionEvent;
import com.google.gwt.user.client.ui.SuggestionHandler;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.CacheListener;
import com.surelogic.sierra.gwt.client.data.Cacheable;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.ScanFilterEntry;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.rules.FindingTypeSuggestOracle.Suggestion;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.ClickLabel;
import com.surelogic.sierra.gwt.client.ui.EnterListener;
import com.surelogic.sierra.gwt.client.ui.StatusBox;
import com.surelogic.sierra.gwt.client.util.UI;

public class ScanFilterContent extends ContentComposite {

	private static final String FILTER = "filter";

	private final ScanFilterList scans = new ScanFilterList();
	private final ScanFilterCache cache = new ScanFilterCache();
	private final ScanFilterComposite sf = new ScanFilterComposite();

	private String filterUuid;

	protected void onInitialize(DockPanel rootPanel) {
		scans.initialize();
		sf.initialize();
		rootPanel.add(scans, DockPanel.WEST);
		rootPanel.add(sf, DockPanel.EAST);
		rootPanel.setCellWidth(scans, "25%");
		rootPanel.setCellWidth(sf, "75%");
		cache.addListener(new CacheListener() {

			public void onItemUpdate(Cache cache, Cacheable item,
					Status status, Throwable failure) {

			}

			public void onRefresh(Cache cache, Throwable failure) {
				checkForFilter();
			}

			public void onStartRefresh(Cache cache) {

			}
		});
	}

	protected void onDeactivate() {
	}

	protected void onUpdate(Context context) {
		filterUuid = context.getParameter(FILTER);
		checkForFilter();
	}

	private void checkForFilter() {
		if (filterUuid != null) {
			for (final Iterator i = cache.getItemIterator(); i.hasNext();) {
				final ScanFilter f = (ScanFilter) i.next();
				if (f.getUuid().equals(filterUuid)) {
					sf.setFilter(f);
				}
			}
		}
	}

	class ScanFilterList extends BlockPanel {
		private final VerticalPanel list = new VerticalPanel();

		protected void onInitialize(VerticalPanel contentPanel) {
			setTitle("Search");
			final HorizontalPanel searchPanel = new HorizontalPanel();
			final TextBox box = new TextBox();
			final Button button = new Button("Create");
			box.addKeyboardListener(new EnterListener() {
				public void onEnter(final Widget sender, final char keyCode,
						final int modifiers) {
					final String name = box.getText();
					if (name.length() > 0) {
						ServiceHelper.getSettingsService().createScanFilter(
								name, new AsyncCallback() {
									public void onFailure(Throwable caught) {
										// TODO
									}

									public void onSuccess(Object result) {
										cache.refresh();
									}
								});
					}
				}
			});
			button.addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
					final String name = box.getText();
					if (name.length() > 0) {
						ServiceHelper.getSettingsService().createScanFilter(
								name, new AsyncCallback() {
									public void onFailure(Throwable caught) {
										// TODO
									}

									public void onSuccess(Object result) {
										cache.refresh();
									}
								});
					}
				}
			});
			searchPanel.add(box);
			searchPanel.add(button);
			contentPanel.add(searchPanel);
			contentPanel.add(list);
			cache.addListener(new CacheListener() {
				public void onItemUpdate(Cache cache, Cacheable item,
						Status status, Throwable failure) {

				}

				public void onRefresh(Cache cache, Throwable failure) {
					list.clear();
					for (final Iterator i = cache.getItemIterator(); i
							.hasNext();) {
						final ScanFilter f = (ScanFilter) i.next();
						list.add(new Hyperlink(f.getName(), "scanfilters/"
								+ FILTER + "=" + f.getUuid()));
					}
				}

				public void onStartRefresh(Cache cache) {

				}
			});
			cache.refresh();
		}
	}

	private class ScanFilterComposite extends BlockPanel {

		private VerticalPanel panel;
		private FilterEntries ftPanel;
		private FilterEntries cPanel;
		private ScanFilter filter;
		private StatusBox status;

		protected void onInitialize(VerticalPanel panel) {
			this.panel = panel;
			status = new StatusBox();
			refresh();
			cache.addListener(new CacheListener() {

				public void onItemUpdate(Cache cache, Cacheable item,
						Status status, Throwable failure) {
					ScanFilterComposite.this.status.setStatus(status);
				}

				public void onRefresh(Cache cache, Throwable failure) {

				}

				public void onStartRefresh(Cache cache) {

				}
			});
		}

		private void refresh() {
			panel.clear();
			if (filter != null) {
				setTitle(filter.getName());
				panel
						.add(new Label(
								"A scan filter specifies the finding types that are included when a scan is loaded into the system.  You can add finding types individually, or you can add all of the finding types in a category at once. You can also set the importance that a particular finding type or category is given."));
				panel.add(UI.h3("Categories"));
				panel.add(addCategoryBox());
				cPanel = new FilterEntries(filter.getCategories());
				panel.add(cPanel);
				panel.add(UI.h3("Finding Types"));
				panel.add(addFindingTypeBox());
				final HorizontalPanel buttonPanel = new HorizontalPanel();
				ftPanel = new FilterEntries(filter.getTypes());
				panel.add(ftPanel);
				panel.add(buttonPanel);
				buttonPanel.add(new Button("Update", new ClickListener() {
					public void onClick(Widget sender) {
						cache.save(filter);
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
			final SuggestBox box = new SuggestBox(
					new FindingTypeSuggestOracle());
			box.addEventHandler(new SuggestionHandler() {
				public void onSuggestionSelected(SuggestionEvent event) {
					final FindingTypeSuggestOracle.Suggestion s = (Suggestion) event
							.getSelectedSuggestion();
					ftPanel.addEntry(s.getEntry());
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
					final CategorySuggestOracle.Suggestion s = (com.surelogic.sierra.gwt.client.rules.CategorySuggestOracle.Suggestion) event
							.getSelectedSuggestion();
					cPanel.addEntry(s.getEntry());
				}
			});
			panel.add(box);
			return panel;
		}

		public void setFilter(ScanFilter filter) {
			this.filter = filter.copy();
			refresh();
		}

	}

	private static class FilterEntries extends Grid {

		private final Set entries;

		public FilterEntries(Set entries) {
			super();
			this.entries = entries;
			resize(entries.size() + 1, 3);
			setText(0, 0, "Name");
			setText(0, 1, "Importance");
			int row = 1;
			for (final Iterator i = entries.iterator(); i.hasNext();) {
				entry(row++, (ScanFilterEntry) i.next());
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
					for (final Iterator i = entries.iterator(); i.hasNext();) {
						entry(row++, (ScanFilterEntry) i.next());
					}
				}
			});
			setWidget(row, 0, h);
			setWidget(row, 1, box);
			setWidget(row, 2, remove);
		}
	}

	// Singleton
	private ScanFilterContent() {

	}

	private static final ScanFilterContent instance = new ScanFilterContent();

	public static ScanFilterContent getInstance() {
		return instance;
	}

}
