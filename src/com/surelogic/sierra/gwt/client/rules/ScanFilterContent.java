package com.surelogic.sierra.gwt.client.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestionEvent;
import com.google.gwt.user.client.ui.SuggestionHandler;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.CacheListenerAdapter;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.ScanFilterEntry;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.rules.FindingTypeSuggestOracle.Suggestion;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.ActionPanel;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.ClickLabel;
import com.surelogic.sierra.gwt.client.ui.ItemLabel;
import com.surelogic.sierra.gwt.client.ui.SelectionTracker;
import com.surelogic.sierra.gwt.client.ui.StatusBox;
import com.surelogic.sierra.gwt.client.ui.StyledButton;
import com.surelogic.sierra.gwt.client.util.ImageHelper;
import com.surelogic.sierra.gwt.client.util.UI;

public class ScanFilterContent extends ContentComposite {

	private static final String FILTER = "filter";
	private final ActionBlock block = new ActionBlock();
	private final ScanFilterList scans = new ScanFilterList();
	private final ScanFilterCache cache = new ScanFilterCache();
	private final ScanFilterComposite sf = new ScanFilterComposite();

	private String filterUuid;

	@Override
	protected void onInitialize(DockPanel rootPanel) {
		block.initialize();
		scans.initialize();
		sf.initialize();
		final VerticalPanel west = new VerticalPanel();
		west.add(block);
		west.add(scans);
		west.setWidth("100%");
		rootPanel.add(west, DockPanel.WEST);
		rootPanel.add(sf, DockPanel.EAST);
		rootPanel.setCellWidth(west, "25%");
		rootPanel.setCellWidth(sf, "75%");
		cache.addListener(new CacheListenerAdapter<ScanFilter>() {

			@Override
			public void onRefresh(Cache<ScanFilter> cache, Throwable failure) {
				checkForFilter();
			}

		});
	}

	@Override
	protected void onDeactivate() {
	}

	@Override
	protected void onUpdate(Context context) {
		filterUuid = context.getParameter(FILTER);
		checkForFilter();
	}

	private void checkForFilter() {
		if (filterUuid != null) {
			for (final ScanFilter f : cache.getItems()) {
				if (f.getUuid().equals(filterUuid)) {
					sf.setFilter(f);
				}
			}
		}
	}

	class ScanFilterList extends BlockPanel {
		private final FlexTable grid = new FlexTable();
		private final ScanFilterResults results = new ScanFilterResults();
		private TextBox searchText;

		@Override
		protected void onInitialize(VerticalPanel contentPanel) {
			final Label searchLabel = new Label("Search");
			searchText = new TextBox();
			searchText.addKeyboardListener(new KeyboardListenerAdapter() {
				@Override
				public void onKeyUp(Widget sender, char keyCode, int modifiers) {
					results.search();
				}
			});
			searchText.addChangeListener(new ChangeListener() {
				public void onChange(Widget sender) {
					results.search();
				}
			});
			grid.setWidth("100%");
			grid.getColumnFormatter().setWidth(0, "25%");
			grid.getColumnFormatter().setWidth(1, "75%");
			searchText.setWidth("100%");
			grid.setWidget(0, 0, searchLabel);
			grid.setWidget(0, 1, searchText);
			contentPanel.add(grid);
			contentPanel.add(results);
			results.initialize();
		}

		private class ScanFilterResults extends BlockPanel {

			private VerticalPanel list;
			private final SelectionTracker selection = new SelectionTracker();

			@Override
			protected void onInitialize(VerticalPanel contentPanel) {
				list = contentPanel;
				cache.addListener(new CacheListenerAdapter<ScanFilter>() {

					@Override
					public void onRefresh(Cache<ScanFilter> cache,
							Throwable failure) {
						search();
					}
				});
				cache.refresh();
			}

			void search() {
				final String text = searchText.getText();
				final StringBuffer queryBuf = new StringBuffer();
				queryBuf.append(".*");
				for (int i = 0; i < text.length(); i++) {
					final char ch = text.charAt(i);
					if (Character.isLetterOrDigit(ch)) {
						queryBuf.append(Character.toLowerCase(ch));
					}
				}
				queryBuf.append(".*");
				final String query = queryBuf.toString();
				list.clear();
				final ClickListener listener = new ClickListener() {
					public void onClick(Widget sender) {
						ItemLabel label = (ItemLabel) sender;
						goToScanFilter(((ScanFilter) (label.getItem()))
								.getUuid());
					}
				};
				boolean success = false;
				for (final ScanFilter f : cache) {
					if (f.getName().toLowerCase().matches(query)) {
						success = true;
						list.add(new ItemLabel(f.getName(), f, selection,
								listener));
					}
				}
				if (!success) {
					list.add(new HTML("None found."));
				}
			}

		}
	}

