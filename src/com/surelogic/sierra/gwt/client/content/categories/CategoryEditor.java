package com.surelogic.sierra.gwt.client.content.categories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.content.findingtypes.FindingTypesContent;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FindingTypeFilter;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.cache.CategoryCache;
import com.surelogic.sierra.gwt.client.ui.StyleHelper;
import com.surelogic.sierra.gwt.client.ui.link.ContentLink;
import com.surelogic.sierra.gwt.client.ui.panel.BlockPanel;

public class CategoryEditor extends BlockPanel {
	public static final String PRIMARY_STYLE = "categories-category";
	private final FlexTable categoryInfo = new FlexTable();
	private final TextBox nameEditText = new TextBox();
	private final TextArea description = new TextArea();
	private final FindingsEditor findingsEditor = new FindingsEditor();

	private Category category;

	@Override
	protected void onInitialize(final VerticalPanel contentPanel) {
		categoryInfo.setWidth("100%");

		categoryInfo.setText(0, 0, "Name:");
		nameEditText.setWidth("100%");
		categoryInfo.setWidget(0, 1, nameEditText);

		categoryInfo.setText(1, 0, "Description:");
		description.setVisibleLines(5);
		categoryInfo.setWidget(2, 0, description);

		categoryInfo.getColumnFormatter().setWidth(0, "15%");
		categoryInfo.getColumnFormatter().setWidth(1, "35%");
		categoryInfo.getColumnFormatter().setWidth(2, "50%");
		categoryInfo.getFlexCellFormatter().setColSpan(2, 0, 3);

		contentPanel.add(categoryInfo);

		findingsEditor.setSubsectionStyle(true);
		findingsEditor.initialize();
		findingsEditor.addAction("Add Finding", new ClickListener() {

			public void onClick(final Widget sender) {
				promptForFindings(getCategory());
			}
		});
		contentPanel.add(findingsEditor);

	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(final Category cat) {
		category = cat.copy();
		setSummary(category.getName());
		nameEditText.setText(category.getName());
		String catInfo = category.getInfo();
		if (catInfo == null) {
			catInfo = "";
		}
		description.setText(catInfo);

		findingsEditor.update();
	}

	public Category getUpdatedCategory() {
		category.setName(nameEditText.getText());
		category.setInfo(description.getText());

		return category;
	}

	private void promptForFindings(final Category cat) {
		final AddCategoriesDialog dialog = new AddCategoriesDialog();
		dialog.addPopupListener(new PopupListener() {

			public void onPopupClosed(final PopupPanel sender,
					final boolean autoClosed) {
				final Status s = dialog.getStatus();
				if (s != null && s.isSuccess()) {
					addFindings(dialog.getSelectedCategories(), dialog
							.getSelectedFindings(), dialog
							.getExcludedFindings());
				}
			}

		});
		dialog.center();
		dialog.update(CategoryCache.getInstance(), cat);
	}

	private void addFindings(final Set<Category> selectedCategories,
			final Set<FindingTypeFilter> selectedFindings,
			final Set<FindingTypeFilter> excludedFindings) {
		final Set<Category> catParents = category.getParents();
		for (final Category newCat : selectedCategories) {
			if (!catParents.contains(newCat)) {
				catParents.add(newCat.copy());
			}
		}
		final Set<FindingTypeFilter> catEntries = category.getEntries();

		for (final FindingTypeFilter excludedFinding : excludedFindings) {
			catEntries.remove(excludedFinding);
			final FindingTypeFilter newEntry = excludedFinding.copy();
			newEntry.setFiltered(true);
			catEntries.add(newEntry);
		}

		for (final FindingTypeFilter selectedFinding : selectedFindings) {
			catEntries.remove(selectedFinding);
		}
		refresh();
	}

	private void refresh() {
		setCategory(category);
	}

	public class FindingsEditor extends BlockPanel {

		@Override
		protected void onInitialize(final VerticalPanel contentPanel) {
			setTitle("Finding Types");
		}

		public void update() {
			final VerticalPanel contentPanel = getContentPanel();
			contentPanel.clear();

			// add the findings that belong to the selected category
			final List<FindingTypeFilter> catFindings = new ArrayList<FindingTypeFilter>(
					category.getEntries());
			Collections.sort(catFindings);

			for (final FindingTypeFilter finding : catFindings) {
				if (!category.parentContains(finding) && !finding.isFiltered()) {
					contentPanel.add(createFindingUI(category, finding));
				}
			}

			// add findings that belong to the parent categories of the selected
			// category
			final Set<FindingTypeFilter> excluded = category
					.getExcludedEntries();
			final List<Category> sortedParents = new ArrayList<Category>(
					category.getParents());
			Collections.sort(sortedParents);
			for (final Category parent : sortedParents) {
				final DisclosurePanel parentPanel = new DisclosurePanel(
						"From: " + parent.getName());
				final VerticalPanel findingsPanel = new VerticalPanel();
				parentPanel.setContent(findingsPanel);
				parentPanel.setOpen(true);

				final List<FindingTypeFilter> parentFindings = new ArrayList<FindingTypeFilter>(
						parent.getIncludedEntries());
				Collections.sort(parentFindings);
				boolean showingFindings = false;
				for (final FindingTypeFilter finding : parentFindings) {
					if (!excluded.contains(finding)) {
						findingsPanel.add(createFindingUI(category, finding));
						showingFindings = true;
					}
				}

				if (showingFindings) {
					contentPanel.add(parentPanel);
				}
			}
		}

		private Widget createFindingUI(final Category category,
				final FindingTypeFilter finding) {
			final ContentLink findingLink = new ContentLink(finding.getName(),
					FindingTypesContent.getInstance(), finding.getUuid());

			final Label removeLabel = StyleHelper
					.clickable(new Label("Remove"));
			removeLabel.addClickListener(new RemoveFindingListener(finding));

			final HorizontalPanel findingPanel = new HorizontalPanel();
			findingPanel.setWidth("100%");
			findingPanel.add(findingLink);
			findingPanel.add(removeLabel);
			findingPanel.setCellHorizontalAlignment(removeLabel,
					HasHorizontalAlignment.ALIGN_RIGHT);
			return findingPanel;
		}

	}

	private class RemoveFindingListener implements ClickListener {
		private final FindingTypeFilter finding;

		public RemoveFindingListener(final FindingTypeFilter finding) {
			super();
			this.finding = finding;
		}

		public void onClick(final Widget sender) {
			finding.setFiltered(true);
			category.updateFilter(finding);

			refresh();
		}
	}

}
