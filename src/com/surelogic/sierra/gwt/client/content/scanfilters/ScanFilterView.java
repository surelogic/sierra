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
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.ScanFilterEntry;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.ClickLabel;
import com.surelogic.sierra.gwt.client.ui.ImportanceChoice;
import com.surelogic.sierra.gwt.client.ui.ItalicLabel;
import com.surelogic.sierra.gwt.client.ui.StatusBox;

public class ScanFilterView extends BlockPanel {

	private final ScanFilterBlock categoryFilters = new ScanFilterBlock(
			"Categories");
	private final ScanFilterBlock findingFilters = new ScanFilterBlock(
			"Findings");
	private ScanFilter selection;
	private StatusBox status;

	@Override
	protected void onInitialize(VerticalPanel panel) {
		final StringBuilder viewDesc = new StringBuilder();
		viewDesc
				.append("A scan filter specifies the finding types that are included when a scan is loaded into the system.");
		viewDesc
				.append("  You can add finding types individually, or you can add all of the finding types in a category at once.");
		viewDesc
				.append(" You can also set the importance that a particular finding type or category has.");
		panel.add(new Label(viewDesc.toString()));

		categoryFilters.initialize();
		panel.add(categoryFilters);

		findingFilters.initialize();
		panel.add(findingFilters);

		status = new StatusBox();
		panel.add(status);
	}

	public ScanFilter getSelection() {
		return selection;
	}

	public void setSelection(ScanFilter filter) {
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

	public void setStatus(Status s) {
		this.status.setStatus(s);
	}

	private static class ScanFilterBlock extends BlockPanel {
		private final FlexTable filterGrid = new FlexTable();

		public ScanFilterBlock(String title) {
			super();
			setSubsectionStyle(true);
			setTitle(title);
		}

		@Override
		protected void onInitialize(VerticalPanel contentPanel) {
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

		public void setFilters(Set<ScanFilterEntry> filters) {
			while (filterGrid.getRowCount() > 1) {
				filterGrid.removeRow(1);
			}
			if (filters == null || filters.size() == 0) {
				filterGrid.setWidget(1, 0, new ItalicLabel("None"));
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
			final ImportanceChoice box = new ImportanceChoice();
			box.setSelectedImportance(filter.getImportance());
			box.addChangeListener(new ChangeListener() {
				public void onChange(Widget sender) {
					filter.setImportance(((ImportanceChoice) sender)
							.getSelectedImportance());
				}
			});
			final int rowIndex = filterGrid.getRowCount();
			final Label remove = new ClickLabel("remove", new ClickListener() {
				public void onClick(Widget sender) {
					filters.remove(filter);
					filterGrid.removeRow(rowIndex);
				}
			});
			remove.setHorizontalAlignment(Label.ALIGN_CENTER);

			filterGrid.setWidget(rowIndex, 0, h);
			filterGrid.setWidget(rowIndex, 1, box);
			filterGrid.getCellFormatter().setHorizontalAlignment(rowIndex, 1,
					HasHorizontalAlignment.ALIGN_CENTER);
			filterGrid.setWidget(rowIndex, 2, remove);
		}
	}
}
