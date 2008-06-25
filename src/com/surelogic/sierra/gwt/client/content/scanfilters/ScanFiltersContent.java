package com.surelogic.sierra.gwt.client.content.scanfilters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestionEvent;
import com.google.gwt.user.client.ui.SuggestionHandler;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ListContentComposite;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.ScanFilterEntry;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.cache.Cache;
import com.surelogic.sierra.gwt.client.data.cache.CacheListener;
import com.surelogic.sierra.gwt.client.data.cache.CacheListenerAdapter;
import com.surelogic.sierra.gwt.client.data.cache.ScanFilterCache;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.ClickLabel;
import com.surelogic.sierra.gwt.client.ui.FormButton;
import com.surelogic.sierra.gwt.client.ui.StatusBox;
import com.surelogic.sierra.gwt.client.util.LangUtil;
import com.surelogic.sierra.gwt.client.util.UI;

public class ScanFiltersContent extends
		ListContentComposite<ScanFilter, ScanFilterCache> {
	private final ScanFilterComposite sf = new ScanFilterComposite();
	private CacheListener<ScanFilter> cacheListener;

	@Override
	protected void onInitialize(DockPanel rootPanel,
			VerticalPanel selectionPanel) {
		setCaption("Scan Filters");

		addAction(new CreateScanFilterForm());
		sf.initialize();
		selectionPanel.add(sf);

		cacheListener = new CacheListenerAdapter<ScanFilter>() {

			@Override
			public void onItemUpdate(Cache<ScanFilter> cache, ScanFilter item,
					Status status, Throwable failure) {
				sf.setStatus(status);
			}
		};
	}

	@Override
	protected void onUpdate(Context context) {
		if (!isActive()) {
			getCache().addListener(cacheListener);
		}

		super.onUpdate(context);
	}

	@Override
	protected void onDeactivate() {
		getCache().removeListener(cacheListener);

		super.onDeactivate();
	}

	@Override
	protected String getItemText(ScanFilter item) {
		return item.getName();
	}

	@Override
	protected boolean isMatch(ScanFilter item, String query) {
		return LangUtil.containsIgnoreCase(item.getName(), query);
	}

	@Override
	protected void onSelectionChanged(ScanFilter item) {
		sf.setFilter(item);
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
											getCache().refresh();
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
				panel.add(UI.h3("Projects"));
				panel.add(addProjectBox());
				for (final String project : filter.getProjects()) {
					panel.add(new Label(project));
				}
				final HorizontalPanel buttonPanel = new HorizontalPanel();
				panel.add(buttonPanel);
				buttonPanel.add(new Button("Update", new ClickListener() {
					public void onClick(Widget sender) {
						getCache().save(filter);
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

		private Widget addProjectBox() {
			final VerticalPanel panel = new VerticalPanel();
			panel
					.add(new Label(
							"Begin typing to search for a category to add to this scan filter.  Use * to match any text."));
			final HorizontalPanel hPanel = new HorizontalPanel();
			final SuggestBox box = new SuggestBox(new ProjectSuggestOracle());
			final Button projectButton = new Button("Add Project",
					new ClickListener() {
						public void onClick(Widget sender) {
							String project = box.getText();
							if ((project != null) && (project.length() > 0)) {
								filter.getProjects().add(project);
								refresh();
							}
						}
					});
			hPanel.add(box);
			hPanel.add(projectButton);
			panel.add(hPanel);
			return panel;
		}

		public void setFilter(ScanFilter filter) {
			this.filter = filter;
			refresh();
		}

		public void setStatus(Status s) {
			this.status.setStatus(s);
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

	private class CreateScanFilterForm extends FormButton {
		private final TextBox sfName = new TextBox();

		public CreateScanFilterForm() {
			super("Create a Scan Filter", "Create");
			getForm().addField("Name:", sfName);
		}

		@Override
		protected void onOpen() {
			sfName.setText("");
		}

		@Override
		protected void doOkClick() {
			final String name = sfName.getText();

			if (LangUtil.notEmpty(name)) {
				setWaitStatus();

				ServiceHelper.getSettingsService().createScanFilter(name,
						new AsyncCallback<ScanFilter>() {

							public void onFailure(Throwable caught) {
								clearWaitStatus();

								Window.alert("Scan Filter creation failed: "
										+ caught.getMessage());
							}

							public void onSuccess(ScanFilter result) {
								clearWaitStatus();
								setOpen(false);

								getCache().refresh();
								Context.createWithUuid(result).submit();
							}
						});
			}
		}
	}

	// Singleton
	private ScanFiltersContent() {
		super(new ScanFilterCache());
	}

	private static final ScanFiltersContent instance = new ScanFiltersContent();

	public static ScanFiltersContent getInstance() {
		return instance;
	}

}
