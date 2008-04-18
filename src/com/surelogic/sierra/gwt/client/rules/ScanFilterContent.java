package com.surelogic.sierra.gwt.client.rules;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.ImportanceView;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.ScanFilterEntry;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.util.UI;

public class ScanFilterContent extends ContentComposite {

	private static final ScanFilterContent instance = new ScanFilterContent();

	private final VerticalPanel list = new VerticalPanel();
	private List filters;
	private final ScanFilterComposite sf = new ScanFilterComposite();

	protected void onActivate(Context context) {
		refreshFilterList();
	}

	protected boolean onDeactivate() {
		return true;
	}

	protected void onInitialize(DockPanel rootPanel) {
		final HorizontalPanel panel = new HorizontalPanel();
		final VerticalPanel select = new VerticalPanel();
		final HorizontalPanel add = new HorizontalPanel();
		final TextBox box = new TextBox();
		final Button button = new Button("Create");
		box.addKeyboardListener(new KeyboardListenerAdapter() {
			public void onKeyUp(final Widget sender, final char keyCode,
					final int modifiers) {
				if (keyCode == KEY_ENTER) {
					final String name = box.getText();
					if (name.length() > 0) {
						ServiceHelper.getSettingsService().createScanFilter(
								name, new AsyncCallback() {
									public void onFailure(Throwable caught) {
										// TODO
									}

									public void onSuccess(Object result) {
										refreshFilterList();
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
					ServiceHelper.getSettingsService().createScanFilter(name,
							new AsyncCallback() {
								public void onFailure(Throwable caught) {
									// TODO
								}

								public void onSuccess(Object result) {
									refreshFilterList();
								}
							});
				}
			}
		});
		add.add(box);
		add.add(button);
		select.add(add);
		select.add(list);
		panel.add(select);
		panel.add(sf);
		rootPanel.add(panel, DockPanel.CENTER);
	}

	private void refreshFilterList() {
		ServiceHelper.getSettingsService().getScanFilters(new AsyncCallback() {

			public void onFailure(Throwable caught) {
				// TODO
			}

			public void onSuccess(Object result) {
				list.clear();
				filters = (List) result;
				for (final Iterator i = filters.iterator(); i.hasNext();) {
					final ScanFilter f = (ScanFilter) i.next();
					final Label l = new Label(f.getName());
					l.addStyleName("clickable");
					l.addClickListener(new ClickListener() {
						public void onClick(Widget sender) {
							sf.setFilter(f);
						}
					});
					list.add(l);
				}
			}
		});
	}

	public static ScanFilterContent getInstance() {
		return instance;
	}

	private static class ScanFilterComposite extends Composite {

		private final VerticalPanel panel;
		private ScanFilter filter;

		ScanFilterComposite() {
			panel = new VerticalPanel();
			initWidget(panel);
			refresh();
		}

		private void refresh() {
			panel.clear();
			if (filter != null) {
				panel.add(UI.h2(filter.getName()));
				panel.add(UI.h3("Categories"));
				for (final Iterator i = filter.getCategories().iterator(); i
						.hasNext();) {
					panel.add(entry((ScanFilterEntry) i.next()));
				}
				panel.add(addCategoryBox());
				panel.add(UI.h3("Finding Types"));
				for (final Iterator i = filter.getTypes().iterator(); i
						.hasNext();) {
					panel.add(entry((ScanFilterEntry) i.next()));
				}
				panel.add(addFindingTypeBox());
			} else {
				panel.add(UI.h1("None selected"));
			}
		}

		private Widget addFindingTypeBox() {
			return new SuggestBox(new FindingTypeSuggestOracle());
		}

		private Widget addCategoryBox() {
			return new SuggestBox(new CategorySuggestOracle());
		}

		private static Widget entry(ScanFilterEntry e) {
			final HorizontalPanel panel = new HorizontalPanel();
			final HTML h = new HTML(e.getName());
			h.setTitle(e.getShortMessage());
			final ListBox box = ImportanceView.createChoice();
			box.addChangeListener(new ChangeListener() {
				public void onChange(Widget sender) {
					panel
							.add(new Label(box.getItemText(box
									.getSelectedIndex())));
				}
			});
			panel.add(h);
			panel.add(box);
			return panel;
		}

		public void setFilter(ScanFilter filter) {
			this.filter = filter;
			refresh();
		}
	}

}
