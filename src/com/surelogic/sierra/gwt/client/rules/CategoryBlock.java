package com.surelogic.sierra.gwt.client.rules;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.Editable;
import com.surelogic.sierra.gwt.client.ui.EditableListener;
import com.surelogic.sierra.gwt.client.ui.EditableListenerCollection;

public class CategoryBlock extends BlockPanel implements Editable {
	public static final String PRIMARY_STYLE = "categories-category";
	private final FlexTable categoryInfo = new FlexTable();
	private final TextBox nameEditText = new TextBox();
	private final TextArea description = new TextArea();
	private final CategoryFindingsBlock findingTypes = new CategoryFindingsBlock();

	private final EditableListenerCollection listeners = new EditableListenerCollection();
	private Category currentCategory;
	private boolean editing;

	protected void onInitialize(VerticalPanel contentPanel) {
		setTitle("Category");

		categoryInfo.setWidth("100%");
		categoryInfo.getColumnFormatter().setWidth(0, "15%");
		categoryInfo.getColumnFormatter().setWidth(1, "35%");
		categoryInfo.getColumnFormatter().setWidth(2, "50%");
		categoryInfo.setText(0, 0, "Description:");
		categoryInfo.setWidget(1, 0, description);
		categoryInfo.getFlexCellFormatter().setColSpan(1, 0, 3);
		description.setVisibleLines(5);
		contentPanel.add(categoryInfo);

		nameEditText.setWidth("100%");

		findingTypes.setSubsectionStyle(true);
		contentPanel.add(findingTypes);
	}

	public void setSelection(Category cat) {
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

		listeners.fireEdit(this);
	}

	public void cancelEdit() {
		setSelection(currentCategory);

		listeners.fireCancelEdit(this);
	}

	public void saveEdit() {
		final Category rpcCategory = currentCategory.copy();

		rpcCategory.setName(nameEditText.getText());
		rpcCategory.setInfo(description.getText());

		findingTypes.saveTo(rpcCategory);

		listeners.fireSave(this, rpcCategory);
	}

	public boolean isEditing() {
		return editing;
	}

	public void addListener(EditableListener listener) {
		listeners.addListener(listener);
	}

	public void removeListener(EditableListener listener) {
		listeners.removeListener(listener);
	}

}