	private class ScanFilterComposite extends BlockPanel {

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
			cache.addListener(new CacheListenerAdapter<ScanFilter>() {

				@Override
				public void onItemUpdate(Cache<ScanFilter> cache,
						ScanFilter item, Status status, Throwable failure) {
					cache.refresh();
					ScanFilterComposite.this.status.setStatus(status);
				}
			});
			addAction("Delete", new ClickListener() {

				public void onClick(Widget sender) {
					if (filter != null) {
						ServiceHelper.getSettingsService().deleteScanFilter(
								filter.getUuid(), new AsyncCallback<Status>() {

									public void onFailure(Throwable caught) {
										status.setStatus(Status.failure(caught
												.getMessage()));
									}

									public void onSuccess(Status result) {
										if (result.isSuccess()) {
											cache.refresh();
											setFilter(null);
										} else {
											status.setStatus(result);
										}
									}
								});
					}
				}
			});
		}

		private void refresh() {
			panel.clear();
			if (filter != null) {
				setTitle(filter.getName());
				panel
						.add(new Label(
								"A scan filter specifies the finding types that are included when a scan is loaded into the system.  You can add finding types individually, or you can add all of the finding types in a category at once. You can also set the importance that a particular finding type or category has."));
				panel.add(UI.h3("Categories"));
				cPanel = new FilterEntries(filter.getCategories());
				panel.add(addCategoryBox());
				panel.add(cPanel);
				panel.add(UI.h3("Finding Types"));
				final HorizontalPanel buttonPanel = new HorizontalPanel();
				ftPanel = new FilterEntries(filter.getTypes());
				panel.add(addFindingTypeBox());
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
			this.filter = filter;
			refresh();
		}

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

	private class ActionBlock extends BlockPanel {
		private final StyledButton createScanFilterButton = new StyledButton(
				"Create a Scan Filter");
		private final VerticalPanel scanFilterCreatePanel = new VerticalPanel();
		private final ActionPanel scanFilterActions = new ActionPanel();
		private final FlexTable fieldTable = new FlexTable();
		private final TextBox categoryName = new TextBox();
		private final Image waitImage = ImageHelper.getWaitImage(16);

		@Override
		protected void onInitialize(VerticalPanel contentPanel) {
			createScanFilterButton.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					toggleCreateScanFilter();
				}
			});
			contentPanel.add(createScanFilterButton);

			scanFilterCreatePanel.setWidth("100%");
			fieldTable.setWidth("100%");
			fieldTable.setText(0, 0, "Name:");
			fieldTable.setWidget(0, 1, categoryName);
			scanFilterCreatePanel.add(fieldTable);

			scanFilterActions.addAction("Save", new ClickListener() {

				public void onClick(Widget sender) {
					createScanFilter(categoryName.getText());
				}
			});
			scanFilterActions.addAction("Cancel", new ClickListener() {

				public void onClick(Widget sender) {
					toggleCreateScanFilter();
				}
			});
			scanFilterCreatePanel.add(scanFilterActions);
			scanFilterCreatePanel.setCellHorizontalAlignment(scanFilterActions,
					HasHorizontalAlignment.ALIGN_RIGHT);
		}

		private void toggleCreateScanFilter() {
			final VerticalPanel contentPanel = getContentPanel();
			if (contentPanel.getWidgetIndex(scanFilterCreatePanel) != -1) {
				contentPanel.remove(scanFilterCreatePanel);
			} else {
				categoryName.setText("");
				if (scanFilterCreatePanel.getWidgetIndex(scanFilterActions) == -1) {
					scanFilterCreatePanel.add(scanFilterActions);
				}
				if (scanFilterCreatePanel.getWidgetIndex(waitImage) != -1) {
					scanFilterCreatePanel.remove(waitImage);
				}
				final int panelIndex = contentPanel
						.getWidgetIndex(createScanFilterButton);
				contentPanel.insert(scanFilterCreatePanel, panelIndex + 1);
			}
		}

		private void createScanFilter(String name) {
			scanFilterActions.removeFromParent();
			scanFilterCreatePanel.add(waitImage);

			ServiceHelper.getSettingsService().createScanFilter(name,
					new AsyncCallback<ScanFilter>() {

						public void onFailure(Throwable caught) {
							Window.alert("Category creation failed: "
									+ caught.getMessage());
							scanFilterCreatePanel.remove(waitImage);
							scanFilterCreatePanel.add(scanFilterActions);
						}

						public void onSuccess(ScanFilter result) {
							toggleCreateScanFilter();
							cache.refresh();
							goToScanFilter(result.getUuid());
						}
					});
		}
	}

	private static void goToScanFilter(String uuid) {
		final Map<String, String> map = new HashMap<String, String>();
		map.put(FILTER, uuid);
		ContextManager.setContext(Context.create(Context.create("scanfilters"),
				map));
	}

	// Singleton
	private ScanFilterContent() {

	}

	private static final ScanFilterContent instance = new ScanFilterContent();

	public static ScanFilterContent getInstance() {
		return instance;
	}

}
