package com.surelogic.sierra.gwt.client.rules;

import java.util.Iterator;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
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
import com.surelogic.sierra.gwt.client.data.ImportanceView;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.ScanFilterEntry;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.rules.FindingTypeSuggestOracle.Suggestion;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;
import com.surelogic.sierra.gwt.client.ui.StatusBox;
import com.surelogic.sierra.gwt.client.util.UI;

public class ScanFilterContent extends ContentComposite {

	private static final String FILTER = "filter";

	private final ScanFilterList scans = new ScanFilterList();
	private final ScanFilterCache cache = new ScanFilterCache();
	private final ScanFilterComposite sf = new ScanFilterComposite();

	private String filterUuid;

	protected void onInitialize(DockPanel rootPanel) {
		rootPanel.add(scans, DockPanel.WEST);
		rootPanel.add(sf, DockPanel.EAST);
		rootPanel.setCellWidth(scans, "25%");
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
		scans.deactivate();
	}

	protected void onUpdate(Context context) {
		scans.update(context);
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

	class ScanFilterList extends SectionPanel {
		private final VerticalPanel list = new VerticalPanel();

		protected void onInitialize(VerticalPanel contentPanel) {
			final TextBox box = new TextBox();
			final Button button = new Button("Create");
			box.addKeyboardListener(new KeyboardListenerAdapter() {
				public void onKeyUp(final Widget sender, final char keyCode,
						final int modifiers) {
					if (keyCode == KEY_ENTER) {
						final String name = box.getText();
						if (name.length() > 0) {
							ServiceHelper.getSettingsService()
									.createScanFilter(name,
											new AsyncCallback() {
												public void onFailure(
														Throwable caught) {
													// TODO
												}

												public void onSuccess(
														Object result) {
													cache.refresh();
												}
											});
						}
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
			contentPanel.add(box);
			contentPanel.add(button);
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

		protected void onDeactivate() {

		}

		protected void onUpdate(Context context) {

		}

	}

	private class ScanFilterComposite extends Composite {

		private final VerticalPanel panel;
		private VerticalPanel ftPanel;
		private VerticalPanel cPanel;
		private ScanFilter filter;
		private ScanFilter backup;
		private StatusBox status;

		ScanFilterComposite() {
			panel = new VerticalPanel();
			status = new StatusBox();
			initWidget(panel);
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
				status = new StatusBox();
				panel.add(status);
				panel.add(UI.h2(filter.getName()));
				cPanel = new VerticalPanel();
				panel.add(cPanel);
				cPanel.add(UI.h3("Categories"));
				for (final Iterator i = filter.getCategories().iterator(); i
						.hasNext();) {
					cPanel.add(entry((ScanFilterEntry) i.next()));
				}
				cPanel.add(addCategoryBox());
				ftPanel = new VerticalPanel();
				panel.add(ftPanel);
				ftPanel.add(UI.h3("Finding Types"));
				for (final Iterator i = filter.getTypes().iterator(); i
						.hasNext();) {
					ftPanel.add(entry((ScanFilterEntry) i.next()));
				}
				ftPanel.add(addFindingTypeBox());
				final HorizontalPanel buttonPanel = new HorizontalPanel();
				panel.add(buttonPanel);
				buttonPanel.add(new Button("Revert", new ClickListener() {
					public void onClick(Widget sender) {
						filter = backup;
						refresh();
					}
				}));
				buttonPanel.add(new Button("Update", new ClickListener() {
					public void onClick(Widget sender) {
						cache.save(filter);
					}
				}));
			} else {
				panel.add(UI.h1("None selected"));
			}
		}

		private Widget addFindingTypeBox() {
			final SuggestBox box = new SuggestBox(
					new FindingTypeSuggestOracle());
			box.addEventHandler(new SuggestionHandler() {
				public void onSuggestionSelected(SuggestionEvent event) {
					final FindingTypeSuggestOracle.Suggestion s = (Suggestion) event
							.getSelectedSuggestion();
					final ScanFilterEntry e = s.getEntry();
					if (!filter.getTypes().contains(e)) {
						filter.getTypes().add(e);
						ftPanel.add(entry(e));
					}
				}
			});
			return box;
		}

		private Widget addCategoryBox() {
			final SuggestBox box = new SuggestBox(new CategorySuggestOracle());
			box.addEventHandler(new SuggestionHandler() {
				public void onSuggestionSelected(SuggestionEvent event) {
					final CategorySuggestOracle.Suggestion s = (com.surelogic.sierra.gwt.client.rules.CategorySuggestOracle.Suggestion) event
							.getSelectedSuggestion();
					final ScanFilterEntry e = s.getEntry();
					if (!filter.getCategories().contains(e)) {
						filter.getCategories().add(e);
						cPanel.add(entry(e));
					}
				}
			});
			return box;
		}

		public void setFilter(ScanFilter filter) {
			backup = filter;
			this.filter = filter.copy();
			refresh();
		}
	}

	private static Widget entry(ScanFilterEntry e) {
		final HorizontalPanel panel = new HorizontalPanel();
		final HTML h = new HTML(e.getName());
		h.setTitle(e.getShortMessage());
		final ListBox box = ImportanceView.createChoice();
		box.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {

				panel.add(new Label(box.getItemText(box.getSelectedIndex())));
			}
		});
		panel.add(h);
		panel.add(box);
		return panel;
	}

	// Singleton
	private ScanFilterContent() {

	}

	private static final ScanFilterContent instance = new ScanFilterContent();

	public static ScanFilterContent getInstance() {
		return instance;
	}

}
