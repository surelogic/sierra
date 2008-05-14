package com.surelogic.sierra.gwt.client.rules;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.CacheListener;
import com.surelogic.sierra.gwt.client.data.Cacheable;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.ui.Editable;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;

public class CategoryBlock extends SectionPanel implements Editable {
	private final CategoryCache categories;
	private final FlexTable categoryInfo = new FlexTable();
	private final TextBox nameEditText = new TextBox();
	private final TextArea description = new TextArea();
	private final CategoryFindingsBlock findingTypes = new CategoryFindingsBlock();

	private Category currentCategory;
	private boolean editing;

	public CategoryBlock(CategoryCache categories) {
		super();
		this.categories = categories;
		setTitle("Category");
	}

	protected void onInitialize(VerticalPanel contentPanel) {
		categoryInfo.setWidth("100%");
		categoryInfo.getColumnFormatter().setWidth(0, "15%");
		categoryInfo.getColumnFormatter().setWidth(1, "35%");
		categoryInfo.getColumnFormatter().setWidth(2, "50%");
		categoryInfo.setText(0, 0, "Description:");
		categoryInfo.setWidget(1, 0, description);
		categoryInfo.getFlexCellFormatter().setColSpan(1, 0, 3);
		description.setVisibleLines(5);
		contentPanel.add(categoryInfo);

		findingTypes.setSubsectionStyle(true);
		contentPanel.add(findingTypes);

		categories.addListener(new CacheListener() {

			public void onItemUpdate(Cache cache, Cacheable item,
					Status status, Throwable failure) {
				if (failure == null && status.isSuccess()) {
					editing = false;
					new CategoriesContext((Category) item).updateContext();
				} else if (!status.isSuccess()) {
					Window.alert("Save rejected: " + status.getMessage());
				} else if (failure != null) {
					Window.alert("Save failed: " + failure.getMessage());
				}
			}

			public void onRefresh(Cache cache, Throwable failure) {
				refresh(ContextManager.getContext());
			}

			public void onStartRefresh(Cache cache) {
				// nothing to do
			}
		});
	}

	protected void onUpdate(Context context) {
		findingTypes.update(context);

		refresh(context);
	}

	protected void onDeactivate() {
		findingTypes.deactivate();
	}

	public void edit() {
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

		findingTypes.refresh(currentCategory, true);

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

	public void cancelEdit() {
		refresh(currentCategory);
	}

	public void saveEdit() {
		final Category rpcCategory = currentCategory.copy();

		rpcCategory.setName(nameEditText.getText());
		rpcCategory.setInfo(description.getText());

		findingTypes.saveTo(rpcCategory);

		categories.save(rpcCategory);
	}

	public boolean isEditing() {
		return editing;
	}

	private void refresh(Context context) {
		final String categoryUuid = new CategoriesContext(context).getCategory();
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

		findingTypes.refresh(currentCategory, false);

		removeActions();
		if (cat != null) {
			addAction("Edit", new ClickListener() {

				public void onClick(Widget sender) {
					edit();
				}
			});
		}
	}

}
