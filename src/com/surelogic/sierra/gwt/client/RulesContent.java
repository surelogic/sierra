package com.surelogic.sierra.gwt.client;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.util.ExceptionTracker;
import com.surelogic.sierra.gwt.client.util.ImageHelper;
import com.surelogic.sierra.gwt.client.util.UI;

public class RulesContent extends ContentComposite {
	private static final RulesContent instance = new RulesContent();
	private final VerticalPanel categoriesPanel = new VerticalPanel();
	private final TextBox search = new TextBox();
	private final Tree categories = new Tree();
	private final VerticalPanel detailsPanel = new VerticalPanel();

	public static RulesContent getInstance() {
		return instance;
	}

	private RulesContent() {
		// no instances
	}

	protected void onInitialize(DockPanel rootPanel) {
		rootPanel.add(categoriesPanel, DockPanel.WEST);
		categoriesPanel.add(UI.h3("Categories"));
		categoriesPanel.add(search);
		categoriesPanel.add(categories);
		categories.addTreeListener(new TreeListener() {

			public void onTreeItemSelected(TreeItem item) {
				selectCategory((Category) item.getUserObject());
			}

			public void onTreeItemStateChanged(TreeItem item) {
				// nothing for now
			}
		});

		rootPanel.add(detailsPanel, DockPanel.CENTER);

	}

	protected void onActivate(Context context) {
		// TODO refresh categories
		categories.clear();
		categories.addItem(ImageHelper.getWaitImage(16));
		ServiceHelper.getSettingsService().getCategories(new AsyncCallback() {

			public void onFailure(Throwable caught) {
				// TODO handle this in the normal way, or switch to Callback
				categories.clear();
				categories.addItem("Error retrieving categories");
				ExceptionTracker.logException(caught);
			}

			public void onSuccess(Object result) {
				categories.clear();
				List categories = (List) result;
				for (Iterator it = categories.iterator(); it.hasNext();) {
					Category cat = (Category) it.next();
					addCategory(cat);
				}
			}
		});

	}

	protected boolean onDeactivate() {
		return true;
	}

	private void addCategory(Category cat) {
		TreeItem item = categories.addItem(cat.getName());
		item.setUserObject(cat);
	}

	private void selectCategory(Category cat) {
		detailsPanel.clear();
		final FlexTable catFields = new FlexTable();
		catFields.setWidget(0, 0, UI.h3(cat.getName()));
		catFields.getCellFormatter().setHorizontalAlignment(0, 0,
				HasHorizontalAlignment.ALIGN_CENTER);
		catFields.getFlexCellFormatter().setColSpan(0, 0, 2);
		catFields.setText(1, 0, "Description:");
		catFields.setText(2, 0, cat.getInfo());
		catFields.getFlexCellFormatter().setColSpan(2, 0, 2);

		detailsPanel.add(catFields);
		// TODO select a category or finding type, and update detailsPanel

	}
}
