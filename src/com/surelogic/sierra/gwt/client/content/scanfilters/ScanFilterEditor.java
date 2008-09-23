package com.surelogic.sierra.gwt.client.content.scanfilters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.surelogic.sierra.gwt.client.content.common.CategorySelectionDialog;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FindingTypeFilter;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.ScanFilterEntry;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.ui.StatusBox;
import com.surelogic.sierra.gwt.client.ui.StyleHelper;
import com.surelogic.sierra.gwt.client.ui.StyleHelper.Style;
import com.surelogic.sierra.gwt.client.ui.choice.SingleImportanceChoice;
import com.surelogic.sierra.gwt.client.ui.panel.BasicPanel;

public class ScanFilterEditor extends BasicPanel {

	private final ScanFilterPanel categoryFilters = new ScanFilterPanel(
			"Categories");
	private final ScanFilterPanel findingFilters = new ScanFilterPanel(
			"Findings");
	private ScanFilter selection;
	private StatusBox status;

	@Override
	protected void onInitialize(final VerticalPanel panel) {
		categoryFilters.initialize();
		categoryFilters.addAction("Add Category", new ClickListener() {

			public void onClick(final Widget sender) {
				promptForCategories();
			}
		});
		panel.add(categoryFilters);

		findingFilters.initialize();
		findingFilters.addAction("Add Finding", new ClickListener() {

			public void onClick(final Widget sender) {
				promptForFindings();
			}
		});
		panel.add(findingFilters);

		status = new StatusBox();
		panel.add(status);
	}

	public ScanFilter getSelection() {
		return selection;
	}

	public void setSelection(final ScanFilter filter) {
		selection = filter == null ? null : filter.copy();
		if (selection != null) {
			setSummary(selection.getName());
			categoryFilters.setFilters(selection.getCategories());
			findingFilters.setFilters(selection.getTypes());
		} else {
			setSummary("Select a Scan Filter");
			categoryFilters.setFilters(null);
			findingFilters.setFilters(null);
		}
	}

	public ScanFilter getUpdatedScanFilter() {
		return selection;
	}

	public void setStatus(final Status s) {
		this.status.setStatus(s);
	}

	private void promptForCategories() {
		final CategorySelectionDialog dialog = new CategorySelectionDialog();
		dialog.addPopupListener(new PopupListener() {

			public void onPopupClosed(final PopupPanel sender,
					final boolean autoClosed) {
				final Status s = dialog.getStatus();
				if (s != null && s.isSuccess()) {
					for (final Category selectedCat : dialog
							.getSelectedCategories()) {
						final ScanFilterEntry catEntry = new ScanFilterEntry(
								selectedCat, null);
						selection.getCategories().add(catEntry);
					}
					setSelection(selection);
				}
			}

		});
		dialog.center();
		final List<String> excludeIds = new ArrayList<String>();
		for (final ScanFilterEntry cat : selection.getCategories()) {
			excludeIds.add(cat.getUuid());
		}
		dialog.update(excludeIds, true);
	}

	private void promptForFindings() {
		final AddFindingsDialog dialog = new AddFindingsDialog();
		dialog.addPopupListener(new PopupListener() {

			public void onPopupClosed(final PopupPanel sender,
					final boolean autoClosed) {
				final Status s = dialog.getStatus();
				if (s != null && s.isSuccess()) {
					for (final FindingTypeFilter selectedFinding : dialog
							.getSelectedFindings()) {
						final ScanFilterEntry findingEntry = new ScanFilterEntry(
								selectedFinding, null);
						selection.getTypes().add(findingEntry);
					}
					setSelection(selection);
				}
			}

		});
		dialog.center();
		final List<String> excludeIds = new ArrayList<String>();
		for (final ScanFilterEntry cat : selection.getCategories()) {
			excludeIds.add(cat.getUuid());
		}
		dialog.update(selection.getCategories(), selection.getTypes());
	}

	private static class ScanFilterPanel extends BasicPanel {
		private final FlexTable filterGrid = new FlexTable();

		public ScanFilterPanel(final String title) {
			super();
			setSubsectionStyle(true);
			setTitle(title);
		}

		@Override
		protected void onInitialize(final VerticalPanel contentPanel) {
			final CellFormatter cf = filterGrid.getCellFormatter();
			filterGrid.setWidth("100%");
			filterGrid.setText(0, 0, "Name");
			cf.addStyleName(0, 0, "scan-filter-entry-title");
			filterGrid.getColumnFormatter().setWidth(0, "65%");

			filterGrid.setText(0, 1, "Importance");
			cf.addStyleName(0, 1, "scan-filter-entry-title");
			cf
					.setHorizontalAlignment(0, 1,
							HasHorizontalAlignment.ALIGN_CENTER);
			filterGrid.getColumnFormatter().setWidth(1, "20%");

			filterGrid.setText(0, 2, "");
			cf
					.setHorizontalAlignment(0, 2,
							HasHorizontalAlignment.ALIGN_CENTER);
			filterGrid.getColumnFormatter().setWidth(2, "15%");

			contentPanel.add(filterGrid);
		}

		public void setFilters(final Set<ScanFilterEntry> filters) {
			while (filterGrid.getRowCount() > 1) {
				filterGrid.removeRow(1);
			}
			if (filters == null || filters.size() == 0) {
				filterGrid.setWidget(1, 0, StyleHelper.add(new Label("None"),
						Style.ITALICS));
				filterGrid.setText(1, 1, "");
				filterGrid.setText(1, 2, "");
			} else {
				final List<ScanFilterEntry> sortedFilters = new ArrayList<ScanFilterEntry>(
						filters);
				Collections.sort(sortedFilters);

				for (final ScanFilterEntry filter : sortedFilters) {
					addFilter(filters, filter);
				}
			}
		}

		private void addFilter(final Set<ScanFilterEntry> filters,
				final ScanFilterEntry filter) {
			final HTML h = new HTML(filter.getName());
			h.setTitle(filter.getShortMessage());
			final SingleImportanceChoice box = new SingleImportanceChoice();
			box.setSelectedImportance(filter.getImportance());
			box.addChangeListener(new ChangeListener() {
				public void onChange(final Widget sender) {
					filter.setImportance(((SingleImportanceChoice) sender)
							.getSelectedImportance());
				}
			});

			final Label removeLabel = StyleHelper.add(new Label("Remove"),
					Style.CLICKABLE);
			removeLabel.addClickListener(new ClickListener() {
				public void onClick(final Widget sender) {
					filters.remove(filter);
					final int rowIndex = findRemoveRow(removeLabel);
					filterGrid.removeRow(rowIndex);
				}
			});
			removeLabel.setHorizontalAlignment(Label.ALIGN_CENTER);

			final int rowIndex = filterGrid.getRowCount();
			filterGrid.setWidget(rowIndex, 0, h);
			filterGrid.setWidget(rowIndex, 1, box);
			filterGrid.getCellFormatter().setHorizontalAlignment(rowIndex, 1,
					HasHorizontalAlignment.ALIGN_CENTER);
			filterGrid.setWidget(rowIndex, 2, removeLabel);
		}

		private int findRemoveRow(final Label removeLabel) {
			for (int i = 1; i < filterGrid.getRowCount(); i++) {
				if (filterGrid.getWidget(i, 2) == removeLabel) {
					return i;
				}
			}
			return -1;
		}
	}

}
