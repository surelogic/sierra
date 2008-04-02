package com.surelogic.sierra.gwt.client;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
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
				selectCategoryItem(item);
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

	private void selectCategoryItem(TreeItem item) {
		// TODO select a category or finding type, and update detailsPanel
		Window.alert("Category item clicked: " + item.getText());
	}
}
