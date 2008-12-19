package com.surelogic.sierra.gwt.client.content.categories;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.TextBox;
import com.surelogic.sierra.gwt.client.ui.dialog.FormDialog;

public class CategoryNameDialog extends FormDialog {
	private final TextBox name = new TextBox();

	public CategoryNameDialog() {
		super("Enter the New Category Name", "500px");
	}

	@Override
	protected void doInitialize(final FlexTable contentTable) {
		addField("Name", name);
	}

	@Override
	protected HasFocus getInitialFocus() {
		return name;
	}

	public String getName() {
		return name.getText();
	}

	public void setName(final String categoryName) {
		name.setText(categoryName);
	}
}
