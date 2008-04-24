package com.surelogic.sierra.gwt.client.rules;

import java.util.Iterator;
import java.util.Set;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FilterEntry;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;
import com.surelogic.sierra.gwt.client.ui.SubsectionPanel;

public class CategoryPanel extends Composite {
	public static final String PRIMARY_STYLE = RulesContent.PRIMARY_STYLE;

	private final VerticalPanel rootPanel = new VerticalPanel();
	private final SectionPanel categorySection = new SectionPanel("Category",
			"");
	private final FlexTable categoryInfo = new FlexTable();
	private final TextBox nameEditText = new TextBox();
	private final TextArea description = new TextArea();
	private final SubsectionPanel findingsSubsection = new SubsectionPanel(
			"Finding Types", "");
	private final VerticalPanel findingTypes = findingsSubsection
			.getContentPanel();
	private Category currentCategory;
	private boolean editing;

	public CategoryPanel() {
		super();
		initWidget(rootPanel);
		rootPanel.setWidth("100%");

		final VerticalPanel catInfoContent = categorySection.getContentPanel();
		categoryInfo.setWidth("100%");
		categoryInfo.getColumnFormatter().setWidth(0, "15%");
		categoryInfo.getColumnFormatter().setWidth(1, "35%");
		categoryInfo.getColumnFormatter().setWidth(2, "50%");
		categoryInfo.setText(0, 0, "Description:");
		categoryInfo.setWidget(1, 0, description);
		categoryInfo.getFlexCellFormatter().setColSpan(1, 0, 3);
		description.setVisibleLines(5);
		catInfoContent.add(categoryInfo);

		catInfoContent.add(findingsSubsection);

		rootPanel.add(categorySection);
	}

	public void setCategory(Category cat) {
		if (checkEditing()) {
			return;
		}

		currentCategory = cat;

		categorySection.getSectionInfo().setText(cat.getName());
		categorySection.removeActions();
		categorySection.addAction("Edit", new ClickListener() {

			public void onClick(Widget sender) {
				edit();
			}
		});

		if (nameEditText.isAttached()) {
			categoryInfo.removeRow(0);
		}

		description.setReadOnly(true);
		final String catInfo = cat.getInfo();
		if (catInfo == null || "".equals(catInfo)) {
			description.setText("None");
			description.addStyleName("font-italic");
		} else {
			description.setText(catInfo);
			description.removeStyleName("font-italic");
		}

		findingTypes.clear();
		for (final Iterator it = cat.getEntries().iterator(); it.hasNext();) {
			final FilterEntry finding = (FilterEntry) it.next();
			findingTypes.add(createDetailsRule(finding, false, !finding
					.isFiltered()));
		}
		final Set excluded = cat.getExcludedEntries();
		for (final Iterator catIt = cat.getParents().iterator(); catIt
				.hasNext();) {
			final Category parent = (Category) catIt.next();
			final DisclosurePanel parentPanel = new DisclosurePanel("From: "
					+ parent.getName());
			final VerticalPanel parentFindingsPanel = new VerticalPanel();
			final Set parentFindings = parent.getIncludedEntries();
			for (final Iterator findingIt = parentFindings.iterator(); findingIt
					.hasNext();) {
				final FilterEntry finding = (FilterEntry) findingIt.next();
				parentFindingsPanel.add(createDetailsRule(finding, false,
						!excluded.contains(finding)));
			}
			parentPanel.setContent(parentFindingsPanel);
			findingTypes.add(parentPanel);
		}
	}

	private void edit() {
		if (isEditing() || currentCategory == null) {
			return;
		}

		if (!nameEditText.isAttached()) {
			categoryInfo.insertRow(0);
			categoryInfo.setText(0, 0, "Name:");
			categoryInfo.setWidget(0, 1, nameEditText);
		}
		nameEditText.setText(currentCategory.getName());
		description.setReadOnly(false);
		String catInfo = currentCategory.getInfo();
		if (catInfo == null) {
			catInfo = "";
		}
		description.setText(catInfo);

		// TODO update finding types to allow enable/disable

		categorySection.removeActions();

		categorySection.addAction("Save", new ClickListener() {

			public void onClick(Widget sender) {
				saveEdit();
			}
		});

		categorySection.addAction("Cancel", new ClickListener() {

			public void onClick(Widget sender) {
				cancelEdit();
			}
		});

		editing = true;
	}

	private void cancelEdit() {
		editing = false;
		setCategory(currentCategory);
	}

	private void saveEdit() {
		final Category rpcCategory = currentCategory.copy();

		rpcCategory.setName(nameEditText.getText());
		rpcCategory.setInfo(description.getText());

		// TODO copy filter settings from UI here

		ServiceHelper.getSettingsService().updateCategory(rpcCategory,
				new AsyncCallback() {

					public void onFailure(Throwable caught) {
						// TODO show the error and do not cancel editing
					}

					public void onSuccess(Object result) {
						Status status = (Status) result;
						if (status.isSuccess()) {
							// TODO reload all categories and select the new
							// category by name I guess
							// will need access to RulesContent
						} else {
							// TODO show the error and do not cancel editing
						}
					}
				});
	}

	private boolean isEditing() {
		return editing;
	}

	private boolean checkEditing() {
		final boolean editing = isEditing();
		if (editing) {
			Window
					.alert("You are currently editing a selection. Please save or cancel your changes.");
		}
		return editing;
	}

	private Widget createDetailsRule(FilterEntry finding, boolean editing,
			boolean enabled) {
		if (editing) {
			final CheckBox rule = new CheckBox(finding.getName());
			rule.addStyleName(PRIMARY_STYLE + "-details-finding");
			rule.setTitle(finding.getShortMessage());
			rule.setChecked(enabled);
			return rule;
		}
		if (enabled) {
			final Label rule = new Label(finding.getName());
			rule.addStyleName(PRIMARY_STYLE + "-details-finding");
			rule.setTitle(finding.getShortMessage());
			return rule;
		}
		return null;
	}

}
