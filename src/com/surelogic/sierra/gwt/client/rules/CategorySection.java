package com.surelogic.sierra.gwt.client.rules;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;
import com.surelogic.sierra.gwt.client.util.ExceptionUtil;

public class CategorySection extends SectionPanel {
	private final CategoryCache categories;
	private final FlexTable categoryInfo = new FlexTable();
	private final TextBox nameEditText = new TextBox();
	private final TextArea description = new TextArea();
	// private final SubsectionPanel findingsSubsection = new SubsectionPanel(
	// "Finding Types", "");

	private Category currentCategory;
	private boolean editing;

	public CategorySection(CategoryCache categories) {
		super();
		this.categories = categories;
		setTitle("Category");
	}

	protected void onInitialize(VerticalPanel contentPanel) {
		categoryInfo.getColumnFormatter().setWidth(0, "15%");
		categoryInfo.getColumnFormatter().setWidth(1, "35%");
		categoryInfo.getColumnFormatter().setWidth(2, "50%");
		categoryInfo.setText(0, 0, "Description:");
		categoryInfo.setWidget(1, 0, description);
		categoryInfo.getFlexCellFormatter().setColSpan(1, 0, 3);
		description.setVisibleLines(5);
		contentPanel.add(categoryInfo);

		// contentPanel.add(findingsSubsection);
	}

	protected void onActivate(Context context) {
		refresh(context);
	}

	protected void onDeactivate() {
		// nothing for now
	}

	protected void onUpdate(Context context) {
		refresh(context);
	}

	private void refresh(Context context) {
		final String categoryUuid = new RulesContext(context).getCategory();
		refresh((Category) categories.getItem(categoryUuid));
	}

	private void refresh(Category cat) {
		currentCategory = cat;
		editing = false;

		if (cat != null) {
			setSummary(cat.getName());
		} else {
			setSummary("Select a Category");
		}

		if (nameEditText.isAttached()) {
			categoryInfo.removeRow(0);
		}

		description.setReadOnly(true);
		final String catInfo = cat == null ? "" : cat.getInfo();
		if (catInfo == null || "".equals(catInfo)) {
			description.setText("None");
			description.addStyleName("font-italic");
		} else {
			description.setText(catInfo);
			description.removeStyleName("font-italic");
		}

		// updateFindingTypes(cat, false);

		removeActions();
		if (cat != null) {
			addAction("Edit", new ClickListener() {

				public void onClick(Widget sender) {
					edit();
				}
			});
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
		description.removeStyleName("font-italic");
		String catInfo = currentCategory.getInfo();
		if (catInfo == null) {
			catInfo = "";
		}
		description.setText(catInfo);

		// updateFindingTypes(currentCategory, true);

		removeActions();
		addAction("Save", new ClickListener() {

			public void onClick(Widget sender) {
				saveEdit();
			}
		});

		addAction("Cancel", new ClickListener() {

			public void onClick(Widget sender) {
				cancelEdit();
			}
		});

		editing = true;
	}

	private void cancelEdit() {
		refresh(currentCategory);
	}

	private void saveEdit() {
		final Category rpcCategory = currentCategory.copy();

		rpcCategory.setName(nameEditText.getText());
		rpcCategory.setInfo(description.getText());

		// TODO copy filter settings from UI here

		ServiceHelper.getSettingsService().updateCategory(rpcCategory,
				new AsyncCallback() {

					public void onFailure(Throwable caught) {
						ExceptionUtil.log(caught);

						// TODO show the error and do not cancel editing
					}

					public void onSuccess(Object result) {
						Status status = (Status) result;
						if (status.isSuccess()) {
							editing = false;
							new RulesContext(rpcCategory).updateContext();
						} else {
							// TODO show the error and do not cancel editing

							Window.alert("Save failed: " + status.getMessage());
						}
					}
				});
	}

	private boolean isEditing() {
		return editing;
	}

	private class FindingTypesSection extends SectionPanel {

		protected void onInitialize(VerticalPanel contentPanel) {
			setSubsectionStyle(true);

			// TODO Auto-generated method stub

		}

		protected void onActivate(Context context) {
			// TODO Auto-generated method stub

		}

		protected void onDeactivate() {
			// TODO Auto-generated method stub

		}

		protected void onUpdate(Context context) {
			// TODO Auto-generated method stub

		}

		// private void updateFindingTypes(Category cat, boolean editing) {
		// findingTypes.clear();
		// if (editing) {
		// findingsSubsection.addAction("Add Category",
		// new ClickListener() {
		//
		// public void onClick(Widget sender) {
		// addCategory();
		// }
		// });
		// } else {
		// findingsSubsection.removeActions();
		// }
		//
		// for (final Iterator it = cat.getEntries().iterator(); it.hasNext();)
		// {
		// final FilterEntry finding = (FilterEntry) it.next();
		// final Widget findingUI = createDetailsRule(finding, editing,
		// !finding.isFiltered());
		// if (findingUI != null) {
		// findingTypes.add(findingUI);
		// }
		// }
		// final Set excluded = cat.getExcludedEntries();
		// for (final Iterator catIt = cat.getParents().iterator(); catIt
		// .hasNext();) {
		// final Category parent = (Category) catIt.next();
		// final DisclosurePanel parentPanel = new DisclosurePanel(
		// "From: " + parent.getName());
		// final VerticalPanel parentFindingsPanel = new VerticalPanel();
		// final Set parentFindings = parent.getIncludedEntries();
		// for (final Iterator findingIt = parentFindings.iterator(); findingIt
		// .hasNext();) {
		// final FilterEntry finding = (FilterEntry) findingIt.next();
		// final Widget findingUI = createDetailsRule(finding,
		// editing, !excluded.contains(finding));
		// if (findingUI != null) {
		// parentFindingsPanel.add(findingUI);
		// }
		// }
		// parentPanel.setContent(parentFindingsPanel);
		// parentPanel.setOpen(true);
		// findingTypes.add(parentPanel);
		// }
		// }
		//
		// private Widget createDetailsRule(FilterEntry finding, boolean
		// editing,
		// boolean enabled) {
		// if (editing) {
		// final CheckBox rule = new CheckBox(finding.getName());
		// rule.setTitle(finding.getShortMessage());
		// rule.setChecked(enabled);
		// return rule;
		// }
		// if (enabled) {
		// final Label rule = new Label(finding.getName());
		// rule.setTitle(finding.getShortMessage());
		// return rule;
		// }
		// return null;
		// }
		//
		// private void addCategory() {
		// // TODO need a dialog or UI update to add categories + findings
		// }

	}
}
